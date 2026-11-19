package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch

/**
 * 再生設定シート内の再生オプション（リピート・自動再生・ジェスチャー・スキップ・SNS共有）セクション
 */
@Composable
fun PlayerSettingsPlaybackOptionsSection(
    isRepeatEnabled: Boolean,
    onRepeatToggle: (Boolean) -> Unit,
    isAutoplayEnabled: Boolean,
    onAutoplayToggle: (Boolean) -> Unit,
    isResumePlaybackEnabled: Boolean,
    onResumePlaybackToggle: (Boolean) -> Unit,
    gestureMode: PlayerGestureMode,
    onGestureModeSelected: (PlayerGestureMode) -> Unit,
    doubleTapSeekSeconds: Int,
    onDoubleTapSeekSecondsChange: (Int) -> Unit,
    useNicovideoGay: Boolean,
    onUseNicovideoGayChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 1. 1曲リピート再生
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "1曲リピート再生",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "動画終了時に自動で最初から再生します",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HycoSwitch(
                checked = isRepeatEnabled,
                onCheckedChange = onRepeatToggle
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 次の動画の自動再生
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "次の動画を自動再生",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "動画終了時に次のシリーズ動画を連続再生します",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HycoSwitch(
                checked = isAutoplayEnabled,
                onCheckedChange = onAutoplayToggle
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2.5 前回の続きから再生
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "前回の続きから再生",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "動画を開いたときに前回の再生位置から自動で再開します",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            HycoSwitch(
                checked = isResumePlaybackEnabled,
                onCheckedChange = onResumePlaybackToggle
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. スライド操作（音量・輝度）設定
        Column {
            Text(
                text = "スライド操作（音量・輝度）",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = gestureMode.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlayerGestureMode.entries.forEach { mode ->
                    HycoFilterChip(
                        selected = gestureMode == mode,
                        onClick = { onGestureModeSelected(mode) },
                        label = mode.label
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. 左右ダブルタップのスキップ秒数
        Column {
            Text(
                text = "ダブルタップのスキップ秒数",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "画面左右ダブルタップでスキップする秒数（現在: ${doubleTapSeekSeconds}秒）",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 30, 60).forEach { sec ->
                    HycoFilterChip(
                        selected = doubleTapSeekSeconds == sec,
                        onClick = { onDoubleTapSeekSecondsChange(sec) },
                        label = "${sec}秒"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. SNS共有設定 (nicovideo.gay)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "共有URLを nicovideo.gay に置換",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "X (Twitter) や Discord でサムネイル付き埋め込みが表示されるリンクで共有",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            HycoSwitch(
                checked = useNicovideoGay,
                onCheckedChange = onUseNicovideoGayChange
            )
        }
    }
}
