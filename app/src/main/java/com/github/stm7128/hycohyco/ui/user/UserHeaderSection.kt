package com.github.stm7128.hycohyco.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.OwnerProfile
import com.github.stm7128.hycohyco.ui.common.HycoShareUtils
import com.github.stm7128.hycohyco.ui.common.HycoUrlShareSheet
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.player.DescriptionLinkHandlers
import com.github.stm7128.hycohyco.ui.player.DescriptionLinkParser

/**
 * ユーザー詳細画面のプロフィールヘッダー
 */
@Composable
fun UserHeaderSection(
    userId: Long,
    profile: OwnerProfile?,
    fallbackName: String,
    followeesCount: Int,
    followersCount: Int,
    onClose: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onFolloweesClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onUserClick: (Long) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDescExpanded by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val icon = profile?.iconUrl.orEmpty()
                if (icon.isNotBlank()) {
                    HycoAsyncImage(
                        url = icon,
                        contentDescription = "ユーザーアイコン",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val displayName = profile?.nickname?.takeIf { it.isNotBlank() }
                        ?: fallbackName.takeIf { it.isNotBlank() }
                        ?: "ユーザー $userId"
                    Text(
                        text = displayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ユーザーID: $userId",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayFollowerCount = if ((profile?.followerCount ?: 0L) > 0L) {
                            profile?.followerCount
                        } else if (followersCount > 0) {
                            followersCount.toLong()
                        } else null

                        if (displayFollowerCount != null) {
                            Text(
                                text = "フォロワー: ${displayFollowerCount}人 ›",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onFollowersClick() }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = "フォロー中: ${followeesCount}人 ›",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onFolloweesClick() }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showShareSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "共有",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "閉じる")
                }
            }
        }

        if (profile?.description?.isNotBlank() == true) {
            val annotated = remember(profile.description) {
                DescriptionLinkParser.parseDescription(
                    rawHtml = profile.description,
                    handlers = DescriptionLinkHandlers(
                        onPlayVideo = onPlayVideo,
                        onUserClick = onUserClick,
                        onOpenUrl = onOpenUrl
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = annotated,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isDescExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isDescExpanded = !isDescExpanded }
            )
        }
    }

    if (showShareSheet) {
        val displayName = profile?.nickname?.takeIf { it.isNotBlank() }
            ?: fallbackName.takeIf { it.isNotBlank() }
            ?: "ユーザー $userId"
        HycoUrlShareSheet(
            headerTitle = "ユーザーを共有",
            title = displayName,
            url = HycoShareUtils.userUrl(userId),
            rawId = userId.toString(),
            idLabel = "ユーザーID",
            onDismissRequest = { showShareSheet = false }
        )
    }
}
