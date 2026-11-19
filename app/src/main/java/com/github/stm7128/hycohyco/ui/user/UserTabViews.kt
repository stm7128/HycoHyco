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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.UserSeries
import com.github.stm7128.hycohyco.domain.model.VideoSortOrder
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.applySort
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoSortBar

/**
 * ユーザー投稿動画タブ (無限スクロール対応)
 */
@Composable
fun UserVideosTab(
    videos: List<VideoSummary>,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onVideoClick: (String) -> Unit
) {
    if (videos.isEmpty() && !isLoadingMore) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("投稿動画がありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        var selectedSort by remember { mutableStateOf(VideoSortOrder.REGISTERED_AT_DESC) }
        val sortedVideos = remember(videos, selectedSort) {
            videos.applySort(selectedSort)
        }
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

        Column(modifier = Modifier.fillMaxSize()) {
            VideoSortBar(
                selectedSort = selectedSort,
                onSortSelected = { selectedSort = it },
                sortOptions = VideoSortOrder.USER_VIDEO_SORT_ORDERS,
                totalCount = videos.size
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedVideos, key = { it.contentId }) { video ->
                    VideoItemCard(video = video, onClick = { onVideoClick(video.contentId) })
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
}

/**
 * ユーザー作成シリーズタブ
 */
@Composable
fun UserSeriesTab(
    seriesList: List<UserSeries>,
    onSeriesClick: (UserSeries) -> Unit = {}
) {
    if (seriesList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("シリーズがありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(seriesList, key = { it.id }) { series ->
                HycoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSeriesClick(series) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = series.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${series.itemsCount} 本の動画", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ユーザー公開マイリストタブ
 */
@Composable
fun UserMylistsTab(
    mylists: List<CloudMylist>,
    onMylistClick: (CloudMylist) -> Unit = {}
) {
    if (mylists.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("公開マイリストがありません", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mylists, key = { it.id }) { mylist ->
                HycoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMylistClick(mylist) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = mylist.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${mylist.itemsCount} 件の動画", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
