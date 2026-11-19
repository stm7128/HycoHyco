package com.github.stm7128.hycohyco.ui.main

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.LocalIsInPipMode
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoBottomBar
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoBottomBarItem
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoScaffold
import com.github.stm7128.hycohyco.ui.home.HomeScreen
import com.github.stm7128.hycohyco.ui.library.LibraryScreen
import com.github.stm7128.hycohyco.ui.player.PlayerDisplayMode
import com.github.stm7128.hycohyco.ui.player.PlayerViewModel
import com.github.stm7128.hycohyco.ui.player.setPlaybackQueue
import com.github.stm7128.hycohyco.ui.search.SearchScreen
import com.github.stm7128.hycohyco.ui.search.SearchViewModel
import com.github.stm7128.hycohyco.ui.settings.SettingsScreen
import com.github.stm7128.hycohyco.ui.shorts.ShortsPagerScreen
import com.github.stm7128.hycohyco.ui.shorts.ShortsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * メイン画面骨格コンポーザブル
 *
 * デュアル・デザインシステム対応のトップバー・ボトムナビゲーション（ショートタブON/OFF対応）を管理し、
 * 各タブ画面・通常展開プレイヤー・ミニプレイヤー・ショート連続視聴プレイヤーを統合制御する。
 */
@Composable
fun MainShellScreen(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    themeSettings: ThemeSettings,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onDarkThemeConfigChanged: (DarkThemeConfig) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onAmoledBlackChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDefaultHomeModeChanged: (HomeMode) -> Unit = {},
    gestureMode: PlayerGestureMode = PlayerGestureMode.FULLSCREEN_ONLY,
    onGestureModeChanged: (PlayerGestureMode) -> Unit = {},
    showShortsBottomTab: Boolean = true,
    openedUserId: Long? = null,
    onCloseUserPage: () -> Unit = {},
    openedMylistId: Long? = null,
    onCloseMylist: () -> Unit = {},
    onOpenMylist: (Long) -> Unit = {},
    openedSeriesId: Long? = null,
    onCloseSeries: () -> Unit = {},
    onOpenSeries: (Long) -> Unit = {},
    playerViewModel: PlayerViewModel = koinViewModel(),
    shortsViewModel: ShortsViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val isInPip = LocalIsInPipMode.current
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val shortsUiState by shortsViewModel.uiState.collectAsStateWithLifecycle()

    var isFullscreen by remember { mutableStateOf(false) }
    val activity = context as? Activity
    val isPortraitVideo = playerUiState.videoDetail?.isPortraitVideo == true

    val applyFullscreenSystemState: (Boolean, Boolean) -> Unit = { fullscreenActive, portrait ->
        activity?.let { act ->
            act.requestedOrientation = if (fullscreenActive && !portrait) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            val windowInsetsController = WindowCompat.getInsetsController(act.window, act.window.decorView)
            if (fullscreenActive) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val toggleFullscreen: () -> Unit = {
        val next = !isFullscreen
        isFullscreen = next
        applyFullscreenSystemState(next, isPortraitVideo)
    }

    // 縦型・横型動画切り替え時および最小化・クローズ時の画面回転/システムバー同期
    LaunchedEffect(isFullscreen, isPortraitVideo) {
        if (isFullscreen) {
            applyFullscreenSystemState(true, isPortraitVideo)
        }
    }
    LaunchedEffect(playerUiState.displayMode) {
        if (playerUiState.displayMode != PlayerDisplayMode.EXPANDED && isFullscreen) {
            isFullscreen = false
            applyFullscreenSystemState(false, false)
        }
    }

    // ボトムバー「ショート」タブ切替制御 & 排他再生
    LaunchedEffect(showShortsBottomTab, selectedTab) {
        if (!showShortsBottomTab && selectedTab == MainTab.SHORTS) {
            onSelectTab(MainTab.HOME)
        }
    }
    LaunchedEffect(selectedTab, shortsUiState.isActiveOverlay) {
        if (!shortsUiState.isActiveOverlay) {
            if (selectedTab == MainTab.SHORTS) {
                playerViewModel.closePlayer()
                shortsViewModel.ensureTabFeedLoaded(context)
            } else {
                shortsViewModel.stopShortsPlayback()
            }
        }
    }

    val openShort: (String, List<VideoSummary>) -> Unit = { videoId, queue ->
        playerViewModel.closePlayer()
        shortsViewModel.openShortById(videoId = videoId, context = context, initialQueue = queue)
    }

    val playStandardOrShort: (String, Long?, Boolean) -> Unit = { videoId, posMs, forceOffline ->
        if (videoId.startsWith("ss", ignoreCase = true) && !forceOffline && posMs == null) {
            openShort(videoId, emptyList())
        } else {
            shortsViewModel.closeOverlay()
            if (selectedTab == MainTab.SHORTS) {
                shortsViewModel.stopShortsPlayback()
                onSelectTab(MainTab.HOME)
            }
            playerViewModel.playVideo(
                videoId = videoId,
                context = context,
                initialPositionMs = posMs,
                forceOffline = forceOffline
            )
        }
    }

    val openBaseVideoFromShorts: (String) -> Unit = { baseVideoId ->
        shortsViewModel.closeOverlay()
        if (selectedTab == MainTab.SHORTS) {
            shortsViewModel.stopShortsPlayback()
            onSelectTab(MainTab.HOME)
        }
        playerViewModel.playVideo(videoId = baseVideoId, context = context)
    }

    // 通常動画タグタップ: ミニプレイヤー化して見ながら検索タブへ遷移
    val searchVideoTag: (String) -> Unit = { tag ->
        playerViewModel.minimizePlayer()
        onSelectTab(MainTab.SEARCH)
        searchViewModel.searchTag(tag = tag, isShorts = false)
    }

    // ショート動画タグタップ: ショートを閉じてショートタグ検索へ遷移
    val searchShortTag: (String) -> Unit = { tag ->
        shortsViewModel.closeOverlay()
        shortsViewModel.stopShortsPlayback()
        onSelectTab(MainTab.SEARCH)
        searchViewModel.searchTag(tag = tag, isShorts = true)
    }

    BackHandler(enabled = isFullscreen && !isInPip) { toggleFullscreen() }
    BackHandler(enabled = !shortsUiState.isActiveOverlay && selectedTab == MainTab.SHORTS && !isInPip) {
        onSelectTab(MainTab.HOME)
    }

    val visibleTabs = remember(showShortsBottomTab) {
        if (showShortsBottomTab) MainTab.entries else MainTab.entries.filter { it != MainTab.SHORTS }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (!isInPip && !isFullscreen) {
            HycoScaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (selectedTab != MainTab.SHORTS) {
                        MainShellTopBar(selectedTab = selectedTab)
                    }
                },
                bottomBar = {
                    HycoBottomBar {
                        visibleTabs.forEach { tab ->
                            HycoBottomBarItem(
                                selected = selectedTab == tab,
                                onClick = { onSelectTab(tab) },
                                icon = tab.icon,
                                label = tab.title,
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    when (selectedTab) {
                        MainTab.HOME -> HomeScreen(
                            onPlayVideo = { id -> playStandardOrShort(id, null, false) },
                            onOpenShort = openShort,
                            onSearchTag = { tag ->
                                onSelectTab(MainTab.SEARCH)
                                searchViewModel.searchTag(tag = tag, isShorts = false)
                            }
                        )

                        MainTab.SHORTS -> ShortsPagerScreen(
                            viewModel = shortsViewModel,
                            isOverlayMode = false,
                            onClose = { onSelectTab(MainTab.HOME) },
                            onOpenBaseVideo = openBaseVideoFromShorts,
                            onSearchShortTag = searchShortTag
                        )

                        MainTab.SEARCH -> SearchScreen(
                            onPlayVideo = { id -> playStandardOrShort(id, null, false) },
                            onOpenShort = openShort,
                            onOpenMylist = onOpenMylist,
                            onOpenSeries = onOpenSeries,
                            viewModel = searchViewModel
                        )

                        MainTab.LIBRARY -> LibraryScreen(
                            isPlayerExpanded = playerUiState.displayMode == PlayerDisplayMode.EXPANDED,
                            onPlayVideo = { id -> playStandardOrShort(id, null, false) },
                            onPlayVideoAtPosition = { id, posMs -> playStandardOrShort(id, posMs, false) },
                            onPlayDownloadedVideo = { id -> playStandardOrShort(id, null, true) },
                            onPlayPlaylist = { videoIds, startIndex ->
                                shortsViewModel.closeOverlay()
                                playerViewModel.setPlaybackQueue(videoIds)
                                if (videoIds.isNotEmpty() && startIndex in videoIds.indices) {
                                    playerViewModel.playVideo(videoIds[startIndex], context)
                                }
                            }
                        )

                        MainTab.SETTINGS -> SettingsScreen(
                            themeSettings = themeSettings,
                            onThemeModeChanged = onThemeModeChanged,
                            onDarkThemeConfigChanged = onDarkThemeConfigChanged,
                            onDynamicColorChanged = onDynamicColorChanged,
                            onAmoledBlackChanged = onAmoledBlackChanged,
                            onDefaultHomeModeChanged = onDefaultHomeModeChanged,
                            gestureMode = gestureMode,
                            onGestureModeChanged = onGestureModeChanged,
                            onPlayVideo = { id -> playStandardOrShort(id, null, false) }
                        )
                    }
                }
            }
        }

        MainShellAuxiliaryOverlays(
            shortsViewModel = shortsViewModel, shortsUiState = shortsUiState, isInPip = isInPip,
            onOpenBaseVideoFromShorts = openBaseVideoFromShorts, onSearchShortTag = searchShortTag,
            openedUserId = openedUserId, onCloseUserPage = onCloseUserPage,
            onPlayVideoFromUser = { id -> playStandardOrShort(id, null, false) },
            openedMylistId = openedMylistId, onCloseMylist = onCloseMylist,
            onPlayVideoFromMylist = { id -> playStandardOrShort(id, null, false) },
            openedSeriesId = openedSeriesId, onCloseSeries = onCloseSeries,
            onPlayVideoFromSeries = { id -> playStandardOrShort(id, null, false) },
        )

        MainShellPlayerOverlays(
            playerViewModel = playerViewModel, playerUiState = playerUiState,
            isInPip = isInPip, isFullscreen = isFullscreen, onToggleFullscreen = toggleFullscreen,
            onPlayVideo = { id -> playStandardOrShort(id, null, false) },
            onSearchTag = searchVideoTag, onOpenMylist = onOpenMylist,
            onOpenSeries = onOpenSeries, context = context
        )
    }
}
