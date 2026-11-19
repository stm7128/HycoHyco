package com.github.stm7128.hycohyco.ui.shorts

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.data.download.OfflineDownloadManager
import com.github.stm7128.hycohyco.data.local.NgPreferencesDataSource
import com.github.stm7128.hycohyco.data.local.PlayerPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.CommentNgFilter
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import com.github.stm7128.hycohyco.domain.repository.InteractionRepository
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository
import com.github.stm7128.hycohyco.domain.repository.VideoRepository
import com.github.stm7128.hycohyco.player.NicoPlaybackManager
import com.github.stm7128.hycohyco.player.danmaku.DanmakuLayoutEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * ニコニコショート (縦スワイプ連続視聴) ViewModel
 */
class ShortsViewModel(
    internal val videoRepository: VideoRepository,
    internal val playbackManager: NicoPlaybackManager,
    internal val ngPreferencesDataSource: NgPreferencesDataSource,
    internal val playerPreferencesDataSource: PlayerPreferencesDataSource,
    internal val libraryRepository: LibraryRepository,
    internal val interactionRepository: InteractionRepository,
    internal val cloudLibraryRepository: CloudLibraryRepository? = null,
    internal val offlineDownloadManager: OfflineDownloadManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShortsUiState())
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    internal val pageCache = ConcurrentHashMap<String, ShortPageCache>()
    internal var latestNgConfig = CommentNgConfig()
    private var playLoadJob: Job? = null
    internal var extendJob: Job? = null
    internal val prefetchingIds = ConcurrentHashMap.newKeySet<String>()
    private var isShortsSessionActive = false
    internal var lastRequestedIndex = -1
    internal var lastRequestedVideoId: String? = null
    private var prevRepeatOne: Boolean = false
    private var prevSpeed: Float = 1.0f

    internal fun getPageCache(videoId: String): ShortPageCache? {
        val entry = pageCache[videoId] ?: return null
        if (entry.isExpired()) {
            pageCache.remove(videoId)
            return null
        }
        return entry
    }

    internal fun putPageCache(videoId: String, cache: ShortPageCache) {
        if (pageCache.size >= 20) {
            val oldest = pageCache.entries.minByOrNull { it.value.cachedAt }?.key
            if (oldest != null) pageCache.remove(oldest)
        }
        pageCache[videoId] = cache
    }

    init {
        playerPreferencesDataSource.danmakuSettings
            .onEach { settings -> _uiState.update { it.copy(danmakuSettings = settings) } }
            .launchIn(viewModelScope)

        playerPreferencesDataSource.useNicovideoGay
            .onEach { gay -> _uiState.update { it.copy(useNicovideoGay = gay) } }
            .launchIn(viewModelScope)

        ngPreferencesDataSource.ngConfig
            .onEach { config ->
                latestNgConfig = config
                refreshActiveCommentsFilter()
            }
            .launchIn(viewModelScope)

        libraryRepository.getPlaylists()
            .onEach { lists -> _uiState.update { it.copy(localPlaylists = lists) } }
            .launchIn(viewModelScope)

        offlineDownloadManager?.allDownloads
            ?.onEach { downloads ->
                _uiState.update { it.copy(downloadsByVideoId = downloads.associateBy { item -> item.videoId }) }
            }
            ?.launchIn(viewModelScope)
    }

    internal fun updateUiState(transform: (ShortsUiState) -> ShortsUiState) {
        _uiState.update(transform)
    }

    /** 指定したショート動画ID (および任意の初期キュー) からオーバーレイ再生を開始する */
    fun openShortById(videoId: String, context: Context?, initialQueue: List<VideoSummary>? = null) {
        openShortByIdInternal(videoId, context, initialQueue)
    }

    /** ボトムバー『ショート』タブ等からフィードをロードして開始する */
    fun ensureTabFeedLoaded(context: Context?, forceRefresh: Boolean = false) {
        ensureTabFeedLoadedInternal(context, forceRefresh)
    }

    /** VerticalPager のページ確定時に該当ページの映像・コメント・弾幕を再生する */
    fun onPageSettled(index: Int, context: Context?) {
        val target = _uiState.value.items.getOrNull(index) ?: return
        val isAlreadyActive = isShortsSessionActive &&
            index == lastRequestedIndex &&
            target.contentId == lastRequestedVideoId &&
            (_uiState.value.isLoadingCurrentVideo || _uiState.value.activeDetail?.id == target.contentId)
        if (isAlreadyActive) return

        if (!isShortsSessionActive) {
            prevRepeatOne = playbackManager.isRepeatOne
            prevSpeed = playbackManager.currentPlaybackSpeed
        }
        isShortsSessionActive = true
        lastRequestedIndex = index
        lastRequestedVideoId = target.contentId

        playLoadJob?.cancel()
        playbackManager.stop()

        _uiState.update {
            it.copy(
                currentIndex = index,
                isLoadingCurrentVideo = true,
                errorMessage = null,
                activeDetail = null,
                comments = emptyList(),
                danmakuItems = emptyList(),
                isCommentSheetVisible = false,
                isCommentInputVisible = false,
                isShareSheetVisible = false,
                isPlaylistSheetVisible = false,
                isDetailSheetVisible = false
            )
        }

        playLoadJob = viewModelScope.launch {
            playbackManager.setRepeatOne(true)
            val cached = getPageCache(target.contentId)
            val pageData = cached ?: fetchShortPageData(target.contentId, context)
            if (pageData == null || pageData.streamSession == null) {
                _uiState.update {
                    it.copy(isLoadingCurrentVideo = false, errorMessage = "このショート動画を再生できませんでした")
                }
                return@launch
            }

            putPageCache(target.contentId, pageData)
            val detail = pageData.detail
            val filteredComments = withContext(Dispatchers.Default) {
                CommentNgFilter.filter(pageData.rawComments, latestNgConfig)
            }
            val danmaku = withContext(Dispatchers.Default) {
                DanmakuLayoutEngine.buildRenderItems(filteredComments, isPortraitMode = true)
            }

            _uiState.update { state ->
                val updatedItems = state.items.map { item ->
                    if (item.contentId == detail.id && (item.title == "読み込み中..." || item.thumbnailUrl.isBlank())) {
                        item.copy(
                            title = detail.title,
                            ownerName = detail.ownerName,
                            ownerId = detail.ownerId.toString(),
                            thumbnailUrl = detail.thumbnailUrl,
                            viewCounter = detail.viewCount.toInt(),
                            commentCounter = detail.commentCount.toInt(),
                            likeCounter = detail.likeCount.toInt(),
                            mylistCounter = detail.mylistCount.toInt(),
                            lengthSeconds = detail.durationSeconds.toInt(),
                            contentType = "short"
                        )
                    } else item
                }
                state.copy(
                    items = updatedItems,
                    isLoadingCurrentVideo = false,
                    activeDetail = detail,
                    comments = filteredComments,
                    danmakuItems = danmaku,
                    isLiked = detail.isLiked,
                    likeCount = detail.likeCount,
                    isOwnerFollowed = detail.isOwnerFollowed
                )
            }

            playbackManager.prepareAndPlay(
                session = pageData.streamSession,
                title = detail.title,
                artist = detail.ownerName,
                thumbnailUrl = detail.thumbnailUrl
            )
            saveHistoryIfNeeded(detail)
            refreshOwnerFollowState(detail.ownerId)
            enrichTagsWithNicodic(detail.id, detail.tags)
            prefetchNextShort(index + 1, context)
            prefetchNextShort(index + 2, context)
            if (index >= _uiState.value.items.size - 5) {
                extendQueueFromSeed(detail.id)
            }
        }
    }

    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)

    fun toggleDanmaku() {
        val next = _uiState.value.danmakuSettings.copy(isEnabled = !_uiState.value.danmakuSettings.isEnabled)
        viewModelScope.launch { playerPreferencesDataSource.setDanmakuSettings(next) }
    }

    /** ショート再生を停止して ExoPlayer のリピート・速度設定を通常へ戻す */
    fun stopShortsPlayback() {
        if (!isShortsSessionActive && playLoadJob == null) return
        isShortsSessionActive = false
        lastRequestedIndex = -1
        lastRequestedVideoId = null
        playLoadJob?.cancel()
        playLoadJob = null
        playbackManager.setRepeatOne(prevRepeatOne)
        if (prevSpeed > 0f) {
            playbackManager.setPlaybackSpeed(prevSpeed)
        }
        playbackManager.stop()
    }

    /** オーバーレイ型ショート画面を閉じる */
    fun closeOverlay() {
        if (!_uiState.value.isActiveOverlay) return
        stopShortsPlayback()
        _uiState.update {
            it.copy(
                isActiveOverlay = false,
                isCommentSheetVisible = false,
                isCommentInputVisible = false,
                isShareSheetVisible = false,
                isPlaylistSheetVisible = false,
                isDetailSheetVisible = false
            )
        }
    }
}
