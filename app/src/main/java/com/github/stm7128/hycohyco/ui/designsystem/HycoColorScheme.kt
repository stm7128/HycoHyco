package com.github.stm7128.hycohyco.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * HycoHyco 統合セマンティックカラースキーム
 *
 * HyperOS (Miuix) と Material You (Material 3) の双方で共通のセマンティック（意味）を持つカラー定義。
 * これにより、画面側はテーマの種類（Miuix / M3）を意識することなく、一貫したコントラストと視認性を保つことができる。
 */
data class HycoColorScheme(
    /** プライマリ・アクセントカラー */
    val primary: Color,
    /** 画面全体の背景色 */
    val background: Color,
    /** 背景上の主要テキスト色（ダーク時: 白/高輝度、ライト時: 黒） */
    val onBackground: Color,
    /** 背景上の補助テキスト色（サブタイトル、説明文等） */
    val onBackgroundVariant: Color,
    /** カード・ダイアログ・ボトムシートの表面色 */
    val surface: Color,
    /** 表面上の主要テキスト色 */
    val onSurface: Color,
    /** 表面上の補助テキスト色（件数、再生時間等） */
    val onSurfaceVariant: Color,
    /** 入力欄・ネストされたコンテナの背景色 */
    val surfaceContainer: Color,
    /** 区切り線・境界線カラー */
    val outline: Color,
    /** エラー表示色 */
    val error: Color,
)

/**
 * 現在の HycoColorScheme を提供する CompositionLocal
 */
val LocalHycoColors = staticCompositionLocalOf {
    HycoColorScheme(
        primary = Color(0xFF007AFF),
        background = Color.White,
        onBackground = Color.Black,
        onBackgroundVariant = Color(0xFF666666),
        surface = Color.White,
        onSurface = Color.Black,
        onSurfaceVariant = Color(0xFF666666),
        surfaceContainer = Color(0xFFF2F2F7),
        outline = Color(0xFFE5E5EA),
        error = Color(0xFFFF3B30),
    )
}

/**
 * HycoHyco 共通テーマアクセサ
 */
object HycoTheme {
    /** 現在のテーマに応じたセマンティックカラー */
    val colors: HycoColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalHycoColors.current
}
