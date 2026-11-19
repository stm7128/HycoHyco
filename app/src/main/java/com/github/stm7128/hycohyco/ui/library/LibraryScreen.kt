package com.github.stm7128.hycohyco.ui.library

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import org.koin.androidx.compose.koinViewModel

/**
 * ライブラリ画面（履歴 [端末/視聴/いいね/ニコられた/ニコった]・マイリスト [ローカル/あとで見る/マイリスト]・ダウンロード）
 */
@Composable
fun LibraryScreen(
    onPlayVideo: (String) -> Unit,
    onPlayPlaylist: (videoIds: List<String>, startIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    isPlayerExpanded: Boolean = false,
    onPlayDownloadedVideo: (String) -> Unit = onPlayVideo,
    onPlayVideoAtPosition: (String, Long?) -> Unit = { id, _ -> onPlayVideo(id) },
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.actionMessage) {
        val message = uiState.actionMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.clearActionMessage()
    }

    val isBackEnabled = !isPlayerExpanded
    if (uiState.activePlaylist != null) {
        BackHandler(enabled = isBackEnabled) { viewModel.closePlaylistDetail() }
    } else if (uiState.activeCloudMylist != null) {
        BackHandler(enabled = isBackEnabled) { viewModel.closeCloudMylist() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.activePlaylist != null) {
            PlaylistDetailView(
                playlistTitle = uiState.activePlaylist!!.title,
                videos = uiState.activePlaylistVideos,
                onBack = { viewModel.closePlaylistDetail() },
                onPlayAll = {
                    val ids = uiState.activePlaylistVideos.map { it.videoId }
                    if (ids.isNotEmpty()) onPlayPlaylist(ids, 0)
                },
                onPlayShuffle = {
                    val ids = uiState.activePlaylistVideos.map { it.videoId }.shuffled()
                    if (ids.isNotEmpty()) onPlayPlaylist(ids, 0)
                },
                onPlayItem = { index ->
                    val ids = uiState.activePlaylistVideos.map { it.videoId }
                    onPlayPlaylist(ids, index)
                },
                onRemoveItem = { videoId ->
                    viewModel.removeVideoFromPlaylist(uiState.activePlaylist!!.id, videoId)
                }
            )
        } else if (uiState.activeCloudMylist != null) {
            val active = uiState.activeCloudMylist!!
            CloudMylistDetailView(
                mylist = active,
                videos = uiState.activeCloudMylistVideos,
                isLoading = uiState.isCloudLoading,
                onBackClick = { viewModel.closeCloudMylist() },
                onVideoClick = onPlayVideo,
                onDeleteVideo = { viewModel.deleteCloudMylistItem(active.id, it) },
                onRefresh = { viewModel.openCloudMylist(active) }
            )
        } else {
            // メイン大分類タブバー (履歴 / マイリスト / ダウンロード)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LibrarySubTab.entries.forEach { tab ->
                    HycoFilterChip(
                        selected = uiState.selectedSubTab == tab,
                        onClick = { viewModel.selectSubTab(tab) },
                        label = tab.title
                    )
                }
            }

            when (uiState.selectedSubTab) {
                LibrarySubTab.HISTORY -> HistoryContainer(
                    uiState = uiState,
                    onSelectHistorySubTab = { viewModel.selectHistorySubTab(it) },
                    onPlayVideo = onPlayVideo,
                    onPlayVideoAtPosition = onPlayVideoAtPosition,
                    viewModel = viewModel
                )

                LibrarySubTab.MYLIST -> MylistContainer(
                    uiState = uiState,
                    onSelectMylistSubTab = { viewModel.selectMylistSubTab(it) },
                    onVideoClick = onPlayVideo,
                    viewModel = viewModel
                )

                LibrarySubTab.DOWNLOADS -> DownloadListView(
                    downloads = uiState.downloads,
                    totalSizeBytes = uiState.totalDownloadSizeBytes,
                    onPlayVideo = onPlayDownloadedVideo,
                    onDeleteDownload = { viewModel.deleteDownload(it) }
                )
            }
        }
    }
}
