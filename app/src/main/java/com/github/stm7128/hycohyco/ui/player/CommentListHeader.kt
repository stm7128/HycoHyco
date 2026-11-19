package com.github.stm7128.hycohyco.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * コメント一覧のヘッダーバー
 *
 * コメント件数、自動スクロール（動画時間追従）ON/OFF トグル、弾幕・NG設定開閉ボタンを含む。
 */
@Composable
fun CommentListHeader(
    commentCount: Int,
    isLoading: Boolean,
    isSettingsExpanded: Boolean,
    isAutoScrollEnabled: Boolean,
    onToggleSettings: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    textColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    isPastLogActive: Boolean = false,
    onOpenPastLogPicker: () -> Unit = {},
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isSettingsExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 左側: コメント件数（タップで設定開閉）
        Row(
            modifier = Modifier
                .clickable { onToggleSettings() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = Color(0xFF0095FF),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "コメント (${commentCount}件)",
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (isLoading) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF0095FF)
                )
            }
        }

        // 2. 右側: 過去ログボタン & 自動スクロールトグル & 弾幕・NG設定トグル
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 過去ログ日時選択ボタン
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isPastLogActive) Color(0xFFFF9800).copy(alpha = 0.16f) else Color.Transparent,
                modifier = Modifier.clickable { onOpenPastLogPicker() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "過去ログを表示",
                        tint = if (isPastLogActive) Color(0xFFFF9800) else secondaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "過去ログ",
                        color = if (isPastLogActive) Color(0xFFFF9800) else secondaryColor.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = if (isPastLogActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 自動スクロール（動画時間追従）トグルボタン
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isAutoScrollEnabled) Color(0xFF0095FF).copy(alpha = 0.12f) else Color.Transparent,
                modifier = Modifier.clickable { onToggleAutoScroll() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAutoScrollEnabled) Icons.Default.Sync else Icons.Default.SyncDisabled,
                        contentDescription = if (isAutoScrollEnabled) "自動追従中" else "自動追従オフ",
                        tint = if (isAutoScrollEnabled) Color(0xFF0095FF) else secondaryColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "追従",
                        color = if (isAutoScrollEnabled) Color(0xFF0095FF) else secondaryColor.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = if (isAutoScrollEnabled) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 弾幕・NG設定開閉ボタン
            IconButton(
                onClick = onToggleSettings,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "弾幕・NG設定",
                    tint = if (isSettingsExpanded) Color(0xFF0095FF) else secondaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = secondaryColor,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(arrowRotation)
                    .clickable { onToggleSettings() }
            )
        }
    }
}
