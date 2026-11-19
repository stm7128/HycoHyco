package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import com.github.stm7128.hycohyco.player.model.isPlaying
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.model.VideoSummary

/**
 * 縦画面スマートフォン向けの通常の1ペイン縦スクロール展開プレイヤーレイアウト
 */
@Composable
fun PlayerSinglePaneLayout(
    renderSurface: @Composable () -> Unit,
    detail: VideoDetail,
    uiState: PlayerUiState,
    textColor: Color,
    secondaryColor: Color,
    onPlayVideo: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onOpenAddToPlaylist: () -> Unit,
    onOpenCommentInput: () -> Unit,
    onShareVideo: () -> Unit,
    onOwnerClick: (Long, String) -> Unit,
    onMylistClick: (Long) -> Unit = {},
    onSeriesClick: (Long) -> Unit = {},
    onToggleLike: () -> Unit,
    onToggleFollow: () -> Unit,
    onBottomTabChange: (PlayerBottomTab) -> Unit,
    onUpdateDanmakuAlpha: (Float) -> Unit,
    onUpdateNgConfig: (CommentNgConfig) -> Unit,
    onAddNgUserId: (String) -> Unit,
    onAddNgWord: (String) -> Unit,
    onDeleteComment: (CommentItem) -> Unit,
    onToggleNicoru: (CommentItem) -> Unit,
    modifier: Modifier = Modifier,
    onUpdateDanmakuSettings: (com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings) -> Unit = { onUpdateDanmakuAlpha(it.alpha) },
    onLoadPastComments: (Long, String) -> Unit = { _, _ -> },
    onResetToLatestComments: () -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onAddTag: (String) -> Unit = {},
    onDeleteTag: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        renderSurface()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            VideoInfoSection(
                detail = detail,
                textColor = textColor,
                secondaryColor = secondaryColor,
                isLiked = uiState.isLiked,
                likeCount = uiState.likeCount,
                isFollowed = uiState.isOwnerFollowed,
                isFollowSubmitting = uiState.isFollowSubmitting,
                onToggleLike = onToggleLike,
                onToggleFollow = onToggleFollow,
                onPlayVideo = onPlayVideo,
                onSeekTo = onSeekTo,
                onOpenAddToPlaylist = onOpenAddToPlaylist,
                onOpenCommentInput = onOpenCommentInput,
                onShareVideo = onShareVideo,
                onOwnerClick = onOwnerClick,
                onMylistClick = onMylistClick,
                onSeriesClick = onSeriesClick,
                currentPositionMs = uiState.playbackState.currentPositionMs,
                isTagSubmitting = uiState.isTagSubmitting,
                onTagClick = onTagClick,
                onAddTag = onAddTag,
                onDeleteTag = onDeleteTag,
            )

            detail.series?.let { seriesNav ->
                Spacer(modifier = Modifier.height(16.dp))
                SeriesSection(
                    series = seriesNav,
                    onPlayVideo = onPlayVideo,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PlayerBottomTabs(
                selectedTab = uiState.selectedBottomTab,
                commentCount = uiState.comments.size,
                recommendCount = uiState.recommendVideos.size,
                onTabSelected = onBottomTabChange,
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState.selectedBottomTab) {
                PlayerBottomTab.COMMENTS -> {
                    CommentListSection(
                        comments = uiState.comments,
                        isLoading = uiState.isCommentLoading,
                        danmakuSettings = uiState.danmakuSettings,
                        ngConfig = uiState.ngConfig,
                        onSeekTo = onSeekTo,
                        onUpdateAlpha = onUpdateDanmakuAlpha,
                        onUpdateNgConfig = onUpdateNgConfig,
                        onUpdateDanmakuSettings = onUpdateDanmakuSettings,
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        currentPositionMs = uiState.playbackState.currentPositionMs,
                        isPlaying = uiState.playbackState.isPlaying,
                        onAddNgUserId = onAddNgUserId,
                        onAddNgWord = onAddNgWord,
                        onDeleteComment = onDeleteComment,
                        onToggleNicoru = onToggleNicoru,
                        pastLogDateLabel = uiState.pastLogDateLabel,
                        onLoadPastComments = onLoadPastComments,
                        onResetToLatestComments = onResetToLatestComments,
                    )
                }
                PlayerBottomTab.RECOMMENDS -> {
                    RecommendVideoSection(
                        recommendVideos = uiState.recommendVideos,
                        isLoading = uiState.isRecommendLoading,
                        onPlayVideo = onPlayVideo,
                        secondaryColor = secondaryColor,
                    )
                }
            }
        }
    }
}
