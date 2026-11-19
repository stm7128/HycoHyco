package com.github.stm7128.hycohyco.player.danmaku

import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentPosition

/**
 * 事前レイアウト計算済みの描画用コメントアイテム
 *
 * @property comment 元のドメインコメント
 * @property lines 改行分割されたテキスト行
 * @property startMs 表示開始時刻 (ミリ秒)
 * @property endMs 表示終了時刻 (ミリ秒)
 * @property fontSize 仮想座標系でのフォントサイズ (px)
 * @property lineHeight 仮想座標系での行高 (px)
 * @property textWidth 仮想座標系での最大行幅 (px)
 * @property totalHeight 仮想座標系での全行合計高さ (px)
 * @property virtualY 仮想座標系での描画 Y 座標
 */
data class DanmakuRenderItem(
    val comment: CommentItem,
    val lines: List<String>,
    val startMs: Long,
    val endMs: Long,
    val fontSize: Float,
    val lineHeight: Float,
    val textWidth: Float,
    val totalHeight: Float,
    val virtualY: Float,
    val virtualCanvasWidth: Float = DanmakuConfig.VIRTUAL_WIDTH
) {
    /**
     * 指定された再生時刻 [currentMs] と速度倍率 [speedScale] における仮想 X 座標を算出する
     */
    fun calculateVirtualX(currentMs: Long, speedScale: Float = 1.0f): Float {
        return when (comment.position) {
            CommentPosition.NAKA -> {
                val elapsed = currentMs - startMs
                val baseDuration = (endMs - startMs).coerceAtLeast(1L).toFloat()
                val safeSpeed = if (speedScale > 0.1f) speedScale else 1.0f
                val duration = baseDuration / safeSpeed
                val progress = (elapsed / duration).coerceIn(0f, 1f)
                val totalDistance = virtualCanvasWidth + textWidth
                virtualCanvasWidth - (totalDistance * progress)
            }
            CommentPosition.UE, CommentPosition.SHITA -> {
                // 画面中央揃え
                (virtualCanvasWidth - textWidth) / 2f
            }
        }
    }
}
