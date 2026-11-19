package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard

/**
 * 1件のダウンロードアイテムカード
 */
@Composable
fun DownloadItemCard(
    item: DownloadedVideoItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAudio = item.downloadType == DownloadType.AUDIO_ONLY_AND_DANMAKU

    HycoCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // サムネイル / アイコン
                Box(
                    modifier = Modifier
                        .size(width = 88.dp, height = 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HycoTheme.colors.surfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.thumbnailUrl.isNotBlank()) {
                        HycoAsyncImage(
                            url = item.thumbnailUrl,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (isAudio) Icons.Default.MusicNote else Icons.Default.Videocam,
                            contentDescription = null,
                            tint = HycoTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 再生時間バッジ
                    if (item.durationMs > 0L) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(3.dp)
                                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = item.formattedDuration,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // タイトル & メタデータ
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HycoTheme.colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 種別バッジ
                        Box(
                            modifier = Modifier
                                .background(
                                    HycoTheme.colors.primary.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isAudio) "音声+弾幕" else "動画+弾幕",
                                fontSize = 10.sp,
                                color = HycoTheme.colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // ステータスまたは容量
                        if (item.status == DownloadStatus.COMPLETED) {
                            Text(
                                text = item.formattedTotalSize,
                                fontSize = 11.sp,
                                color = HycoTheme.colors.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = item.status.displayName,
                                fontSize = 11.sp,
                                color = if (item.status == DownloadStatus.FAILED) HycoTheme.colors.error else HycoTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }

                // 削除ボタン
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "削除",
                        tint = HycoTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // ダウンロード中の進捗バー
            if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.QUEUED) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { (item.progressPercent.coerceIn(0, 100)) / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = HycoTheme.colors.primary,
                        trackColor = HycoTheme.colors.surfaceContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.progressPercent}%",
                        fontSize = 11.sp,
                        color = HycoTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
