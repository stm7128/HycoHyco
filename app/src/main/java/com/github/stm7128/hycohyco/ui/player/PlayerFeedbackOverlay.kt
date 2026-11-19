package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import kotlinx.coroutines.delay

/**
 * プレイヤー画面上の共有ボトムシートおよび操作フィードバック通知バナー
 */
@Composable
fun BoxScope.PlayerFeedbackOverlay(
    uiState: PlayerUiState,
    isShareSheetVisible: Boolean,
    onDismissShareSheet: () -> Unit,
    onClearUserFeedbackMessage: () -> Unit,
) {
    if (isShareSheetVisible && uiState.videoDetail != null) {
        PlayerShareSheet(
            detail = uiState.videoDetail,
            currentPositionMs = uiState.playbackState.currentPositionMs,
            useNicovideoGay = uiState.useNicovideoGay,
            onDismissRequest = onDismissShareSheet
        )
    }

    uiState.userFeedbackMessage?.let { msg ->
        LaunchedEffect(msg) {
            delay(3000L)
            onClearUserFeedbackMessage()
        }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = msg,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}
