package com.github.stm7128.hycohyco.player.gesture

/**
 * プレイヤージェスチャー操作中の HUD オーバーレイ表示状態
 */
sealed interface PlayerGestureOverlay {
    /** 表示なし */
    data object None : PlayerGestureOverlay

    /** 輝度調整中 (0 〜 100%) */
    data class Brightness(val percent: Int) : PlayerGestureOverlay

    /** 音量調整中 (0 〜 100%) */
    data class Volume(val percent: Int) : PlayerGestureOverlay

    /** スキップ (±10秒) */
    data class Seek(val deltaSeconds: Int) : PlayerGestureOverlay

    /** 長押し 2.0x 倍速再生中 */
    data object Speed2x : PlayerGestureOverlay
}
