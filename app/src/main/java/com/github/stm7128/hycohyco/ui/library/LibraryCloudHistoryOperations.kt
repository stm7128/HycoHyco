package com.github.stm7128.hycohyco.ui.library

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ライブラリ画面のニコニコアカウント各種クラウド履歴（視聴・いいね！・ニコられた・ニコった）操作拡張
 */
fun LibraryViewModel.selectHistorySubTab(subTab: HistorySubTab) {
    if (uiState.value.selectedHistorySubTab != subTab) {
        cloudHistoryJob?.cancel()
    }
    updateUiState { it.copy(selectedHistorySubTab = subTab) }
    if (uiState.value.isUserLoggedIn) {
        loadHistoryTabIfNeeded(subTab)
    }
}

fun LibraryViewModel.refreshSelectedHistoryIfCloud() {
    when (uiState.value.selectedHistorySubTab) {
        HistorySubTab.LOCAL -> Unit
        HistorySubTab.CLOUD_WATCH -> loadCloudWatchHistory(reset = true)
        HistorySubTab.CLOUD_LIKE -> loadCloudLikeHistory(reset = true)
        HistorySubTab.CLOUD_NICORU_PASSIVE -> loadNicoruPassiveHistory(reset = true)
        HistorySubTab.CLOUD_NICORU_ACTIVE -> loadNicoruActiveHistory(reset = true)
    }
}

fun LibraryViewModel.loadHistoryTabIfNeeded(subTab: HistorySubTab) {
    val s = uiState.value
    when (subTab) {
        HistorySubTab.LOCAL -> Unit
        HistorySubTab.CLOUD_WATCH -> if (!s.cloudWatchHasLoaded || s.cloudWatchError != null) loadCloudWatchHistory(reset = true)
        HistorySubTab.CLOUD_LIKE -> if (!s.cloudLikeHasLoaded || s.cloudLikeError != null) loadCloudLikeHistory(reset = true)
        HistorySubTab.CLOUD_NICORU_PASSIVE -> if (!s.nicoruPassiveHasLoaded || s.nicoruPassiveError != null) loadNicoruPassiveHistory(reset = true)
        HistorySubTab.CLOUD_NICORU_ACTIVE -> if (!s.nicoruActiveHasLoaded || s.nicoruActiveError != null) loadNicoruActiveHistory(reset = true)
    }
}

// ==========================================
// 1. クラウド視聴履歴 (/v2/users/me/watch/history)
// ==========================================
fun LibraryViewModel.loadCloudWatchHistory(reset: Boolean = true) {
    val repo = cloudLibraryRepository ?: return
    val current = uiState.value
    if (reset && current.isCloudWatchLoading) return
    if (!reset && (current.isCloudWatchLoading || current.isCloudWatchLoadingMore || current.cloudWatchNextCursor == null)) return

    val cursor = if (reset) null else current.cloudWatchNextCursor
    cloudHistoryJob?.cancel()
    cloudHistoryJob = viewModelScope.launch {
        updateUiState {
            if (reset) it.copy(isCloudWatchLoading = true, cloudWatchError = null)
            else it.copy(isCloudWatchLoadingMore = true)
        }
        repo.getCloudWatchHistory(cursor).onSuccess { page ->
            updateUiState { state ->
                val merged = if (reset) page.items else (state.cloudWatchItems + page.items).distinctBy { it.itemId }
                state.copy(
                    cloudWatchItems = merged,
                    cloudWatchNextCursor = page.nextCursor,
                    cloudWatchHasLoaded = true,
                    isCloudWatchLoading = false,
                    isCloudWatchLoadingMore = false,
                    cloudWatchError = null
                )
            }
        }.onFailure { err ->
            Log.e("LibraryCloudHistory", "loadCloudWatchHistory失敗: ${err.message}", err)
            updateUiState {
                it.copy(
                    isCloudWatchLoading = false,
                    isCloudWatchLoadingMore = false,
                    cloudWatchError = if (reset) (err.localizedMessage ?: "視聴履歴の取得に失敗しました") else it.cloudWatchError
                )
            }
        }
    }
}

