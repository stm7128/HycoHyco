package com.github.stm7128.hycohyco.ui.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage

/**
 * ショートプレイヤー画面のオーバーレイコントロール（右側縦アクション・下部タイトル・ピン留め本編動画誘導・シークバー）
 */
@Composable
fun ShortsOverlayControls(
    item: VideoSummary,
    detail: VideoDetail?,
    isOverlayMode: Boolean,
    isDanmakuEnabled: Boolean,
    isLiked: Boolean,
    likeCount: Long,
    isOwnerFollowed: Boolean,
    commentCount: Long,
    downloadItem: DownloadedVideoItem?,
    currentPositionMs: Long,
    durationMs: Long,
    onClose: () -> Unit,
    onToggleDanmaku: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleFollow: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onOpenShare: () -> Unit,
    onOpenDetail: () -> Unit,
    onDownload: () -> Unit,
    onOpenOwnerProfile: (Long, String) -> Unit,
    onOpenBaseVideo: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val ownerId = detail?.ownerId ?: item.ownerId.toLongOrNull() ?: 0L
    val ownerName = detail?.ownerName?.ifBlank { item.ownerName } ?: item.ownerName
    val ownerIcon = detail?.ownerIconUrl ?: ""
    val title = detail?.title?.ifBlank { item.title } ?: item.title
    val baseVideo = detail?.baseVideo

    Box(modifier = modifier.fillMaxSize()) {
        ShortsTopBar(
            isOverlayMode = isOverlayMode,
            isDanmakuEnabled = isDanmakuEnabled,
            onClose = onClose,
            onToggleDanmaku = onToggleDanmaku,
            onOpenDetail = onOpenDetail,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 下部グラデーション背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // 右側縦並びフローティングアクションバー
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .let { if (isOverlayMode) it.navigationBarsPadding() else it }
                .padding(end = 10.dp, bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            if (ownerId > 0L || ownerName.isNotBlank()) {
                Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.TopCenter) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .background(Color.DarkGray)
                            .clickable(enabled = ownerId > 0L) { onOpenOwnerProfile(ownerId, ownerName) }
                    ) {
                        HycoAsyncImage(url = ownerIcon, contentDescription = ownerName, modifier = Modifier.fillMaxSize())
                    }
                    if (ownerId > 0L) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomCenter)
                                .clip(CircleShape)
                                .background(if (isOwnerFollowed) Color(0xFF10B981) else Color(0xFFFF2E63))
                                .clickable { onToggleFollow() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOwnerFollowed) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = if (isOwnerFollowed) "フォロー中" else "フォローする",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            ShortActionButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = formatCompactNumber(likeCount),
                tint = if (isLiked) Color(0xFFFF4D8D) else Color.White,
                onClick = onToggleLike
            )
            ShortActionButton(
                icon = Icons.AutoMirrored.Filled.Comment,
                label = formatCompactNumber(commentCount),
                onClick = onOpenComments
            )
            ShortActionButton(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                label = "保存",
                onClick = onOpenPlaylist
            )
            val isDownloaded = downloadItem?.status == DownloadStatus.COMPLETED
            val isDownloading = downloadItem?.status == DownloadStatus.DOWNLOADING ||
                downloadItem?.status == DownloadStatus.QUEUED
            val progressPercent = downloadItem?.progressPercent ?: 0
            ShortActionButton(
                icon = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                label = when {
                    isDownloaded -> "保存済"
                    isDownloading -> "${progressPercent}%"
                    else -> "DL"
                },
                tint = if (isDownloaded) Color(0xFF10B981) else Color.White,
                onClick = onDownload
            )
            ShortActionButton(
                icon = Icons.Default.Share,
                label = "共有",
                onClick = onOpenShare
            )
        }

        // 左下: 投稿者・タイトル（タップで概要欄詳細）・ピン留め本編動画誘導
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .let { if (isOverlayMode) it.navigationBarsPadding() else it }
                .padding(start = 14.dp, end = 74.dp, bottom = 38.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (baseVideo != null && baseVideo.videoId.isNotBlank()) {
                Surface(
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenBaseVideo(baseVideo.videoId) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "本編動画: ${baseVideo.title.ifBlank { baseVideo.videoId }}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (ownerName.isNotBlank()) {
                Text(
                    text = "@$ownerName",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = ownerId > 0L) { onOpenOwnerProfile(ownerId, ownerName) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = detail != null, onClick = onOpenDetail)
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (detail != null) {
                    Text(
                        text = "概要欄・詳細を見る ›",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 最下部ミニシークバー
        ShortsBottomSeekBar(
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            onSeekTo = onSeekTo,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .let { if (isOverlayMode) it.navigationBarsPadding() else it }
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
