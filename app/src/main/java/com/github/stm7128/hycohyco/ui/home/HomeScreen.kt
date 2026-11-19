package com.github.stm7128.hycohyco.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.RankingGenre
import com.github.stm7128.hycohyco.domain.model.RankingTerm
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoTextField
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import org.koin.androidx.compose.koinViewModel

/**
 * ホーム画面（おすすめ / 人気動画ランキング / フォロー新着 / ショート）
 */
@Composable
fun HomeScreen(
    onPlayVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenShort: (String, List<VideoSummary>) -> Unit = { id, _ -> onPlayVideo(id) },
    onSearchTag: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val textColor = HycoTheme.colors.onBackground

    val homeMode by viewModel.homeMode.collectAsStateWithLifecycle()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val selectedTerm by viewModel.selectedTerm.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRankingRefreshing by viewModel.isRankingRefreshing.collectAsStateWithLifecycle()
    val recommendFeed by viewModel.recommendFeed.collectAsStateWithLifecycle()
    val isRecommendLoading by viewModel.isRecommendLoading.collectAsStateWithLifecycle()
    val recommendError by viewModel.recommendError.collectAsStateWithLifecycle()
    val followFeed by viewModel.followFeed.collectAsStateWithLifecycle()
    val isFollowLoading by viewModel.isFollowLoading.collectAsStateWithLifecycle()
    val followError by viewModel.followError.collectAsStateWithLifecycle()
    val shortsFeed by viewModel.shortsFeed.collectAsStateWithLifecycle()
    val isShortsLoading by viewModel.isShortsLoading.collectAsStateWithLifecycle()
    val shortsError by viewModel.shortsError.collectAsStateWithLifecycle()

    var inputVideoId by remember { mutableStateOf("") }
    var isDirectInputExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // 1. ホームモード切替 (おすすめ / ランキング / フォロー新着 / ショート)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeMode.entries.forEach { mode ->
                HycoFilterChip(
                    selected = homeMode == mode,
                    onClick = { viewModel.selectHomeMode(mode) },
                    label = mode.title
                )
            }
        }

        when (homeMode) {
            HomeMode.RECOMMEND -> HomeRecommendFeedView(
                feed = recommendFeed,
                isUserLoggedIn = isUserLoggedIn,
                isLoading = isRecommendLoading,
                errorMessage = recommendError,
                onRefresh = viewModel::loadRecommendFeed,
                onPlayVideo = onPlayVideo,
                onOpenShort = onOpenShort,
                onTagClick = onSearchTag
            )

            HomeMode.SHORTS -> HomeShortsFeedView(
                videos = shortsFeed,
                isLoading = isShortsLoading,
                errorMessage = shortsError,
                onRefresh = viewModel::loadShortsFeed,
                onOpenShort = onOpenShort
            )

            HomeMode.FOLLOW -> HomeFollowFeedView(
                isUserLoggedIn = isUserLoggedIn,
                followFeed = followFeed,
                isLoading = isFollowLoading,
                errorMessage = followError,
                onRefresh = viewModel::loadFollowFeed,
                onPlayVideo = onPlayVideo,
                onOpenShort = onOpenShort
            )

            HomeMode.RANKING -> {
                // 2. 人気ランキングヘッダー（更新ボタン＋動画ID直接指定）
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "人気ランキング",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { isDirectInputExpanded = !isDirectInputExpanded }) {
                                Text(if (isDirectInputExpanded) "ID入力を閉じる" else "動画ID直接指定")
                            }
                            HomeRefreshButton(
                                isLoading = isRankingRefreshing || uiState is HomeRankingUiState.Loading,
                                onClick = viewModel::refresh
                            )
                        }
                    }

                    if (isDirectInputExpanded) {
                        HycoCard(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HycoTextField(
                                    value = inputVideoId,
                                    onValueChange = { inputVideoId = it.trim() },
                                    placeholder = "例: sm9",
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                HycoButton(
                                    text = "再生",
                                    onClick = {
                                        if (inputVideoId.isNotBlank()) {
                                            onPlayVideo(inputVideoId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. ジャンル選択チップ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RankingGenre.entries.forEach { genre ->
                        HycoFilterChip(
                            selected = selectedGenre == genre,
                            onClick = { viewModel.selectGenre(genre) },
                            label = genre.displayName
                        )
                    }
                }

                // 3.5. 集計期間選択チップ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RankingTerm.entries.forEach { term ->
                        HycoFilterChip(
                            selected = selectedTerm == term,
                            onClick = { viewModel.selectTerm(term) },
                            label = term.displayName
                        )
                    }
                }

                // 4. ランキング動画コンテンツ
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (val state = uiState) {
                        is HomeRankingUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        is HomeRankingUiState.Error -> {
                            HycoPullToRefreshBox(
                                isRefreshing = isRankingRefreshing,
                                onRefresh = viewModel::refresh,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    item {
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        TextButton(onClick = viewModel::refresh) {
                                            Text("再読み込み")
                                        }
                                    }
                                }
                            }
                        }

                        is HomeRankingUiState.Success -> {
                            HycoPullToRefreshBox(
                                isRefreshing = isRankingRefreshing,
                                onRefresh = viewModel::refresh,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(state.videos, key = { it.contentId }) { video ->
                                        VideoItemCard(
                                            video = video,
                                            onClick = { onPlayVideo(video.contentId) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
