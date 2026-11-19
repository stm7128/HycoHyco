package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.Switch as MaterialSwitch
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch

/**
 * デュアル・デザインシステム対応共通ボタン
 *
 * @param text ボタンのラベル文字列
 * @param onClick クリック時のコールバック
 * @param modifier 修飾子
 * @param enabled 有効/無効状態
 */
@Composable
fun HycoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val themeMode = LocalAppThemeMode.current

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            MiuixButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled
            ) {
                Text(text = text)
            }
        }

        AppThemeMode.MATERIAL_YOU -> {
            MaterialButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled
            ) {
                Text(text = text)
            }
        }
    }
}

/**
 * デュアル・デザインシステム対応共通スイッチ
 *
 * @param checked オン/オフ状態
 * @param onCheckedChange 状態変更時のコールバック
 * @param modifier 修飾子
 * @param enabled 有効/無効状態
 */
@Composable
fun HycoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val themeMode = LocalAppThemeMode.current

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            MiuixSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = modifier,
                enabled = enabled
            )
        }

        AppThemeMode.MATERIAL_YOU -> {
            MaterialSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = modifier,
                enabled = enabled
            )
        }
    }
}

/**
 * 設定画面用の共通行アイテムコンポーネント
 *
 * タイトル、説明文、末尾のコントロール（スイッチやテキストなど）を配置する。
 *
 * @param title 項目タイトル
 * @param summary 補足説明（省略可）
 * @param modifier 修飾子
 * @param onClick 行タップ時のコールバック（null の場合はタップ不可）
 * @param trailing 末尾に配置する Composable（スイッチやアイコンなど）
 */
@Composable
fun HycoPreferenceRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val themeMode = LocalAppThemeMode.current
    val titleColor = when (themeMode) {
        AppThemeMode.HYPER_OS -> top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurface
        AppThemeMode.MATERIAL_YOU -> MaterialTheme.colorScheme.onSurface
    }
    val summaryColor = when (themeMode) {
        AppThemeMode.HYPER_OS -> top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary
        AppThemeMode.MATERIAL_YOU -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            if (summary != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = summaryColor
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }
}
