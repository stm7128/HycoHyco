package com.github.stm7128.hycohyco.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard

/**
 * ホーム画面各タブで共通利用するヘッダー更新ボタン
 * （読み込み中はスピナー表示し、通常時はリフレッシュアイコンを表示）
 */
@Composable
internal fun HomeRefreshButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HycoTheme.colors
    IconButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.size(36.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = colors.primary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "更新",
                tint = colors.onBackgroundVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * ホーム画面「フォロー新着」タブ表示ビュー
 */
@Composable
fun HomeFollowFeedView(
    isUserLoggedIn: Boolean,
    followFeed: List<VideoSummary>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onOpenShort: (String, List<VideoSummary>) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HycoTheme.colors

    if (!isUserLoggedIn) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "設定画面からログインすると、フォロー中の投稿者の新着動画タイムラインが表示されます",
                color = colors.onBackgroundVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (followFeed.isNotEmpty()) {
                    "フォロー新着 (${followFeed.size}件)"
                } else {
                    "フォロー新着タイムライン"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
            HomeRefreshButton(
                isLoading = isLoading,
                onClick = onRefresh
            )
        }

        HycoPullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            when {
                isLoading && followFeed.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }

                errorMessage != null && followFeed.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onRefresh) { Text("再読み込み") }
                            }
                        }
                    }
                }

                followFeed.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "フォロー中の投稿者の新着動画はありません",
                                    color = colors.onBackgroundVariant,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onRefresh) { Text("更新する") }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(followFeed, key = { it.contentId }) { video ->
                            VideoItemCard(
                                video = video,
                                onClick = {
                                    if (video.isShort) {
                                        onOpenShort(video.contentId, followFeed.filter { it.isShort })
                                    } else {
                                        onPlayVideo(video.contentId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
