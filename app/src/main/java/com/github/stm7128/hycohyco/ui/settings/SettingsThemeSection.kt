package com.github.stm7128.hycohyco.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPreferenceRow
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch

/**
 * 設定画面の外観・テーマ設定セクション
 */
@Composable
fun SettingsThemeSection(
    themeSettings: ThemeSettings,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onDarkThemeConfigChanged: (DarkThemeConfig) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onAmoledBlackChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "外観・テーマ設定",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        HycoCard {
            Column {
                // テーマモード切り替え (HyperOS / Material You)
                HycoPreferenceRow(
                    title = "HyperOS デザイン",
                    summary = if (themeSettings.themeMode == AppThemeMode.HYPER_OS) {
                        "現在 HyperOS スタイルが適用されています"
                    } else {
                        "タップして HyperOS スタイルに切り替えます"
                    },
                    trailing = {
                        HycoSwitch(
                            checked = themeSettings.themeMode == AppThemeMode.HYPER_OS,
                            onCheckedChange = { isChecked ->
                                onThemeModeChanged(
                                    if (isChecked) AppThemeMode.HYPER_OS else AppThemeMode.MATERIAL_YOU
                                )
                            }
                        )
                    }
                )

                // Dynamic Color (壁紙連動 Monet) - Material You デザイン時のみ表示・有効化
                AnimatedVisibility(visible = themeSettings.themeMode == AppThemeMode.MATERIAL_YOU) {
                    HycoPreferenceRow(
                        title = "Dynamic Color (壁紙連動 Monet)",
                        summary = "壁紙から抽出したカラーをアプリ全体に適用します",
                        trailing = {
                            HycoSwitch(
                                checked = themeSettings.isDynamicColor,
                                onCheckedChange = onDynamicColorChanged
                            )
                        }
                    )
                }

                // ダークテーマ設定トグル
                val darkConfigLabel = when (themeSettings.darkThemeConfig) {
                    DarkThemeConfig.FOLLOW_SYSTEM -> "システム設定に従う"
                    DarkThemeConfig.LIGHT -> "ライトテーマ"
                    DarkThemeConfig.DARK -> "ダークテーマ"
                }

                HycoPreferenceRow(
                    title = "ダークモード",
                    summary = darkConfigLabel,
                    onClick = {
                        val nextConfig = when (themeSettings.darkThemeConfig) {
                            DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfig.LIGHT
                            DarkThemeConfig.LIGHT -> DarkThemeConfig.DARK
                            DarkThemeConfig.DARK -> DarkThemeConfig.FOLLOW_SYSTEM
                        }
                        onDarkThemeConfigChanged(nextConfig)
                    }
                )

                // AMOLED ピュアブラック設定 (有機ELの完全な黒)
                HycoPreferenceRow(
                    title = "AMOLED ピュアブラック",
                    summary = "ダークモード時に背景を完全な漆黒 (#000000) にします",
                    trailing = {
                        HycoSwitch(
                            checked = themeSettings.isAmoledBlack,
                            onCheckedChange = onAmoledBlackChanged,
                            enabled = themeSettings.darkThemeConfig != DarkThemeConfig.LIGHT
                        )
                    }
                )
            }
        }
    }
}
