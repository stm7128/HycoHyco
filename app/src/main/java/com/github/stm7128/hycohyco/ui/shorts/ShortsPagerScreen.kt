package com.github.stm7128.hycohyco.ui.shorts

import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import com.github.stm7128.hycohyco.player.model.isPlaying
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive

/**
 * ニコニコショート 全画面縦スワイプ (VerticalPager) 視聴画面
 */
@OptIn(UnstableApi::class)
@Composable
fun ShortsPagerScreen(
    viewModel: ShortsViewModel,
    isOverlayMode: Boolean,
    onClose: () -> Unit,
    onOpenBaseVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchShortTag: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackManager.playbackState.collectAsStateWithLifecycle()
    val exoPlayer = viewModel.playbackManager.exoPlayer
    val context = LocalContext.current

    var selectedOwner by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var showPauseFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.feedbackMessage) {
        val msg = uiState.feedbackMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearFeedbackMessage()
    }

    var animPositionMs by remember { mutableLongStateOf(0L) }
    val currentPosState by rememberUpdatedState(playbackState.currentPositionMs)
    LaunchedEffect(playbackState.isPlaying, uiState.currentIndex) {
        if (playbackState.isPlaying) {
            var basePos = currentPosState
            var baseTime = SystemClock.uptimeMillis()
            while (isActive) {
                withFrameMillis {
                    val cur = currentPosState
                    val expected = basePos + (SystemClock.uptimeMillis() - baseTime)
                    if (kotlin.math.abs(cur - expected) > 300L) {
                        basePos = cur
                        baseTime = SystemClock.uptimeMillis()
                        animPositionMs = cur
                    } else {
                        animPositionMs = expected
                    }
                }
            }
        } else {
            animPositionMs = playbackState.currentPositionMs
        }
    }

    LaunchedEffect(showPauseFeedback) {
        if (showPauseFeedback) {
            delay(700L)
            showPauseFeedback = false
        }
    }

    BackHandler(enabled = isOverlayMode) { onClose() }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when {
            uiState.isLoadingFeed && uiState.items.isEmpty() -> {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.Center))
            }

            uiState.items.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "ショート動画を読み込めませんでした",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HycoButton(
                        text = "再読み込み",
                        onClick = { viewModel.ensureTabFeedLoaded(context, forceRefresh = true) }
                    )
                }
            }

            else -> {
                val pagerState = rememberPagerState(
                    initialPage = uiState.currentIndex.coerceIn(0, (uiState.items.size - 1).coerceAtLeast(0)),
                    pageCount = { uiState.items.size }
                )
                val latestItemCount by rememberUpdatedState(uiState.items.size)

                // 外部からの明示的ジャンプ要求 (scrollRequestToken) 時のみスクロールする
                LaunchedEffect(uiState.scrollRequestToken) {
                    val targetIdx = uiState.currentIndex
                    if (targetIdx in uiState.items.indices && pagerState.currentPage != targetIdx) {
                        pagerState.scrollToPage(targetIdx)
                    }
                }

                // ユーザーのスワイプ確定を漏れなく検知して次動画をロード
                LaunchedEffect(pagerState) {
                    snapshotFlow {
                        Triple(pagerState.currentPage, pagerState.isScrollInProgress, latestItemCount)
                    }
                        .distinctUntilChanged()
                        .collect { (page, scrolling, count) ->
                            if (!scrolling && page in 0 until count) {
                                viewModel.onPageSettled(page, context)
                            }
                        }
                }

                VerticalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val item = uiState.items[page]
                    val isCurrentSettled = page == pagerState.currentPage && !pagerState.isScrollInProgress
                    ShortPageContent(
                        item = item,
                        isCurrentSettled = isCurrentSettled,
                        isOverlayMode = isOverlayMode,
                        uiState = uiState,
                        playbackState = playbackState,
                        exoPlayer = exoPlayer,
                        animPositionMs = animPositionMs,
                        showPauseFeedback = showPauseFeedback,
                        onTapSurface = {
                            viewModel.togglePlayPause()
                            showPauseFeedback = true
                        },
                        onClose = onClose,
                        onToggleDanmaku = viewModel::toggleDanmaku,
                        onToggleLike = viewModel::toggleLike,
                        onToggleFollow = viewModel::toggleFollowOwner,
                        onOpenComments = { viewModel.toggleCommentSheet(true) },
                        onOpenPlaylist = { viewModel.togglePlaylistSheet(true) },
                        onOpenShare = { viewModel.toggleShareSheet(true) },
                        onOpenDetail = { viewModel.toggleDetailSheet(true) },
                        onDownload = { viewModel.downloadActiveShort() },
                        onOpenOwnerProfile = { uid, uname -> selectedOwner = uid to uname },
                        onOpenBaseVideo = onOpenBaseVideo,
                        onSeekTo = viewModel::seekTo
                    )
                }
            }
        }

        ShortsDialogsAndSheets(
            uiState = uiState,
            viewModel = viewModel,
            context = context,
            animPositionMs = animPositionMs,
            selectedOwner = selectedOwner,
            onOpenOwnerProfile = { uid, uname -> selectedOwner = uid to uname },
            onDismissOwner = { selectedOwner = null },
            onOpenBaseVideo = onOpenBaseVideo,
            onSearchShortTag = onSearchShortTag
        )
    }
}
