package com.github.stm7128.hycohyco.domain.model

/**
 * 弾幕の表示領域モード
 */
enum class DanmakuAreaMode(val label: String) {
    /** 画面全体に表示 (100%) */
    ALL("全画面"),

    /** 画面上半分のみに表示 (50%) */
    TOP_HALF("上半分"),

    /** 下部字幕部分を空けて上80%に表示 */
    AVOID_SUBTITLE("下部字幕空け");

    companion object {
        fun fromName(name: String?): DanmakuAreaMode = entries.find { it.name == name } ?: ALL
    }
}

/**
 * 弾幕の表示・描画設定ドメインモデル
 *
 * @property isEnabled コメント表示が有効かどうか
 * @property alpha コメントの全体不透明度 (0.0f 〜 1.0f)
 * @property scale フォント倍率 (0.5f 〜 2.0f)
 * @property speedScale 流れる速度倍率 (0.5f 〜 2.0f)
 * @property areaMode 弾幕の表示領域モード
 */
data class DanmakuDisplaySettings(
    val isEnabled: Boolean = true,
    val alpha: Float = 0.90f,
    val scale: Float = 1.0f,
    val speedScale: Float = 1.0f,
    val areaMode: DanmakuAreaMode = DanmakuAreaMode.ALL
)
