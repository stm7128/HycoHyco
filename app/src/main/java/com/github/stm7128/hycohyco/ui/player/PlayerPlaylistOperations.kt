package com.github.stm7128.hycohyco.ui.player

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * プレイヤー画面におけるプレイリスト・マイリスト・あとで見る操作
 */
fun PlayerViewModel.toggleAddToPlaylistSheet(visible: Boolean) {
    updatePlayerUiState { it.copy(isAddToPlaylistSheetVisible = visible) }
    if (visible) {
        loadUserMylists()
    }
}

fun PlayerViewModel.loadUserMylists() {
    viewModelScope.launch {
        val repo = cloudLibraryRepository ?: return@launch
        repo.getMylists().onSuccess { mylists ->
            updatePlayerUiState { it.copy(userMylists = mylists) }
        }
    }
}

fun PlayerViewModel.addCurrentVideoToWatchLater() {
    val detail = uiState.value.videoDetail ?: return
    viewModelScope.launch {
        val repo = cloudLibraryRepository
        if (repo == null) {
            updatePlayerUiState { it.copy(playlistMessage = "機能が利用できません") }
            return@launch
        }
        repo.addWatchLater(detail.id).fold(
            onSuccess = {
                updatePlayerUiState { it.copy(playlistMessage = "「あとで見る」に追加しました") }
            },
            onFailure = { error ->
                updatePlayerUiState { it.copy(playlistMessage = "あとで見る追加失敗: ${error.localizedMessage ?: "エラー"}") }
            }
        )
    }
}

fun PlayerViewModel.addCurrentVideoToMylist(mylistId: Long) {
    val detail = uiState.value.videoDetail ?: return
    viewModelScope.launch {
        val repo = cloudLibraryRepository
        if (repo == null) {
            updatePlayerUiState { it.copy(playlistMessage = "機能が利用できません") }
            return@launch
        }
        repo.addMylistItem(mylistId, detail.id).fold(
            onSuccess = {
                updatePlayerUiState { it.copy(playlistMessage = "マイリストに追加しました") }
            },
            onFailure = { error ->
                updatePlayerUiState { it.copy(playlistMessage = "マイリスト追加失敗: ${error.localizedMessage ?: "エラー"}") }
            }
        )
    }
}

fun PlayerViewModel.addCurrentVideoToPlaylist(playlistId: String) {
    val detail = uiState.value.videoDetail ?: return
    viewModelScope.launch {
        PlayerPlaylistHelper.addVideoToPlaylist(libraryRepository, playlistId, detail)
        updatePlayerUiState { it.copy(playlistMessage = "プレイリストに追加しました") }
    }
}

fun PlayerViewModel.createPlaylistAndAddCurrentVideo(title: String) {
    val detail = uiState.value.videoDetail ?: return
    viewModelScope.launch {
        PlayerPlaylistHelper.createAndAdd(libraryRepository, title, detail)
        updatePlayerUiState { it.copy(playlistMessage = "プレイリスト「$title」を作成して追加しました") }
    }
}

fun PlayerViewModel.clearPlaylistMessage() {
    updatePlayerUiState { it.copy(playlistMessage = null) }
}
