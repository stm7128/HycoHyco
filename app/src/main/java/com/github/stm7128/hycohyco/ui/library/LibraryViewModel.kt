package com.github.stm7128.hycohyco.ui.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.data.download.OfflineDownloadManager
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.PlaylistVideoItem
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.WatchHistoryItem
import com.github.stm7128.hycohyco.domain.repository.AuthRepository
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository
import com.github.stm7128.hycohyco.domain.repository.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * 視聴履歴・ローカルプレイリスト・ダウンロードおよびクラウドライブラリを管理する ViewModel
 */
class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val offlineDownloadManager: OfflineDownloadManager? = null,
    private val authRepository: AuthRepository? = null,
    internal val cloudLibraryRepository: CloudLibraryRepository? = null,
    private val videoRepository: VideoRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    internal fun updateUiState(transform: (LibraryUiState) -> LibraryUiState) {
        _uiState.update(transform)
    }

    // 重複通信を防止する動画詳細メモリキャッシュ
    private val videoSummaryCache = ConcurrentHashMap<String, VideoSummary>()

    // 重複購読・通信を防止する Job 管理
    private var playlistDetailJob: Job? = null
    private var enrichHistoryJob: Job? = null
    private var enrichPlaylistJob: Job? = null
    internal var cloudHistoryJob: Job? = null
    internal var cloudMylistJob: Job? = null

    init {
        // 視聴履歴の購読 & 最新情報の非同期自動補完
        libraryRepository.getWatchHistory()
            .onEach { history ->
                _uiState.update { it.copy(historyItems = history) }
                enrichHistoryItems(history)
            }
            .launchIn(viewModelScope)

        // プレイリスト一覧の購読
        libraryRepository.getPlaylists()
            .onEach { playlists -> _uiState.update { it.copy(playlists = playlists) } }
            .launchIn(viewModelScope)

        // オフラインダウンロード一覧の購読
        offlineDownloadManager?.allDownloads
            ?.onEach { downloads ->
                val totalBytes = offlineDownloadManager.getTotalSizeBytes()
                _uiState.update {
                    it.copy(downloads = downloads, totalDownloadSizeBytes = totalBytes)
                }
            }
            ?.launchIn(viewModelScope)

        // ログイン状態の購読
        authRepository?.userSession
            ?.onEach { session ->
                val wasLoggedIn = _uiState.value.isUserLoggedIn
                _uiState.update { it.copy(isUserLoggedIn = session.isLoggedIn) }
                if (session.isLoggedIn && (!wasLoggedIn || _uiState.value.selectedSubTab == LibrarySubTab.MYLIST)) {
                    refreshCloudData()
                }
                if (session.isLoggedIn && !wasLoggedIn) {
                    refreshSelectedHistoryIfCloud()
                }
            }
            ?.launchIn(viewModelScope)
    }

    /** サブタブ (履歴 / マイリスト / ダウンロード) を切り替える */
    fun selectSubTab(tab: LibrarySubTab) {
        if (_uiState.value.selectedSubTab != tab) {
            cloudHistoryJob?.cancel()
            cloudMylistJob?.cancel()
        }
        _uiState.update { it.copy(selectedSubTab = tab, activePlaylist = null, activeCloudMylist = null) }
        if (tab == LibrarySubTab.MYLIST && _uiState.value.isUserLoggedIn) {
            refreshCloudData()
        } else if (tab == LibrarySubTab.HISTORY && _uiState.value.isUserLoggedIn) {
            loadHistoryTabIfNeeded(_uiState.value.selectedHistorySubTab)
        }
    }

    fun deleteDownload(videoId: String) = offlineDownloadManager?.deleteDownload(videoId)
    fun deleteHistoryItem(videoId: String) = viewModelScope.launch { libraryRepository.deleteWatchHistory(videoId) }
    fun clearAllHistory() = viewModelScope.launch { libraryRepository.clearAllWatchHistory() }

    fun openPlaylistDetail(playlist: LocalPlaylist) {
        _uiState.update { it.copy(activePlaylist = playlist) }
        playlistDetailJob?.cancel()
        playlistDetailJob = libraryRepository.getPlaylistVideos(playlist.id)
            .onEach { videos ->
                _uiState.update { it.copy(activePlaylistVideos = videos) }
                enrichPlaylistVideos(videos)
            }
            .launchIn(viewModelScope)
    }

    private fun enrichHistoryItems(items: List<WatchHistoryItem>) {
        if (videoRepository == null) return
        // キャッシュ済みデータがあれば即時 UI 反映 (0ms)
        if (items.any { videoSummaryCache.containsKey(it.videoId) }) {
            _uiState.update { state ->
                state.copy(historyItems = state.historyItems.map { h ->
                    val s = videoSummaryCache[h.videoId] ?: return@map h
                    h.copy(
                        ownerName = if (s.ownerName.isNotBlank() && !s.ownerName.startsWith("ID:")) s.ownerName else h.ownerName,
                        viewCounter = s.viewCounter, commentCounter = s.commentCounter,
                        likeCounter = s.likeCounter, mylistCounter = s.mylistCounter
                    )
                })
            }
        }

        val targets = items.filter { it.viewCounter <= 0 && !videoSummaryCache.containsKey(it.videoId) }
        Log.d("LibraryViewModel", "enrichHistoryItems: 全体=${items.size}件, 未取得=${targets.size}件")
        if (targets.isEmpty()) return

        enrichHistoryJob?.cancel()
        enrichHistoryJob = viewModelScope.launch {
            videoRepository.getVideoSummaries(targets.map { it.videoId })
                .onSuccess { summaries ->
                    Log.d("LibraryViewModel", "履歴スナップショット一括補完成功: ${summaries.size}件取得")
                    summaries.forEach { s -> videoSummaryCache[s.contentId] = s }
                    _uiState.update { state ->
                        state.copy(historyItems = state.historyItems.map { h ->
                            val s = videoSummaryCache[h.videoId] ?: return@map h
                            h.copy(
                                ownerName = if (s.ownerName.isNotBlank() && !s.ownerName.startsWith("ID:")) s.ownerName else h.ownerName,
                                viewCounter = s.viewCounter, commentCounter = s.commentCounter,
                                likeCounter = s.likeCounter, mylistCounter = s.mylistCounter
                            )
                        })
                    }
                }
                .onFailure { error -> Log.e("LibraryViewModel", "履歴スナップショット一括補完失敗", error) }
        }
    }

    private fun enrichPlaylistVideos(videos: List<PlaylistVideoItem>) {
        if (videoRepository == null) return
        // キャッシュ済みデータがあれば即時 UI 反映 (0ms)
        if (videos.any { videoSummaryCache.containsKey(it.videoId) }) {
            _uiState.update { state ->
                state.copy(activePlaylistVideos = state.activePlaylistVideos.map { p ->
                    val s = videoSummaryCache[p.videoId] ?: return@map p
                    p.copy(
                        ownerName = if (s.ownerName.isNotBlank() && !s.ownerName.startsWith("ID:")) s.ownerName else p.ownerName,
                        viewCounter = s.viewCounter, commentCounter = s.commentCounter,
                        likeCounter = s.likeCounter, mylistCounter = s.mylistCounter
                    )
                })
            }
        }

        val targets = videos.filter { it.viewCounter <= 0 && !videoSummaryCache.containsKey(it.videoId) }
        Log.d("LibraryViewModel", "enrichPlaylistVideos: 全体=${videos.size}件, 未取得=${targets.size}件")
        if (targets.isEmpty()) return

        enrichPlaylistJob?.cancel()
        enrichPlaylistJob = viewModelScope.launch {
            videoRepository.getVideoSummaries(targets.map { it.videoId })
                .onSuccess { summaries ->
                    Log.d("LibraryViewModel", "プレイリストスナップショット一括補完成功: ${summaries.size}件取得")
                    summaries.forEach { s -> videoSummaryCache[s.contentId] = s }
                    _uiState.update { state ->
                        state.copy(activePlaylistVideos = state.activePlaylistVideos.map { p ->
                            val s = videoSummaryCache[p.videoId] ?: return@map p
                            p.copy(
                                ownerName = if (s.ownerName.isNotBlank() && !s.ownerName.startsWith("ID:")) s.ownerName else p.ownerName,
                                viewCounter = s.viewCounter, commentCounter = s.commentCounter,
                                likeCounter = s.likeCounter, mylistCounter = s.mylistCounter
                            )
                        })
                    }
                }
                .onFailure { error -> Log.e("LibraryViewModel", "プレイリストスナップショット一括補完失敗", error) }
        }
    }

    fun closePlaylistDetail() {
        playlistDetailJob?.cancel()
        playlistDetailJob = null
        enrichPlaylistJob?.cancel()
        enrichPlaylistJob = null
        _uiState.update { it.copy(activePlaylist = null, activePlaylistVideos = emptyList()) }
    }
    fun createPlaylist(title: String) = viewModelScope.launch { libraryRepository.createPlaylist(title) }
    fun deletePlaylist(playlistId: String) = viewModelScope.launch {
        libraryRepository.deletePlaylist(playlistId)
        if (_uiState.value.activePlaylist?.id == playlistId) closePlaylistDetail()
    }
    fun removeVideoFromPlaylist(playlistId: String, videoId: String) = viewModelScope.launch {
        libraryRepository.removeVideoFromPlaylist(playlistId, videoId)
    }
}
