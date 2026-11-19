package com.github.stm7128.hycohyco.ui.shorts

import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.CommentFork
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentNgFilter
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.player.danmaku.DanmakuLayoutEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ショート動画におけるいいね！・フォロー・コメント・ニコる・プレイリスト保存操作拡張
 */
fun ShortsViewModel.toggleLike() {
    val detail = uiState.value.activeDetail ?: return
    val currentLiked = uiState.value.isLiked
    val currentCount = uiState.value.likeCount
    if (uiState.value.isLikeSubmitting) return

    viewModelScope.launch {
        updateUiState { it.copy(isLikeSubmitting = true) }
        if (currentLiked) {
            interactionRepository.unlikeVideo(detail.id)
                .onSuccess {
                    updateUiState {
                        it.copy(isLiked = false, likeCount = (currentCount - 1).coerceAtLeast(0L), isLikeSubmitting = false)
                    }
                }
                .onFailure { err ->
                    updateUiState { it.copy(isLikeSubmitting = false, feedbackMessage = err.localizedMessage) }
                }
        } else {
            interactionRepository.likeVideo(detail.id)
                .onSuccess { result ->
                    updateUiState {
                        it.copy(
                            isLiked = true,
                            likeCount = currentCount + 1,
                            isLikeSubmitting = false,
                            likeThanksMessage = result.thanksMessage
                        )
                    }
                }
                .onFailure { err ->
                    updateUiState { it.copy(isLikeSubmitting = false, feedbackMessage = err.localizedMessage) }
                }
        }
    }
}

fun ShortsViewModel.dismissLikeThanks() {
    updateUiState { it.copy(likeThanksMessage = null) }
}

fun ShortsViewModel.toggleFollowOwner() {
    val detail = uiState.value.activeDetail ?: return
    if (detail.ownerId <= 0L || uiState.value.isFollowSubmitting) return
    val currentFollowed = uiState.value.isOwnerFollowed

    viewModelScope.launch {
        updateUiState { it.copy(isFollowSubmitting = true) }
        val result = if (currentFollowed) {
            interactionRepository.unfollowUser(detail.ownerId)
        } else {
            interactionRepository.followUser(detail.ownerId)
        }
        result.onSuccess {
            updateUiState { it.copy(isOwnerFollowed = !currentFollowed, isFollowSubmitting = false) }
        }.onFailure { err ->
            updateUiState { it.copy(isFollowSubmitting = false, feedbackMessage = err.localizedMessage) }
        }
    }
}

fun ShortsViewModel.submitComment(body: String, commands: List<String>) {
    val detail = uiState.value.activeDetail ?: return
    val threadId = detail.defaultThreadId ?: return
    val serverUrl = detail.nvCommentServer ?: "https://public.nvcomment.nicovideo.jp"
    if (body.isBlank() || uiState.value.isCommentSubmitting) return

    viewModelScope.launch {
        updateUiState { it.copy(isCommentSubmitting = true) }
        val vposMs = playbackManager.exoPlayer.currentPosition.coerceAtLeast(0L)
        interactionRepository.postComment(detail.id, threadId, serverUrl, body, commands, vposMs)
            .onSuccess { posted ->
                val updatedRaw = (pageCache[detail.id]?.rawComments ?: uiState.value.comments) + posted
                pageCache[detail.id]?.let { pageCache[detail.id] = it.copy(rawComments = updatedRaw) }
                val filtered = withContext(Dispatchers.Default) {
                    CommentNgFilter.filter(updatedRaw.sortedBy { it.vposMs }, latestNgConfig)
                }
                val danmaku = withContext(Dispatchers.Default) {
                    DanmakuLayoutEngine.buildRenderItems(filtered, isPortraitMode = true)
                }
                updateUiState {
                    it.copy(
                        isCommentSubmitting = false,
                        isCommentInputVisible = false,
                        comments = filtered,
                        danmakuItems = danmaku,
                        feedbackMessage = "コメントを投稿しました"
                    )
                }
            }
            .onFailure { err ->
                updateUiState { it.copy(isCommentSubmitting = false, feedbackMessage = "投稿失敗: ${err.localizedMessage}") }
            }
    }
}

fun ShortsViewModel.toggleNicoru(comment: CommentItem) {
    val detail = uiState.value.activeDetail ?: return
    val threadId = detail.defaultThreadId ?: return
    val serverUrl = detail.nvCommentServer ?: "https://public.nvcomment.nicovideo.jp"
    val latest = uiState.value.comments.find { it.id == comment.id } ?: comment
    if (latest.nicoruId?.startsWith("temp_") == true) return

    val targetId = latest.nicoruId
    val optimistic = uiState.value.comments.map {
        if (it.id == latest.id) {
            it.copy(
                nicoruCount = if (targetId != null) (latest.nicoruCount - 1).coerceAtLeast(0) else latest.nicoruCount + 1,
                nicoruId = if (targetId != null) null else "temp_${latest.id}"
            )
        } else it
    }
    updateUiState { it.copy(comments = optimistic) }

    viewModelScope.launch {
        if (targetId != null) {
            interactionRepository.deleteNicoru(targetId).onFailure { err ->
                updateUiState {
                    it.copy(comments = it.comments.map { c -> if (c.id == latest.id) latest else c }, feedbackMessage = err.localizedMessage)
                }
            }
        } else {
            interactionRepository.postNicoru(
                detail.id, threadId, serverUrl, latest.no, latest.body,
                if (latest.fork == CommentFork.OWNER) "owner" else "main"
            ).onSuccess { (realId, realCount) ->
                updateUiState {
                    it.copy(comments = it.comments.map { c -> if (c.id == latest.id) c.copy(nicoruId = realId, nicoruCount = realCount) else c })
                }
            }.onFailure { err ->
                updateUiState {
                    it.copy(comments = it.comments.map { c -> if (c.id == latest.id) latest else c }, feedbackMessage = err.localizedMessage)
                }
            }
        }
    }
}

