package com.github.stm7128.hycohyco.ui.main

import android.app.Activity
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.stm7128.hycohyco.ui.player.*

/**
 * メインシェル画面上の通常展開プレイヤーおよびフローティングミニプレイヤーのオーバーレイ
 */
@Composable
internal fun MainShellPlayerOverlays(
    playerViewModel: PlayerViewModel,
    playerUiState: PlayerUiState,
    isInPip: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onSearchTag: (String) -> Unit,
    onOpenMylist: (Long) -> Unit = {},
    onOpenSeries: (Long) -> Unit = {},
    context: Context
) {
    // Android 14/15 予測型戻る（Predictive Back）によるプレイヤー縮小制御
    val predictiveBackState = rememberPlayerPredictiveBackState()
    val isPredictiveBackEnabled = !isFullscreen && !isInPip && playerUiState.displayMode == PlayerDisplayMode.EXPANDED
    PlayerPredictiveBackHandler(
        enabled = isPredictiveBackEnabled,
        state = predictiveBackState,
        onComplete = { playerViewModel.minimizePlayer() }
    )

    // 展開プレイヤー（EXPANDED モードまたは PiP モード時に表示）
    AnimatedVisibility(
        visible = (playerUiState.displayMode == PlayerDisplayMode.EXPANDED || isInPip) &&
            (playerUiState.isLoading || playerUiState.videoDetail != null || playerUiState.errorMessage != null),
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.fillMaxSize(),
    ) {
        PlayerSheetContent(
            exoPlayer = playerViewModel.playbackManager.exoPlayer,
            uiState = playerUiState,
            modifier = Modifier.playerPredictiveBackGraphics(predictiveBackState),
            onToggleControls = { playerViewModel.toggleControlsVisibility() },
            onTogglePlayPause = { playerViewModel.togglePlayPause() },
            onSeekTo = { pos -> playerViewModel.seekTo(pos) },
            onSeekBy = { delta -> playerViewModel.seekBy(delta) },
            onSpeedChange = { speed -> playerViewModel.setPlaybackSpeed(speed) },
            onEnterPip = { (context as? Activity)?.let { PipHelper.enterPip(it) } },
            onMinimize = { playerViewModel.minimizePlayer() },
            onClose = { playerViewModel.closePlayer() },
            onRetry = { playerViewModel.retryCurrentVideo(context) },
            onToggleDanmaku = { playerViewModel.toggleDanmaku() },
            onUpdateDanmakuAlpha = { alpha -> playerViewModel.setDanmakuAlpha(alpha) },
            onUpdateDanmakuSettings = { settings -> playerViewModel.updateDanmakuSettings(settings) },
            onUpdateNgConfig = { config -> playerViewModel.updateNgConfig(config) },
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            onOpenSettings = { playerViewModel.toggleSettingsSheet(true) },
            onCloseSettings = { playerViewModel.toggleSettingsSheet(false) },
            onSpeedSelected = { speed -> playerViewModel.setSpeed(speed) },
            onRepeatToggle = { enabled -> playerViewModel.setRepeatOne(enabled) },
            onQualitySelected = { qId -> playerViewModel.changeQuality(qId) },
            onPlayVideo = onPlayVideo,
            onOpenMylist = onOpenMylist,
            onOpenSeries = onOpenSeries,
            onAddNgUserId = { userId -> playerViewModel.addNgUserId(userId) },
            onAddNgWord = { word -> playerViewModel.addNgWord(word) },
            onOpenAddToPlaylist = { playerViewModel.toggleAddToPlaylistSheet(true) },
            onCloseAddToPlaylist = { playerViewModel.toggleAddToPlaylistSheet(false) },
            onAddToPlaylist = { playerViewModel.addCurrentVideoToPlaylist(it) },
            onCreateAndAddToPlaylist = { playerViewModel.createPlaylistAndAddCurrentVideo(it) },
            onAddToWatchLater = { playerViewModel.addCurrentVideoToWatchLater() },
            onAddToMylist = { playerViewModel.addCurrentVideoToMylist(it) },
            onClearPlaylistMessage = { playerViewModel.clearPlaylistMessage() },
            onGestureModeSelected = { mode -> playerViewModel.setGestureMode(mode) },
            onDownloadVideo = { type -> playerViewModel.downloadCurrentVideo(type) },
            onToggleLike = { playerViewModel.toggleLike() },
            onToggleFollow = { playerViewModel.toggleFollowOwner() },
            onDismissLikeThanks = { playerViewModel.dismissLikeThanksMessage() },
            onOpenCommentInput = { playerViewModel.toggleCommentInputSheet(true) },
            onCloseCommentInput = { playerViewModel.toggleCommentInputSheet(false) },
            onSubmitComment = { body, commands -> playerViewModel.submitComment(body, commands) },
            onToggleAutoplay = { playerViewModel.toggleAutoplay() },
            onAutoplayToggle = { enabled -> playerViewModel.setAutoplayEnabled(enabled) },
            onResumePlaybackToggle = { playerViewModel.setResumePlaybackEnabled(it) },
            onCancelNextVideoCountdown = { playerViewModel.cancelNextVideoCountdown() },
            onPlayNextVideo = { playerViewModel.playNextSeriesVideo(context) },
            onDeleteComment = { playerViewModel.deleteComment(it) },
            onToggleNicoru = { playerViewModel.toggleNicoru(it) },
            onDoubleTapSeekSecondsChange = { playerViewModel.setDoubleTapSeekSeconds(it) },
            onUseNicovideoGayChange = { playerViewModel.setUseNicovideoGay(it) },
            onTimerModeSelected = { playerViewModel.setSleepTimerMode(it) },
            onCoexistAudioChange = { playerViewModel.setCoexistAudio(it) },
            onAutoEnterPipChange = { playerViewModel.setAutoEnterPip(it) },
            onBottomTabChange = { playerViewModel.setBottomTab(it) },
            onClearUserFeedbackMessage = { playerViewModel.clearUserFeedbackMessage() },
            onLoadPastComments = { ts, label -> playerViewModel.loadPastComments(ts, label) },
            onResetToLatestComments = { playerViewModel.resetToLatestComments() },
            onTagClick = onSearchTag,
            onAddTag = { playerViewModel.addTag(it) },
            onDeleteTag = { playerViewModel.deleteTag(it) },
        )
    }

    // ドラッグ移動可能なフローティング・ミニプレイヤー小窓
    AnimatedVisibility(
        visible = !isInPip && !isFullscreen && playerUiState.displayMode == PlayerDisplayMode.MINI &&
            (playerUiState.isLoading || playerUiState.videoDetail != null),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        FloatingMiniPlayer(
            exoPlayer = playerViewModel.playbackManager.exoPlayer,
            playbackState = playerUiState.playbackState,
            danmakuItems = playerUiState.danmakuRenderItems,
            danmakuSettings = playerUiState.danmakuSettings,
            playbackSpeed = playerUiState.playbackSpeed,
            onExpand = { playerViewModel.expandPlayer() },
            onTogglePlayPause = { playerViewModel.togglePlayPause() },
            onClose = { playerViewModel.closePlayer() },
            isSurfaceActive = playerUiState.displayMode == PlayerDisplayMode.MINI
        )
    }
}
