package com.github.stm7128.hycohyco.ui.settings

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPreferenceRow

/**
 * 設定画面のアプリ情報セクション
 */
@Composable
fun SettingsAppInfoSection(
    themeSettings: ThemeSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pkgInfo.versionName
        }.getOrNull().orEmpty().ifBlank { "1.0.0" }
    }

    Column(modifier = modifier) {
        Text(
            text = "アプリ情報",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        HycoCard {
            Column {
                HycoPreferenceRow(
                    title = "アプリ名",
                    summary = "HycoHyco (ハイコハイコ)"
                )
                HycoPreferenceRow(
                    title = "バージョン",
                    summary = versionName
                )
                HycoPreferenceRow(
                    title = "デザインシステム",
                    summary = if (themeSettings.themeMode == AppThemeMode.HYPER_OS) "HyperOS (miuix)" else "Material You (Material 3)"
                )
            }
        }
    }
}
