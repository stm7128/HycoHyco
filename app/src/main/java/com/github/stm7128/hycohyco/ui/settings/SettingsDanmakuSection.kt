package com.github.stm7128.hycohyco.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.player.danmaku.DanmakuAreaMode
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPreferenceRow
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSlider
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch
import kotlin.math.abs

private val DANMAKU_FONT_SCALES = listOf(0.75f to "小 (75%)", 1.0f to "標準", 1.25f to "大 (125%)")
private val DANMAKU_SPEED_SCALES = listOf(0.8f to "0.8x", 1.0f to "1.0x (標準)", 1.25f to "1.25x")

/**
 * メイン設定画面用の弾幕表示カスタマイズセクション
 */
@Composable
fun SettingsDanmakuSection(
    danmakuSettings: DanmakuDisplaySettings,
    onUpdateDanmakuSettings: (DanmakuDisplaySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HycoTheme.colors
    var alphaDraft by remember(danmakuSettings.alpha) { mutableFloatStateOf(danmakuSettings.alpha) }

    Column(modifier = modifier) {
        Text(
            text = "弾幕・コメント表示設定",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        HycoCard {
            Column {
                // 1. 弾幕表示 ON/OFF
                HycoPreferenceRow(
                    title = "動画上に弾幕を表示",
                    summary = "コメントを動画映像の上にオーバーレイ描画します",
                    trailing = {
                        HycoSwitch(
                            checked = danmakuSettings.isEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateDanmakuSettings(danmakuSettings.copy(isEnabled = enabled))
                            }
                        )
                    }
                )

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // 2. 不透明度スライダー
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "弾幕の不透明度: ${(alphaDraft * 100).toInt()}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    HycoSlider(
                        value = alphaDraft,
                        onValueChange = { alphaDraft = it },
                        onValueChangeFinished = {
                            onUpdateDanmakuSettings(danmakuSettings.copy(alpha = alphaDraft))
                        },
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // 3. 文字サイズ倍率
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "弾幕の文字サイズ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DANMAKU_FONT_SCALES.forEach { (scale, label) ->
                            HycoFilterChip(
                                selected = abs(danmakuSettings.scale - scale) < 0.05f,
                                onClick = { onUpdateDanmakuSettings(danmakuSettings.copy(scale = scale)) },
                                label = label
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // 4. 流れる速度倍率
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "流れる速度",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DANMAKU_SPEED_SCALES.forEach { (speed, label) ->
                            HycoFilterChip(
                                selected = abs(danmakuSettings.speedScale - speed) < 0.05f,
                                onClick = { onUpdateDanmakuSettings(danmakuSettings.copy(speedScale = speed)) },
                                label = label
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // 5. 表示領域モード
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "表示領域",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "字幕と重ならない表示範囲などを選べます",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DanmakuAreaMode.entries.forEach { mode ->
                            HycoFilterChip(
                                selected = danmakuSettings.areaMode == mode,
                                onClick = { onUpdateDanmakuSettings(danmakuSettings.copy(areaMode = mode)) },
                                label = mode.label
                            )
                        }
                    }
                }
            }
        }
    }
}
