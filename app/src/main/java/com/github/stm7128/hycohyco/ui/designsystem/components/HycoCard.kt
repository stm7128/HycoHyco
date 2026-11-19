package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import androidx.compose.material3.Card as MaterialCard
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.theme.LocalContentColor as MiuixLocalContentColor

/**
 * デュアル・デザインシステム対応共通カードコンポーネント
 *
 * HyperOS モード時は Miuix のスクワークル・角丸カードを描画し、
 * Material You モード時は Material 3 の Elevated/Filled カードを描画する。
 *
 * @param modifier 修飾子
 * @param onClick クリック時のコールバック（null の場合はクリック不可）
 * @param cornerRadius 角丸の半径 (dp)
 * @param content カード内部のコンテンツ
 */
@Composable
fun HycoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val themeMode = LocalAppThemeMode.current

    val onSurfaceColor = HycoTheme.colors.onSurface

    CompositionLocalProvider(
        LocalContentColor provides onSurfaceColor,
        MiuixLocalContentColor provides onSurfaceColor
    ) {
        when (themeMode) {
            AppThemeMode.HYPER_OS -> {
                MiuixCard(
                    modifier = modifier.fillMaxWidth(),
                    cornerRadius = if (cornerRadius == 16.dp) 18.dp else cornerRadius,
                    onClick = onClick
                ) {
                    Box(
                        modifier = Modifier.padding(16.dp),
                        content = content
                    )
                }
            }

            AppThemeMode.MATERIAL_YOU -> {
                MaterialCard(
                    modifier = modifier.fillMaxWidth(),
                    onClick = onClick ?: {},
                    enabled = onClick != null,
                    shape = RoundedCornerShape(cornerRadius),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(16.dp),
                        content = content
                    )
                }
            }
        }
    }
}
