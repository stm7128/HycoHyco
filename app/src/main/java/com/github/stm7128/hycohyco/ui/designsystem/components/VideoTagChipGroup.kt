package com.github.stm7128.hycohyco.ui.designsystem.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.VideoTag
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import com.github.stm7128.hycohyco.ui.player.PipHelper
import java.net.URLEncoder

/**
 * 動画・ニコニコショート共通の折り返しタグチップ一覧コンポーネント
 *
 * - タップ: タグ検索へ遷移
 * - 「百」バッジタップ / 長押しメニュー: ニコニコ大百科記事を開く・一般タグを削除
 * - 編集可能時: 末尾の「＋ タグ追加」チップから新規タグを投稿
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoTagChipGroup(
    tags: List<VideoTag>,
    isEditable: Boolean,
    isSubmitting: Boolean = false,
    hasR18Tag: Boolean = false,
    onTagClick: (String) -> Unit,
    onAddTag: (String) -> Unit = {},
    onDeleteTag: (String) -> Unit = {},
    onOpenNicodic: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty() && !isEditable) return

    val colors = HycoTheme.colors
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var selectedTagForMenu by remember { mutableStateOf<VideoTag?>(null) }
    var isAddDialogVisible by remember { mutableStateOf(false) }

    val openNicodicArticle: (VideoTag) -> Unit = { targetTag ->
        val resolvedUrl = targetTag.nicodicUrl?.takeIf { it.isNotBlank() } ?: run {
            val encoded = runCatching {
                URLEncoder.encode(targetTag.name, "UTF-8").replace("+", "%20")
            }.getOrDefault(targetTag.name)
            "https://dic.nicovideo.jp/a/$encoded"
        }
        if (onOpenNicodic != null) {
            onOpenNicodic(resolvedUrl)
        } else {
            (context as? Activity)?.let { activity ->
                PipHelper.enterPip(activity)
            }
            runCatching { uriHandler.openUri(resolvedUrl) }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "タグ (${tags.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurfaceVariant
                )
                if (hasR18Tag) {
                    Text(
                        text = "R-18",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.error,
                        modifier = Modifier
                            .background(colors.error.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(13.dp),
                        strokeWidth = 2.dp,
                        color = colors.primary
                    )
                }
            }
            Text(
                text = "長押しで大百科・操作",
                fontSize = 11.sp,
                color = colors.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                VideoTagChip(
                    tag = tag,
                    onClick = { onTagClick(tag.name) },
                    onLongClick = { selectedTagForMenu = tag },
                    onNicodicClick = { openNicodicArticle(tag) }
                )
            }

            if (isEditable) {
                AddTagChip(
                    enabled = !isSubmitting,
                    onClick = { isAddDialogVisible = true }
                )
            }
        }
    }

    selectedTagForMenu?.let { targetTag ->
        VideoTagActionDialog(
            tag = targetTag,
            isEditable = isEditable,
            onDismiss = { selectedTagForMenu = null },
            onSearchTag = { onTagClick(targetTag.name) },
            onOpenNicodic = { openNicodicArticle(targetTag) },
            onRequestDelete = { onDeleteTag(targetTag.name) }
        )
    }

    if (isAddDialogVisible) {
        AddVideoTagDialog(
            onDismiss = { isAddDialogVisible = false },
            onConfirmAdd = onAddTag
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoTagChip(
    tag: VideoTag,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onNicodicClick: () -> Unit
) {
    val colors = HycoTheme.colors
    val isHyperOs = LocalAppThemeMode.current == AppThemeMode.HYPER_OS
    val shape = RoundedCornerShape(if (isHyperOs) 10.dp else 16.dp)
    val bgColor = if (tag.isLocked) colors.primary.copy(alpha = 0.11f) else colors.surfaceContainer
    val borderColor = if (tag.isLocked) {
        colors.primary.copy(alpha = 0.45f)
    } else {
        colors.outline.copy(alpha = 0.45f)
    }

    Surface(
        shape = shape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .clip(shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tag.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "ロックタグ",
                    tint = colors.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = tag.name,
                fontSize = 12.sp,
                fontWeight = if (tag.isLocked) FontWeight.SemiBold else FontWeight.Normal,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (tag.isNicodicArticleExists) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFB7282E))
                        .clickable(onClick = onNicodicClick)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "百",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTagChip(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = HycoTheme.colors
    val isHyperOs = LocalAppThemeMode.current == AppThemeMode.HYPER_OS
    val shape = RoundedCornerShape(if (isHyperOs) 10.dp else 16.dp)

    Surface(
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = if (enabled) 0.65f else 0.3f)),
        modifier = Modifier
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "タグを追加",
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "タグ追加",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
        }
    }
}
