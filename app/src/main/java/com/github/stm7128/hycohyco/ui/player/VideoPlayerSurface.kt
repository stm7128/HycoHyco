package com.github.stm7128.hycohyco.ui.player

import android.os.SystemClock
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CommentsDisabled
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.player.danmaku.DanmakuCanvas
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.player.danmaku.DanmakuRenderItem
import com.github.stm7128.hycohyco.player.gesture.PlayerGestureBox
import com.github.stm7128.hycohyco.player.model.PlaybackState
import com.github.stm7128.hycohyco.player.model.bufferedPositionMs
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import com.github.stm7128.hycohyco.player.model.durationMs
import com.github.stm7128.hycohyco.player.model.isBuffering
import com.github.stm7128.hycohyco.player.model.isPlaying
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** 映像描画サーフェスとオーバーレイ操作コントロール */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSurface(
    exoPlayer: ExoPlayer,
    playbackState: PlaybackState,
    isControlsVisible: Boolean,
    isLoading: Boolean,
    onToggleControls: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier,
    danmakuItems: List<DanmakuRenderItem> = emptyList(),
    danmakuSettings: DanmakuDisplaySettings = DanmakuDisplaySettings(),
    onToggleDanmaku: () -> Unit = {},
    onSeekBy: (Long) -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    onEnterPip: () -> Unit = {},
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    gestureMode: PlayerGestureMode = PlayerGestureMode.FULLSCREEN_ONLY,
    totalDurationFallbackMs: Long = 0L,
    playbackSpeed: Float = 1.0f,
    isAutoplayEnabled: Boolean = true,
    onToggleAutoplay: () -> Unit = {},
    nextVideoCountdownSeconds: Int? = null,
    seriesNextVideo: com.github.stm7128.hycohyco.domain.model.VideoSeriesItem? = null,
    onCancelNextVideoCountdown: () -> Unit = {},
    onPlayNextVideo: () -> Unit = {},
    doubleTapSeekSeconds: Int = 10,
    heatmapDensity: FloatArray? = null,
    isSurfaceActive: Boolean = true,
    isSidePanelOpen: Boolean = false,
    onToggleSidePanel: () -> Unit = {},
) {
    val context = LocalContext.current
    val playerView = remember {
        PlayerView(context).apply {
            useController = false
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
    var previewPositionMs by remember { mutableStateOf<Long?>(null) }

    DisposableEffect(exoPlayer, isSurfaceActive) {
        if (isSurfaceActive) {
            playerView.player = exoPlayer
        } else if (playerView.player === exoPlayer) {
            playerView.player = null
        }
        onDispose {
            playerView.keepScreenOn = false
            if (playerView.player === exoPlayer) {
                playerView.player = null
            }
        }
    }

    var animPositionMs by remember { mutableLongStateOf(0L) }
    val currentPosState by rememberUpdatedState(playbackState.currentPositionMs)
    val playbackSpeedState by rememberUpdatedState(playbackSpeed)

    LaunchedEffect(playbackState.isPlaying) {
        if (playbackState.isPlaying) {
            var basePlayerPos = currentPosState
            var baseRealTime = SystemClock.uptimeMillis()
            while (isActive) {
                withFrameMillis {
                    val currentPos = currentPosState
                    val realElapsedMs = SystemClock.uptimeMillis() - baseRealTime
                    val speed = if (playbackSpeedState > 0f) playbackSpeedState else 1.0f
                    val expectedPos = basePlayerPos + (realElapsedMs * speed).toLong()

                    // シーク等で実際の再生位置と補間位置が大きく乖離した場合 (300ms以上) はベースを同期
                    if (kotlin.math.abs(currentPos - expectedPos) > 300L) {
                        basePlayerPos = currentPos
                        baseRealTime = SystemClock.uptimeMillis()
                        animPositionMs = currentPos
                    } else {
                        animPositionMs = expectedPos
                    }
                }
            }
        } else {
            animPositionMs = playbackState.currentPositionMs
        }
    }

    LaunchedEffect(isControlsVisible, playbackState.isPlaying, previewPositionMs) {
        if (isControlsVisible && playbackState.isPlaying && previewPositionMs == null) {
            delay(3000L)
            onToggleControls()
        }
    }

    val gestureModifier = if (isFullscreen) {
        modifier.fillMaxSize().clipToBounds().background(Color.Black)
    } else {
        modifier.fillMaxWidth().aspectRatio(16f / 9f).clipToBounds().background(Color.Black)
    }

    PlayerGestureBox(
        onToggleControls = onToggleControls,
        onSeekBy = onSeekBy,
        onSpeedChange = onSpeedChange,
        currentSpeed = playbackSpeed,
        gestureMode = gestureMode,
        isFullscreen = isFullscreen,
        onMinimize = onMinimize,
        doubleTapSeekSeconds = doubleTapSeekSeconds,
        modifier = gestureModifier,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val shouldKeepScreenOn = isSurfaceActive && (playbackState.isPlaying || playbackState.isBuffering)
            AndroidView(
                factory = { playerView },
                update = { view ->
                    view.keepScreenOn = shouldKeepScreenOn
                    if (isSurfaceActive) {
                        if (view.player !== exoPlayer) view.player = exoPlayer
                    } else if (view.player === exoPlayer) {
                        view.player = null
                    }
                },
                onRelease = { view ->
                    view.keepScreenOn = false
                    if (view.player === exoPlayer) view.player = null
                },
                modifier = Modifier.fillMaxSize()
            )

            DanmakuCanvas(
                renderItems = danmakuItems,
                currentPositionMs = animPositionMs,
                settings = danmakuSettings,
                modifier = Modifier.fillMaxSize(),
            )

            if (isLoading || playbackState.isBuffering) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
            }

            if (playbackState is PlaybackState.Error) {
                Surface(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = playbackState.message,
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HycoButton(
                            text = "再読み込み",
                            onClick = onTogglePlayPause,
                        )
                    }
                }
            }

            val effectiveDurationMs = if (playbackState.durationMs > 0L) playbackState.durationMs else totalDurationFallbackMs

            // 1. コントロール表示時: フル操作オーバーレイ
            VideoPlayerControlsOverlay(
                isVisible = isControlsVisible,
                isPlaying = playbackState.isPlaying,
                currentPositionMs = playbackState.currentPositionMs,
                durationMs = effectiveDurationMs,
                bufferedPositionMs = playbackState.bufferedPositionMs,
                previewPositionMs = previewPositionMs,
                isFullscreen = isFullscreen,
                isAutoplayEnabled = isAutoplayEnabled,
                isDanmakuEnabled = danmakuSettings.isEnabled,
                onToggleFullscreen = onToggleFullscreen,
                onMinimize = onMinimize,
                onToggleAutoplay = onToggleAutoplay,
                onOpenSettings = onOpenSettings,
                onEnterPip = onEnterPip,
                onToggleDanmaku = onToggleDanmaku,
                onTogglePlayPause = onTogglePlayPause,
                onSeekTo = onSeekTo,
                onPreviewPositionMs = { previewPositionMs = it },
                heatmapDensity = heatmapDensity,
                isSidePanelOpen = isSidePanelOpen,
                onToggleSidePanel = onToggleSidePanel,
            )

            // 2. コントロール非表示時: 直接タップ・ドラッグシーク可能なインタラクティブ極細シークバー
            if (!isControlsVisible) {
                InteractiveMiniBottomSeekBar(
                    currentPositionMs = playbackState.currentPositionMs,
                    durationMs = effectiveDurationMs,
                    bufferedPositionMs = playbackState.bufferedPositionMs,
                    onSeekTo = onSeekTo,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // 次の動画の自動再生カウントダウンオーバーレイ
            NextVideoCountdownOverlay(
                countdownSeconds = nextVideoCountdownSeconds,
                nextVideo = seriesNextVideo,
                onCancel = onCancelNextVideoCountdown,
                onPlayNow = onPlayNextVideo,
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = if (isControlsVisible) 48.dp else 12.dp)
            )
        }
    }
}
