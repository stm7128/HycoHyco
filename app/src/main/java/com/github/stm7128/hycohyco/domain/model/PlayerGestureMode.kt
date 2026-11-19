package com.github.stm7128.hycohyco.domain.model

/**
 * プレイヤーのスライドジェスチャー（音量・輝度）の有効モード
 */
enum class PlayerGestureMode(val label: String, val description: String) {
    /** 常に有効（通常再生時・全画面時ともに有効） */
    ALWAYS("常に有効", "通常画面・全画面のどちらでもスライド調整できます"),

    /** 全画面（横画面）表示時のみ有効 */
    FULLSCREEN_ONLY("全画面のみ", "シークバーの誤爆を防ぎ、全画面表示時のみスライド調整できます"),

    /** 無効（音量・輝度スライドを行わない） */
    DISABLED("無効", "画面スライドによる音量・輝度の変更をオフにします");

    companion object {
        fun fromName(name: String?): PlayerGestureMode {
            return entries.find { it.name == name } ?: FULLSCREEN_ONLY
        }
    }
}
