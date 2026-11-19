package com.github.stm7128.hycohyco.ui.library

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.PlaylistVideoItem
import com.github.stm7128.hycohyco.domain.model.VideoSortOrder
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.applySort
import com.github.stm7128.hycohyco.domain.util.DateTimeFormatters
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoSortBar

/**
 * ローカルプレイリスト詳細（動画一覧 & 連続再生）画面
 */
@Composable
fun PlaylistDetailView(
    playlistTitle: String,
    videos: List<PlaylistVideoItem>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onPlayShuffle: () -> Unit,
    onPlayItem: (Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSort by remember { mutableStateOf(VideoSortOrder.ADDED_AT_DESC) }

    // PlaylistVideoItem を VideoSummary に変換しつつソート適用
    val summaryList = remember(videos) {
        videos.map { it.toVideoSummary() }
    }
    val sortedSummaries = remember(summaryList, selectedSort) {
        summaryList.applySort(selectedSort)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // トップバー
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "戻る",
                    tint = HycoTheme.colors.onBackground
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlistTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HycoTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${videos.size} 件の動画",
                    fontSize = 12.sp,
                    color = HycoTheme.colors.onBackgroundVariant
                )
            }
        }

        // 再生コントロールボタン (全曲連続再生 / シャッフル再生)
        if (videos.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HycoButton(text = "▶ 全曲再生", onClick = onPlayAll, modifier = Modifier.weight(1f))
                HycoButton(text = "🔀 シャッフル", onClick = onPlayShuffle, modifier = Modifier.weight(1f))
            }

            // ソートバー (公式マイリスト準拠の全項目ソート)
            VideoSortBar(
                selectedSort = selectedSort,
                onSortSelected = { selectedSort = it },
                sortOptions = VideoSortOrder.MYLIST_SORT_ORDERS,
                totalCount = sortedSummaries.size,
                isWatchLater = false
            )
        }

        if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "このプレイリストにはまだ動画がありません",
                    color = HycoTheme.colors.onBackgroundVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(sortedSummaries, key = { _, video -> video.contentId }) { _, video ->
                    val originalIndex = videos.indexOfFirst { it.videoId == video.contentId }.coerceAtLeast(0)
                    VideoItemCard(
                        video = video,
                        onClick = { onPlayItem(originalIndex) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        trailingAction = {
                            IconButton(
                                onClick = { onRemoveItem(video.contentId) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "削除",
                                    tint = HycoTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * プレイリスト動画モデルを統一カード用の VideoSummary に変換
 */
fun PlaylistVideoItem.toVideoSummary(): VideoSummary {
    val dateText = DateTimeFormatters.formatEpochMillisToDate(addedAt)

    return VideoSummary(
        contentId = videoId,
        title = title,
        viewCounter = viewCounter,
        commentCounter = commentCounter,
        likeCounter = likeCounter,
        mylistCounter = mylistCounter,
        lengthSeconds = (durationMs / 1000).toInt(),
        thumbnailUrl = thumbnailUrl,
        startTime = dateText,
        ownerName = ownerName
    )
}
