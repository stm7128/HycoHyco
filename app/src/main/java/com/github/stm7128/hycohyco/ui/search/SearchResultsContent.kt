package com.github.stm7128.hycohyco.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.SearchHistoryItem
import com.github.stm7128.hycohyco.domain.model.SearchTarget
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard

/**
 * 検索画面のコンテンツ領域（初期履歴・ローディング・空/エラー・動画/ユーザー結果＆Pull-to-Refresh）
 */
@Composable
internal fun SearchResultsContent(
    uiState: SearchUiState,
    isRefreshing: Boolean,
    target: SearchTarget,
    searchHistory: List<SearchHistoryItem>,
    onSelectKeyword: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onSelectPopularTag: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onOpenShort: (String, List<VideoSummary>) -> Unit,
    onUserClick: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (uiState) {
            is SearchUiState.Idle -> {
                SearchHistorySection(
                    searchHistory = searchHistory,
                    onSelectKeyword = onSelectKeyword,
                    onClearAllHistory = onClearAllHistory,
                    onSelectPopularTag = onSelectPopularTag,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            is SearchUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is SearchUiState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "該当する動画が見つかりませんでした",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            is SearchUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onRetry) { Text("再試行") }
                }
            }

            is SearchUiState.Success -> {
                val listState = rememberLazyListState()
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val totalItems = listState.layoutInfo.totalItemsCount
                        totalItems > 0 && lastVisible >= totalItems - 3
                    }
                }
                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore && uiState.hasMore && !uiState.isLoadingMore) {
                        onLoadMore()
                    }
                }

                HycoPullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.videos, key = { it.contentId }) { video ->
                            VideoItemCard(
                                video = video,
                                onClick = {
                                    if (target == SearchTarget.SHORTS || video.isShort) {
                                        onOpenShort(video.contentId, uiState.videos)
                                    } else {
                                        onPlayVideo(video.contentId)
                                    }
                                }
                            )
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                                }
                            }
                        } else if (uiState.hasMore) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = onLoadMore) {
                                        Text("さらに読み込む", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is SearchUiState.UserSuccess -> {
                HycoPullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    UserSearchResultList(
                        users = uiState.users,
                        onUserClick = onUserClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
