package com.github.stm7128.hycohyco.ui.player

import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.CommentFork
import com.github.stm7128.hycohyco.domain.model.CommentItem
import kotlinx.coroutines.launch

/**
 * プレイヤー画面におけるコメントのニコる／ニコる解除操作
 */
fun PlayerViewModel.toggleNicoru(comment: CommentItem) {
    val detail = uiState.value.videoDetail ?: return
    val threadId = detail.defaultThreadId ?: return
    val serverUrl = detail.nvCommentServer ?: "https://public.nvcomment.nicovideo.jp"

    // uiState から最新のコメント状態を取得
    val latestComment = uiState.value.comments.find { it.id == comment.id } ?: comment

    // まだ通信中（temp_プレフィックス）の場合は連打防止
    if (latestComment.nicoruId?.startsWith("temp_") == true) {
        return
    }

    val isCurrentlyNicoru = latestComment.nicoruId != null
    val targetNicoruId = latestComment.nicoruId

    val updatedCount = if (isCurrentlyNicoru) (latestComment.nicoruCount - 1).coerceAtLeast(0) else latestComment.nicoruCount + 1
    val updatedId = if (isCurrentlyNicoru) null else "temp_${latestComment.id}"

    // 0ms 楽観的UI更新
    val currentComments = uiState.value.comments
    val optimisticComments = currentComments.map {
        if (it.id == latestComment.id) it.copy(nicoruCount = updatedCount, nicoruId = updatedId) else it
    }
    updatePlayerUiState { it.copy(comments = optimisticComments) }

    viewModelScope.launch {
        val repo = interactionRepository ?: return@launch
        if (targetNicoruId != null) {
            repo.deleteNicoru(targetNicoruId).onFailure { error ->
                val rollback = uiState.value.comments.map { if (it.id == latestComment.id) latestComment else it }
                updatePlayerUiState { it.copy(comments = rollback, userFeedbackMessage = "ニコる解除失敗: ${error.localizedMessage ?: "エラー"}") }
            }
        } else {
            repo.postNicoru(
                videoId = detail.id,
                threadId = threadId,
                serverUrl = serverUrl,
                commentNo = latestComment.no,
                commentBody = latestComment.body,
                fork = if (latestComment.fork == CommentFork.OWNER) "owner" else "main"
            ).onSuccess { (realId, realCount) ->
                val finalComments = uiState.value.comments.map {
                    if (it.id == latestComment.id) it.copy(nicoruId = realId, nicoruCount = realCount) else it
                }
                updatePlayerUiState { it.copy(comments = finalComments) }
            }.onFailure { error ->
                val rollback = uiState.value.comments.map { if (it.id == latestComment.id) latestComment else it }
                updatePlayerUiState { it.copy(comments = rollback, userFeedbackMessage = "ニコる失敗: ${error.localizedMessage ?: "エラー"}") }
            }
        }
    }
}
