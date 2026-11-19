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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme

/**
 * オフラインダウンロード一覧画面
 */
@Composable
fun DownloadListView(
    downloads: List<DownloadedVideoItem>,
    totalSizeBytes: Long,
    onPlayVideo: (String) -> Unit,
    onDeleteDownload: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSizeText = DownloadedVideoItem.formatFileSize(totalSizeBytes)

    if (downloads.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DownloadDone,
                    contentDescription = null,
                    tint = HycoTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ダウンロード済みの動画はありません",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = HycoTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "動画再生中の⚙設定シートから、\n120fps弾幕付きでオフライン保存できます",
                    fontSize = 12.sp,
                    color = HycoTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        ) {
            // ストレージ使用量サマリー
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "保存済み (${downloads.size}件)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HycoTheme.colors.onBackground
                    )
                    Text(
                        text = "使用容量: $totalSizeText",
                        fontSize = 12.sp,
                        color = HycoTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(downloads, key = { it.videoId }) { item ->
                DownloadItemCard(
                    item = item,
                    onClick = { onPlayVideo(item.videoId) },
                    onDelete = { onDeleteDownload(item.videoId) }
                )
            }
        }
    }
}

