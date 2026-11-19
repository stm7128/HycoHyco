package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip

/**
 * プレイヤー設定シート内のオフライン保存セクション
 */
@Composable
fun PlayerSettingsDownloadSection(
    onDownloadSelected: (DownloadType) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "オフライン保存",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "圏外でも120fps弾幕付きで完全オフライン再生できます",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HycoFilterChip(
                selected = false,
                onClick = {
                    onDownloadSelected(DownloadType.VIDEO_AND_DANMAKU)
                    onDismissRequest()
                },
                label = "動画 + 弾幕"
            )
            HycoFilterChip(
                selected = false,
                onClick = {
                    onDownloadSelected(DownloadType.AUDIO_ONLY_AND_DANMAKU)
                    onDismissRequest()
                },
                label = "音声のみ + 弾幕"
            )
        }
    }
}
