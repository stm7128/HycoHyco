package com.github.stm7128.hycohyco.ui.player

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.data.download.OfflineDownloadManager
import com.github.stm7128.hycohyco.data.local.NgPreferencesDataSource
import com.github.stm7128.hycohyco.data.local.PlayerPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.CommentNgFilter
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.repository.InteractionRepository
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository
import com.github.stm7128.hycohyco.domain.repository.VideoRepository
import com.github.stm7128.hycohyco.domain.usecase.OfflinePlaybackData
import com.github.stm7128.hycohyco.domain.usecase.OfflinePlaybackResolver
import com.github.stm7128.hycohyco.player.NicoPlaybackManager
import com.github.stm7128.hycohyco.player.danmaku.DanmakuLayoutEngine
import com.github.stm7128.hycohyco.player.model.PlaybackState
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import com.github.stm7128.hycohyco.player.service.HycoPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 動画再生画面・ミニプレイヤーおよびコメント弾幕を管理する ViewModel
 */
class PlayerViewModel(
    internal val videoRepository: VideoRepository,
    val playbackManager: NicoPlaybackManager,
    internal val ngPreferencesDataSource: NgPreferencesDataSource,
    internal val libraryRepository: LibraryRepository,
    internal val playerPreferencesDataSource: PlayerPreferencesDataSource? = null,
    private val offlinePlaybackResolver: OfflinePlaybackResolver? = null,
    private val offlineDownloadManager: OfflineDownloadManager? = null,
    internal val interactionRepository: InteractionRepository? = null,
    internal val cloudLibraryRepository: com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository? = null,
    val sleepTimerManager: com.github.stm7128.hycohyco.player.SleepTimerManager? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    internal var seriesCountdownJob: kotlinx.coroutines.Job? = null
    internal var playJob: kotlinx.coroutines.Job? = null
    internal var danmakuSaveJob: kotlinx.coroutines.Job? = null

    internal fun updatePlayerUiState(transform: (PlayerUiState) -> PlayerUiState) {
        _uiState.update(transform)
    }

    init {
        playbackManager.playbackState
            .onEach { state ->
                if (_uiState.value.displayMode == PlayerDisplayMode.HIDDEN && _uiState.value.videoDetail == null) {
                    return@onEach
                }
                _uiState.update { it.copy(playbackState = state) }
                if (state is PlaybackState.Ready && !state.isPlaying) {
                    saveCurrentProgress()
                }
                if (state is PlaybackState.Ended && !_uiState.value.isRepeatOne) {
                    if (_uiState.value.playbackQueue.isNotEmpty()) {
                        playNextInQueue()
                    } else if (_uiState.value.isAutoplayEnabled && _uiState.value.videoDetail?.series?.next != null) {
                        handleSeriesPlaybackEnded()
                    }
                }
            }.launchIn(viewModelScope)

        ngPreferencesDataSource.ngConfig
            .onEach { config ->
                val raw = _uiState.value.comments
                val renderItems = withContext(Dispatchers.Default) {
                    DanmakuLayoutEngine.buildRenderItems(CommentNgFilter.filter(raw, config))
                }
                _uiState.update { it.copy(ngConfig = config, danmakuRenderItems = renderItems) }
            }.launchIn(viewModelScope)

        libraryRepository.getPlaylists()
            .onEach { playlists -> _uiState.update { it.copy(availablePlaylists = playlists) } }
            .launchIn(viewModelScope)

        playerPreferencesDataSource?.gestureMode?.onEach { mode -> _uiState.update { it.copy(gestureMode = mode) } }?.launchIn(viewModelScope)
        playerPreferencesDataSource?.autoplayEnabled?.onEach { enabled -> _uiState.update { it.copy(isAutoplayEnabled = enabled) } }?.launchIn(viewModelScope)
        playerPreferencesDataSource?.doubleTapSeekSeconds?.onEach { s -> _uiState.update { it.copy(doubleTapSeekSeconds = s) } }?.launchIn(viewModelScope)
        playerPreferencesDataSource?.useNicovideoGay?.onEach { gay -> _uiState.update { it.copy(useNicovideoGay = gay) } }?.launchIn(viewModelScope)
        playerPreferencesDataSource?.coexistAudio?.onEach { coexist -> playbackManager.setAudioCoexistence(coexist); _uiState.update { it.copy(isCoexistAudio = coexist) } }?.launchIn(viewModelScope)
        playerPreferencesDataSource?.autoEnterPip?.onEach { pip -> _uiState.update { it.copy(isAutoEnterPip = pip) } }?.launchIn(viewModelScope)
        playerPreferencesDataSource?.isResumePlaybackEnabled?.onEach { resume -> _uiState.update { it.copy(isResumePlaybackEnabled = resume) } }?.launchIn(viewModelScope)
        playerPreferencesDataSource?.danmakuSettings?.onEach { settings -> _uiState.update { it.copy(danmakuSettings = settings) } }?.launchIn(viewModelScope)
        sleepTimerManager?.currentMode?.onEach { mode -> _uiState.update { it.copy(sleepTimerMode = mode) } }?.launchIn(viewModelScope)
        sleepTimerManager?.remainingSeconds?.onEach { sec -> _uiState.update { it.copy(sleepTimerRemainingSeconds = sec) } }?.launchIn(viewModelScope)
    }

    /** 指定された動画 ID の再生を開始する */
    fun playVideo(videoId: String, context: Context? = null, forceOffline: Boolean = false, initialPositionMs: Long? = null) {
        playJob?.cancel()
        playJob = viewModelScope.launch {
            seriesCountdownJob?.cancel()
            seriesCountdownJob = null
            saveCurrentProgress()
            playbackManager.stop()

            _uiState.update {
                it.copy(
                    displayMode = PlayerDisplayMode.EXPANDED,
                    isLoading = true,
                    isCommentLoading = true,
                    errorMessage = null,
                    videoDetail = null,
                    comments = emptyList(),
                    danmakuRenderItems = emptyList(),
                    recommendVideos = emptyList(),
                    isRecommendLoading = true,
                    isOfflineMode = false,
                    isLiked = false,
                    likeCount = 0L,
                    isOwnerFollowed = false,
                    nextVideoCountdownSeconds = null,
                    pastLogDateLabel = null
                )
            }

            context?.let { ctx ->
                try { ctx.startService(Intent(ctx, HycoPlaybackService::class.java)) } catch (_: Exception) {}
            }

            if (forceOffline) {
                val offline = offlinePlaybackResolver?.resolve(videoId)
                if (offline != null) {
                    PlayerOfflinePlaybackHelper.playOffline(this@PlayerViewModel, offline, initialPositionMs)
                    return@launch
                }
            }
            playOnlineVideo(videoId, context, initialPositionMs)
        }
    }

    private suspend fun playOnlineVideo(videoId: String, context: Context?, initialPositionMs: Long? = null) {
        viewModelScope.launch { loadCommentsWithTimestamp(videoId) }
        viewModelScope.launch {
            PlayerRecommendOperations.loadRecommendVideos(videoRepository, videoId) { recs, loading ->
                _uiState.update { it.copy(recommendVideos = recs, isRecommendLoading = loading) }
            }
        }

        val detailResult = videoRepository.getVideoDetail(videoId)
        if (detailResult.isFailure) {
            val offline = offlinePlaybackResolver?.resolve(videoId)
            if (offline != null) {
                PlayerOfflinePlaybackHelper.playOffline(this@PlayerViewModel, offline, initialPositionMs)
                return
            }
            val error = detailResult.exceptionOrNull()
            _uiState.update { it.copy(isLoading = false, errorMessage = error?.localizedMessage ?: "動画取得失敗") }
            return
        }
        val detail = detailResult.getOrThrow()
        _uiState.update {
            it.copy(
                videoDetail = detail,
                isLiked = detail.isLiked,
                likeCount = detail.likeCount,
                isOwnerFollowed = detail.isOwnerFollowed
            )
        }
        syncOwnerFollowStatus(detail.ownerId)
        enrichTagsWithNicodic(detail.id, detail.tags)

        val qualityPref = PlayerQualityResolver.resolveQualityPreference(context, playerPreferencesDataSource)
        val sessionResult = videoRepository.getStreamSession(videoId = videoId, qualityPreference = qualityPref)
        if (sessionResult.isFailure) {
            val offline = offlinePlaybackResolver?.resolve(videoId)
            if (offline != null) {
                PlayerOfflinePlaybackHelper.playOffline(this@PlayerViewModel, offline, initialPositionMs)
                return
            }
            val error = sessionResult.exceptionOrNull()
            _uiState.update { it.copy(isLoading = false, errorMessage = "HLS取得失敗: ${error?.localizedMessage}") }
            return
        }
        val session = sessionResult.getOrThrow()

        playbackManager.prepareAndPlay(session = session, title = detail.title, artist = detail.ownerName, thumbnailUrl = detail.thumbnailUrl, initialPositionMs = initialPositionMs ?: 0L)
        val isResumeEnabled = playerPreferencesDataSource?.isResumePlaybackEnabled?.first() ?: _uiState.value.isResumePlaybackEnabled
        PlayerResumeHelper.restoreResumePosition(libraryRepository, playbackManager, detail.id, detail.durationSeconds * 1000L, detail.title, detail.ownerName, detail.thumbnailUrl, initialPositionMs, isResumeEnabled)
        _uiState.update { it.copy(isLoading = false) }
    }

    /** オフライン保存を予約・開始する */
    fun downloadCurrentVideo(downloadType: DownloadType = DownloadType.VIDEO_AND_DANMAKU) {
        val d = _uiState.value.videoDetail ?: return
        offlineDownloadManager?.enqueueDownload(
            videoId = d.id,
            title = d.title,
            durationMs = d.durationSeconds * 1000L,
            ownerName = d.ownerName,
            thumbnailUrl = d.thumbnailUrl,
            downloadType = downloadType
        )
    }

    /** 再生制御 */
    fun togglePlayPause() {
        if (_uiState.value.nextVideoCountdownSeconds != null) {
            cancelNextVideoCountdown()
        }
        playbackManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        if (_uiState.value.nextVideoCountdownSeconds != null) {
            cancelNextVideoCountdown()
        }
        playbackManager.seekTo(positionMs)
    }
    fun seekBy(deltaMs: Long) = playbackManager.seekBy(deltaMs)
    fun setPlaybackSpeed(speed: Float) { playbackManager.setPlaybackSpeed(speed); _uiState.update { it.copy(playbackSpeed = speed) } }
    fun setSpeed(speed: Float) = setPlaybackSpeed(speed)
    fun setRepeatOne(enabled: Boolean) { playbackManager.setRepeatOne(enabled); _uiState.update { it.copy(isRepeatOne = enabled) } }
    fun toggleSettingsSheet(visible: Boolean) = _uiState.update { it.copy(isSettingsSheetVisible = visible) }

    fun changeQuality(qualityId: String) {
        val detail = _uiState.value.videoDetail ?: return
        val currentPos = _uiState.value.playbackState.currentPositionMs
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedQualityId = qualityId) }
            videoRepository.getStreamSession(videoId = detail.id, preferredQualityId = qualityId).onSuccess { session ->
                playbackManager.prepareAndPlay(session = session, title = detail.title, artist = detail.ownerName, thumbnailUrl = detail.thumbnailUrl)
                playbackManager.seekTo(currentPos)
                _uiState.update { it.copy(isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = "画質切替失敗: ${error.localizedMessage}") }
            }
        }
    }

    fun expandPlayer() = _uiState.update { it.copy(displayMode = PlayerDisplayMode.EXPANDED) }
    fun minimizePlayer() = _uiState.update { it.copy(displayMode = PlayerDisplayMode.MINI) }
    fun toggleControlsVisibility() = _uiState.update { it.copy(isControlsVisible = !it.isControlsVisible) }

    fun closePlayer() {
        saveCurrentProgress()
        playbackManager.stop()
        _uiState.update {
            it.copy(
                displayMode = PlayerDisplayMode.HIDDEN,
                videoDetail = null,
                comments = emptyList(),
                danmakuRenderItems = emptyList(),
                errorMessage = null,
                isOfflineMode = false
            )
        }
    }

    /** エラー発生時や読み込み失敗時に現在の動画を再試行する */
    fun retryCurrentVideo(context: Context? = null) {
        val videoId = _uiState.value.videoDetail?.id
        if (!videoId.isNullOrBlank()) {
            val currentPos = _uiState.value.playbackState.currentPositionMs
            playVideo(videoId, context, initialPositionMs = currentPos)
        }
    }

    private fun saveCurrentProgress() {
        viewModelScope.launch {
            PlayerResumeHelper.saveCurrentProgress(
                libraryRepository,
                _uiState.value.videoDetail,
                _uiState.value.playbackState
            )
        }
    }

    fun setBottomTab(tab: PlayerBottomTab) = _uiState.update { it.copy(selectedBottomTab = tab) }
    fun clearUserFeedbackMessage() = _uiState.update { it.copy(userFeedbackMessage = null) }

    override fun onCleared() {
        val detail = _uiState.value.videoDetail
        val state = _uiState.value.playbackState
        if (detail != null) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                PlayerResumeHelper.saveCurrentProgress(libraryRepository, detail, state)
            }
        }
        super.onCleared()
    }
}
