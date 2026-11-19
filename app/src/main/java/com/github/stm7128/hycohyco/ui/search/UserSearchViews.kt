package com.github.stm7128.hycohyco.ui.search

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * ユーザー検索結果一覧リスト
 */
@Composable
fun UserSearchResultList(
    users: List<OwnerProfile>,
    onUserClick: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (users.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("該当するユーザーが見つかりませんでした", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users, key = { it.id }) { user ->
                UserSearchItemCard(user = user, onClick = { onUserClick(user.id, user.nickname) })
            }
        }
    }
}

/**
 * ユーザー検索結果アイテムカード
 */
@Composable
fun UserSearchItemCard(
    user: OwnerProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HycoCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (user.iconUrl.isNotBlank()) {
                HycoAsyncImage(
                    url = user.iconUrl,
                    contentDescription = "アイコン",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nickname,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${user.id}" + if (user.followerCount > 0) " • フォロワー: ${user.followerCount}" else "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (user.description.isNotBlank()) {
                    Text(
                        text = user.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
