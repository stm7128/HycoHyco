package com.github.stm7128.hycohyco.player.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * プレイヤーのジェスチャーフィードバック HUD
 *
 * 輝度・音量・シークは画面中央に表示し、長押し2倍速（[PlayerGestureOverlay.Speed2x]）は
 * 映像や弾幕の中央視認性を妨げないよう画面上部中央のコンパクトなピルで表示する。
 */
@Composable
fun BoxScope.PlayerGestureHud(
    overlayState: PlayerGestureOverlay,
    modifier: Modifier = Modifier
) {
    // 長押し2倍速インジケーター（画面上部中央・コンパクトピル）
    AnimatedVisibility(
        visible = overlayState is PlayerGestureOverlay.Speed2x,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .align(Alignment.TopCenter)
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = "倍速",
                tint = Color(0xFF0095FF),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "2.0x 再生中",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // 輝度・音量・シークインジケーター（画面中央）
    AnimatedVisibility(
        visible = overlayState !is PlayerGestureOverlay.None && overlayState !is PlayerGestureOverlay.Speed2x,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.align(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = overlayState) {
                is PlayerGestureOverlay.Brightness -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BrightnessHigh,
                            contentDescription = "輝度",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "輝度 ${state.percent}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is PlayerGestureOverlay.Volume -> {
                    val icon = when {
                        state.percent == 0 -> Icons.AutoMirrored.Filled.VolumeMute
                        state.percent < 50 -> Icons.AutoMirrored.Filled.VolumeDown
                        else -> Icons.AutoMirrored.Filled.VolumeUp
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "音量",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "音量 ${state.percent}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is PlayerGestureOverlay.Seek -> {
                    val isForward = state.deltaSeconds > 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = "シーク",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${if (isForward) "+" else ""}${state.deltaSeconds} 秒",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is PlayerGestureOverlay.Speed2x,
                PlayerGestureOverlay.None -> {}
            }
        }
    }
}
