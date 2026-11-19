package com.github.stm7128.hycohyco.ui.shorts

import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.model.VideoStreamSession
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.player.danmaku.DanmakuRenderItem

/**
 * ショートページのプリフェッチキャッシュ情報
 */
data class ShortPageCache(
    val detail: VideoDetail,
    val streamSession: VideoStreamSession? = null,
    val rawComments: List<CommentItem> = emptyList(),
    val danmakuItems: List<DanmakuRenderItem> = emptyList(),
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun isExpired(ttlMs: Long = 300_000L): Boolean = System.currentTimeMillis() - cachedAt > ttlMs
}

/**
 * ニコニコショート連続視聴画面 (VerticalPager) の UI 状態
 */
data class ShortsUiState(
    val isActiveOverlay: Boolean = false,
    val items: List<VideoSummary> = emptyList(),
    val currentIndex: Int = 0,
    val scrollRequestToken: Int = 0,
    val isLoadingFeed: Boolean = false,
    val isLoadingCurrentVideo: Boolean = false,
    val errorMessage: String? = null,
    val activeDetail: VideoDetail? = null,
    val comments: List<CommentItem> = emptyList(),
    val danmakuItems: List<DanmakuRenderItem> = emptyList(),
    val danmakuSettings: DanmakuDisplaySettings = DanmakuDisplaySettings(),
    val useNicovideoGay: Boolean = false,
    val isLiked: Boolean = false,
    val likeCount: Long = 0L,
    val isLikeSubmitting: Boolean = false,
    val likeThanksMessage: String? = null,
    val isOwnerFollowed: Boolean = false,
    val isFollowSubmitting: Boolean = false,
    val isCommentSheetVisible: Boolean = false,
    val isCommentInputVisible: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val isShareSheetVisible: Boolean = false,
    val isPlaylistSheetVisible: Boolean = false,
    val isDetailSheetVisible: Boolean = false,
    val localPlaylists: List<LocalPlaylist> = emptyList(),
    val userMylists: List<CloudMylist> = emptyList(),
    val downloadsByVideoId: Map<String, DownloadedVideoItem> = emptyMap(),
    val isTagSubmitting: Boolean = false,
    val feedbackMessage: String? = null
)
