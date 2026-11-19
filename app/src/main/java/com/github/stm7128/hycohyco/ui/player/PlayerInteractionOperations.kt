package com.github.stm7128.hycohyco.ui.player

import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.CommentCommandParser
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentNgFilter
import com.github.stm7128.hycohyco.player.danmaku.DanmakuLayoutEngine
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * プレイヤー画面における双方向インタラクション（いいね！送信/解除・お礼メッセージ）操作
 */
fun PlayerViewModel.toggleLike() {
    val detail = uiState.value.videoDetail ?: return
    val currentLiked = uiState.value.isLiked
    val currentCount = uiState.value.likeCount
    if (uiState.value.isLikeSubmitting) return

    viewModelScope.launch {
        updatePlayerUiState { it.copy(isLikeSubmitting = true) }
        val repo = interactionRepository
        if (repo == null) {
            updatePlayerUiState { it.copy(isLikeSubmitting = false, userFeedbackMessage = "機能が利用できません") }
            return@launch
        }

        if (currentLiked) {
            repo.unlikeVideo(detail.id)
                .onSuccess {
                    updatePlayerUiState {
                        it.copy(
                            isLiked = false,
                            likeCount = (currentCount - 1).coerceAtLeast(0L),
                            isLikeSubmitting = false
                        )
                    }
                }
                .onFailure { error ->
                    updatePlayerUiState { it.copy(isLikeSubmitting = false, userFeedbackMessage = error.localizedMessage) }
                }
        } else {
            repo.likeVideo(detail.id)
                .onSuccess { result ->
                    updatePlayerUiState {
                        it.copy(
                            isLiked = true,
                            likeCount = currentCount + 1,
                            isLikeSubmitting = false,
                            likeThanksMessage = result.thanksMessage
                        )
                    }
                }
                .onFailure { error ->
                    updatePlayerUiState { it.copy(isLikeSubmitting = false, userFeedbackMessage = error.localizedMessage) }
                }
        }
    }
}

/** お礼メッセージダイアログを閉じる */
fun PlayerViewModel.dismissLikeThanksMessage() {
    updatePlayerUiState { it.copy(likeThanksMessage = null) }
}

/** 投稿者のフォロー / フォロー解除をトグル切り替えする */
fun PlayerViewModel.toggleFollowOwner() {
    val detail = uiState.value.videoDetail ?: return
    val ownerId = detail.ownerId
    if (ownerId <= 0L) return
    val currentFollowed = uiState.value.isOwnerFollowed
    if (uiState.value.isFollowSubmitting) return

    viewModelScope.launch {
        updatePlayerUiState { it.copy(isFollowSubmitting = true) }
        val repo = interactionRepository
        if (repo == null) {
            updatePlayerUiState { it.copy(isFollowSubmitting = false, userFeedbackMessage = "機能が利用できません") }
            return@launch
        }

        if (currentFollowed) {
            repo.unfollowUser(ownerId)
                .onSuccess {
                    updatePlayerUiState { it.copy(isOwnerFollowed = false, isFollowSubmitting = false) }
                }
                .onFailure { error ->
                    updatePlayerUiState { it.copy(isFollowSubmitting = false, userFeedbackMessage = error.localizedMessage) }
                }
        } else {
            repo.followUser(ownerId)
                .onSuccess {
                    updatePlayerUiState { it.copy(isOwnerFollowed = true, isFollowSubmitting = false) }
                }
                .onFailure { error ->
                    updatePlayerUiState { it.copy(isFollowSubmitting = false, userFeedbackMessage = error.localizedMessage) }
                }
        }
    }
}

/** 投稿者のフォロー状態を非同期で確認・UIへ同期する */
fun PlayerViewModel.syncOwnerFollowStatus(ownerId: Long) {
    if (ownerId <= 0L) return
    viewModelScope.launch {
        interactionRepository?.isUserFollowed(ownerId)?.onSuccess { isFollowed ->
            updatePlayerUiState { it.copy(isOwnerFollowed = isFollowed) }
        }
    }
}

/** コメント入力ボトムシートの表示/非表示を切り替える */
fun PlayerViewModel.toggleCommentInputSheet(visible: Boolean) {
    updatePlayerUiState { it.copy(isCommentInputSheetVisible = visible) }
}

/**
 * 現在の動画・再生位置に対してコメントを投稿する (0ms 即時ローカル反映)
 */
