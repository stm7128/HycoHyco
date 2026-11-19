package com.github.stm7128.hycohyco.ui.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CommentsDisabled
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ショートプレイヤー上部のバッジ＆閉じる・弾幕切替バー
 */
@Composable
internal fun ShortsTopBar(
    isOverlayMode: Boolean,
    isDanmakuEnabled: Boolean,
    onClose: () -> Unit,
    onToggleDanmaku: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOverlayMode) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "閉じる",
                            tint = Color.White
                        )
                    }
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ニコニコショート",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDetail) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "概要・詳細",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onToggleDanmaku) {
                    Icon(
                        imageVector = if (isDanmakuEnabled) Icons.AutoMirrored.Filled.Comment else Icons.Default.CommentsDisabled,
                        contentDescription = if (isDanmakuEnabled) "弾幕ON" else "弾幕OFF",
                        tint = if (isDanmakuEnabled) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * ショート画面右側の縦アクションボタン
 */
@Composable
internal fun ShortActionButton(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * ショート画面最下部の極細シークバー
 */
@Composable
internal fun ShortsBottomSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    var isDragging by remember { mutableStateOf(false) }
    var dragRatio by remember { mutableFloatStateOf(0f) }
    val displayRatio = if (isDragging) {
        dragRatio
    } else {
        (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    }

    Slider(
        value = displayRatio,
        onValueChange = {
            isDragging = true
            dragRatio = it
        },
        onValueChangeFinished = {
            isDragging = false
            onSeekTo((dragRatio * safeDuration).toLong())
        },
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color(0xFF38BDF8),
            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
        ),
        modifier = modifier.height(26.dp)
    )
}

internal fun formatCompactNumber(value: Long): String = when {
    value >= 100_000_000L -> String.format("%.1f億", value / 100_000_000f)
    value >= 10_000L -> String.format("%.1f万", value / 10_000f)
    else -> value.toString()
}
