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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.player.SleepTimerMode
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch

/**
 * 再生設定シート内のスリープタイマー & オーディオフォーカス共存設定セクション
 */
@Composable
fun PlayerSettingsTimerSection(
    currentTimerMode: SleepTimerMode,
    remainingSeconds: Int?,
    onTimerModeSelected: (SleepTimerMode) -> Unit,
    isCoexistAudio: Boolean,
    onCoexistAudioChange: (Boolean) -> Unit,
    isAutoEnterPip: Boolean = true,
    onAutoEnterPipChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 1. スリープタイマー
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "スリープタイマー",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (remainingSeconds != null && remainingSeconds > 0) {
                        "自動停止まで残り: ${formatDuration(remainingSeconds * 1000L)}"
                    } else if (currentTimerMode == SleepTimerMode.END_OF_VIDEO) {
                        "この動画の終了時に自動停止"
                    } else {
                        "設定時間経過後に動画の再生を自動停止"
                    },
                    fontSize = 12.sp,
                    color = if (remainingSeconds != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (remainingSeconds != null) FontWeight.Medium else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SleepTimerMode.entries.forEach { mode ->
                HycoFilterChip(
                    selected = currentTimerMode == mode,
                    onClick = { onTimerModeSelected(mode) },
                    label = mode.label
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 音声共存モード
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "他アプリの音と同時再生",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ゲームや音楽アプリの音を止めずに裏で再生（オーディオフォーカスを要求しない）",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            HycoSwitch(
                checked = isCoexistAudio,
                onCheckedChange = onCoexistAudioChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 自動 PiP 小窓移行モード (オフでバックグラウンド音声＋スーパー島へ直行)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ホーム移動時に自動でPiP小窓化",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "オフにすると小窓を出さずバックグラウンド再生＋スーパー島（通知）へ移行します",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            HycoSwitch(
                checked = isAutoEnterPip,
                onCheckedChange = onAutoEnterPipChange
            )
        }
    }
}
