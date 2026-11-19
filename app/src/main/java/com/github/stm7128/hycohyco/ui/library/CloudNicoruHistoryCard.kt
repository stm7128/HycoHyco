package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.NicoruHistoryItem
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.hycoBounceClick
import com.github.stm7128.hycohyco.ui.player.formatDuration

/**
 * ニコられた / ニコった 履歴1件分のカードコンポーネント
 */
@Composable
internal fun NicoruHistoryCard(
    item: NicoruHistoryItem,
    isPassive: Boolean,
    onPlayVideo: () -> Unit,
    onPlayAtCommentTime: () -> Unit,
    onDeleteClick: (() -> Unit)?
) {
    val isHyperOs = LocalAppThemeMode.current == AppThemeMode.HYPER_OS
    val cardRadius = if (isHyperOs) 18.dp else 12.dp
    val cardBg = if (isHyperOs) HycoTheme.colors.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val cardBorder = if (isHyperOs) {
        BorderStroke(0.5.dp, HycoTheme.colors.outline.copy(alpha = 0.25f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    }
    val gold = Color(0xFFFFB300)

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cardRadius)).hycoBounceClick(onClick = onPlayAtCommentTime),
        shape = RoundedCornerShape(cardRadius),
        color = cardBg,
        border = cardBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            if (isPassive && (item.isVideoOwnerNicoru || item.isFeatured)) {
                Row(
                    modifier = Modifier.padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isVideoOwnerNicoru) {
                        val ownerLabel = if (item.video.ownerName.isNotBlank()) "投稿者 ${item.video.ownerName} さんがニコる" else "動画投稿者がニコる"
                        StatusPill(text = ownerLabel, color = Color(0xFFE91E63))
                    }
                    if (item.isFeatured) {
                        StatusPill(text = "注目コメント!", color = gold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.commentBody,
                    color = HycoTheme.colors.onSurface,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = gold.copy(alpha = 0.16f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SentimentSatisfiedAlt, contentDescription = null, tint = gold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${item.nicoruCount}", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "履歴から削除", tint = HycoTheme.colors.onSurfaceVariant, modifier = Modifier.size(17.dp))
                    }
                }
            }

            val dateParts = buildList {
                if (item.formattedCreatedDate.isNotBlank()) add("${if (isPassive) "ニコられた日" else "ニコった日"}: ${item.formattedCreatedDate}")
                if (isPassive && item.formattedCommentDate.isNotBlank()) add("投稿: ${item.formattedCommentDate}")
            }
            if (dateParts.isNotEmpty()) {
                Text(
                    text = dateParts.joinToString(" • "),
                    fontSize = 11.sp,
                    color = HycoTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HycoTheme.colors.surface.copy(alpha = 0.65f))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HycoAsyncImage(
                    url = item.video.thumbnailUrl,
                    contentDescription = item.video.title,
                    modifier = Modifier
                        .width(80.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onPlayVideo)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.video.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = HycoTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(onClick = onPlayVideo)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HycoTheme.colors.primary.copy(alpha = 0.14f),
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onPlayAtCommentTime)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = HycoTheme.colors.primary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "コメント時間 (${formatDuration(item.commentVposMs)}) から再生",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HycoTheme.colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.14f)) {
        Row(modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = text, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
