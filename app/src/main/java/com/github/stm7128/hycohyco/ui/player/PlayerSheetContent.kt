package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import com.github.stm7128.hycohyco.player.model.durationMs
import com.github.stm7128.hycohyco.player.model.isPlaying
import com.github.stm7128.hycohyco.ui.LocalIsInPipMode
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton

/**
 * 展開時のプレイヤー画面全体コンテンツ
 */
@Composable
fun PlayerSheetContent(
    exoPlayer: ExoPlayer,
    uiState: PlayerUiState,
    onToggleControls: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onToggleDanmaku: () -> Unit = {},
    onUpdateDanmakuAlpha: (Float) -> Unit = {},
    onUpdateDanmakuSettings: (com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings) -> Unit = { onUpdateDanmakuAlpha(it.alpha) },
    onUpdateNgConfig: (CommentNgConfig) -> Unit = {},
    onSeekBy: (Long) -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    onEnterPip: () -> Unit = {},
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onCloseSettings: () -> Unit = {},
    onSpeedSelected: (Float) -> Unit = {},
    onRepeatToggle: (Boolean) -> Unit = {},
    onQualitySelected: (String) -> Unit = {},
    onPlayVideo: (String) -> Unit = {},
    onAddNgUserId: (String) -> Unit = {},
    onAddNgWord: (String) -> Unit = {},
    onOpenAddToPlaylist: () -> Unit = {},
    onCloseAddToPlaylist: () -> Unit = {},
    onAddToPlaylist: (String) -> Unit = {},
    onCreateAndAddToPlaylist: (String) -> Unit = {},
    onGestureModeSelected: (PlayerGestureMode) -> Unit = {},
    onDownloadVideo: (DownloadType) -> Unit = {},
    onToggleLike: () -> Unit = {},
    onToggleFollow: () -> Unit = {},
    onDismissLikeThanks: () -> Unit = {},
    onOpenCommentInput: () -> Unit = {},
    onCloseCommentInput: () -> Unit = {},
    onSubmitComment: (String, List<String>) -> Unit = { _, _ -> },
    onToggleAutoplay: () -> Unit = {},
    onAutoplayToggle: (Boolean) -> Unit = {},
    onResumePlaybackToggle: (Boolean) -> Unit = {},
    onCancelNextVideoCountdown: () -> Unit = {},
    onPlayNextVideo: () -> Unit = {},
    onDeleteComment: (com.github.stm7128.hycohyco.domain.model.CommentItem) -> Unit = {},
    onToggleNicoru: (com.github.stm7128.hycohyco.domain.model.CommentItem) -> Unit = {},
    onDoubleTapSeekSecondsChange: (Int) -> Unit = {},
    onAddToWatchLater: () -> Unit = {},
    onAddToMylist: (Long) -> Unit = {},
    onClearPlaylistMessage: () -> Unit = {},
    onUseNicovideoGayChange: (Boolean) -> Unit = {},
    onTimerModeSelected: (com.github.stm7128.hycohyco.player.SleepTimerMode) -> Unit = {},
    onCoexistAudioChange: (Boolean) -> Unit = {},
    onAutoEnterPipChange: (Boolean) -> Unit = {},
    onBottomTabChange: (PlayerBottomTab) -> Unit = {},
    onClearUserFeedbackMessage: () -> Unit = {},
    onLoadPastComments: (Long, String) -> Unit = { _, _ -> },
    onResetToLatestComments: () -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onAddTag: (String) -> Unit = {},
    onDeleteTag: (String) -> Unit = {},
    onOpenMylist: (Long) -> Unit = {},
    onOpenSeries: (Long) -> Unit = {},
) {
    val isInPip = LocalIsInPipMode.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val backgroundColor = HycoTheme.colors.background
    val textColor = HycoTheme.colors.onBackground
    val secondaryColor = HycoTheme.colors.onBackgroundVariant
    var selectedOwnerInfo by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var isShareSheetVisible by remember { mutableStateOf(false) }

    val handleShareVideo: () -> Unit = { isShareSheetVisible = true }

    @Composable
    fun RenderSurface(
        fullscreen: Boolean,
        surfMod: Modifier = Modifier,
        isSidePanelOpen: Boolean = false,
        onToggleSidePanel: () -> Unit = {},
    ) {
        val totalMs = (uiState.videoDetail?.durationSeconds ?: 0L) * 1000L
        val effectiveDurationMs = if (uiState.playbackState.durationMs > 0L) uiState.playbackState.durationMs else totalMs
        val heatmapDensity = remember(uiState.comments, effectiveDurationMs) {
            DanmakuHeatmapHelper.calculateHeatmapDensity(uiState.comments, effectiveDurationMs)
        }

        VideoPlayerSurface(
            exoPlayer = exoPlayer,
            playbackState = uiState.playbackState,
            isControlsVisible = if (isInPip) false else uiState.isControlsVisible,
            isLoading = uiState.isLoading,
            onToggleControls = if (isInPip) ({}) else onToggleControls,
            onTogglePlayPause = onTogglePlayPause,
            onSeekTo = onSeekTo,
            onMinimize = onMinimize,
            danmakuItems = uiState.danmakuRenderItems,
            danmakuSettings = uiState.danmakuSettings,
            onToggleDanmaku = onToggleDanmaku,
            onSeekBy = onSeekBy,
            onSpeedChange = onSpeedChange,
            onEnterPip = onEnterPip,
            isFullscreen = fullscreen,
            onToggleFullscreen = onToggleFullscreen,
            onOpenSettings = onOpenSettings,
            gestureMode = uiState.gestureMode,
            totalDurationFallbackMs = totalMs,
            playbackSpeed = uiState.playbackSpeed,
            isAutoplayEnabled = uiState.isAutoplayEnabled,
            onToggleAutoplay = onToggleAutoplay,
            nextVideoCountdownSeconds = uiState.nextVideoCountdownSeconds,
            seriesNextVideo = uiState.videoDetail?.series?.next,
            onCancelNextVideoCountdown = onCancelNextVideoCountdown,
            onPlayNextVideo = onPlayNextVideo,
            doubleTapSeekSeconds = uiState.doubleTapSeekSeconds,
            heatmapDensity = heatmapDensity,
            isSurfaceActive = isInPip || uiState.displayMode == PlayerDisplayMode.EXPANDED,
            isSidePanelOpen = isSidePanelOpen,
            onToggleSidePanel = onToggleSidePanel,
            modifier = surfMod
        )
    }

    if (isInPip) {
        RenderSurface(fullscreen = false, surfMod = modifier.fillMaxSize())
        return
    }

    Box(modifier = modifier.fillMaxSize().background(if (isFullscreen) Color.Black else backgroundColor)) {
        if (isFullscreen) {
            FullscreenPlayerContainer(
                isFullscreen = true,
                uiState = uiState,
                renderSurface = { isSidePanelOpen, onToggleSidePanel, surfMod ->
                    RenderSurface(
                        fullscreen = true,
                        surfMod = surfMod,
                        isSidePanelOpen = isSidePanelOpen,
                        onToggleSidePanel = onToggleSidePanel
                    )
                },
                onPlayVideo = onPlayVideo,
                onSeekTo = onSeekTo,
                onToggleLike = onToggleLike,
                onOpenCommentInput = onOpenCommentInput,
                onUpdateDanmakuAlpha = onUpdateDanmakuAlpha,
                onUpdateDanmakuSettings = onUpdateDanmakuSettings,
                onUpdateNgConfig = onUpdateNgConfig,
                onAddNgUserId = onAddNgUserId,
                onAddNgWord = onAddNgWord,
                onDeleteComment = onDeleteComment,
                onToggleNicoru = onToggleNicoru,
                onLoadPastComments = onLoadPastComments,
                onResetToLatestComments = onResetToLatestComments,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                if (uiState.errorMessage != null) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                HycoButton(text = "再試行", onClick = onRetry)
                                HycoButton(text = "閉じる", onClick = onClose)
                            }
                        }
                    }
                } else {
                    val detail = uiState.videoDetail
                    if (detail != null) {
                        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                        val isWideScreen = configuration.screenWidthDp >= 600 ||
                            configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                        PlayerResponsiveLayout(
                            isWideScreen = isWideScreen,
                            renderSurface = { RenderSurface(fullscreen = false) },
                            detail = detail,
                            uiState = uiState,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            onPlayVideo = onPlayVideo,
                            onSeekTo = onSeekTo,
                            onOpenAddToPlaylist = onOpenAddToPlaylist,
                            onOpenCommentInput = onOpenCommentInput,
                            onShareVideo = handleShareVideo,
                            onOwnerClick = { id, name -> selectedOwnerInfo = Pair(id, name) },
                            onMylistClick = onOpenMylist,
                            onSeriesClick = onOpenSeries,
                            onToggleLike = onToggleLike,
                            onToggleFollow = onToggleFollow,
                            onBottomTabChange = onBottomTabChange,
                            onUpdateDanmakuAlpha = onUpdateDanmakuAlpha,
                            onUpdateDanmakuSettings = onUpdateDanmakuSettings,
                            onUpdateNgConfig = onUpdateNgConfig,
                            onAddNgUserId = onAddNgUserId,
                            onAddNgWord = onAddNgWord,
                            onDeleteComment = onDeleteComment,
                            onToggleNicoru = onToggleNicoru,
                            onLoadPastComments = onLoadPastComments,
                            onResetToLatestComments = onResetToLatestComments,
                            onTagClick = onTagClick,
                            onAddTag = onAddTag,
                            onDeleteTag = onDeleteTag,
                        )
                    }
                }
            }
        }

        PlayerDialogsAndSheets(
            uiState = uiState,
            selectedOwnerInfo = selectedOwnerInfo,
            onDismissOwnerSheet = { selectedOwnerInfo = null },
            onCloseSettings = onCloseSettings,
            onSpeedSelected = onSpeedSelected,
            onRepeatToggle = onRepeatToggle,
            onQualitySelected = onQualitySelected,
            onGestureModeSelected = onGestureModeSelected,
            onDownloadVideo = onDownloadVideo,
            onCloseAddToPlaylist = onCloseAddToPlaylist,
            onAddToPlaylist = onAddToPlaylist,
            onCreateAndAddToPlaylist = onCreateAndAddToPlaylist,
            onPlayVideo = onPlayVideo,
            onDismissLikeThanks = onDismissLikeThanks,
            onCloseCommentInput = onCloseCommentInput,
            onSubmitComment = onSubmitComment,
            onAutoplayToggle = onAutoplayToggle,
            onResumePlaybackToggle = onResumePlaybackToggle,
            onDoubleTapSeekSecondsChange = onDoubleTapSeekSecondsChange,
            onAddToWatchLater = onAddToWatchLater,
            onAddToMylist = onAddToMylist,
            onClearPlaylistMessage = onClearPlaylistMessage,
            onUseNicovideoGayChange = onUseNicovideoGayChange,
            onTimerModeSelected = onTimerModeSelected,
            onCoexistAudioChange = onCoexistAudioChange,
            onAutoEnterPipChange = onAutoEnterPipChange,
        )

        PlayerFeedbackOverlay(
            uiState = uiState,
            isShareSheetVisible = isShareSheetVisible,
            onDismissShareSheet = { isShareSheetVisible = false },
            onClearUserFeedbackMessage = onClearUserFeedbackMessage,
        )
    }
}
