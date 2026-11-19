package com.github.stm7128.hycohyco.ui.shorts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoTagChipGroup
import com.github.stm7128.hycohyco.ui.player.DescriptionLinkHandlers
import com.github.stm7128.hycohyco.ui.player.DescriptionLinkParser

/**
 * ショート動画のタイトル全文・統計情報・概要欄・オフライン保存操作ボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsDetailSheet(
    detail: VideoDetail,
    isOwnerFollowed: Boolean,
    downloadItem: DownloadedVideoItem?,
    onDismiss: () -> Unit,
    onToggleFollow: () -> Unit,
    onOpenOwnerProfile: (Long, String) -> Unit,
    onPlayLinkedVideo: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onDownload: (DownloadType) -> Unit,
    isTagSubmitting: Boolean = false,
    onTagClick: (String) -> Unit = {},
    onAddTag: (String) -> Unit = {},
    onDeleteTag: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val colors = HycoTheme.colors
    val uriHandler = LocalUriHandler.current

    val annotatedDescription = remember(detail.description) {
        DescriptionLinkParser.parseDescription(
            rawHtml = detail.description,
            handlers = DescriptionLinkHandlers(
                onPlayVideo = { id ->
                    onDismiss()
                    onPlayLinkedVideo(id)
                },
                onSeekTo = { ms ->
                    onSeekTo(ms)
                    onDismiss()
                },
                onUserClick = { uid ->
                    onDismiss()
                    onOpenOwnerProfile(uid, "")
                },
                onOpenUrl = { url -> runCatching { uriHandler.openUri(url) } }
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ショート動画の詳細・概要",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "閉じる", tint = colors.onSurfaceVariant)
                }
            }

            // タイトル全文 + メタ情報
            Text(
                text = detail.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                lineHeight = 22.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = detail.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                if (detail.registeredAt.isNotBlank()) {
                    Text(
                        text = detail.registeredAt.replace("T", " ").substringBefore("+").substringBefore("Z").take(16),
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                if (detail.videoWidth > 0 && detail.videoHeight > 0) {
                    Text(
                        text = "${detail.videoWidth}×${detail.videoHeight}",
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // 4大指標
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailStatBadge(Icons.Default.PlayArrow, formatCompactNumber(detail.viewCount), colors.onSurfaceVariant)
                DetailStatBadge(Icons.AutoMirrored.Filled.Comment, formatCompactNumber(detail.commentCount), colors.onSurfaceVariant)
                DetailStatBadge(Icons.Default.Favorite, formatCompactNumber(detail.likeCount), colors.onSurfaceVariant)
                DetailStatBadge(Icons.Default.Folder, formatCompactNumber(detail.mylistCount), colors.onSurfaceVariant)
            }

            // 投稿者カード
            HycoCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = if (detail.ownerId > 0L) {
                    {
                        onDismiss()
                        onOpenOwnerProfile(detail.ownerId, detail.ownerName)
                    }
                } else null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HycoAsyncImage(
                        url = detail.ownerIconUrl,
                        contentDescription = detail.ownerName,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = detail.ownerName.ifBlank { "投稿者不明" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                        if (detail.ownerId > 0L) {
                            Text(text = "ID: ${detail.ownerId}", fontSize = 11.sp, color = colors.onSurfaceVariant)
                        }
                    }
                    if (detail.ownerId > 0L) {
                        HycoButton(
                            text = if (isOwnerFollowed) "フォロー中" else "フォロー",
                            onClick = onToggleFollow
                        )
                    }
                }
            }

            // オフライン保存カード
            ShortDetailDownloadCard(
                downloadItem = downloadItem,
                onDownload = onDownload
            )

            // ピン留め本編動画
            val baseVideo = detail.baseVideo
            if (baseVideo != null && baseVideo.videoId.isNotBlank()) {
                HycoCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDismiss()
                        onPlayLinkedVideo(baseVideo.videoId)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "ピン留め本編動画を開く", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                            Text(
                                text = baseVideo.title.ifBlank { baseVideo.videoId },
                                fontSize = 13.sp,
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }

            // タグ一覧（タップでショートタグ検索・長押しで大百科/削除・＋追加）
            VideoTagChipGroup(
                tags = detail.tags,
                isEditable = detail.isTagEditable,
                isSubmitting = isTagSubmitting,
                hasR18Tag = detail.hasR18Tag,
                onTagClick = { tag ->
                    onDismiss()
                    onTagClick(tag)
                },
                onAddTag = onAddTag,
                onDeleteTag = onDeleteTag
            )

            HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

            // 概要欄本文 (HTML装飾 + インラインリンク直タップ)
            Text(text = "概要欄", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.onSurfaceVariant)
            if (annotatedDescription.text.isBlank()) {
                Text(text = "概要欄はありません", fontSize = 13.sp, color = colors.onSurfaceVariant)
            } else {
                Text(
                    text = annotatedDescription,
                    color = colors.onSurface,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun DetailStatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = text, fontSize = 12.sp, color = color)
    }
}