fun PlayerViewModel.submitComment(body: String, commands: List<String>) {
    val detail = uiState.value.videoDetail ?: return
    val threadId = detail.defaultThreadId ?: run {
        updatePlayerUiState { it.copy(userFeedbackMessage = "コメントスレッドが見つかりません") }
        return
    }
    val serverUrl = detail.nvCommentServer ?: "https://public.nvcomment.nicovideo.jp"
    if (body.isBlank() || uiState.value.isCommentSubmitting) return

    val currentVpos = uiState.value.playbackState.currentPositionMs
    val tempId = "temp_my_${System.currentTimeMillis()}"
    val parsed = CommentCommandParser.parse(commands)

    // 0ms 即時ローカル反映: 自コメント（isMyComment = true）として即座に画面へ流す
    val optimisticComment = CommentItem(
        id = tempId,
        no = 0L,
        vposMs = currentVpos,
        body = body,
        position = parsed.position,
        size = parsed.size,
        colorInt = parsed.colorInt,
        font = parsed.font,
        isTranslucent = parsed.isTranslucent,
        is184 = parsed.is184,
        isMyComment = true
    )

    val currentComments = uiState.value.comments
    val optimisticList = (currentComments + optimisticComment).sortedBy { it.vposMs }
    val ngConfig = uiState.value.ngConfig

    updatePlayerUiState {
        it.copy(
            comments = optimisticList,
            danmakuRenderItems = DanmakuLayoutEngine.buildRenderItems(CommentNgFilter.filter(optimisticList, ngConfig)),
            isCommentSubmitting = true,
            isCommentInputSheetVisible = false
        )
    }

    viewModelScope.launch {
        val repo = interactionRepository
        if (repo == null) {
            rollbackComment(tempId, "機能が利用できません")
            return@launch
        }

        repo.postComment(
            videoId = detail.id,
            threadId = threadId,
            serverUrl = serverUrl,
            body = body,
            commands = commands,
            vposMs = currentVpos
        ).onSuccess { realComment ->
            val finalComments = uiState.value.comments.map {
                if (it.id == tempId) realComment.copy(isMyComment = true) else it
            }.sortedBy { it.vposMs }

            val newRenderItems = withContext(Dispatchers.Default) {
                DanmakuLayoutEngine.buildRenderItems(CommentNgFilter.filter(finalComments, ngConfig))
            }
            updatePlayerUiState {
                it.copy(comments = finalComments, danmakuRenderItems = newRenderItems, isCommentSubmitting = false)
            }
        }.onFailure { error ->
            rollbackComment(tempId, "コメント投稿失敗: ${error.localizedMessage ?: "エラー"}")
        }
    }
}

/** 自コメント投稿失敗時のロールバック消去 */
private fun PlayerViewModel.rollbackComment(tempId: String, errorMsg: String) {
    val restored = uiState.value.comments.filterNot { it.id == tempId }
    val ngConfig = uiState.value.ngConfig
    val restoredRenderItems = DanmakuLayoutEngine.buildRenderItems(CommentNgFilter.filter(restored, ngConfig))
    updatePlayerUiState {
        it.copy(
            comments = restored,
            danmakuRenderItems = restoredRenderItems,
            isCommentSubmitting = false,
            userFeedbackMessage = errorMsg
        )
    }
}

/**
 * 自分が投稿したコメントを削除する
 */
fun PlayerViewModel.deleteComment(comment: CommentItem) {
    val detail = uiState.value.videoDetail ?: return
    val threadId = detail.defaultThreadId ?: return
    val serverUrl = detail.nvCommentServer ?: "https://public.nvcomment.nicovideo.jp"

    // 0ms 即時ローカル消去
    val currentComments = uiState.value.comments
    val filteredComments = currentComments.filterNot { it.id == comment.id }
    val ngConfig = uiState.value.ngConfig
    val newRenderItems = DanmakuLayoutEngine.buildRenderItems(CommentNgFilter.filter(filteredComments, ngConfig))

    updatePlayerUiState {
        it.copy(comments = filteredComments, danmakuRenderItems = newRenderItems)
    }

    viewModelScope.launch {
        val repo = interactionRepository ?: return@launch
        repo.deleteComment(
            videoId = detail.id,
            threadId = threadId,
            serverUrl = serverUrl,
            commentId = comment.id,
            commentNo = comment.no
        ).onFailure { error ->
            updatePlayerUiState {
                it.copy(userFeedbackMessage = "コメント削除失敗: ${error.localizedMessage ?: "エラー"}")
            }
        }
    }
}
