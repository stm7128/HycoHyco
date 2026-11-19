package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * デュアル・デザインシステム対応共通テキスト入力フィールド
 *
 * HyperOS: Xiaomi 純正風の枠線なし・角丸フラット塗りつぶし入力バー
 * Material You: Material 3 のアウトライン入力バー
 */
@Composable
fun HycoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val themeMode = LocalAppThemeMode.current

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            val bg = MiuixTheme.colorScheme.surfaceContainer
            val textColor = MiuixTheme.colorScheme.onSurface
            val placeholderColor = MiuixTheme.colorScheme.onSurfaceVariantSummary

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(bg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = placeholderColor,
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = singleLine,
                        textStyle = LocalTextStyle.current.copy(
                            color = textColor,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF0095FF)),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions
                    )
                }

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }

        AppThemeMode.MATERIAL_YOU -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions
            )
        }
    }
}

/**
 * デュアル・デザインシステム対応共通フィルタチップ
 *
 * HyperOS: Xiaomi カプセルピルスタイル（選択時は青塗りつぶし、非選択時は控えめなサーフェス）
 * Material You: Material 3 の FilterChip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HycoFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val themeMode = LocalAppThemeMode.current

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            val bgColor = if (selected) {
                Color(0xFF0095FF) // ニコニコブルー / HyperOS アクセント
            } else {
                MiuixTheme.colorScheme.surfaceContainer
            }
            val textColor = if (selected) {
                Color.White
            } else {
                MiuixTheme.colorScheme.onSurface
            }

            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(bgColor)
                    .hycoBounceClick(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        AppThemeMode.MATERIAL_YOU -> {
            FilterChip(
                selected = selected,
                onClick = onClick,
                label = { Text(label) },
                modifier = modifier,
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}

/**
 * デュアル・デザインシステム対応共通スライダー
 */
@Composable
fun HycoSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val themeMode = LocalAppThemeMode.current

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            Slider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF0095FF),
                    activeTrackColor = Color(0xFF0095FF),
                    inactiveTrackColor = MiuixTheme.colorScheme.surfaceContainer
                ),
                modifier = modifier
            )
        }

        AppThemeMode.MATERIAL_YOU -> {
            Slider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = modifier
            )
        }
    }
}
