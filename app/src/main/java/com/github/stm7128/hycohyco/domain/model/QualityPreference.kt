package com.github.stm7128.hycohyco.domain.model

/**
 * デフォルト動画画質プリファレンス
 *
 * @property targetHeight 目標縦解像度 (null の場合は動画が提供する最高画質)
 * @property label 画面表示用ラベル (例: "1080p")
 * @property description 画面表示用補足説明 (例: "フルHD高画質")
 */
enum class QualityPreference(
    val targetHeight: Int?,
    val label: String,
    val description: String
) {
    AUTO(null, "自動 (最高画質)", "利用可能な最高解像度を自動選択します"),
    P1080(1080, "1080p", "フルHD高画質"),
    P720(720, "720p", "HD標準画質"),
    P480(480, "480p", "中画質 (通信量控えめ)"),
    P360(360, "360p", "低画質 (モバイル通信向け)"),
    P144(144, "144p", "データ節約 (最小通信量・音声重視)");

    companion object {
        /**
         * 文字列名から安全に Enum を復元する (不正値は AUTO にフォールバック)
         */
        fun fromName(name: String?): QualityPreference =
            entries.find { it.name.equals(name, ignoreCase = true) } ?: AUTO
    }
}
