package com.github.stm7128.hycohyco.ui.shorts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard

/**
 * ショート詳細シート内のオフライン保存操作カード
 */
@Composable
internal fun ShortDetailDownloadCard(
    downloadItem: DownloadedVideoItem?,
    onDownload: (DownloadType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HycoTheme.colors
    HycoCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val statusLabel = when (downloadItem?.status) {
                DownloadStatus.COMPLETED -> "オフライン保存済み（ライブラリ＞ダウンロード）"
                DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> "ダウンロード進行中 (${downloadItem.progressPercent}%)"
                DownloadStatus.FAILED -> "前回の保存に失敗しました（再試行できます）"
                else -> "端末に映像・弾幕を保存してオフライン再生"
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (downloadItem?.status == DownloadStatus.COMPLETED) {
                        Icons.Default.DownloadDone
                    } else {
                        Icons.Default.Download
                    },
                    contentDescription = null,
                    tint = if (downloadItem?.status == DownloadStatus.COMPLETED) Color(0xFF10B981) else colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "オフラインダウンロード",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }
            Text(text = statusLabel, fontSize = 12.sp, color = colors.onSurfaceVariant)
            if (downloadItem?.status != DownloadStatus.COMPLETED &&
                downloadItem?.status != DownloadStatus.DOWNLOADING &&
                downloadItem?.status != DownloadStatus.QUEUED
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HycoButton(
                        text = "動画＋弾幕を保存",
                        onClick = { onDownload(DownloadType.VIDEO_AND_DANMAKU) },
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = colors.surfaceContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onDownload(DownloadType.AUDIO_ONLY_AND_DANMAKU) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AudioFile,
                                contentDescription = null,
                                tint = colors.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "音声のみ", fontSize = 12.sp, color = colors.onSurface)
                        }
                    }
                }
            }
        }
    }
}
