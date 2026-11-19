package com.github.stm7128.hycohyco.ui.series

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.UserSeries
import com.github.stm7128.hycohyco.domain.model.VideoSortOrder
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.applySort
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import com.github.stm7128.hycohyco.ui.common.HycoShareUtils
import com.github.stm7128.hycohyco.ui.common.HycoUrlShareSheet
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoSortBar
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * URL/ディープリンク/検索バー/概要欄から直接指定されたシリーズを単体表示するボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailSheet(
    seriesId: Long,
    onDismissRequest: () -> Unit,
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    repo: CloudLibraryRepository = koinInject()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var series by remember { mutableStateOf<UserSeries?>(null) }
    var rawVideos by remember { mutableStateOf<List<VideoSummary>>(emptyList()) }
    var selectedSort by remember { mutableStateOf(VideoSortOrder.REGISTERED_AT_ASC) }
    var showShareSheet by remember { mutableStateOf(false) }

    fun loadSeries() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            repo.getSeriesDetail(seriesId).fold(
                onSuccess = { (loadedSeries, loadedVideos) ->
                    series = loadedSeries
                    rawVideos = loadedVideos
                    isLoading = false
                },
                onFailure = { err ->
                    // 詳細取得失敗時は動画一覧取得でフォールバック
                    repo.getSeriesVideos(seriesId).fold(
                        onSuccess = { fallbackVideos ->
                            series = UserSeries(
                                id = seriesId,
                                title = "シリーズ #$seriesId",
                                itemsCount = fallbackVideos.size
                            )
                            rawVideos = fallbackVideos
                            isLoading = false
                        },
                        onFailure = { fallbackErr ->
                            errorMessage = fallbackErr.localizedMessage ?: err.localizedMessage ?: "シリーズの取得に失敗しました"
                            isLoading = false
                        }
                    )
                }
            )
        }
    }

    LaunchedEffect(seriesId) {
        loadSeries()
    }

    val displayVideos = remember(rawVideos, selectedSort) {
        rawVideos.applySort(selectedSort)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // ヘッダー行 (タイトル、ID/件数、共有ボタン、閉じるボタン)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = series?.title ?: "シリーズ #$seriesId",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "series/$seriesId • 全 ${rawVideos.size} 本",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showShareSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "共有",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "閉じる",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // シリーズ説明文
            val description = series?.description.orEmpty().trim()
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ソートバー
            if (rawVideos.isNotEmpty()) {
                VideoSortBar(
                    selectedSort = selectedSort,
                    onSortSelected = { selectedSort = it },
                    sortOptions = VideoSortOrder.USER_VIDEO_SORT_ORDERS,
                    totalCount = rawVideos.size,
                    isWatchLater = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // コンテンツエリア
            when {
                isLoading && rawVideos.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }

                errorMessage != null && rawVideos.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "読み込みに失敗しました",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                            Button(onClick = { loadSeries() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("再試行")
                            }
                        }
                    }
                }

                displayVideos.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "シリーズに動画が登録されていません",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayVideos, key = { "${it.contentId}_${it.cloudItemId ?: 0L}" }) { video ->
                            VideoItemCard(
                                video = video,
                                onClick = {
                                    onDismissRequest()
                                    onVideoClick(video.contentId)
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    if (showShareSheet) {
        HycoUrlShareSheet(
            headerTitle = "シリーズを共有",
            title = series?.title ?: "シリーズ #$seriesId",
            url = HycoShareUtils.seriesUrl(seriesId),
            rawId = seriesId.toString(),
            idLabel = "シリーズID",
            onDismissRequest = { showShareSheet = false }
        )
    }
}
