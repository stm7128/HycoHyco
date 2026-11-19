package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CommentItem

/**
 * コメント一覧の1行表示アイテム
 *
 * 公式準拠のニコるヒートマップ背景（Lv0〜Lv4）およびニコる笑顔ボタンに対応。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentRowItem(
    comment: CommentItem,
    onSeekTo: (Long) -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onToggleNicoru: (CommentItem) -> Unit = {},
    textColor: Color,
    secondaryColor: Color,
    isCurrentPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    // ニコる件数に応じた公式準拠 5段階ヒートマップ背景色
    val backgroundColor = when {
        isCurrentPlaying -> Color(0xFF0095FF).copy(alpha = 0.16f)
        comment.nicoruCount >= 30 -> Color(0xFFFFB300).copy(alpha = 0.38f)
        comment.nicoruCount >= 10 -> Color(0xFFFFB300).copy(alpha = 0.24f)
        comment.nicoruCount >= 5  -> Color(0xFFFFB300).copy(alpha = 0.14f)
        comment.nicoruCount >= 1  -> Color(0xFFFFB300).copy(alpha = 0.07f)
        else -> Color.Transparent
    }

    val isNicorued = comment.nicoruId != null
    val nicoruColor = if (isNicorued) Color(0xFFFFD700) else secondaryColor.copy(alpha = 0.7f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = { onSeekTo(comment.vposMs) },
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 左側: 本文 & 下部に再生時間 (mm:ss) + 自コメントバッジ
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (comment.isMyComment) {
                    Text(
                        text = "自分",
                        color = Color(0xFFFFB300),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                Text(
                    text = comment.body,
                    color = if (isCurrentPlaying) Color(0xFF0095FF) else textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isCurrentPlaying || isNicorued) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 再生時間 (mm:ss)
            Text(
                text = formatDuration(comment.vposMs),
                color = if (isCurrentPlaying) Color(0xFF0095FF) else secondaryColor,
                fontSize = 11.sp,
                fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 2. 右側: 自コメント削除ボタン (自分の場合) & ニコる笑顔ボタン
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (comment.isMyComment) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "コメントを削除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // ニコる笑顔アイコン & カウント（公式完全準拠: 上に笑顔マーク、下に数字）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleNicoru(comment) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (isNicorued) Icons.Default.SentimentSatisfiedAlt else Icons.Default.SentimentSatisfied,
                    contentDescription = "ニコる",
                    tint = nicoruColor,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "${comment.nicoruCount}",
                    color = nicoruColor,
                    fontSize = 10.sp,
                    fontWeight = if (isNicorued) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
