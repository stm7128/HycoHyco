package com.github.stm7128.hycohyco.ui.player

import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.CommentNgFilter
import com.github.stm7128.hycohyco.player.danmaku.DanmakuLayoutEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * PlayerViewModel の過去ログコメント取得・切替操作拡張
 */
internal suspend fun PlayerViewModel.loadCommentsWithTimestamp(
    videoId: String,
    whenTimestampSec: Long? = null,
    pastLogDateLabel: String? = null
) {
    val result = videoRepository.getVideoComments(videoId, whenTimestampSec)
    if (result.isFailure) {
        updatePlayerUiState {
            it.copy(
                isCommentLoading = false,
                userFeedbackMessage = "コメント取得失敗: ${result.exceptionOrNull()?.localizedMessage ?: "エラー"}"
            )
        }
        return
    }
    val rawComments = result.getOrDefault(emptyList())
    val renderItems = withContext(Dispatchers.Default) {
        DanmakuLayoutEngine.buildRenderItems(CommentNgFilter.filter(rawComments, uiState.value.ngConfig))
    }
    updatePlayerUiState {
        it.copy(
            comments = rawComments,
            danmakuRenderItems = renderItems,
            isCommentLoading = false,
            pastLogDateLabel = pastLogDateLabel
        )
    }
}

/**
 * 指定された UNIX 秒時点の過去ログコメントを取得し、弾幕・リストを差し替える
 */
fun PlayerViewModel.loadPastComments(timestampSec: Long, dateLabel: String) {
    val detail = uiState.value.videoDetail ?: return
    if (uiState.value.isOfflineMode) {
        updatePlayerUiState { it.copy(userFeedbackMessage = "オフライン再生中は過去ログを取得できません") }
        return
    }
    viewModelScope.launch {
        updatePlayerUiState { it.copy(isCommentLoading = true) }
        loadCommentsWithTimestamp(
            videoId = detail.id,
            whenTimestampSec = timestampSec,
            pastLogDateLabel = dateLabel
        )
    }
}

/**
 * 過去ログ表示を解除し、最新のコメントを再取得する
 */
fun PlayerViewModel.resetToLatestComments() {
    val detail = uiState.value.videoDetail ?: return
    if (uiState.value.pastLogDateLabel == null) return
    viewModelScope.launch {
        updatePlayerUiState { it.copy(isCommentLoading = true) }
        loadCommentsWithTimestamp(
            videoId = detail.id,
            whenTimestampSec = null,
            pastLogDateLabel = null
        )
    }
}
