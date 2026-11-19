package com.github.stm7128.hycohyco.ui.player

import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.CommentNgFilter
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.usecase.OfflinePlaybackData
import com.github.stm7128.hycohyco.player.NicoPlaybackManager
import com.github.stm7128.hycohyco.player.danmaku.DanmakuLayoutEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * オフライン再生の準備と実行を補助するヘルパークラス
 */
object PlayerOfflinePlaybackHelper {

    /**
     * オフライン動画用の擬似 VideoDetail を生成
     */
    fun createOfflineVideoDetail(offline: OfflinePlaybackData): VideoDetail {
        return VideoDetail(
            id = offline.videoId,
            title = offline.title,
            description = "【完全オフライン再生中】\n保存されたメディアと弾幕コメントをローカルから再生しています。",
            durationSeconds = offline.durationMs / 1000,
            thumbnailUrl = "",
            viewCount = 0L,
            commentCount = offline.comments.size.toLong(),
            mylistCount = 0L,
            likeCount = 0L,
            ownerName = offline.ownerName
        )
    }

    /**
     * オフラインコメントの弾幕アイテムを生成
     */
    suspend fun buildOfflineDanmaku(
        offline: OfflinePlaybackData,
        ngConfig: CommentNgConfig
    ) = withContext(Dispatchers.Default) {
        DanmakuLayoutEngine.buildRenderItems(
            CommentNgFilter.filter(offline.comments, ngConfig)
        )
    }

    /**
     * オフラインメディアの再生をマネージャーに指示
     */
    suspend fun startOfflinePlayback(
        playbackManager: NicoPlaybackManager,
        offline: OfflinePlaybackData
    ) {
        playbackManager.prepareAndPlayLocal(
            videoFilePath = offline.videoFilePath,
            audioFilePath = offline.audioFilePath,
            title = offline.title,
            artist = offline.ownerName,
            masterPlaylistPath = offline.masterPlaylistPath,
            durationMs = offline.durationMs
        )
    }

    /**
     * オフライン動画再生シーケンスを実行
     */
    suspend fun playOffline(
        viewModel: PlayerViewModel,
        offline: OfflinePlaybackData,
        initialPositionMs: Long? = null
    ) {
        val fakeDetail = createOfflineVideoDetail(offline)
        val renderItems = buildOfflineDanmaku(offline, viewModel.uiState.value.ngConfig)
        viewModel.updatePlayerUiState {
            it.copy(
                videoDetail = fakeDetail,
                isOfflineMode = true,
                comments = offline.comments,
                danmakuRenderItems = renderItems,
                isCommentLoading = false
            )
        }
        startOfflinePlayback(viewModel.playbackManager, offline)
        val isResumeEnabled = viewModel.playerPreferencesDataSource?.isResumePlaybackEnabled?.first()
            ?: viewModel.uiState.value.isResumePlaybackEnabled
        PlayerResumeHelper.restoreResumePosition(
            viewModel.libraryRepository,
            viewModel.playbackManager,
            offline.videoId,
            offline.durationMs,
            offline.title,
            offline.ownerName,
            "",
            initialPositionMs,
            isResumeEnabled
        )
        viewModel.updatePlayerUiState { it.copy(isLoading = false) }
    }
}
