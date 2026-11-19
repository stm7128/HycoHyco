package com.github.stm7128.hycohyco.player.danmaku

import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentPosition
import kotlin.math.max

/**
 * 弾幕の衝突回避レーン割り当ておよび CA（コメントアート）仮想レイアウトエンジン
 */
object DanmakuLayoutEngine {

    /**
     * 生のコメントリストから、衝突回避レーンを適用した描画アイテム一覧を生成する
     *
     * @param comments vposMs 昇順ソート済みコメントリスト
     * @param isPortraitMode true の場合は 9:16 縦型ショート向け安全領域でレイアウト
     * @return 事前レイアウト計算済みの [DanmakuRenderItem] リスト
     */
    fun buildRenderItems(
        comments: List<CommentItem>,
        isPortraitMode: Boolean = false
    ): List<DanmakuRenderItem> {
        val renderItems = ArrayList<DanmakuRenderItem>(comments.size)
        val virtualWidth = if (isPortraitMode) DanmakuConfig.PORTRAIT_VIRTUAL_WIDTH else DanmakuConfig.VIRTUAL_WIDTH
        val virtualHeight = if (isPortraitMode) DanmakuConfig.PORTRAIT_VIRTUAL_HEIGHT else DanmakuConfig.VIRTUAL_HEIGHT
        val usableTop = if (isPortraitMode) DanmakuConfig.PORTRAIT_SAFE_TOP_MARGIN else 0f
        val usableBottom = if (isPortraitMode) virtualHeight * DanmakuConfig.PORTRAIT_SAFE_BOTTOM_RATIO else virtualHeight
        val usableHeight = (usableBottom - usableTop).coerceAtLeast(1f)

        // レーンごとの直前コメント終了時刻管理
        val laneCount = if (isPortraitMode) DanmakuConfig.PORTRAIT_LANE_COUNT else DanmakuConfig.DEFAULT_LANE_COUNT
        val nakaLaneTailEnterMs = LongArray(laneCount) { 0L } // 直前コメントの右端が画面に入った時刻
        val nakaLaneLeaveMs = LongArray(laneCount) { 0L }     // 直前コメントが画面左端から消えた時刻

        val ueLaneEndMs = LongArray(laneCount) { 0L }
        val shitaLaneEndMs = LongArray(laneCount) { 0L }

        for (comment in comments) {
            val lines = comment.body.split("\n")
            val rawFontSize = DanmakuConfig.getFontSize(comment.size, isPortraitMode)
            val rawLineHeight = DanmakuConfig.getLineHeight(comment.size, isPortraitMode)

            // 1. 複数行 CA の高さ縮小計算 (安全領域高さを超える場合は等倍リサイズ)
            val lineCount = lines.size
            val rawTotalHeight = lineCount * rawLineHeight
            val heightScale = if (rawTotalHeight > usableHeight * 0.95f) {
                (usableHeight * 0.95f) / rawTotalHeight
            } else {
                1.0f
            }

            // 2. 固定コメント (UE / SHITA) の横幅オートフィット (画面幅を超える長文の左右はみ出し防止)
            val initialFontSize = rawFontSize * heightScale
            val initialTextWidth = calculateMaxLineWidth(lines, initialFontSize)
            val maxSafeWidth = virtualWidth * 0.94f
            val widthScale = if (comment.position != CommentPosition.NAKA && initialTextWidth > maxSafeWidth) {
                maxSafeWidth / initialTextWidth
            } else {
                1.0f
            }

            val finalScale = heightScale * widthScale
            val fontSize = rawFontSize * finalScale
            val lineHeight = rawLineHeight * finalScale
            val totalHeight = rawTotalHeight * finalScale
            val textWidth = calculateMaxLineWidth(lines, fontSize)

            val startMs = comment.vposMs
            val duration = if (comment.position == CommentPosition.NAKA) {
                DanmakuConfig.FLOW_DURATION_MS
            } else {
                DanmakuConfig.FIXED_DURATION_MS
            }
            val endMs = startMs + duration

            // 3. レーン (Y 座標) の割り当て
            val virtualY = when (comment.position) {
                CommentPosition.NAKA -> {
                    assignNakaLane(
                        startMs = startMs,
                        endMs = endMs,
                        totalHeight = totalHeight,
                        textWidth = textWidth,
                        lineHeight = lineHeight,
                        laneCount = laneCount,
                        tailEnterMsArray = nakaLaneTailEnterMs,
                        leaveMsArray = nakaLaneLeaveMs,
                        virtualWidth = virtualWidth,
                        usableTop = usableTop,
                        usableBottom = usableBottom
                    )
                }
                CommentPosition.UE -> {
                    assignFixedLane(
                        startMs = startMs,
                        endMs = endMs,
                        totalHeight = totalHeight,
                        lineHeight = lineHeight,
                        laneCount = laneCount,
                        laneEndMsArray = ueLaneEndMs,
                        isBottomUp = false,
                        usableTop = usableTop,
                        usableBottom = usableBottom
                    )
                }
                CommentPosition.SHITA -> {
                    assignFixedLane(
                        startMs = startMs,
                        endMs = endMs,
                        totalHeight = totalHeight,
                        lineHeight = lineHeight,
                        laneCount = laneCount,
                        laneEndMsArray = shitaLaneEndMs,
                        isBottomUp = true,
                        usableTop = usableTop,
                        usableBottom = usableBottom
                    )
                }
            }

            renderItems.add(
                DanmakuRenderItem(
                    comment = comment,
                    lines = lines,
                    startMs = startMs,
                    endMs = endMs,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    textWidth = textWidth,
                    totalHeight = totalHeight,
                    virtualY = virtualY,
                    virtualCanvasWidth = virtualWidth
                )
            )
        }

        return renderItems
    }

