package com.github.stm7128.hycohyco.ui.player

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.github.stm7128.hycohyco.domain.model.CommentItem

/**
 * 自コメント削除確認ダイアログ
 */
@Composable
fun CommentDeleteDialog(
    comment: CommentItem?,
    onDismissRequest: () -> Unit,
    onConfirmDelete: (CommentItem) -> Unit
) {
    if (comment == null) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "コメントの削除", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = "「${comment.body}」\nこのコメントを削除しますか？")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmDelete(comment)
                    onDismissRequest()
                }
            ) {
                Text(
                    text = "削除",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "キャンセル")
            }
        }
    )
}
