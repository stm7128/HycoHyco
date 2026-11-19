package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import androidx.compose.material3.Scaffold as MaterialScaffold
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.theme.LocalContentColor as MiuixLocalContentColor

/**
 * デュアル・デザインシステム対応共通 Scaffold
 *
 * HyperOS モード時は Miuix の Scaffold を用い、
 * Material You モード時は Material 3 の Scaffold を用いる。
 *
 * @param modifier 修飾子
 * @param topBar 上部アプリバー
 * @param bottomBar 下部ナビゲーションバー
 * @param content メインコンテンツ (PaddingValues を受け取る)
 */
@Composable
fun HycoScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val themeMode = LocalAppThemeMode.current

    val onBgColor = HycoTheme.colors.onBackground
    val wrappedContent: @Composable (PaddingValues) -> Unit = { padding ->
        CompositionLocalProvider(
            LocalContentColor provides onBgColor,
            MiuixLocalContentColor provides onBgColor
        ) {
            content(padding)
        }
    }

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            MiuixScaffold(
                modifier = modifier,
                topBar = topBar,
                bottomBar = bottomBar,
                content = wrappedContent
            )
        }

        AppThemeMode.MATERIAL_YOU -> {
            MaterialScaffold(
                modifier = modifier,
                topBar = topBar,
                bottomBar = bottomBar,
                containerColor = MaterialTheme.colorScheme.background,
                content = wrappedContent
            )
        }
    }
}
