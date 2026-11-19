package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import top.yukonga.miuix.kmp.basic.PullToRefresh as MiuixPullToRefresh
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState as rememberMiuixPullToRefreshState

private val HycoRefreshTexts = listOf(
    "下に引いて更新",
    "離して更新",
    "更新中...",
    "更新完了"
)

/**
 * デュアル・デザインシステム対応 Pull-to-Refresh コンテナ
 *
 * - HyperOS モード: Miuix の [MiuixPullToRefresh]（弾性スプリング＆日本語ステータス）
 * - Material You モード: Material 3 の [PullToRefreshBox]（Monet カラースピナー）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HycoPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val themeMode = LocalAppThemeMode.current
    val colors = HycoTheme.colors

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            val state = rememberMiuixPullToRefreshState()
            val textColor = colors.onBackgroundVariant
            MiuixPullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier,
                pullToRefreshState = state,
                color = textColor,
                refreshTexts = HycoRefreshTexts,
                refreshTextStyle = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }

        AppThemeMode.MATERIAL_YOU -> {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier,
                content = content
            )
        }
    }
}
