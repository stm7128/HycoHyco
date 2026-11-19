package com.github.stm7128.hycohyco.ui.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.github.stm7128.hycohyco.player.danmaku.DanmakuCanvas
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.player.danmaku.DanmakuRenderItem
import com.github.stm7128.hycohyco.player.model.PlaybackState
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import com.github.stm7128.hycohyco.player.model.isBuffering
import com.github.stm7128.hycohyco.player.model.isPlaying
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

/**
 * YouTube 公式アプリ準拠の操作体系（左上再生停止 / 右上閉じる / 小窓タップでフル展開）と
 * 156dp × 88dp のコンパクトサイズを備えたドラッグ移動可能フローティング・ミニプレイヤー
 */
@OptIn(UnstableApi::class)
@Composable
fun FloatingMiniPlayer(
    exoPlayer: ExoPlayer,
    playbackState: PlaybackState,
    danmakuItems: List<DanmakuRenderItem>,
    danmakuSettings: DanmakuDisplaySettings,
    playbackSpeed: Float,
    onExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isSurfaceActive: Boolean = true,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // 画面幅の約 48% (YouTube 公式キャプチャ準拠の黄金比サイズ)
    val miniWidthDp = (configuration.screenWidthDp * 0.48f).coerceIn(180f, 240f).dp
    val miniHeightDp = miniWidthDp * (9f / 16f)

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val miniWidthPx = with(density) { miniWidthDp.toPx() }
    val miniHeightPx = with(density) { miniHeightDp.toPx() }

    // 初期位置: 画面右下 (マージン 12dp、ナビバー上 90dp)
    var offsetX by remember { mutableFloatStateOf(screenWidthPx - miniWidthPx - with(density) { 12.dp.toPx() }) }
    var offsetY by remember { mutableFloatStateOf(screenHeightPx - miniHeightPx - with(density) { 90.dp.toPx() }) }

    // 画面回転やウィンドウサイズ変化時に位置を安全圏へクランプ (M-05)
    LaunchedEffect(screenWidthPx, screenHeightPx, miniWidthPx, miniHeightPx) {
        val maxOffsetX = (screenWidthPx - miniWidthPx).coerceAtLeast(0f)
        val minOffsetY = with(density) { 32.dp.toPx() }
        val maxOffsetY = (screenHeightPx - miniHeightPx - with(density) { 48.dp.toPx() }).coerceAtLeast(minOffsetY)
        offsetX = offsetX.coerceIn(0f, maxOffsetX)
        offsetY = offsetY.coerceIn(minOffsetY, maxOffsetY)
    }

    // ExoPlayer の実再生位置に 120fps/60fps 完全同期する弾幕位置
    var livePositionMs by remember { mutableLongStateOf(exoPlayer.currentPosition) }
    LaunchedEffect(playbackState.isPlaying) {
        if (!playbackState.isPlaying) {
            livePositionMs = exoPlayer.currentPosition
            return@LaunchedEffect
        }
        while (isActive) {
            withFrameMillis {
                livePositionMs = exoPlayer.currentPosition
            }
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(miniWidthDp)
            .aspectRatio(16f / 9f)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val maxOffsetX = (screenWidthPx - miniWidthPx).coerceAtLeast(0f)
                    val minOffsetY = with(density) { 32.dp.toPx() }
                    val maxOffsetY = (screenHeightPx - miniHeightPx - with(density) { 48.dp.toPx() }).coerceAtLeast(minOffsetY)
                    offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxOffsetX)
                    offsetY = (offsetY + dragAmount.y).coerceIn(minOffsetY, maxOffsetY)
                }
            }
            .clickable { onExpand() }
    ) {
        val shouldKeepScreenOn = isSurfaceActive && (playbackState.isPlaying || playbackState.isBuffering)
        // 1. ExoPlayer 映像サーフェス (排他アタッチ制御 & リリース時 null 化)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    keepScreenOn = shouldKeepScreenOn
                    if (isSurfaceActive) player = exoPlayer
                }
            },
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

        // 2. 120fps 弾幕描画レイヤー
        if (danmakuSettings.isEnabled) {
            DanmakuCanvas(
                renderItems = danmakuItems,
                currentPositionMs = livePositionMs,
                settings = danmakuSettings,
                modifier = Modifier.fillMaxSize()
            )
        }

        // バッファリングインジケーター
        if (playbackState.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                color = Color.White,
                strokeWidth = 2.dp
            )
        }

        // 3. YouTube 準拠コントロール (左上: 一時停止/再生、右上: 閉じる)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onTogglePlayPause() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playbackState.isPlaying) "一時停止" else "再生",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "閉じる",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
