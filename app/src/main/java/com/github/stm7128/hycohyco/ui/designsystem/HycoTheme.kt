package com.github.stm7128.hycohyco.ui.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.LocalContentColor as MiuixLocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme

/**
 * 現在アクティブなテーマモードを提供する CompositionLocal
 */
val LocalAppThemeMode = staticCompositionLocalOf { AppThemeMode.HYPER_OS }

/**
 * HycoHyco 統合テーマラッパー
 *
 * 設定された [themeSettings] に応じて、HyperOS (Miuix) と Material You (Material 3) の
 * テーマコンテキストを下位ツリーに途切れず提供する。
 * ツリーを破棄せずに切り替えるため、画面状態やスクロール位置が完全に維持される。
 *
 * @param themeSettings 現在のテーマ設定ドメインモデル
 * @param content テーマが適用される Composable コンテンツ
 */
@Composable
fun HycoTheme(
    themeSettings: ThemeSettings,
    content: @Composable () -> Unit
) {
    val isDark = when (themeSettings.darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }

    // ステータスバー・ナビゲーションバーのアイコン明暗設定
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val isHyperOs = themeSettings.themeMode == AppThemeMode.HYPER_OS
    // HyperOS デザイン時はテーマの一貫性を保つため Dynamic Color (Monet) を強制オフにする
    val effectiveDynamicColor = themeSettings.effectiveDynamicColor

    // --- HyperOS (Miuix) 側のカラースキーム決定 ---
    // HyperOS モード時は effectiveDynamicColor が常に false のため、純粋な標準パレット (Dark / Light) が適用される
    val miuixColorSchemeMode = when {
        isDark && themeSettings.isAmoledBlack -> ColorSchemeMode.Dark
        effectiveDynamicColor -> {
            if (isDark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
        }
        else -> {
            if (isDark) ColorSchemeMode.Dark else ColorSchemeMode.Light
        }
    }

    val miuixDarkColors = remember(themeSettings.isAmoledBlack) {
        if (themeSettings.isAmoledBlack) {
            // 有機EL ピュアブラック (漆黒 #000000)
            miuixDarkColorScheme().copy(
                background = Color.Black,
                surface = Color(0xFF101010),
                surfaceContainer = Color(0xFF141414)
            )
        } else {
            // 通常ダークモード (標準的なダークグレー)
            miuixDarkColorScheme().copy(
                background = Color(0xFF181818),
                surface = Color(0xFF222222),
                surfaceContainer = Color(0xFF2A2A2A)
            )
        }
    }

    val miuixController = remember(miuixColorSchemeMode, isDark, miuixDarkColors) {
        ThemeController(
            colorSchemeMode = miuixColorSchemeMode,
            darkColors = miuixDarkColors,
            isDark = isDark
        )
    }

    // --- Material You (Material 3) 側のカラースキーム決定 ---
    val context = LocalContext.current
    val baseM3Scheme = when {
        effectiveDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> darkColorScheme()
        else -> lightColorScheme()
    }

    val m3ColorScheme = remember(baseM3Scheme, isDark, themeSettings.isAmoledBlack) {
        if (isDark && themeSettings.isAmoledBlack) {
            baseM3Scheme.copy(
                background = Color.Black,
                surface = Color(0xFF121212),
                surfaceContainer = Color(0xFF161616)
            )
        } else {
            baseM3Scheme
        }
    }

    // 統一セマンティックカラーの導出
    val hycoColorScheme = remember(isHyperOs, isDark, themeSettings.isAmoledBlack, m3ColorScheme, miuixDarkColors) {
        if (isHyperOs) {
            val bg = if (isDark) {
                if (themeSettings.isAmoledBlack) Color.Black else Color(0xFF181818)
            } else Color(0xFFF7F7F7)
            val onBg = if (isDark) Color(0xFFEBEBEB) else Color(0xFF1A1A1A)
            val surf = if (isDark) {
                if (themeSettings.isAmoledBlack) Color(0xFF101010) else Color(0xFF222222)
            } else Color.White
            val onSurf = if (isDark) Color(0xFFF2F2F2) else Color(0xFF202020)
            val onSurfVar = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)
            val surfCont = if (isDark) {
                if (themeSettings.isAmoledBlack) Color(0xFF141414) else Color(0xFF2A2A2A)
            } else Color(0xFFF0F0F0)

            HycoColorScheme(
                primary = Color(0xFF007AFF),
                background = bg,
                onBackground = onBg,
                onBackgroundVariant = onSurfVar,
                surface = surf,
                onSurface = onSurf,
                onSurfaceVariant = onSurfVar,
                surfaceContainer = surfCont,
                outline = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0),
                error = Color(0xFFFF3B30),
            )
        } else {
            HycoColorScheme(
                primary = m3ColorScheme.primary,
                background = m3ColorScheme.background,
                onBackground = m3ColorScheme.onBackground,
                onBackgroundVariant = m3ColorScheme.onSurfaceVariant,
                surface = m3ColorScheme.surface,
                onSurface = m3ColorScheme.onSurface,
                onSurfaceVariant = m3ColorScheme.onSurfaceVariant,
                surfaceContainer = m3ColorScheme.surfaceContainer,
                outline = m3ColorScheme.outline,
                error = m3ColorScheme.error,
            )
        }
    }

    // HyperOS モード時に M3 コンポーネントへ渡すカラースキーム（紫色の混入を根絶し HyperOS ブルー基調に調和させる）
    val effectiveM3Scheme = remember(isHyperOs, m3ColorScheme, hycoColorScheme) {
        if (isHyperOs) {
            m3ColorScheme.copy(
                primary = hycoColorScheme.primary,
                onPrimary = Color.White,
                primaryContainer = hycoColorScheme.primary.copy(alpha = 0.2f),
                onPrimaryContainer = hycoColorScheme.primary,
                surface = hycoColorScheme.surface,
                onSurface = hycoColorScheme.onSurface,
                surfaceVariant = hycoColorScheme.surfaceContainer,
                onSurfaceVariant = hycoColorScheme.onSurfaceVariant,
                background = hycoColorScheme.background,
                onBackground = hycoColorScheme.onBackground,
                outline = hycoColorScheme.outline,
                error = hycoColorScheme.error,
            )
        } else {
            m3ColorScheme
        }
    }

    CompositionLocalProvider(
        LocalAppThemeMode provides themeSettings.themeMode,
        LocalHycoColors provides hycoColorScheme,
    ) {
        MiuixTheme(controller = miuixController) {
            MaterialTheme(colorScheme = effectiveM3Scheme) {
                CompositionLocalProvider(
                    LocalContentColor provides hycoColorScheme.onBackground,
                    MiuixLocalContentColor provides hycoColorScheme.onBackground,
                    content = content
                )
            }
        }
    }
}
