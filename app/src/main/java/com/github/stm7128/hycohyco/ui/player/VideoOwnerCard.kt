package com.github.stm7128.hycohyco.ui.player

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard

/**
 * 視聴画面の投稿者カード（アイコン・ニックネーム・ユーザーID・フォローボタン）
 */
@Composable
fun VideoOwnerCard(
    detail: VideoDetail,
    isFollowed: Boolean,
    isFollowSubmitting: Boolean,
    onToggleFollow: () -> Unit,
    onOwnerClick: (Long, String) -> Unit,
    textColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    HycoCard(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            if (detail.ownerId > 0L) {
                onOwnerClick(detail.ownerId, detail.ownerName)
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 投稿者アイコン
            if (detail.ownerIconUrl.isNotBlank()) {
                HycoAsyncImage(
                    url = detail.ownerIconUrl,
                    contentDescription = "投稿者アイコン",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "投稿者",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ニックネーム & ユーザーID
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = detail.ownerName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (detail.ownerId > 0L) {
                    Text(
                        text = "ユーザーID: ${detail.ownerId}",
                        fontSize = 12.sp,
                        color = secondaryColor,
                        maxLines = 1
                    )
                }
            }

            // 右側フォローボタン（ownerId が存在する場合のみ表示）
            if (detail.ownerId > 0L) {
                Spacer(modifier = Modifier.width(8.dp))
                FollowButton(
                    isFollowed = isFollowed,
                    isSubmitting = isFollowSubmitting,
                    onClick = onToggleFollow
                )
            }
        }
    }
}

/**
 * 投稿者カード右端のフォローボタン
 */
@Composable
private fun FollowButton(
    isFollowed: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val buttonBackground = if (isFollowed) {
        HycoTheme.colors.surfaceContainer
    } else {
        primaryColor
    }
    val contentColor = if (isFollowed) {
        HycoTheme.colors.onSurface
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Surface(
        onClick = onClick,
        enabled = !isSubmitting,
        shape = RoundedCornerShape(20.dp),
        color = buttonBackground,
        contentColor = contentColor,
        shadowElevation = if (isFollowed) 0.dp else 1.dp,
        modifier = modifier
            .animateContentSize()
            .height(34.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else if (isFollowed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = if (isFollowed) "フォロー中" else "フォロー",
                fontSize = 13.sp,
                fontWeight = if (isFollowed) FontWeight.Medium else FontWeight.Bold
            )
        }
    }
}
