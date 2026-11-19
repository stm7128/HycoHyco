package com.github.stm7128.hycohyco.ui.shorts

import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.github.stm7128.hycohyco.R
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.player.danmaku.DanmakuCanvas
import com.github.stm7128.hycohyco.player.model.PlaybackState
import com.github.stm7128.hycohyco.player.model.durationMs
import com.github.stm7128.hycohyco.player.model.isBuffering
import com.github.stm7128.hycohyco.player.model.isPlaying
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage

/**
 * ショート動画 VerticalPager の 1 ページ分表示コンポーザブル
 *
 * ページごとに独立した TextureView ベース PlayerView をバインドし、
 * 別ページの破棄 (onRelease) による `player = null` 競合・Surface 黒抜けを防止する。
 */
@OptIn(UnstableApi::class)
@Composable
internal fun ShortPageContent(
    item: VideoSummary,
    isCurrentSettled: Boolean,
    isOverlayMode: Boolean,
    uiState: ShortsUiState,
    playbackState: PlaybackState,
    exoPlayer: ExoPlayer,
    animPositionMs: Long,
    showPauseFeedback: Boolean,
    onTapSurface: () -> Unit,
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
    onSeekTo: (Long) -> Unit
) {
    val activeDetail = if (isCurrentSettled) uiState.activeDetail else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isCurrentSettled) {
                detectTapGestures(onTap = { if (isCurrentSettled) onTapSurface() })
            }
    ) {
        HycoAsyncImage(
            url = item.shortThumbnailUrl.ifBlank { item.thumbnailUrl },
            contentDescription = item.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        if (isCurrentSettled && uiState.errorMessage == null) {
            val shouldKeepScreenOn = playbackState.isPlaying || playbackState.isBuffering
            AndroidView(
                factory = { ctx ->
                    val pv = LayoutInflater.from(ctx).inflate(
                        R.layout.view_shorts_texture_player,
                        null,
                        false
                    ) as PlayerView
                    pv.keepScreenOn = shouldKeepScreenOn
                    pv.player = exoPlayer
                    pv
                },
                update = { pv ->
                    pv.keepScreenOn = shouldKeepScreenOn
                    if (pv.player !== exoPlayer) {
                        pv.player = exoPlayer
                    }
                },
                onRelease = { pv ->
                    pv.keepScreenOn = false
                    pv.player = null
                },
                modifier = Modifier.fillMaxSize()
            )
            DanmakuCanvas(
                renderItems = uiState.danmakuItems,
                currentPositionMs = animPositionMs,
                settings = uiState.danmakuSettings,
                isPortraitMode = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isCurrentSettled && (uiState.isLoadingCurrentVideo || playbackState.isBuffering)) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(40.dp).align(Alignment.Center)
            )
        } else if (isCurrentSettled && uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )
        }

        AnimatedVisibility(
            visible = isCurrentSettled && showPauseFeedback,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(color = Color.Black.copy(alpha = 0.55f), shape = CircleShape) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(16.dp).size(36.dp)
                )
            }
        }

        val totalMs = playbackState.durationMs.takeIf { it > 0L }
            ?: ((activeDetail?.durationSeconds ?: item.lengthSeconds.toLong()) * 1000L)

        ShortsOverlayControls(
            item = item,
            detail = activeDetail,
            isOverlayMode = isOverlayMode,
            isDanmakuEnabled = uiState.danmakuSettings.isEnabled,
            isLiked = if (isCurrentSettled) uiState.isLiked else false,
            likeCount = if (isCurrentSettled) uiState.likeCount else item.likeCounter.toLong(),
            isOwnerFollowed = if (isCurrentSettled) uiState.isOwnerFollowed else false,
            commentCount = if (isCurrentSettled) {
                uiState.comments.size.toLong().coerceAtLeast(activeDetail?.commentCount ?: 0L)
            } else {
                item.commentCounter.toLong()
            },
            downloadItem = uiState.downloadsByVideoId[item.contentId],
            currentPositionMs = if (isCurrentSettled) animPositionMs else 0L,
            durationMs = totalMs,
            onClose = onClose,
            onToggleDanmaku = onToggleDanmaku,
            onToggleLike = onToggleLike,
            onToggleFollow = onToggleFollow,
            onOpenComments = onOpenComments,
            onOpenPlaylist = onOpenPlaylist,
            onOpenShare = onOpenShare,
            onOpenDetail = onOpenDetail,
            onDownload = onDownload,
            onOpenOwnerProfile = onOpenOwnerProfile,
            onOpenBaseVideo = onOpenBaseVideo,
            onSeekTo = onSeekTo
        )
    }
}
