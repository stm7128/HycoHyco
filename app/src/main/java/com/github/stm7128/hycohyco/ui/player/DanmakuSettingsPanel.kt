package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.player.danmaku.DanmakuAreaMode
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSlider
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch

/**
 * コメント一覧上部に展開する弾幕詳細カスタマイズ & NG 設定パネル
 */
@Composable
fun DanmakuSettingsPanel(
    danmakuSettings: DanmakuDisplaySettings,
    ngConfig: CommentNgConfig,
    onUpdateDanmakuSettings: (DanmakuDisplaySettings) -> Unit,
    onUpdateNgConfig: (CommentNgConfig) -> Unit,
    textColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 10.dp)) {
        // 1. 不透明度
        Text(
            text = "弾幕不透明度: ${(danmakuSettings.alpha * 100).toInt()}%",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        HycoSlider(
            value = danmakuSettings.alpha,
            onValueChange = { alpha ->
                onUpdateDanmakuSettings(danmakuSettings.copy(alpha = alpha))
            },
            valueRange = 0.1f..1.0f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 文字サイズ倍率
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "文字サイズ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.width(68.dp)
            )
            listOf(
                0.75f to "小 (75%)",
                1.0f to "標準",
                1.25f to "大 (125%)"
            ).forEach { (scale, label) ->
                val isSelected = kotlin.math.abs(danmakuSettings.scale - scale) < 0.05f
                HycoFilterChip(
                    selected = isSelected,
                    onClick = { onUpdateDanmakuSettings(danmakuSettings.copy(scale = scale)) },
                    label = label
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. 流れる速度倍率
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "流れる速度",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.width(68.dp)
            )
            listOf(
                0.8f to "0.8x",
                1.0f to "1.0x (標準)",
                1.25f to "1.25x"
            ).forEach { (speed, label) ->
                val isSelected = kotlin.math.abs(danmakuSettings.speedScale - speed) < 0.05f
                HycoFilterChip(
                    selected = isSelected,
                    onClick = { onUpdateDanmakuSettings(danmakuSettings.copy(speedScale = speed)) },
                    label = label
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. 表示領域モード
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "表示領域",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.width(68.dp)
            )
            DanmakuAreaMode.entries.forEach { mode ->
                HycoFilterChip(
                    selected = danmakuSettings.areaMode == mode,
                    onClick = { onUpdateDanmakuSettings(danmakuSettings.copy(areaMode = mode)) },
                    label = mode.label
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. 184 コメント非表示トグル
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "184 (匿名) コメントを非表示",
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "コマンド 184 付きのコメントを一括非表示",
                    color = secondaryColor,
                    fontSize = 11.sp
                )
            }
            HycoSwitch(
                checked = ngConfig.hide184Comments,
                onCheckedChange = { hide ->
                    onUpdateNgConfig(ngConfig.copy(hide184Comments = hide))
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = secondaryColor.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(4.dp))
    }
}
