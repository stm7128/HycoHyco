package com.github.stm7128.hycohyco.ui.player

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.stm7128.hycohyco.domain.model.CommentItem

/**
 * 弾幕盛り上がりヒートマップの集計・正規化および波形描画ヘルパー
 */
object DanmakuHeatmapHelper {

    /** ヒートマップの分割バケット数 (横解像度) */
    const val BUCKET_COUNT = 60

    /**
     * コメント一覧と動画長から、0.0f〜1.0f に正規化されたヒートマップ密度配列を計算する
     */
    fun calculateHeatmapDensity(comments: List<CommentItem>, durationMs: Long): FloatArray {
        if (comments.isEmpty() || durationMs <= 0L) return FloatArray(0)

        val rawCounts = IntArray(BUCKET_COUNT) { 0 }
        val bucketDuration = durationMs.toFloat() / BUCKET_COUNT

        for (comment in comments) {
            val idx = (comment.vposMs / bucketDuration).toInt().coerceIn(0, BUCKET_COUNT - 1)
            rawCounts[idx]++
        }

        // 3点移動平均によるスムージング
        val smoothed = FloatArray(BUCKET_COUNT)
        for (i in 0 until BUCKET_COUNT) {
            val prev = if (i > 0) rawCounts[i - 1].toFloat() else rawCounts[i].toFloat()
            val curr = rawCounts[i].toFloat()
            val next = if (i < BUCKET_COUNT - 1) rawCounts[i + 1].toFloat() else rawCounts[i].toFloat()
            smoothed[i] = (prev * 0.25f) + (curr * 0.5f) + (next * 0.25f)
        }

        val maxVal = smoothed.maxOrNull() ?: 0f
        if (maxVal <= 0f) return FloatArray(0)

        return FloatArray(BUCKET_COUNT) { i ->
            (smoothed[i] / maxVal).coerceIn(0f, 1f)
        }
    }

    private val cachedPath = Path()

    /**
     * シークバー上部に滑らかな弾幕盛り上がり波形を描画する
     *
     * @param density 0.0f〜1.0f の密度配列
     * @param playedRatio 再生済み割合 (0.0f〜1.0f)
     * @param topY 波形の上端 Y 座標
     * @param bottomY 波形の下端 (シークバー上端) Y 座標
     */
    fun DrawScope.drawHeatmapWave(
        density: FloatArray,
        playedRatio: Float,
        topY: Float,
        bottomY: Float
    ) {
        if (density.isEmpty()) return

        val width = size.width
        val maxHeight = bottomY - topY
        if (maxHeight <= 0f || width <= 0f) return

        val stepX = width / (BUCKET_COUNT - 1).toFloat()

        // 1. 再利用 Path インスタンスでゼロアロケーション描画
        val fullPath = cachedPath.apply {
            reset()
            moveTo(0f, bottomY)
            for (i in 0 until BUCKET_COUNT) {
                val x = i * stepX
                val h = density[i] * maxHeight
                val y = bottomY - h
                lineTo(x, y)
            }
            lineTo(width, bottomY)
            close()
        }

        // 全体波形 (控えめな白半透明)
        drawPath(
            path = fullPath,
            color = Color.White.copy(alpha = 0.22f)
        )

        // 2. 再生済み領域のクリップ＆鮮やかなアクセント色描画
        if (playedRatio > 0.001f) {
            val playedWidth = (width * playedRatio).coerceIn(0f, width)
            drawContext.canvas.save()
            drawContext.canvas.clipRect(
                left = 0f,
                top = topY,
                right = playedWidth,
                bottom = bottomY
            )
            drawPath(
                path = fullPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00C8FF).copy(alpha = 0.70f),
                        Color(0xFF0095FF).copy(alpha = 0.45f)
                    ),
                    startY = topY,
                    endY = bottomY
                )
            )
            drawContext.canvas.restore()
        }
    }
}
