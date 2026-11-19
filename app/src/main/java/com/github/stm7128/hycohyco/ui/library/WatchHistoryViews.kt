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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.WatchHistoryItem
import com.github.stm7128.hycohyco.domain.util.DateTimeFormatters
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import com.github.stm7128.hycohyco.ui.player.formatDuration

/**
 * 視聴履歴一覧表示ビュー
 */
@Composable
fun HistoryListView(
    historyItems: List<WatchHistoryItem>,
    onPlayVideo: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (historyItems.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "視聴履歴はありません",
                color = HycoTheme.colors.onBackgroundVariant,
                fontSize = 14.sp
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
            Text("${historyItems.size} 件の履歴", fontSize = 12.sp, color = HycoTheme.colors.onBackgroundVariant)
            IconButton(onClick = { showClearDialog = true }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "全消去",
                    tint = HycoTheme.colors.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(historyItems, key = { it.videoId }) { item ->
                VideoItemCard(
                    video = item.toVideoSummary(),
                    onClick = { onPlayVideo(item.videoId) },
                    progressFraction = item.progressRatio,
                    progressDurationText = "${formatDuration(item.lastPositionMs)} / ${formatDuration(item.durationMs)}",
                    trailingAction = {
                        IconButton(
                            onClick = { onDeleteHistory(item.videoId) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "削除",
                                tint = HycoTheme.colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("視聴履歴を全消去") },
            text = { Text("端末内の視聴履歴をすべて削除しますか？この操作は元に戻せません。") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearAll()
                }) {
                    Text("すべて削除", color = HycoTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

/**
 * 視聴履歴モデルを統一カード用の VideoSummary に変換
 */
fun WatchHistoryItem.toVideoSummary(): VideoSummary {
    val dateText = DateTimeFormatters.formatEpochMillisToDate(lastWatchedAt)

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
