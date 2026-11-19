package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
 * 画面幅・向きに応じて2ペイン（横画面・タブレット）または1ペイン（縦画面）レイアウトを選択する
 */
@Composable
fun PlayerResponsiveLayout(
    isWideScreen: Boolean,
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
    onMylistClick: (Long) -> Unit,
    onSeriesClick: (Long) -> Unit,
    onToggleLike: () -> Unit,
    onToggleFollow: () -> Unit,
    onBottomTabChange: (PlayerBottomTab) -> Unit,
    onUpdateDanmakuAlpha: (Float) -> Unit,
    onUpdateNgConfig: (CommentNgConfig) -> Unit,
    onAddNgUserId: (String) -> Unit,
    onAddNgWord: (String) -> Unit,
    onDeleteComment: (CommentItem) -> Unit,
    onToggleNicoru: (CommentItem) -> Unit,
    onUpdateDanmakuSettings: (com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings) -> Unit,
    onLoadPastComments: (Long, String) -> Unit,
    onResetToLatestComments: () -> Unit,
    onTagClick: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onDeleteTag: (String) -> Unit,
) {
    if (isWideScreen) {
        PlayerTwoPaneLayout(
            renderSurface = renderSurface,
            detail = detail,
            uiState = uiState,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onPlayVideo = onPlayVideo,
            onSeekTo = onSeekTo,
            onOpenAddToPlaylist = onOpenAddToPlaylist,
            onOpenCommentInput = onOpenCommentInput,
            onShareVideo = onShareVideo,
            onOwnerClick = onOwnerClick,
            onMylistClick = onMylistClick,
            onSeriesClick = onSeriesClick,
            onToggleLike = onToggleLike,
            onToggleFollow = onToggleFollow,
            onBottomTabChange = onBottomTabChange,
            onUpdateDanmakuAlpha = onUpdateDanmakuAlpha,
            onUpdateDanmakuSettings = onUpdateDanmakuSettings,
            onUpdateNgConfig = onUpdateNgConfig,
            onAddNgUserId = onAddNgUserId,
            onAddNgWord = onAddNgWord,
            onDeleteComment = onDeleteComment,
            onToggleNicoru = onToggleNicoru,
            onLoadPastComments = onLoadPastComments,
            onResetToLatestComments = onResetToLatestComments,
            onTagClick = onTagClick,
            onAddTag = onAddTag,
            onDeleteTag = onDeleteTag,
        )
    } else {
        PlayerSinglePaneLayout(
            renderSurface = renderSurface,
            detail = detail,
            uiState = uiState,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onPlayVideo = onPlayVideo,
            onSeekTo = onSeekTo,
            onOpenAddToPlaylist = onOpenAddToPlaylist,
            onOpenCommentInput = onOpenCommentInput,
            onShareVideo = onShareVideo,
            onOwnerClick = onOwnerClick,
            onMylistClick = onMylistClick,
            onSeriesClick = onSeriesClick,
            onToggleLike = onToggleLike,
            onToggleFollow = onToggleFollow,
            onBottomTabChange = onBottomTabChange,
            onUpdateDanmakuAlpha = onUpdateDanmakuAlpha,
            onUpdateDanmakuSettings = onUpdateDanmakuSettings,
            onUpdateNgConfig = onUpdateNgConfig,
            onAddNgUserId = onAddNgUserId,
            onAddNgWord = onAddNgWord,
            onDeleteComment = onDeleteComment,
            onToggleNicoru = onToggleNicoru,
            onLoadPastComments = onLoadPastComments,
            onResetToLatestComments = onResetToLatestComments,
            onTagClick = onTagClick,
            onAddTag = onAddTag,
            onDeleteTag = onDeleteTag,
        )
    }
}

/**
 * 横画面・タブレット・大画面端末向けの左右2ペイン展開プレイヤーレイアウト
 */
@Composable
fun PlayerTwoPaneLayout(
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
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // 左ペイン: 動画再生サーフェス + 概要欄・シリーズ (55%幅)
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(end = 8.dp)
        ) {
            renderSurface()

            Column(modifier = Modifier.padding(16.dp)) {
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
            }
        }

        Spacer(modifier = Modifier.width(1.dp))

        // 右ペイン: コメント一覧 / 関連動画フルハイト (45%幅)
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            PlayerBottomTabs(
                selectedTab = uiState.selectedBottomTab,
                commentCount = uiState.comments.size,
                recommendCount = uiState.recommendVideos.size,
                onTabSelected = onBottomTabChange,
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                        maxHeight = null, // フルハイト表示
                        modifier = Modifier.fillMaxSize()
                    )
                }
                PlayerBottomTab.RECOMMENDS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
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
}
