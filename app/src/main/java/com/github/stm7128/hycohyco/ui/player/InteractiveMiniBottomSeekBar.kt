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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * コントロール非表示時でも直接タップ・ドラッグでシークできるインタラクティブ極細シークバー
 */
@Composable
fun InteractiveMiniBottomSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long = 0L,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.coerceAtLeast(1L).toFloat()
    var isDragging by remember { mutableStateOf(false) }
    var dragRatio by remember { mutableFloatStateOf(0f) }

    val actualRatio = (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val bufferedRatio = (bufferedPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val displayRatio = if (isDragging) dragRatio else actualRatio

    val barHeight by animateDpAsState(
        targetValue = if (isDragging) 5.dp else 2.5.dp,
        label = "MiniBarHeight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(safeDuration) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isDragging = true
                    dragRatio = (down.position.x / size.width.toFloat()).coerceIn(0f, 1f)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            change.consume()
                            dragRatio = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        } else {
                            change.consume()
                            break
                        }
                    }

                    isDragging = false
                    val targetMs = (dragRatio * safeDuration).toLong()
                    onSeekTo(targetMs)
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // ドラッグ中のプレビュー時刻ピル
        if (isDragging) {
            val previewMs = (dragRatio * safeDuration).toLong()
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "${formatDuration(previewMs)} / ${formatDuration(durationMs)}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val width = size.width
            val height = size.height

            // 1. 背景
            drawRect(
                color = Color.White.copy(alpha = 0.25f),
                size = Size(width, height)
            )

            // 2. バッファ進捗
            if (bufferedRatio > 0f) {
                drawRect(
                    color = Color.White.copy(alpha = 0.45f),
                    size = Size(width * bufferedRatio, height)
                )
            }

            // 3. 再生進捗
            drawRect(
                color = Color(0xFF0095FF),
                size = Size(width * displayRatio, height)
            )

            // 4. ドラッグ時のサム
            if (isDragging) {
                val thumbRadius = 5.dp.toPx()
                val thumbCenterX = (width * displayRatio).coerceIn(thumbRadius, width - thumbRadius)
                drawCircle(
                    color = Color.White,
                    radius = thumbRadius,
                    center = Offset(thumbCenterX, height / 2f)
                )
            }
        }
    }
}
