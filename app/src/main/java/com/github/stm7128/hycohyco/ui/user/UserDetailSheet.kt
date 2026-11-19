package com.github.stm7128.hycohyco.ui.user

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import com.github.stm7128.hycohyco.ui.common.HycoShareUtils
import com.github.stm7128.hycohyco.ui.common.HycoUrlShareSheet
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * 投稿者・ユーザー詳細情報ボトムシート（投稿動画・シリーズ・マイリスト・フォロー中・フォロワー）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailSheet(
    userId: Long,
    initialNickname: String = "",
    onDismissRequest: () -> Unit,
    onVideoClick: (String) -> Unit,
    cloudLibraryRepository: CloudLibraryRepository = koinInject()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val closeSheet: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
            onDismissRequest()
        }
    }

    var userStack by remember(userId) { mutableStateOf(listOf(userId)) }
    val currentUserId = userStack.lastOrNull() ?: userId

    val state = rememberUserDetailState(userId = currentUserId, repo = cloudLibraryRepository)
    var selectedTab by remember(currentUserId) { mutableIntStateOf(0) }
    var showMylistShareSheet by remember { mutableStateOf(false) }
    var showSeriesShareSheet by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 3) state.ensureFolloweesLoaded()
        else if (selectedTab == 4) state.ensureFollowersLoaded()
    }

    val canPopStack = userStack.size > 1
    val hasDrillDown = state.selectedSeries != null || state.selectedMylist != null

    // シリーズ/マイリスト詳細、またはユーザー遷移スタックの戻る処理 (C-10: シート再帰破綻根絶)
    BackHandler(enabled = hasDrillDown || canPopStack) {
        if (state.selectedSeries != null) state.selectSeries(null)
        else if (state.selectedMylist != null) state.selectMylist(null)
        else if (canPopStack) userStack = userStack.dropLast(1)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ユーザープロフィールヘッダー
            UserHeaderSection(
                userId = currentUserId,
                profile = state.profile,
                fallbackName = if (currentUserId == userId) initialNickname else "",
                followeesCount = state.followees.size,
                followersCount = state.followers.size,
                onClose = {
                    if (canPopStack) {
                        userStack = userStack.dropLast(1)
                    } else {
                        closeSheet()
                    }
                },
                onPlayVideo = { videoId ->
                    closeSheet()
                    onVideoClick(videoId)
                },
                onFolloweesClick = {
                    state.selectedSeries = null
                    state.selectedMylist = null
                    selectedTab = 3
                },
                onFollowersClick = {
                    state.selectedSeries = null
                    state.selectedMylist = null
                    selectedTab = 4
                },
                onUserClick = { newId -> userStack = userStack + newId },
                onOpenUrl = { url -> runCatching { uriHandler.openUri(url) } }
            )

            // ドリルダウン表示中でない場合のみタブバーを表示
            if (state.selectedSeries == null && state.selectedMylist == null) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("動画 (${state.videos.size}${if (state.hasMoreVideos) "+" else ""})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("シリーズ (${state.seriesList.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("マイリスト (${state.mylists.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("フォロー中 (${state.followees.size}${if (state.hasMoreFollowees) "+" else ""})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = {
                            val countStr = state.profile?.followerCount?.takeIf { it > 0 }?.toString()
                                ?: "${state.followers.size}${if (state.hasMoreFollowers) "+" else ""}"
                            Text("フォロワー ($countStr)", fontSize = 12.sp)
                        }
                    )
                }
            }

            // タブコンテンツまたはドリルダウン詳細ビュー
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                } else if (state.selectedSeries != null) {
                    UserItemDetailView(
                        title = state.selectedSeries!!.title,
                        subtitle = "${state.selectedSeries!!.itemsCount} 本の動画",
                        videos = state.detailVideos,
                        isLoading = state.isDetailLoading,
                        onBack = { state.selectSeries(null) },
                        onVideoClick = { videoId ->
                            closeSheet()
                            onVideoClick(videoId)
                        },
                        onShareClick = { showSeriesShareSheet = true }
                    )
                } else if (state.selectedMylist != null) {
                    UserItemDetailView(
                        title = state.selectedMylist!!.name,
                        subtitle = "${state.selectedMylist!!.itemsCount} 件の動画",
                        videos = state.detailVideos,
                        isLoading = state.isDetailLoading,
                        onBack = { state.selectMylist(null) },
                        onVideoClick = { videoId ->
                            closeSheet()
                            onVideoClick(videoId)
                        },
                        onShareClick = { showMylistShareSheet = true }
                    )
                } else {
                    when (selectedTab) {
                        0 -> UserVideosTab(
                            videos = state.videos,
                            isLoadingMore = state.isLoadingMoreVideos,
                            hasMore = state.hasMoreVideos,
                            onLoadMore = { state.loadMoreVideos() },
                            onVideoClick = {
                                onVideoClick(it)
                                onDismissRequest()
                            }
                        )
                        1 -> UserSeriesTab(
                            seriesList = state.seriesList,
                            onSeriesClick = { state.selectSeries(it) }
                        )
                        2 -> UserMylistsTab(
                            mylists = state.mylists,
                            onMylistClick = { state.selectMylist(it) }
                        )
                        3 -> UserFolloweesTab(
                            followees = state.followees,
                            isLoadingMore = state.isLoadingMoreFollowees,
                            hasMore = state.hasMoreFollowees,
                            onLoadMore = { state.loadMoreFollowees() },
                            onUserClick = { newId -> userStack = userStack + newId }
                        )
                        4 -> UserFollowersTab(
                            followers = state.followers,
                            isLoadingMore = state.isLoadingMoreFollowers,
                            hasMore = state.hasMoreFollowers,
                            onLoadMore = { state.loadMoreFollowers() },
                            onUserClick = { newId -> userStack = userStack + newId }
                        )
                    }
                }
            }
        }
    }

    if (showMylistShareSheet && state.selectedMylist != null) {
        val mylist = state.selectedMylist!!
        HycoUrlShareSheet(
            headerTitle = "マイリストを共有",
            title = mylist.name,
            url = HycoShareUtils.mylistUrl(mylist.id),
            rawId = mylist.id.toString(),
            idLabel = "マイリストID",
            onDismissRequest = { showMylistShareSheet = false }
        )
    }

    if (showSeriesShareSheet && state.selectedSeries != null) {
        val series = state.selectedSeries!!
        HycoUrlShareSheet(
            headerTitle = "シリーズを共有",
            title = series.title,
            url = HycoShareUtils.seriesUrl(series.id),
            rawId = series.id.toString(),
            idLabel = "シリーズID",
            onDismissRequest = { showSeriesShareSheet = false }
        )
    }
}
