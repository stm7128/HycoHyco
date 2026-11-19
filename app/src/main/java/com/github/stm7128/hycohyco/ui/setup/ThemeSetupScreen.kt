package com.github.stm7128.hycohyco.ui.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard

/**
 * 初回起動時のテーマ選択画面
 *
 * ユーザーに HyperOS スタイルと Material You スタイルの特徴を提示し、
 * 好みのデザインを選択させる。
 *
 * @param themeSettings 現在のテーマ設定
 * @param onSelectThemeMode テーマ選択時のコールバック
 * @param onConfirm 確定ボタン押下時のコールバック
 */
@Composable
fun ThemeSetupScreen(
    themeSettings: ThemeSettings,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ようこそ HycoHyco へ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "アプリのデザインスタイルを選択してください。\n後から設定画面でいつでも切り替えられます。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // HyperOS 選択カード
                ThemeOptionCard(
                    title = "HyperOS スタイル",
                    subtitle = "Xiaomi HyperOS 風のスクワークル角丸と\nすりガラス質感のモダンなデザイン",
                    icon = Icons.Default.PhoneAndroid,
                    isSelected = themeSettings.themeMode == AppThemeMode.HYPER_OS,
                    onClick = { onSelectThemeMode(AppThemeMode.HYPER_OS) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Material You 選択カード
                ThemeOptionCard(
                    title = "Material You スタイル",
                    subtitle = "壁紙と連動する Dynamic Color と\nGoogle Pixel ライクなフラットデザイン",
                    icon = Icons.Default.ColorLens,
                    isSelected = themeSettings.themeMode == AppThemeMode.MATERIAL_YOU,
                    onClick = { onSelectThemeMode(AppThemeMode.MATERIAL_YOU) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ライブプレビューコンポーネント
                LiveThemePreview(themeMode = themeSettings.themeMode)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                HycoButton(
                    text = "このスタイルで始める",
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            }
        }
    }
}

/**
 * テーマ選択肢のカード
 */
@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "選択中",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 選択中テーマのプレビュー表示
 */
@Composable
private fun LiveThemePreview(themeMode: AppThemeMode) {
    HycoCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = if (themeMode == AppThemeMode.HYPER_OS) 20.dp else 12.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "プレビュー: ${if (themeMode == AppThemeMode.HYPER_OS) "HyperOS" else "Material You"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "公式アプリより軽く、あなたの好みにフィットするUI体験",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