fun ShortsViewModel.toggleCommentSheet(visible: Boolean) = updateUiState { it.copy(isCommentSheetVisible = visible) }
fun ShortsViewModel.toggleCommentInput(visible: Boolean) = updateUiState { it.copy(isCommentInputVisible = visible) }
fun ShortsViewModel.toggleShareSheet(visible: Boolean) = updateUiState { it.copy(isShareSheetVisible = visible) }
fun ShortsViewModel.toggleDetailSheet(visible: Boolean) = updateUiState { it.copy(isDetailSheetVisible = visible) }
fun ShortsViewModel.clearFeedbackMessage() = updateUiState { it.copy(feedbackMessage = null) }

/** 視聴中のショート動画をオフライン保存キューに登録する */
fun ShortsViewModel.downloadActiveShort(downloadType: DownloadType = DownloadType.VIDEO_AND_DANMAKU) {
    val detail = uiState.value.activeDetail
    if (detail == null) {
        updateUiState { it.copy(feedbackMessage = "動画情報の読み込み完了後にお試しください") }
        return
    }
    val manager = offlineDownloadManager
    if (manager == null) {
        updateUiState { it.copy(feedbackMessage = "ダウンロード機能が利用できません") }
        return
    }
    val existing = uiState.value.downloadsByVideoId[detail.id] ?: manager.getDownload(detail.id)
    when (existing?.status) {
        DownloadStatus.COMPLETED -> {
            updateUiState { it.copy(feedbackMessage = "すでにオフライン保存済みです（ライブラリ＞ダウンロード）") }
            return
        }
        DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING -> {
            updateUiState { it.copy(feedbackMessage = "ダウンロード進行中です (${existing.progressPercent}%)") }
            return
        }
        else -> Unit
    }
    val itemThumb = uiState.value.items.firstOrNull { it.contentId == detail.id }?.shortThumbnailUrl
        ?.ifBlank { detail.thumbnailUrl }
        ?: detail.thumbnailUrl
    manager.enqueueDownload(
        videoId = detail.id,
        title = detail.title,
        durationMs = detail.durationSeconds * 1000L,
        ownerName = detail.ownerName,
        thumbnailUrl = itemThumb,
        downloadType = downloadType
    )
    updateUiState {
        it.copy(
            feedbackMessage = if (downloadType == DownloadType.AUDIO_ONLY_AND_DANMAKU) {
                "音声のオフライン保存を開始しました"
            } else {
                "ショートのオフライン保存を開始しました"
            }
        )
    }
}

fun ShortsViewModel.togglePlaylistSheet(visible: Boolean) {
    updateUiState { it.copy(isPlaylistSheetVisible = visible) }
    if (visible && uiState.value.userMylists.isEmpty()) {
        viewModelScope.launch {
            cloudLibraryRepository?.getMylists()?.onSuccess { mylists ->
                updateUiState { it.copy(userMylists = mylists) }
            }
        }
    }
}

fun ShortsViewModel.addToWatchLater() {
    val detail = uiState.value.activeDetail ?: return
    viewModelScope.launch {
        val result = cloudLibraryRepository?.addWatchLater(detail.id)
        updateUiState {
            it.copy(feedbackMessage = if (result?.isSuccess == true) "「あとで見る」に追加しました" else "追加に失敗しました")
        }
    }
}

fun ShortsViewModel.addToMylist(mylistId: Long) {
    val detail = uiState.value.activeDetail ?: return
    viewModelScope.launch {
        val result = cloudLibraryRepository?.addMylistItem(mylistId, detail.id)
        updateUiState {
            it.copy(feedbackMessage = if (result?.isSuccess == true) "マイリストに追加しました" else "追加に失敗しました")
        }
    }
}

fun ShortsViewModel.addToLocalPlaylist(playlistId: String) {
    val detail = uiState.value.activeDetail ?: return
    viewModelScope.launch {
        libraryRepository.addVideoToPlaylist(
            playlistId = playlistId,
            videoId = detail.id,
            title = detail.title,
            thumbnailUrl = detail.thumbnailUrl,
            durationMs = detail.durationSeconds * 1000L,
            ownerName = detail.ownerName,
            viewCounter = detail.viewCount.toInt(),
            commentCounter = detail.commentCount.toInt(),
            likeCounter = detail.likeCount.toInt(),
            mylistCounter = detail.mylistCount.toInt()
        )
        updateUiState { it.copy(feedbackMessage = "プレイリストに追加しました") }
    }
}

fun ShortsViewModel.createPlaylistAndAdd(title: String) {
    val detail = uiState.value.activeDetail ?: return
    if (title.isBlank()) return
    viewModelScope.launch {
        val newId = libraryRepository.createPlaylist(title.trim())
        libraryRepository.addVideoToPlaylist(
            playlistId = newId,
            videoId = detail.id,
            title = detail.title,
            thumbnailUrl = detail.thumbnailUrl,
            durationMs = detail.durationSeconds * 1000L,
            ownerName = detail.ownerName,
            viewCounter = detail.viewCount.toInt(),
            commentCounter = detail.commentCount.toInt(),
            likeCounter = detail.likeCount.toInt(),
            mylistCounter = detail.mylistCount.toInt()
        )
        updateUiState { it.copy(feedbackMessage = "「${title.trim()}」に保存しました") }
    }
}