fun LibraryViewModel.deleteCloudWatchHistoryItem(videoId: String) {
    val repo = cloudLibraryRepository ?: return
    viewModelScope.launch {
        updateUiState { s -> s.copy(cloudWatchItems = s.cloudWatchItems.filterNot { it.video.contentId == videoId }) }
        repo.deleteCloudWatchHistory(videoId).onFailure { err ->
            Log.e("LibraryCloudHistory", "クラウド視聴履歴削除失敗: ${err.message}", err)
            loadCloudWatchHistory(reset = true)
        }
    }
}

fun LibraryViewModel.clearAllCloudWatchHistory() {
    val repo = cloudLibraryRepository ?: return
    viewModelScope.launch {
        updateUiState { it.copy(cloudWatchItems = emptyList(), cloudWatchNextCursor = null) }
        repo.deleteCloudWatchHistory(null).onFailure { err ->
            Log.e("LibraryCloudHistory", "クラウド視聴履歴全消去失敗: ${err.message}", err)
            loadCloudWatchHistory(reset = true)
        }
    }
}

// ==========================================
// 2. いいね！履歴 (/v1/users/me/likes)
// ==========================================
fun LibraryViewModel.loadCloudLikeHistory(reset: Boolean = true) {
    val repo = cloudLibraryRepository ?: return
    val current = uiState.value
    if (reset && current.isCloudLikeLoading) return
    if (!reset && (current.isCloudLikeLoading || current.isCloudLikeLoadingMore || !current.cloudLikeHasMore)) return

    val targetPage = if (reset) 1 else current.cloudLikePage + 1
    cloudHistoryJob?.cancel()
    cloudHistoryJob = viewModelScope.launch {
        updateUiState {
            if (reset) it.copy(isCloudLikeLoading = true, cloudLikeError = null)
            else it.copy(isCloudLikeLoadingMore = true)
        }
        repo.getCloudLikeHistory(page = targetPage).onSuccess { page ->
            updateUiState { state ->
                val merged = if (reset) page.items else (state.cloudLikeItems + page.items).distinctBy { it.video.contentId }
                state.copy(
                    cloudLikeItems = merged,
                    cloudLikePage = targetPage,
                    cloudLikeHasMore = page.hasNext,
                    cloudLikeHasLoaded = true,
                    isCloudLikeLoading = false,
                    isCloudLikeLoadingMore = false,
                    cloudLikeError = null
                )
            }
        }.onFailure { err ->
            Log.e("LibraryCloudHistory", "loadCloudLikeHistory失敗: ${err.message}", err)
            updateUiState {
                it.copy(
                    isCloudLikeLoading = false,
                    isCloudLikeLoadingMore = false,
                    cloudLikeError = if (reset) (err.localizedMessage ?: "いいね！履歴の取得に失敗しました") else it.cloudLikeError
                )
            }
        }
    }
}

fun LibraryViewModel.unlikeHistoryVideo(videoId: String) {
    val repo = cloudLibraryRepository ?: return
    viewModelScope.launch {
        updateUiState { s -> s.copy(cloudLikeItems = s.cloudLikeItems.filterNot { it.video.contentId == videoId }) }
        repo.unlikeCloudVideo(videoId).onFailure { err ->
            Log.e("LibraryCloudHistory", "いいね！解除失敗: ${err.message}", err)
            loadCloudLikeHistory(reset = true)
        }
    }
}

// ==========================================
// 3. ニコられた履歴 (/v1/users/me/nicoru/receive)
// ==========================================
fun LibraryViewModel.toggleNicoruByVideoOwner(enabled: Boolean) {
    updateUiState { it.copy(nicoruByVideoOwner = enabled) }
    loadNicoruPassiveHistory(reset = true)
}

