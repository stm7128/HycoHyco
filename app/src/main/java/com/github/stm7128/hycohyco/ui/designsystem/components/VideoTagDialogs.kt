package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoTag
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme

/**
 * 動画タグ長押し時のアクションメニューダイアログ
 * （タグ検索、ニコニコ大百科、一般タグの削除を提供）
 */
@Composable
internal fun VideoTagActionDialog(
    tag: VideoTag,
    isEditable: Boolean,
    onDismiss: () -> Unit,
    onSearchTag: () -> Unit,
    onOpenNicodic: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val colors = HycoTheme.colors
    var confirmDelete by remember(tag.name) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurface,
        title = {
            Text(
                text = if (confirmDelete) "タグの削除" else tag.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (confirmDelete) {
                Text(
                    text = "タグ「${tag.name}」をこの動画から削除しますか？",
                    fontSize = 14.sp,
                    color = colors.onSurface
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = buildString {
                            append(if (tag.isLocked) "ロックされたタグ" else "一般タグ")
                            if (tag.isNicodicArticleExists) append("・大百科記事あり")
                        },
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                    if (!tag.nicodicSummary.isNullOrBlank()) {
                        Text(
                            text = tag.nicodicSummary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TagDialogActionItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp)) },
                        label = "このタグで検索する",
                        labelColor = colors.onSurface,
                        onClick = {
                            onDismiss()
                            onSearchTag()
                        }
                    )
                    TagDialogActionItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp)) },
                        label = if (tag.isNicodicArticleExists) {
                            "ニコニコ大百科の記事を開く"
                        } else {
                            "ニコニコ大百科で調べる"
                        },
                        labelColor = colors.onSurface,
                        onClick = {
                            onDismiss()
                            onOpenNicodic()
                        }
                    )
                    if (isEditable && !tag.isLocked) {
                        TagDialogActionItem(
                            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp)) },
                            label = "このタグを削除する",
                            labelColor = colors.error,
                            onClick = { confirmDelete = true }
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (confirmDelete) {
                TextButton(
                    onClick = {
                        onDismiss()
                        onRequestDelete()
                    }
                ) {
                    Text("削除", color = colors.error, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("閉じる", color = colors.onSurfaceVariant)
                }
            }
        },
        dismissButton = {
            if (confirmDelete) {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("戻る", color = colors.onSurfaceVariant)
                }
            }
        }
    )
}

@Composable
private fun TagDialogActionItem(
    icon: @Composable () -> Unit,
    label: String,
    labelColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, color = labelColor, fontSize = 14.sp)
        }
    }
}

/**
 * 新規タグ追加ダイアログ
 */
@Composable
internal fun AddVideoTagDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (String) -> Unit
) {
    val colors = HycoTheme.colors
    var tagInput by remember { mutableStateOf("") }
    val trimmed = tagInput.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurface,
        title = {
            Text(text = "タグを追加", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                HycoTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    placeholder = "追加するタグ名を入力",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (trimmed.isNotEmpty()) {
                                onConfirmAdd(trimmed)
                                onDismiss()
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "※ 1件ずつ追加してください（既存のロックや上限数を超える場合は登録できません）。",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = trimmed.isNotEmpty(),
                onClick = {
                    onConfirmAdd(trimmed)
                    onDismiss()
                }
            ) {
                Text(
                    text = "追加",
                    fontWeight = FontWeight.Bold,
                    color = if (trimmed.isNotEmpty()) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = colors.onSurfaceVariant)
            }
        }
    )
}
