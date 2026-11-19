package com.github.stm7128.hycohyco.domain.model

/**
 * アプリのデザインシステム（テーマ種別）
 */
enum class AppThemeMode {
    /** Xiaomi HyperOS デザインシステム (miuix) */
    HYPER_OS,

    /** Google Material You デザインシステム (Material 3 Dynamic Color) */
    MATERIAL_YOU
}

/**
 * ダークテーマ表示設定
 */
enum class DarkThemeConfig {
    /** システム設定に従う */
    FOLLOW_SYSTEM,

    /** 常にライトテーマ */
    LIGHT,

    /** 常にダークテーマ */
    DARK
}

/**
 * アプリのテーマ設定ドメインモデル
 *
 * @property themeMode 選択中のデザインシステム
 * @property darkThemeConfig ダークテーマ設定
 * @property isDynamicColor 壁紙連動ダイナミックカラー (Monet) を有効にするか
 * @property isAmoledBlack ダークモード時に完全な漆黒 (#000000) を適用するか
 * @property isFirstLaunchCompleted 初回セットアップ（テーマ選択）が完了しているか
 */
data class ThemeSettings(
    val themeMode: AppThemeMode = AppThemeMode.HYPER_OS,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val isDynamicColor: Boolean = true,
    val isAmoledBlack: Boolean = false,
    val isFirstLaunchCompleted: Boolean = false,
    val defaultHomeMode: HomeMode = HomeMode.RANKING
) {
    /**
     * 実際に描画へ適用される Dynamic Color の有効状態。
     * HyperOS デザイン選択時はテーマの一貫性を保つため強制的にオフとなる。
     */
    val effectiveDynamicColor: Boolean
        get() = themeMode == AppThemeMode.MATERIAL_YOU && isDynamicColor
}
