package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.VideoSortOrder
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.applySort
import com.github.stm7128.hycohyco.ui.common.HycoShareUtils
import com.github.stm7128.hycohyco.ui.common.HycoUrlShareSheet
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoSortBar

/**
 * 公式マイリスト詳細（マイリスト内動画一覧）画面
 */
@Composable
fun CloudMylistDetailView(
    mylist: CloudMylist,
    videos: List<VideoSummary>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onVideoClick: (String) -> Unit,
    onDeleteVideo: ((String) -> Unit)? = null,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showShareSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
            }
            Text(
                text = mylist.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showShareSheet = true }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "共有",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isLoading && videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            }
        } else if (videos.isEmpty()) {
            HycoPullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "マイリストに動画はありません",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            var selectedSort by remember { mutableStateOf(VideoSortOrder.ADDED_AT_DESC) }
            val sortedVideos = remember(videos, selectedSort) {
                videos.applySort(selectedSort)
            }

            VideoSortBar(
                selectedSort = selectedSort,
                onSortSelected = { selectedSort = it },
                sortOptions = VideoSortOrder.MYLIST_SORT_ORDERS,
                totalCount = videos.size,
                isWatchLater = false
            )

            HycoPullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sortedVideos, key = { "${it.contentId}_${it.cloudItemId ?: 0L}" }) { video ->
                        VideoItemCard(
                            video = video,
                            onClick = { onVideoClick(video.contentId) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            trailingAction = onDeleteVideo?.let { onDelete ->
                                {
                                    IconButton(onClick = { onDelete(video.cloudItemId?.toString() ?: video.contentId) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "削除",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        HycoUrlShareSheet(
            headerTitle = "マイリストを共有",
            title = mylist.name,
            url = HycoShareUtils.mylistUrl(mylist.id),
            rawId = mylist.id.toString(),
            idLabel = "マイリストID",
            onDismissRequest = { showShareSheet = false }
        )
    }
}
