package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.CloudLikeHistoryItem
import com.github.stm7128.hycohyco.domain.model.CloudWatchHistoryItem
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import com.github.stm7128.hycohyco.ui.player.formatDuration

/**
 * クラウド視聴履歴一覧ビュー (/v2/users/me/watch/history)
 */
@Composable
fun CloudWatchHistoryView(
    items: List<CloudWatchHistoryItem>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPlayVideo: (String, Long?) -> Unit,
    onDeleteItem: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    CloudHistoryStateScaffold(
        isEmpty = items.isEmpty(),
        isLoading = isLoading,
        errorMessage = errorMessage,
        emptyText = "アカウントの視聴履歴はありません",
        onRetry = onRefresh,
        modifier = modifier
    ) {
        val listState = rememberLazyListState()
        EndlessScrollEffect(listState = listState, hasMore = hasMore, isLoadingMore = isLoadingMore, onLoadMore = onLoadMore)

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "アカウント視聴履歴 (${items.size}件${if (hasMore) "+" else ""})",
                    fontSize = 12.sp,
                    color = HycoTheme.colors.onBackgroundVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新", tint = HycoTheme.colors.onBackgroundVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { showClearDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "全消去", tint = HycoTheme.colors.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            HycoPullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.itemId }) { item ->
                        val posMs = (item.playbackPositionSec * 1000L).takeIf { item.playbackPositionSec > 5L }
                        val durMs = item.video.lengthSeconds * 1000L
                        val progressText = if (item.playbackPositionSec > 0L && durMs > 0L) {
                            "${formatDuration(item.playbackPositionSec * 1000L)} / ${formatDuration(durMs)}"
                        } else null

                        VideoItemCard(
                            video = item.video.copy(startTime = item.viewedAt.ifBlank { item.video.startTime }),
                            onClick = { onPlayVideo(item.video.contentId, posMs) },
                            progressFraction = item.progressFraction.takeIf { it > 0f },
                            progressDurationText = progressText,
                            trailingAction = {
                                IconButton(onClick = { onDeleteItem(item.video.contentId) }, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "履歴から削除", tint = HycoTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                    }
                    pagingFooterItem(isLoadingMore = isLoadingMore, hasMore = hasMore, onLoadMore = onLoadMore)
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("アカウント視聴履歴を全削除") },
            text = { Text("ニコニコアカウント上の視聴履歴をすべて削除しますか？この操作は元に戻せません。") },
            confirmButton = {
                TextButton(onClick = { showClearDialog = false; onClearAll() }) {
                    Text("すべて削除", color = HycoTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("キャンセル") }
            }
        )
    }
}

/**
 * いいね！履歴一覧ビュー (/v1/users/me/likes)
 */
@Composable
fun CloudLikeHistoryView(
    items: List<CloudLikeHistoryItem>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPlayVideo: (String, Long?) -> Unit,
    onUnlikeVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHyperOs = LocalAppThemeMode.current == AppThemeMode.HYPER_OS
    var unlikeTarget by remember { mutableStateOf<CloudLikeHistoryItem?>(null) }

    CloudHistoryStateScaffold(
        isEmpty = items.isEmpty(),
        isLoading = isLoading,
        errorMessage = errorMessage,
        emptyText = "「いいね！」した動画はありません",
        onRetry = onRefresh,
        modifier = modifier
    ) {
        val listState = rememberLazyListState()
        EndlessScrollEffect(listState = listState, hasMore = hasMore, isLoadingMore = isLoadingMore, onLoadMore = onLoadMore)

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("いいね！履歴 (${items.size}件${if (hasMore) "+" else ""})", fontSize = 12.sp, color = HycoTheme.colors.onBackgroundVariant)
                IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "更新", tint = HycoTheme.colors.onBackgroundVariant, modifier = Modifier.size(18.dp))
                }
            }

            HycoPullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.video.contentId }) { item ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            VideoItemCard(
                                video = item.video.copy(startTime = item.likedAt.ifBlank { item.video.startTime }),
                                onClick = { onPlayVideo(item.video.contentId, null) },
                                trailingAction = {
                                    IconButton(onClick = { unlikeTarget = item }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Default.Favorite, contentDescription = "いいね！解除", tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
                                    }
                                }
                            )
                            // お礼メッセージが存在する場合はカード直下に表示
                            if (!item.thanksMessage.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    shape = RoundedCornerShape(if (isHyperOs) 14.dp else 10.dp),
                                    color = Color(0xFFE91E63).copy(alpha = 0.08f),
                                    border = BorderStroke(0.5.dp, Color(0xFFE91E63).copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("投稿者のお礼メッセージ", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                                            Text(item.thanksMessage, fontSize = 12.sp, color = HycoTheme.colors.onSurface, lineHeight = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    pagingFooterItem(isLoadingMore = isLoadingMore, hasMore = hasMore, onLoadMore = onLoadMore)
                }
            }
        }
    }

    unlikeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { unlikeTarget = null },
            title = { Text("「いいね！」を解除") },
            text = { Text("「${target.video.title}」のいいね！を取り消しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    val vid = target.video.contentId
                    unlikeTarget = null
                    onUnlikeVideo(vid)
                }) { Text("解除する", color = HycoTheme.colors.error) }
            },
            dismissButton = { TextButton(onClick = { unlikeTarget = null }) { Text("キャンセル") } }
        )
    }
}
