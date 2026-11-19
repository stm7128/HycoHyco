package com.github.stm7128.hycohyco.ui.shorts

import android.content.Context
import androidx.compose.runtime.Composable
import com.github.stm7128.hycohyco.ui.player.AddToPlaylistSheet
import com.github.stm7128.hycohyco.ui.player.CommentInputSheet
import com.github.stm7128.hycohyco.ui.player.LikeThanksDialog
import com.github.stm7128.hycohyco.ui.player.PlayerShareSheet
import com.github.stm7128.hycohyco.ui.user.UserDetailSheet

/**
 * ショート画面内の各種ボトムシート・ダイアログ群
 */
@Composable
internal fun ShortsDialogsAndSheets(
    uiState: ShortsUiState,
    viewModel: ShortsViewModel,
    context: Context,
    animPositionMs: Long,
    selectedOwner: Pair<Long, String>?,
    onOpenOwnerProfile: (Long, String) -> Unit,
    onDismissOwner: () -> Unit,
    onOpenBaseVideo: (String) -> Unit,
    onSearchShortTag: (String) -> Unit = {}
) {
    val activeDetail = uiState.activeDetail
    if (uiState.isDetailSheetVisible && activeDetail != null) {
        ShortsDetailSheet(
            detail = activeDetail,
            isOwnerFollowed = uiState.isOwnerFollowed,
            downloadItem = uiState.downloadsByVideoId[activeDetail.id],
            onDismiss = { viewModel.toggleDetailSheet(false) },
            onToggleFollow = viewModel::toggleFollowOwner,
            onOpenOwnerProfile = onOpenOwnerProfile,
            onPlayLinkedVideo = { linkedId ->
                if (linkedId.startsWith("ss", ignoreCase = true)) {
                    viewModel.openShortById(linkedId, context)
                } else {
                    onOpenBaseVideo(linkedId)
                }
            },
            onSeekTo = viewModel::seekTo,
            onDownload = { type -> viewModel.downloadActiveShort(type) },
            isTagSubmitting = uiState.isTagSubmitting,
            onTagClick = onSearchShortTag,
            onAddTag = viewModel::addTag,
            onDeleteTag = viewModel::deleteTag
        )
    }

    if (uiState.isCommentSheetVisible) {
        ShortsCommentSheet(
            comments = uiState.comments,
            onDismiss = { viewModel.toggleCommentSheet(false) },
            onSeekTo = viewModel::seekTo,
            onToggleNicoru = viewModel::toggleNicoru,
            onOpenCommentInput = { viewModel.toggleCommentInput(true) }
        )
    }

    if (uiState.isCommentInputVisible) {
        CommentInputSheet(
            isSubmitting = uiState.isCommentSubmitting,
            onSubmitComment = { body, cmds -> viewModel.submitComment(body, cmds) },
            onDismissRequest = { viewModel.toggleCommentInput(false) }
        )
    }

    if (uiState.isPlaylistSheetVisible) {
        AddToPlaylistSheet(
            videoDetail = uiState.activeDetail,
            playlists = uiState.localPlaylists,
            userMylists = uiState.userMylists,
            onDismiss = { viewModel.togglePlaylistSheet(false) },
            onAddToWatchLater = viewModel::addToWatchLater,
            onAddToMylist = viewModel::addToMylist,
            onAddToPlaylist = viewModel::addToLocalPlaylist,
            onCreateAndAdd = viewModel::createPlaylistAndAdd
        )
    }

    val detailForShare = uiState.activeDetail
    if (uiState.isShareSheetVisible && detailForShare != null) {
        PlayerShareSheet(
            detail = detailForShare,
            currentPositionMs = animPositionMs,
            useNicovideoGay = uiState.useNicovideoGay,
            onDismissRequest = { viewModel.toggleShareSheet(false) }
        )
    }

    val thanks = uiState.likeThanksMessage
    if (!thanks.isNullOrBlank()) {
        LikeThanksDialog(
            thanksMessage = thanks,
            ownerName = uiState.activeDetail?.ownerName ?: "",
            onDismissRequest = viewModel::dismissLikeThanks
        )
    }

    selectedOwner?.let { (uid, uname) ->
        UserDetailSheet(
            userId = uid,
            initialNickname = uname,
            onDismissRequest = onDismissOwner,
            onVideoClick = { clickedId ->
                onDismissOwner()
                if (clickedId.startsWith("ss", ignoreCase = true)) {
                    viewModel.openShortById(clickedId, context)
                } else {
                    onOpenBaseVideo(clickedId)
                }
            }
        )
    }
}
