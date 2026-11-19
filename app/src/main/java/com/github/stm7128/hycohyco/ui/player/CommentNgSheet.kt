package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonOff
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard

/**
 * コメント長押し時に表示する NG 登録ボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentNgSheet(
    comment: CommentItem?,
    onDismiss: () -> Unit,
    onAddNgUserId: (String) -> Unit,
    onAddNgWord: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (comment == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "コメントの NG 登録",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "閉じる")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 対象コメントプレビューカード
            HycoCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = comment.body,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ユーザーID: ${if (comment.is184) "${comment.userId} (184)" else comment.userId}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. ユーザー ID の NG 追加ボタン
            if (comment.userId.isNotBlank()) {
                HycoButton(
                    text = "このユーザーIDをNGに追加",
                    onClick = {
                        onAddNgUserId(comment.userId)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 2. 本文の NG ワード追加ボタン
            HycoButton(
                text = "このコメント本文をNGワードに追加",
                onClick = {
                    onAddNgWord(comment.body.trim())
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
