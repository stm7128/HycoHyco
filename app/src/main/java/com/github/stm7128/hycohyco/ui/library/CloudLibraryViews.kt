package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WatchLater
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.VideoSortOrder
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.applySort
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoSortBar

/**
 * 公式「あとで見る」一覧表示
 */
@Composable
fun WatchLaterListView(
    videos: List<VideoSummary>,
    isLoading: Boolean,
    onVideoClick: (String) -> Unit,
    onDeleteVideo: ((String) -> Unit)? = null,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isLoading && videos.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        }
        return
    }

    if (videos.isEmpty()) {
        HycoPullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WatchLater,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "「あとで見る」に動画はありません",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        return
    }

    var selectedSort by remember { mutableStateOf(VideoSortOrder.ADDED_AT_DESC) }
    val sortedVideos = remember(videos, selectedSort) {
        videos.applySort(selectedSort)
    }

    Column(modifier = modifier.fillMaxSize()) {
        VideoSortBar(
            selectedSort = selectedSort,
            onSortSelected = { selectedSort = it },
            sortOptions = VideoSortOrder.MYLIST_SORT_ORDERS,
            totalCount = videos.size,
            isWatchLater = true
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

/**
 * 公式マイリスト一覧表示
 */
@Composable
fun CloudMylistListView(
    mylists: List<CloudMylist>,
    isLoading: Boolean,
    onMylistClick: (CloudMylist) -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isLoading && mylists.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        }
        return
    }

    if (mylists.isEmpty()) {
        HycoPullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "公式マイリストがありません",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
        return
    }

    HycoPullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(mylists, key = { it.id }) { mylist ->
            HycoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { onMylistClick(mylist) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mylist.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!mylist.isPublic) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "非公開",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        if (mylist.description.isNotBlank()) {
                            Text(
                                text = mylist.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "${mylist.itemsCount} 本の動画",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        }
    }
}

