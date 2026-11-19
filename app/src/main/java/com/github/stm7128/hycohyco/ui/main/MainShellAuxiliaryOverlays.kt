package com.github.stm7128.hycohyco.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.stm7128.hycohyco.ui.library.MylistDetailSheet
import com.github.stm7128.hycohyco.ui.series.SeriesDetailSheet
import com.github.stm7128.hycohyco.ui.shorts.ShortsPagerScreen
import com.github.stm7128.hycohyco.ui.shorts.ShortsUiState
import com.github.stm7128.hycohyco.ui.shorts.ShortsViewModel
import com.github.stm7128.hycohyco.ui.user.UserDetailSheet

/**
 * メインシェル画面上の補助オーバーレイ（ショート全画面オーバーレイ & Deep Link ユーザー/マイリスト/シリーズ詳細シート）
 */
@Composable
internal fun MainShellAuxiliaryOverlays(
    shortsViewModel: ShortsViewModel,
    shortsUiState: ShortsUiState,
    isInPip: Boolean,
    onOpenBaseVideoFromShorts: (String) -> Unit,
    onSearchShortTag: (String) -> Unit,
    openedUserId: Long?,
    onCloseUserPage: () -> Unit,
    onPlayVideoFromUser: (String) -> Unit,
    openedMylistId: Long? = null,
    onCloseMylist: () -> Unit = {},
    onPlayVideoFromMylist: (String) -> Unit = {},
    openedSeriesId: Long? = null,
    onCloseSeries: () -> Unit = {},
    onPlayVideoFromSeries: (String) -> Unit = {},
) {
    // ショート全画面オーバーレイ (ホームカード / 検索 / DeepLink 起動時)
    AnimatedVisibility(
        visible = shortsUiState.isActiveOverlay && !isInPip,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        ShortsPagerScreen(
            viewModel = shortsViewModel,
            isOverlayMode = true,
            onClose = { shortsViewModel.closeOverlay() },
            onOpenBaseVideo = onOpenBaseVideoFromShorts,
            onSearchShortTag = onSearchShortTag
        )
    }

    // ユーザーページ Deep Link 起動時の投稿者詳細シート
    openedUserId?.let { userId ->
        UserDetailSheet(
            userId = userId,
            onDismissRequest = onCloseUserPage,
            onVideoClick = { videoId ->
                onCloseUserPage()
                onPlayVideoFromUser(videoId)
            }
        )
    }

    // マイリスト Deep Link / 共有 / 検索直接展開時のマイリスト詳細シート
    openedMylistId?.let { mylistId ->
        MylistDetailSheet(
            mylistId = mylistId,
            onDismissRequest = onCloseMylist,
            onVideoClick = { videoId ->
                onCloseMylist()
                onPlayVideoFromMylist(videoId)
            }
        )
    }

    // シリーズ Deep Link / 共有 / 検索直接展開時のシリーズ詳細シート
    openedSeriesId?.let { seriesId ->
        SeriesDetailSheet(
            seriesId = seriesId,
            onDismissRequest = onCloseSeries,
            onVideoClick = { videoId ->
                onCloseSeries()
                onPlayVideoFromSeries(videoId)
            }
        )
    }
}
