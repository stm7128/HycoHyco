package com.github.stm7128.hycohyco.ui.library

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import kotlinx.coroutines.launch

/**
 * ライブラリ画面「マイリスト」タブの公式クラウドリソース操作（あとで見る / 公式マイリスト）拡張
 */
fun LibraryViewModel.selectMylistSubTab(tab: MylistSubTab) {
    if (uiState.value.selectedMylistSubTab != tab) {
        cloudMylistJob?.cancel()
    }
    updateUiState { it.copy(selectedMylistSubTab = tab, activePlaylist = null, activeCloudMylist = null) }
    if (!uiState.value.isUserLoggedIn) return
    when (tab) {
        MylistSubTab.LOCAL -> Unit
        MylistSubTab.WATCH_LATER -> loadWatchLater()
        MylistSubTab.CLOUD_MYLIST -> loadCloudMylists()
    }
}

fun LibraryViewModel.refreshCloudData() {
    when (uiState.value.selectedMylistSubTab) {
        MylistSubTab.WATCH_LATER -> loadWatchLater()
        MylistSubTab.CLOUD_MYLIST -> loadCloudMylists()
        MylistSubTab.LOCAL -> Unit
    }
}

private fun LibraryViewModel.loadWatchLater() {
    cloudMylistJob?.cancel()
    cloudMylistJob = viewModelScope.launch {
        updateUiState { it.copy(isCloudLoading = true) }
        cloudLibraryRepository?.getWatchLaterVideos()?.onSuccess { videos ->
            updateUiState { it.copy(watchLaterVideos = videos, isCloudLoading = false) }
        }?.onFailure { error ->
            Log.e("LibraryViewModel", "loadWatchLater失敗: ${error.message}", error)
            updateUiState { it.copy(isCloudLoading = false) }
        }
    }
}

private fun LibraryViewModel.loadCloudMylists() {
    cloudMylistJob?.cancel()
    cloudMylistJob = viewModelScope.launch {
        updateUiState { it.copy(isCloudLoading = true) }
        cloudLibraryRepository?.getMylists()?.onSuccess { mylists ->
            updateUiState { it.copy(cloudMylists = mylists, isCloudLoading = false) }
        }?.onFailure { error ->
            Log.e("LibraryViewModel", "loadCloudMylists失敗: ${error.message}", error)
            updateUiState { it.copy(isCloudLoading = false) }
        }
    }
}

fun LibraryViewModel.openCloudMylist(mylist: CloudMylist) {
    updateUiState { it.copy(activeCloudMylist = mylist, isCloudLoading = true) }
    cloudMylistJob?.cancel()
    cloudMylistJob = viewModelScope.launch {
        cloudLibraryRepository?.getMylistVideos(mylist.id)?.onSuccess { videos ->
            updateUiState { it.copy(activeCloudMylistVideos = videos, isCloudLoading = false) }
        }?.onFailure { error ->
            Log.e("LibraryViewModel", "openCloudMylist失敗: ${error.message}", error)
            updateUiState { it.copy(isCloudLoading = false) }
        }
    }
}

fun LibraryViewModel.closeCloudMylist() {
    updateUiState { it.copy(activeCloudMylist = null, activeCloudMylistVideos = emptyList()) }
}

/** 公式「あとで見る」から指定アイテムを削除する */
fun LibraryViewModel.deleteWatchLaterItem(itemId: String) {
    viewModelScope.launch {
        updateUiState { state ->
            state.copy(
                watchLaterVideos = state.watchLaterVideos.filterNot {
                    it.cloudItemId?.toString() == itemId || it.contentId == itemId
                }
            )
        }
        cloudLibraryRepository?.deleteWatchLaterItem(itemId)
            ?.onSuccess {
                updateUiState { it.copy(actionMessage = "あとで見るから削除しました") }
            }
            ?.onFailure { error ->
                Log.e("LibraryViewModel", "あとで見る削除失敗: ${error.message}", error)
                updateUiState {
                    it.copy(actionMessage = "削除に失敗しました: ${error.localizedMessage ?: "通信エラー"}")
                }
                refreshCloudData()
            }
    }
}

/** 公式「マイリスト」から指定アイテムを削除する */
fun LibraryViewModel.deleteCloudMylistItem(mylistId: Long, itemId: String) {
    viewModelScope.launch {
        updateUiState { state ->
            state.copy(
                activeCloudMylistVideos = state.activeCloudMylistVideos.filterNot {
                    it.cloudItemId?.toString() == itemId || it.contentId == itemId
                }
            )
        }
        cloudLibraryRepository?.deleteMylistItem(mylistId, itemId)
            ?.onSuccess {
                updateUiState { it.copy(actionMessage = "マイリストから削除しました") }
            }
            ?.onFailure { error ->
                Log.e("LibraryViewModel", "マイリスト内動画削除失敗: ${error.message}", error)
                updateUiState {
                    it.copy(actionMessage = "削除に失敗しました: ${error.localizedMessage ?: "通信エラー"}")
                }
                val active = uiState.value.activeCloudMylist
                if (active != null) openCloudMylist(active)
            }
    }
}

/** 一時メッセージ表示フラグをクリアする */
fun LibraryViewModel.clearActionMessage() {
    updateUiState { it.copy(actionMessage = null) }
}
