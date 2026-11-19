package com.github.stm7128.hycohyco.ui.shorts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.player.CommentRowItem

/**
 * ショートプレイヤー用のコメント一覧＆投稿トリガーボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsCommentSheet(
    comments: List<CommentItem>,
    onDismiss: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleNicoru: (CommentItem) -> Unit,
    onOpenCommentInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val colors = HycoTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "コメント (${comments.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "閉じる",
                        tint = colors.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = colors.onSurfaceVariant.copy(alpha = 0.15f))

            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "コメントはまだありません",
                        color = colors.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        CommentRowItem(
                            comment = comment,
                            onSeekTo = { vpos ->
                                onSeekTo(vpos)
                                onDismiss()
                            },
                            onLongClick = {},
                            onToggleNicoru = onToggleNicoru,
                            textColor = colors.onSurface,
                            secondaryColor = colors.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.onSurfaceVariant.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            // 下部コメント投稿バー
            Surface(
                color = colors.surfaceContainer,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onOpenCommentInput)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "コメントを追加...",
                        color = colors.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "投稿",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