fun LibraryViewModel.loadNicoruPassiveHistory(reset: Boolean = true) {
    val repo = cloudLibraryRepository ?: return
    val current = uiState.value
    if (reset && current.isNicoruPassiveLoading) return
    if (!reset && (current.isNicoruPassiveLoading || current.isNicoruPassiveLoadingMore || current.nicoruPassiveNextCursor == null)) return

    cloudHistoryJob?.cancel()
    cloudHistoryJob = viewModelScope.launch {
        updateUiState {
            if (reset) it.copy(isNicoruPassiveLoading = true, nicoruPassiveError = null)
            else it.copy(isNicoruPassiveLoadingMore = true)
        }
        if (reset) {
            launch {
                repo.getNicoruReceiveCount().onSuccess { count ->
                    updateUiState { it.copy(nicoruPassiveTotalCount = count) }
                }
            }
        }
        val cursor = if (reset) null else current.nicoruPassiveNextCursor
        val byOwner = uiState.value.nicoruByVideoOwner
        repo.getNicoruReceiveHistory(cursor = cursor, byVideoOwner = byOwner).onSuccess { page ->
            updateUiState { state ->
                val merged = if (reset) page.items else (state.nicoruPassiveItems + page.items).distinctBy { it.id }
                state.copy(
                    nicoruPassiveItems = merged,
                    nicoruPassiveNextCursor = page.nextCursor,
                    nicoruPassiveHasLoaded = true,
                    isNicoruPassiveLoading = false,
                    isNicoruPassiveLoadingMore = false,
                    nicoruPassiveError = null
                )
            }
        }.onFailure { err ->
            Log.e("LibraryCloudHistory", "loadNicoruPassiveHistory失敗: ${err.message}", err)
            updateUiState {
                it.copy(
                    isNicoruPassiveLoading = false,
                    isNicoruPassiveLoadingMore = false,
                    nicoruPassiveError = if (reset) (err.localizedMessage ?: "ニコられた履歴の取得に失敗しました") else it.nicoruPassiveError
                )
            }
        }
    }
}

// ==========================================
// 4. ニコった履歴 (/v1/users/me/nicoru/send)
// ==========================================
fun LibraryViewModel.loadNicoruActiveHistory(reset: Boolean = true) {
    val repo = cloudLibraryRepository ?: return
    val current = uiState.value
    if (reset && current.isNicoruActiveLoading) return
    if (!reset && (current.isNicoruActiveLoading || current.isNicoruActiveLoadingMore || current.nicoruActiveNextCursor == null)) return

    cloudHistoryJob?.cancel()
    cloudHistoryJob = viewModelScope.launch {
        updateUiState {
            if (reset) it.copy(isNicoruActiveLoading = true, nicoruActiveError = null)
            else it.copy(isNicoruActiveLoadingMore = true)
        }
        val cursor = if (reset) null else current.nicoruActiveNextCursor
        repo.getNicoruSendHistory(cursor = cursor).onSuccess { page ->
            updateUiState { state ->
                val merged = if (reset) page.items else (state.nicoruActiveItems + page.items).distinctBy { it.id }
                state.copy(
                    nicoruActiveItems = merged,
                    nicoruActiveNextCursor = page.nextCursor,
                    nicoruActiveHasLoaded = true,
                    isNicoruActiveLoading = false,
                    isNicoruActiveLoadingMore = false,
                    nicoruActiveError = null
                )
            }
        }.onFailure { err ->
            Log.e("LibraryCloudHistory", "loadNicoruActiveHistory失敗: ${err.message}", err)
            updateUiState {
                it.copy(
                    isNicoruActiveLoading = false,
                    isNicoruActiveLoadingMore = false,
                    nicoruActiveError = if (reset) (err.localizedMessage ?: "ニコった履歴の取得に失敗しました") else it.nicoruActiveError
                )
            }
        }
    }
}

fun LibraryViewModel.deleteNicoruSendHistory(nicoruId: String) {
    val repo = cloudLibraryRepository ?: return
    viewModelScope.launch {
        updateUiState { s -> s.copy(nicoruActiveItems = s.nicoruActiveItems.filterNot { it.id == nicoruId }) }
        repo.deleteNicoruSendItem(nicoruId).onFailure { err ->
            Log.e("LibraryCloudHistory", "ニコる解除失敗: ${err.message}", err)
            loadNicoruActiveHistory(reset = true)
        }
    }
}
