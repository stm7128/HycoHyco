package com.github.stm7128.hycohyco.ui.player

import androidx.compose.runtime.Composable
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.ui.user.UserDetailSheet

/**
 * プレイヤー画面上に重なる各種ボトムシートおよびダイアログ群
 */
@Composable
fun PlayerDialogsAndSheets(
    uiState: PlayerUiState,
    selectedOwnerInfo: Pair<Long, String>?,
    onDismissOwnerSheet: () -> Unit,
    onCloseSettings: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onRepeatToggle: (Boolean) -> Unit,
    onQualitySelected: (String) -> Unit,
    onGestureModeSelected: (PlayerGestureMode) -> Unit,
    onDownloadVideo: (DownloadType) -> Unit,
    onCloseAddToPlaylist: () -> Unit,
    onAddToPlaylist: (String) -> Unit,
    onCreateAndAddToPlaylist: (String) -> Unit,
    onPlayVideo: (String) -> Unit,
    onDismissLikeThanks: () -> Unit = {},
    onCloseCommentInput: () -> Unit = {},
    onSubmitComment: (String, List<String>) -> Unit = { _, _ -> },
    onAutoplayToggle: (Boolean) -> Unit = {},
    onResumePlaybackToggle: (Boolean) -> Unit = {},
    onDoubleTapSeekSecondsChange: (Int) -> Unit = {},
    onAddToWatchLater: () -> Unit = {},
    onAddToMylist: (Long) -> Unit = {},
    onClearPlaylistMessage: () -> Unit = {},
    onUseNicovideoGayChange: (Boolean) -> Unit = {},
    onTimerModeSelected: (com.github.stm7128.hycohyco.player.SleepTimerMode) -> Unit = {},
    onCoexistAudioChange: (Boolean) -> Unit = {},
    onAutoEnterPipChange: (Boolean) -> Unit = {},
) {
    // 1. 再生・画質・速度設定シート
    if (uiState.isSettingsSheetVisible) {
        PlayerSettingsSheet(
            onDismissRequest = onCloseSettings,
            currentSpeed = uiState.playbackSpeed,
            onSpeedSelected = onSpeedSelected,
            isRepeatEnabled = uiState.isRepeatOne,
            onRepeatToggle = onRepeatToggle,
            availableQualities = uiState.videoDetail?.availableQualities.orEmpty(),
            selectedQualityId = uiState.selectedQualityId,
            onQualitySelected = onQualitySelected,
            gestureMode = uiState.gestureMode,
            onGestureModeSelected = onGestureModeSelected,
            onDownloadSelected = onDownloadVideo,
            isAutoplayEnabled = uiState.isAutoplayEnabled,
            onAutoplayToggle = onAutoplayToggle,
            isResumePlaybackEnabled = uiState.isResumePlaybackEnabled,
            onResumePlaybackToggle = onResumePlaybackToggle,
            doubleTapSeekSeconds = uiState.doubleTapSeekSeconds,
            onDoubleTapSeekSecondsChange = onDoubleTapSeekSecondsChange,
            useNicovideoGay = uiState.useNicovideoGay,
            onUseNicovideoGayChange = onUseNicovideoGayChange,
            currentTimerMode = uiState.sleepTimerMode,
            remainingTimerSeconds = uiState.sleepTimerRemainingSeconds,
            onTimerModeSelected = onTimerModeSelected,
            isCoexistAudio = uiState.isCoexistAudio,
            onCoexistAudioChange = onCoexistAudioChange,
            isAutoEnterPip = uiState.isAutoEnterPip,
            onAutoEnterPipChange = onAutoEnterPipChange,
        )
    }

    // 2. プレイリスト追加シート
    if (uiState.isAddToPlaylistSheetVisible) {
        AddToPlaylistSheet(
            videoDetail = uiState.videoDetail,
            playlists = uiState.availablePlaylists,
            userMylists = uiState.userMylists,
            playlistMessage = uiState.playlistMessage,
            onDismiss = onCloseAddToPlaylist,
            onAddToWatchLater = onAddToWatchLater,
            onAddToMylist = onAddToMylist,
            onAddToPlaylist = onAddToPlaylist,
            onCreateAndAdd = onCreateAndAddToPlaylist,
            onClearMessage = onClearPlaylistMessage,
        )
    }

    // 3. 投稿者プロフィール・ユーザー詳細シート
    selectedOwnerInfo?.let { (ownerId, ownerName) ->
        UserDetailSheet(
            userId = ownerId,
            initialNickname = ownerName,
            onDismissRequest = onDismissOwnerSheet,
            onVideoClick = { videoId ->
                onDismissOwnerSheet()
                onPlayVideo(videoId)
            }
        )
    }

    // 4. いいね！お礼メッセージダイアログ
    uiState.likeThanksMessage?.let { thanksMsg ->
        LikeThanksDialog(
            thanksMessage = thanksMsg,
            ownerName = uiState.videoDetail?.ownerName.orEmpty(),
            onDismissRequest = onDismissLikeThanks
        )
    }

    // 5. コメント投稿ボトムシート
    if (uiState.isCommentInputSheetVisible) {
        CommentInputSheet(
            onDismissRequest = onCloseCommentInput,
            onSubmitComment = onSubmitComment,
            isSubmitting = uiState.isCommentSubmitting
        )
    }
}
