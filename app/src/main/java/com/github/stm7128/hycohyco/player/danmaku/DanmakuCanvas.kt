package com.github.stm7128.hycohyco.player.danmaku

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import com.github.stm7128.hycohyco.domain.model.CommentFont
import com.github.stm7128.hycohyco.domain.model.CommentPosition
import kotlin.math.roundToInt

/**
 * 120fps 対応のゼロアロケーション弾幕描画 Canvas
 *
 * 描画ループ (onDraw) 内でのオブジェクト生成を一切行わず、
 * 事前確保した [Paint] インスタンスを使い回して、黒縁取り＋本体色の 2 パス描画を行う。
 *
 * @param renderItems 事前レイアウト計算済みのコメントアイテム一覧
 * @param currentPositionMs 現在の動画再生位置 (ミリ秒)
 * @param settings 弾幕描画設定 (表示有無、不透明度、スケール)
 * @param modifier 修飾子
 */
@Composable
fun DanmakuCanvas(
    renderItems: List<DanmakuRenderItem>,
    currentPositionMs: Long,
    settings: DanmakuDisplaySettings,
    modifier: Modifier = Modifier,
    isPortraitMode: Boolean = false
) {
    if (!settings.isEnabled || renderItems.isEmpty()) {
        return
    }

    // 描画ループ外で Typeface および Paint オブジェクトを事前生成し、毎フレームの Typeface.create() 生成負荷を完全根絶
    val typefaceMincho = remember { Typeface.create(Typeface.SERIF, Typeface.BOLD) }
    val typefaceGothic = remember { Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }

    val fillPaint = remember(typefaceGothic) {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            typeface = typefaceGothic
        }
    }

    val strokePaint = remember(typefaceGothic) {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = android.graphics.Color.BLACK
            typeface = typefaceGothic
        }
    }

    // 自分のコメント強調用の黄色ボーダー枠 Paint (ゼロアロケーションキャッシュ)
    val myBorderPaint = remember {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = 0xFFFFEE00.toInt() // 公式準拠のイエロー (#FFEE00)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        // 仮想キャンバス (16:9 または縦型 9:16) と実キャンバスのアスペクト比を比較し、余白オフセットを算出
        val virtualWidth = if (isPortraitMode) DanmakuConfig.PORTRAIT_VIRTUAL_WIDTH else DanmakuConfig.VIRTUAL_WIDTH
        val virtualHeight = if (isPortraitMode) DanmakuConfig.PORTRAIT_VIRTUAL_HEIGHT else DanmakuConfig.VIRTUAL_HEIGHT
        val virtualAspect = virtualWidth / virtualHeight
        val canvasAspect = canvasWidth / canvasHeight

        val scale: Float
        val offsetX: Float
        val offsetY: Float

        if (canvasAspect >= virtualAspect) {
            scale = canvasHeight / virtualHeight
            val scaledWidth = virtualWidth * scale
            offsetX = (canvasWidth - scaledWidth) / 2f
            offsetY = 0f
        } else {
            scale = canvasWidth / virtualWidth
            val scaledHeight = virtualHeight * scale
            offsetX = 0f
            offsetY = (canvasHeight - scaledHeight) / 2f
        }

        val globalAlpha = settings.alpha.coerceIn(0f, 1f)

        drawContext.canvas.nativeCanvas.apply {
            save()
            clipRect(0f, 0f, canvasWidth, canvasHeight)

            val speedScale = if (settings.speedScale > 0.1f) settings.speedScale else 1.0f

            // 二分探索で現在の再生位置に関連する範囲のみを高速走査 (I-04)
            val maxFlowDuration = (DanmakuConfig.FLOW_DURATION_MS / speedScale.coerceAtLeast(0.2f)).toLong() + 500L
            val minStartMs = currentPositionMs - maxFlowDuration
            var low = 0
            var high = renderItems.size - 1
            var startIndex = 0
            while (low <= high) {
                val mid = (low + high) ushr 1
                if (renderItems[mid].startMs >= minStartMs) {
                    startIndex = mid
                    high = mid - 1
                } else {
                    low = mid + 1
                }
            }

            for (i in startIndex until renderItems.size) {
                val item = renderItems[i]
                // 未来のコメントに達したら走査を早期終了
                if (item.startMs > currentPositionMs) {
                    break
                }

                // 流れるコメント (NAKA) の場合は速度倍率に応じて表示期間を伸縮
                val effectiveDuration = if (item.comment.position == CommentPosition.NAKA) {
                    ((item.endMs - item.startMs) / speedScale).toLong()
                } else {
                    item.endMs - item.startMs
                }
                val effectiveEndMs = item.startMs + effectiveDuration

                // 現在時刻が表示期間内でないアイテムはスキップ
                if (currentPositionMs > effectiveEndMs) {
                    continue
                }

                // 表示領域制限 (areaMode) によるフィルタリング
                when (settings.areaMode) {
                    DanmakuAreaMode.ALL -> Unit
                    DanmakuAreaMode.TOP_HALF -> {
                        if (item.virtualY > virtualHeight * 0.5f) continue
                    }
                    DanmakuAreaMode.AVOID_SUBTITLE -> {
                        if ((item.virtualY + item.totalHeight) > virtualHeight * 0.82f) continue
                    }
                }

                // フォントスケールを反映した実テキスト幅と X 座標 (M-01: 左端カリング・黄色枠ズレ・中央揃え補正)
                val realTextWidth = item.textWidth * scale * settings.scale
                val realX = if (item.comment.position != CommentPosition.NAKA) {
                    offsetX + ((virtualWidth * scale) - realTextWidth) / 2f
                } else {
                    val virtualX = item.calculateVirtualX(currentPositionMs, speedScale)
                    offsetX + (virtualX * scale)
                }
                val baseRealY = offsetY + (item.virtualY * scale)

                // 画面外（左右にはみ出しすぎている場合）はクリップして描画負荷を低減
                if (realX + realTextWidth < -50f || realX > canvasWidth + 50f) {
                    continue
                }

                val realFontSize = item.fontSize * scale * settings.scale
                val realLineHeight = item.lineHeight * scale * settings.scale
                val strokeWidth = realFontSize * DanmakuConfig.STROKE_WIDTH_RATIO

                // フォント設定 (ゴシック / 明朝) のキャッシュ適用 (同一インスタンス時は再代入をスキップ)
                val targetTypeface = if (item.comment.font == CommentFont.MINCHO) typefaceMincho else typefaceGothic
                if (fillPaint.typeface !== targetTypeface) {
                    fillPaint.typeface = targetTypeface
                    strokePaint.typeface = targetTypeface
                }

                // 不透明度計算 (半透明コマンド _live 対応)
                val commentAlpha = if (item.comment.isTranslucent) 0.5f else 1.0f
                val finalAlpha = (globalAlpha * commentAlpha * 255f).roundToInt().coerceIn(0, 255)

                // コメント色の相対輝度判定: 黒・暗色コメントは白縁取り、通常・明色コメントは黒縁取り
                // ITU-R BT.601 相対輝度: (299*R + 587*G + 114*B) / 1000
                val commentColor = item.comment.colorInt
                val r = (commentColor shr 16) and 0xFF
                val g = (commentColor shr 8) and 0xFF
                val b = commentColor and 0xFF
                val isDarkColor = ((r * 299 + g * 587 + b * 114) < 60000)

                val targetStrokeColor = if (isDarkColor) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                if (strokePaint.color != targetStrokeColor) {
                    strokePaint.color = targetStrokeColor
                }

                // 縁取り (アウトライン)
                strokePaint.textSize = realFontSize
                strokePaint.strokeWidth = strokeWidth
                strokePaint.alpha = finalAlpha

                // 本体塗りつぶし色
                fillPaint.textSize = realFontSize
                fillPaint.color = commentColor
                fillPaint.alpha = finalAlpha

                // 複数行コメント (CA) の行ごとの描画
                var currentLineY = baseRealY + (realFontSize * 0.9f)
                for (line in item.lines) {
                    // パス 1: 黒縁取り
                    drawText(line, realX, currentLineY, strokePaint)
                    // パス 2: 本体色塗りつぶし
                    drawText(line, realX, currentLineY, fillPaint)

                    currentLineY += realLineHeight
                }

                // パス 3: 自分が投稿したコメントの場合は黄色い枠線を描画（公式完全再現）
                if (item.comment.isMyComment) {
                    val padX = 4f * scale * settings.scale
                    val padY = 2f * scale * settings.scale
                    val boxLeft = realX - padX
                    val boxTop = baseRealY - padY
                    val boxRight = realX + realTextWidth + padX
                    val boxBottom = baseRealY + (item.totalHeight * scale * settings.scale) + padY
                    myBorderPaint.strokeWidth = 2.5f * scale * settings.scale
                    myBorderPaint.alpha = finalAlpha
                    drawRect(boxLeft, boxTop, boxRight, boxBottom, myBorderPaint)
                }
            }

            restore()
        }
    }
}
