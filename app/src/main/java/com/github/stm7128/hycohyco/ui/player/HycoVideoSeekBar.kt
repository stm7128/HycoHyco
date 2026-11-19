package com.github.stm7128.hycohyco.ui.player

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 画面を圧迫しないスリム＆スマートな動画シークバー
 *
 * @param currentPositionMs 現在の再生位置 (ミリ秒)
 * @param durationMs 動画全体の長さ (ミリ秒)
 * @param bufferedPositionMs バッファ済み位置 (ミリ秒)
 * @param onSeekTo シーク確定時コールバック (ミリ秒)
 * @param onPreviewPositionMs ドラッグ中のプレビュー時刻通知コールバック
 * @param modifier 修飾子
 */
@Composable
fun HycoVideoSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onPreviewPositionMs: (Long?) -> Unit = {},
    heatmapDensity: FloatArray? = null
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragRatio by remember { mutableFloatStateOf(0f) }

    val safeDuration = durationMs.coerceAtLeast(1L).toFloat()
    val actualRatio = (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val bufferedRatio = (bufferedPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val displayRatio = if (isDragging) dragRatio else actualRatio

    val hasHeatmap = heatmapDensity != null && heatmapDensity.isNotEmpty()
    val totalHeight = if (hasHeatmap) 36.dp else 24.dp
    val heatmapHeight = if (hasHeatmap) 14.dp else 0.dp

    // 通常時 3.5dp、ドラッグ中 6.dp にアニメーション
    val barHeight by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 3.5.dp,
        label = "SeekBarHeight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .pointerInput(safeDuration) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isDragging = true
                    dragRatio = (down.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onPreviewPositionMs((dragRatio * safeDuration).toLong())

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            change.consume()
                            dragRatio = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            onPreviewPositionMs((dragRatio * safeDuration).toLong())
                        } else {
                            change.consume()
                            break
                        }
                    }

                    isDragging = false
                    onPreviewPositionMs(null)
                    val targetMs = (dragRatio * safeDuration).toLong()
                    onSeekTo(targetMs)
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
        ) {
            val width = size.width
            val fullH = size.height

            // 1. 弾幕盛り上がりヒートマップ波形 (上部に描画)
            if (heatmapDensity != null && heatmapDensity.isNotEmpty()) {
                val waveBottom = fullH - barHeight.toPx() - 2.dp.toPx()
                DanmakuHeatmapHelper.run {
                    drawHeatmapWave(
                        density = heatmapDensity,
                        playedRatio = displayRatio,
                        topY = 0f,
                        bottomY = waveBottom
                    )
                }
            }

            // 2. シークバーゲージ (下端に配置)
            val barH = barHeight.toPx()
            val barTop = fullH - barH
            val corner = CornerRadius(barH / 2f, barH / 2f)

            // 背景トラック
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(0f, barTop),
                size = Size(width, barH),
                cornerRadius = corner
            )

            // バッファ進捗ゲージ
            if (bufferedRatio > 0f) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.45f),
                    topLeft = Offset(0f, barTop),
                    size = Size(width * bufferedRatio, barH),
                    cornerRadius = corner
                )
            }

            // 再生進捗ゲージ (ニコニコブルー)
            drawRoundRect(
                color = Color(0xFF0095FF),
                topLeft = Offset(0f, barTop),
                size = Size(width * displayRatio, barH),
                cornerRadius = corner
            )

            // ドラッグ時のサム（つまみ）
            if (isDragging) {
                val thumbRadius = 6.dp.toPx()
                val thumbCenterX = (width * displayRatio).coerceIn(thumbRadius, width - thumbRadius)
                drawCircle(
                    color = Color.White,
                    radius = thumbRadius,
                    center = Offset(thumbCenterX, barTop + barH / 2f)
                )
            }
        }
    }
}

/** コントロール非表示時に画面下端に表示する 2.dp の極細進捗ライン */
@Composable
fun MiniBottomProgressBar(currentPositionMs: Long, durationMs: Long, modifier: Modifier = Modifier) {
    val safeDuration = durationMs.coerceAtLeast(1L).toFloat()
    val ratio = (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    Canvas(modifier = modifier.fillMaxWidth().height(2.dp)) {
        drawRect(color = Color.White.copy(alpha = 0.2f), size = size)
        drawRect(color = Color(0xFF0095FF), size = Size(size.width * ratio, size.height))
    }
}

/** ミリ秒を "mm:ss" 形式にフォーマットする */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

