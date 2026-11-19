package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip

/**
 * 履歴統合コンテナ (端末履歴 / クラウド視聴 / いいね！ / ニコられた / ニコった)
 */
@Composable
internal fun HistoryContainer(
    uiState: LibraryUiState,
    onSelectHistorySubTab: (HistorySubTab) -> Unit,
    onPlayVideo: (String) -> Unit,
    onPlayVideoAtPosition: (String, Long?) -> Unit,
    viewModel: LibraryViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistorySubTab.entries.forEach { subTab ->
                HycoFilterChip(
                    selected = uiState.selectedHistorySubTab == subTab,
                    onClick = { onSelectHistorySubTab(subTab) },
                    label = subTab.title
                )
            }
        }

        if (uiState.selectedHistorySubTab == HistorySubTab.LOCAL) {
            HistoryListView(
                historyItems = uiState.historyItems,
                onPlayVideo = onPlayVideo,
                onDeleteHistory = { viewModel.deleteHistoryItem(it) },
                onClearAll = { viewModel.clearAllHistory() }
            )
            return
        }

        if (!uiState.isUserLoggedIn) {
            LoginPromptBox("設定画面からニコニコアカウントにログインすると、公式の視聴・いいね！・ニコる履歴を閲覧できます")
            return
        }

        when (uiState.selectedHistorySubTab) {
            HistorySubTab.LOCAL -> Unit
            HistorySubTab.CLOUD_WATCH -> CloudWatchHistoryView(
                items = uiState.cloudWatchItems,
                isLoading = uiState.isCloudWatchLoading,
                isLoadingMore = uiState.isCloudWatchLoadingMore,
                hasMore = uiState.cloudWatchNextCursor != null,
                errorMessage = uiState.cloudWatchError,
                onRefresh = { viewModel.loadCloudWatchHistory(reset = true) },
                onLoadMore = { viewModel.loadCloudWatchHistory(reset = false) },
                onPlayVideo = onPlayVideoAtPosition,
                onDeleteItem = { viewModel.deleteCloudWatchHistoryItem(it) },
                onClearAll = { viewModel.clearAllCloudWatchHistory() }
            )
            HistorySubTab.CLOUD_LIKE -> CloudLikeHistoryView(
                items = uiState.cloudLikeItems,
                isLoading = uiState.isCloudLikeLoading,
                isLoadingMore = uiState.isCloudLikeLoadingMore,
                hasMore = uiState.cloudLikeHasMore,
                errorMessage = uiState.cloudLikeError,
                onRefresh = { viewModel.loadCloudLikeHistory(reset = true) },
                onLoadMore = { viewModel.loadCloudLikeHistory(reset = false) },
                onPlayVideo = onPlayVideoAtPosition,
                onUnlikeVideo = { viewModel.unlikeHistoryVideo(it) }
            )
            HistorySubTab.CLOUD_NICORU_PASSIVE -> CloudNicoruHistoryView(
                isPassive = true,
                items = uiState.nicoruPassiveItems,
                isLoading = uiState.isNicoruPassiveLoading,
                isLoadingMore = uiState.isNicoruPassiveLoadingMore,
                hasMore = uiState.nicoruPassiveNextCursor != null,
                errorMessage = uiState.nicoruPassiveError,
                totalReceiveCount = uiState.nicoruPassiveTotalCount,
                byVideoOwner = uiState.nicoruByVideoOwner,
                onToggleByVideoOwner = { viewModel.toggleNicoruByVideoOwner(it) },
                onRefresh = { viewModel.loadNicoruPassiveHistory(reset = true) },
                onLoadMore = { viewModel.loadNicoruPassiveHistory(reset = false) },
                onPlayVideoAtPosition = onPlayVideoAtPosition
            )
            HistorySubTab.CLOUD_NICORU_ACTIVE -> CloudNicoruHistoryView(
                isPassive = false,
                items = uiState.nicoruActiveItems,
                isLoading = uiState.isNicoruActiveLoading,
                isLoadingMore = uiState.isNicoruActiveLoadingMore,
                hasMore = uiState.nicoruActiveNextCursor != null,
                errorMessage = uiState.nicoruActiveError,
                onRefresh = { viewModel.loadNicoruActiveHistory(reset = true) },
                onLoadMore = { viewModel.loadNicoruActiveHistory(reset = false) },
                onPlayVideoAtPosition = onPlayVideoAtPosition,
                onDeleteNicoruSend = { viewModel.deleteNicoruSendHistory(it) }
            )
        }
    }
}

/** マイリスト統合コンテナ (ローカル / あとで見る / 公式マイリスト) */
@Composable
internal fun MylistContainer(
    uiState: LibraryUiState,
    onSelectMylistSubTab: (MylistSubTab) -> Unit,
    onVideoClick: (String) -> Unit,
    viewModel: LibraryViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MylistSubTab.entries.forEach { subTab ->
                HycoFilterChip(
                    selected = uiState.selectedMylistSubTab == subTab,
                    onClick = { onSelectMylistSubTab(subTab) },
                    label = subTab.title
                )
            }
        }

        if (uiState.selectedMylistSubTab == MylistSubTab.LOCAL) {
            PlaylistListView(
                playlists = uiState.playlists,
                onOpenPlaylist = { viewModel.openPlaylistDetail(it) },
                onCreatePlaylist = { viewModel.createPlaylist(it) },
                onDeletePlaylist = { viewModel.deletePlaylist(it) }
            )
            return
        }

        if (!uiState.isUserLoggedIn) {
            LoginPromptBox("設定画面からログインすると、公式の「あとで見る」やマイリストと同期できます")
            return
        }

        when (uiState.selectedMylistSubTab) {
            MylistSubTab.LOCAL -> Unit
            MylistSubTab.WATCH_LATER -> WatchLaterListView(
                videos = uiState.watchLaterVideos,
                isLoading = uiState.isCloudLoading,
                onVideoClick = onVideoClick,
                onDeleteVideo = { viewModel.deleteWatchLaterItem(it) },
                onRefresh = { viewModel.refreshCloudData() }
            )
            MylistSubTab.CLOUD_MYLIST -> CloudMylistListView(
                mylists = uiState.cloudMylists,
                isLoading = uiState.isCloudLoading,
                onMylistClick = { viewModel.openCloudMylist(it) },
                onRefresh = { viewModel.refreshCloudData() }
            )
        }
    }
}

@Composable
private fun LoginPromptBox(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = HycoTheme.colors.onBackgroundVariant,
            fontSize = 13.sp,
            modifier = Modifier.padding(24.dp)
        )
    }
}
