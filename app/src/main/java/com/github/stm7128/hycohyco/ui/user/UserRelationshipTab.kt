package com.github.stm7128.hycohyco.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.OwnerProfile
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard

/**
 * ユーザーのフォロー中クリエイタータブ (無限スクロール対応)
 */
@Composable
fun UserFolloweesTab(
    followees: List<OwnerProfile>,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onUserClick: (Long) -> Unit
) {
    UserRelationshipUsersTab(
        users = followees,
        emptyMessage = "フォロー中のユーザーはいません",
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        onLoadMore = onLoadMore,
        onUserClick = onUserClick
    )
}

/**
 * ユーザーのフォロワータブ (無限スクロール対応)
 */
@Composable
fun UserFollowersTab(
    followers: List<OwnerProfile>,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onUserClick: (Long) -> Unit
) {
    UserRelationshipUsersTab(
        users = followers,
        emptyMessage = "フォロワーはいません",
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        onLoadMore = onLoadMore,
        onUserClick = onUserClick
    )
}

/**
 * ユーザー一覧汎用タブ（フォロー中・フォロワー表示用・無限スクロール対応）
 */
@Composable
fun UserRelationshipUsersTab(
    users: List<OwnerProfile>,
    emptyMessage: String = "ユーザーはいません",
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onUserClick: (Long) -> Unit
) {
    if (users.isEmpty() && !isLoadingMore) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        val listState = rememberLazyListState()

        val shouldLoadMore = remember {
            derivedStateOf {
                val total = listState.layoutInfo.totalItemsCount
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                total > 0 && lastVisible >= total - 3
            }
        }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value && hasMore && !isLoadingMore) {
                onLoadMore()
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users, key = { it.id }) { user ->
                HycoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUserClick(user.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user.iconUrl.isNotBlank()) {
                            HycoAsyncImage(
                                url = user.iconUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = user.nickname, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (user.followerCount > 0) {
                                Text(
                                    text = "フォロワー: ${user.followerCount} 人",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (user.description.isNotBlank()) {
                                Text(
                                    text = user.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}
