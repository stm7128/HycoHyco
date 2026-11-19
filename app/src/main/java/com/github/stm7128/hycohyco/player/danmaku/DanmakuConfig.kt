package com.github.stm7128.hycohyco.player.danmaku

import com.github.stm7128.hycohyco.domain.model.CommentSize

/**
 * 弾幕エンジンの仮想座標系およびタイミング定数
 */
object DanmakuConfig {
    /** 16:9 仮想キャンバスの幅 */
    const val VIRTUAL_WIDTH = 640.0f

    /** 16:9 仮想キャンバスの高さ */
    const val VIRTUAL_HEIGHT = 360.0f

    /** 9:16 縦型ショート仮想キャンバスの幅 */
    const val PORTRAIT_VIRTUAL_WIDTH = 360.0f

    /** 9:16 縦型ショート仮想キャンバスの高さ */
    const val PORTRAIT_VIRTUAL_HEIGHT = 640.0f

    /** 縦型ショート時の上部安全マージン (閉じるボタン/ステータスバー回避) */
    const val PORTRAIT_SAFE_TOP_MARGIN = 52.0f

    /** 縦型ショート時の下部境界比率 (下部タイトル・本編バナー領域を回避) */
    const val PORTRAIT_SAFE_BOTTOM_RATIO = 0.72f

    /** 流れるコメント (NAKA) の画面通過時間 (ミリ秒) */
    const val FLOW_DURATION_MS = 4000L

    /** 固定コメント (UE / SHITA) の表示時間 (ミリ秒) */
    const val FIXED_DURATION_MS = 3000L

    /** デフォルトのレーン数 (標準サイズの行高 32px に基づく 360 / 32 ≒ 11レーン) */
    const val DEFAULT_LANE_COUNT = 11

    /** 縦型ショート時のレーン数 */
    const val PORTRAIT_LANE_COUNT = 14

    /** 基準文字サイズ (仮想キャンバス px) */
    const val FONT_SIZE_BIG = 39.0f
    const val FONT_SIZE_MEDIUM = 27.0f
    const val FONT_SIZE_SMALL = 18.0f

    /** 縦型ショート時の基準文字サイズ (360x640 基準) */
    const val PORTRAIT_FONT_SIZE_BIG = 28.0f
    const val PORTRAIT_FONT_SIZE_MEDIUM = 21.0f
    const val PORTRAIT_FONT_SIZE_SMALL = 15.0f

    /** 基準行高 (仮想キャンバス px) */
    const val LINE_HEIGHT_BIG = 45.0f
    const val LINE_HEIGHT_MEDIUM = 32.0f
    const val LINE_HEIGHT_SMALL = 22.0f

    /** 縦型ショート時の基準行高 (360x640 基準) */
    const val PORTRAIT_LINE_HEIGHT_BIG = 34.0f
    const val PORTRAIT_LINE_HEIGHT_MEDIUM = 26.0f
    const val PORTRAIT_LINE_HEIGHT_SMALL = 19.0f

    /** コメントのアウトライン (黒縁取り) の太さ比率 */
    const val STROKE_WIDTH_RATIO = 0.12f

    /**
     * コメントサイズに応じた基準フォントサイズを取得する
     */
    fun getFontSize(size: CommentSize, isPortraitMode: Boolean = false): Float = when (size) {
        CommentSize.BIG -> if (isPortraitMode) PORTRAIT_FONT_SIZE_BIG else FONT_SIZE_BIG
        CommentSize.MEDIUM -> if (isPortraitMode) PORTRAIT_FONT_SIZE_MEDIUM else FONT_SIZE_MEDIUM
        CommentSize.SMALL -> if (isPortraitMode) PORTRAIT_FONT_SIZE_SMALL else FONT_SIZE_SMALL
    }

    /**
     * コメントサイズに応じた基準行高を取得する
     */
    fun getLineHeight(size: CommentSize, isPortraitMode: Boolean = false): Float = when (size) {
        CommentSize.BIG -> if (isPortraitMode) PORTRAIT_LINE_HEIGHT_BIG else LINE_HEIGHT_BIG
        CommentSize.MEDIUM -> if (isPortraitMode) PORTRAIT_LINE_HEIGHT_MEDIUM else LINE_HEIGHT_MEDIUM
        CommentSize.SMALL -> if (isPortraitMode) PORTRAIT_LINE_HEIGHT_SMALL else LINE_HEIGHT_SMALL
    }
}

/**
 * 弾幕の表示・描画設定 (ドメインモデルへのエイリアス)
 */
typealias DanmakuDisplaySettings = com.github.stm7128.hycohyco.domain.model.DanmakuDisplaySettings
