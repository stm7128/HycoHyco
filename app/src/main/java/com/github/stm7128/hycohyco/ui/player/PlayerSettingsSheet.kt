package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.domain.model.VideoQualityOption
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch

private val PLAYBACK_SPEEDS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

/**
 * 動画再生設定ボトムシート（画質・再生速度・リピート再生）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    onDismissRequest: () -> Unit,
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    isRepeatEnabled: Boolean,
    onRepeatToggle: (Boolean) -> Unit,
    availableQualities: List<VideoQualityOption>,
    selectedQualityId: String?,
    onQualitySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    gestureMode: PlayerGestureMode = PlayerGestureMode.FULLSCREEN_ONLY,
    onGestureModeSelected: (PlayerGestureMode) -> Unit = {},
    onDownloadSelected: (DownloadType) -> Unit = {},
    isAutoplayEnabled: Boolean = true,
    onAutoplayToggle: (Boolean) -> Unit = {},
    isResumePlaybackEnabled: Boolean = true,
    onResumePlaybackToggle: (Boolean) -> Unit = {},
    doubleTapSeekSeconds: Int = 10,
    onDoubleTapSeekSecondsChange: (Int) -> Unit = {},
    useNicovideoGay: Boolean = false,
    onUseNicovideoGayChange: (Boolean) -> Unit = {},
    currentTimerMode: com.github.stm7128.hycohyco.player.SleepTimerMode = com.github.stm7128.hycohyco.player.SleepTimerMode.OFF,
    remainingTimerSeconds: Int? = null,
    onTimerModeSelected: (com.github.stm7128.hycohyco.player.SleepTimerMode) -> Unit = {},
    isCoexistAudio: Boolean = false,
    onCoexistAudioChange: (Boolean) -> Unit = {},
    isAutoEnterPip: Boolean = true,
    onAutoEnterPipChange: (Boolean) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "再生設定",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "閉じる")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. 再生速度切替
            Text(
                text = "再生速度",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PLAYBACK_SPEEDS.forEach { speed ->
                    val isSelected = (currentSpeed - speed).let { kotlin.math.abs(it) < 0.05f }
                    HycoFilterChip(
                        selected = isSelected,
                        onClick = { onSpeedSelected(speed) },
                        label = if (speed == 1.0f) "標準 (1.0x)" else "${speed}x"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. スリープタイマー & 音声共存モード & 自動PiP
            PlayerSettingsTimerSection(
                currentTimerMode = currentTimerMode,
                remainingSeconds = remainingTimerSeconds,
                onTimerModeSelected = onTimerModeSelected,
                isCoexistAudio = isCoexistAudio,
                onCoexistAudioChange = onCoexistAudioChange,
                isAutoEnterPip = isAutoEnterPip,
                onAutoEnterPipChange = onAutoEnterPipChange
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. 再生オプション (リピート・自動再生・ジェスチャー・スキップ・SNS共有)
            PlayerSettingsPlaybackOptionsSection(
                isRepeatEnabled = isRepeatEnabled,
                onRepeatToggle = onRepeatToggle,
                isAutoplayEnabled = isAutoplayEnabled,
                onAutoplayToggle = onAutoplayToggle,
                isResumePlaybackEnabled = isResumePlaybackEnabled,
                onResumePlaybackToggle = onResumePlaybackToggle,
                gestureMode = gestureMode,
                onGestureModeSelected = onGestureModeSelected,
                doubleTapSeekSeconds = doubleTapSeekSeconds,
                onDoubleTapSeekSecondsChange = onDoubleTapSeekSecondsChange,
                useNicovideoGay = useNicovideoGay,
                onUseNicovideoGayChange = onUseNicovideoGayChange
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. オフライン保存（動画・音声・120fps弾幕）
            PlayerSettingsDownloadSection(
                onDownloadSelected = onDownloadSelected,
                onDismissRequest = onDismissRequest
            )

            // 5. 画質選択 (利用可能な場合)
            PlayerSettingsQualitySection(
                availableQualities = availableQualities,
                selectedQualityId = selectedQualityId,
                onQualitySelected = onQualitySelected
            )
        }
    }
}