    /**
     * 流れるコメント (NAKA) のレーン割り当て（衝突・追い越し回避・複数行CA占有対応）
     */
    private fun assignNakaLane(
        startMs: Long,
        endMs: Long,
        totalHeight: Float,
        textWidth: Float,
        lineHeight: Float,
        laneCount: Int,
        tailEnterMsArray: LongArray,
        leaveMsArray: LongArray,
        virtualWidth: Float,
        usableTop: Float,
        usableBottom: Float
    ): Float {
        // 自コメントの移動速度 (px/ms)
        val speed = (virtualWidth + textWidth) / DanmakuConfig.FLOW_DURATION_MS
        // 自コメントの右端が画面に入りきる時刻
        val selfTailEnterMs = startMs + (textWidth / speed).toLong()

        // 複数行コメント (CA) が専有する連続レーン数
        val spanLanes = kotlin.math.ceil(totalHeight / lineHeight.coerceAtLeast(1f)).toInt().coerceIn(1, laneCount)
        val maxStartLane = (laneCount - spanLanes).coerceAtLeast(0)

        var bestLane = 0
        var foundAvailable = false

        for (lane in 0..maxStartLane) {
            var canUse = true
            for (offset in 0 until spanLanes) {
                val l = lane + offset
                if (tailEnterMsArray[l] > startMs || leaveMsArray[l] > endMs) {
                    canUse = false
                    break
                }
            }
            if (canUse) {
                bestLane = lane
                foundAvailable = true
                break
            }
        }

        if (!foundAvailable) {
            // 空きがない場合は最も早く右端が入る連続レーンを選択
            var minTailTime = Long.MAX_VALUE
            for (lane in 0..maxStartLane) {
                var maxTailInSpan = 0L
                for (offset in 0 until spanLanes) {
                    maxTailInSpan = max(maxTailInSpan, tailEnterMsArray[lane + offset])
                }
                if (maxTailInSpan < minTailTime) {
                    minTailTime = maxTailInSpan
                    bestLane = lane
                }
            }
        }

        for (offset in 0 until spanLanes) {
            val l = bestLane + offset
            tailEnterMsArray[l] = selfTailEnterMs
            leaveMsArray[l] = endMs
        }

        val calculatedY = usableTop + (bestLane * lineHeight)
        val maxAllowedY = (usableBottom - totalHeight).coerceAtLeast(usableTop)
        return calculatedY.coerceIn(usableTop, maxAllowedY)
    }

    /**
     * 上固定 (UE) / 下固定 (SHITA) の段積みレーン割り当て（複数行CA占有対応）
     */
    private fun assignFixedLane(
        startMs: Long,
        endMs: Long,
        totalHeight: Float,
        lineHeight: Float,
        laneCount: Int,
        laneEndMsArray: LongArray,
        isBottomUp: Boolean,
        usableTop: Float,
        usableBottom: Float
    ): Float {
        val spanLanes = kotlin.math.ceil(totalHeight / lineHeight.coerceAtLeast(1f)).toInt().coerceIn(1, laneCount)
        val maxStartLane = (laneCount - spanLanes).coerceAtLeast(0)

        var assignedLane = 0
        var foundAvailable = false

        for (lane in 0..maxStartLane) {
            var canUse = true
            for (offset in 0 until spanLanes) {
                if (laneEndMsArray[lane + offset] > startMs) {
                    canUse = false
                    break
                }
            }
            if (canUse) {
                assignedLane = lane
                foundAvailable = true
                break
            }
        }

        if (!foundAvailable) {
            var minEndTime = Long.MAX_VALUE
            for (lane in 0..maxStartLane) {
                var maxEndInSpan = 0L
                for (offset in 0 until spanLanes) {
                    maxEndInSpan = max(maxEndInSpan, laneEndMsArray[lane + offset])
                }
                if (maxEndInSpan < minEndTime) {
                    minEndTime = maxEndInSpan
                    assignedLane = lane
                }
            }
        }

        for (offset in 0 until spanLanes) {
            laneEndMsArray[assignedLane + offset] = endMs
        }

        val calculatedY = if (isBottomUp) {
            // 下固定: 安全領域下端から totalHeight 分上に配置
            usableBottom - totalHeight - (assignedLane * lineHeight)
        } else {
            // 上固定: 安全領域上端から下に積む
            usableTop + (assignedLane * lineHeight)
        }

        val maxAllowedY = (usableBottom - totalHeight).coerceAtLeast(usableTop)
        return calculatedY.coerceIn(usableTop, maxAllowedY)
    }

    /**
     * テキスト行の最大幅を算出する（全角: 1.0em, 半角: 0.55em）
     */
    private fun calculateMaxLineWidth(lines: List<String>, fontSize: Float): Float {
        var maxWidth = 0f
        for (line in lines) {
            var width = 0f
            for (ch in line) {
                width += if (ch.code <= 0x7F) {
                    fontSize * 0.55f
                } else {
                    fontSize * 1.0f
                }
            }
            maxWidth = max(maxWidth, width)
        }
        return maxWidth
    }
}
