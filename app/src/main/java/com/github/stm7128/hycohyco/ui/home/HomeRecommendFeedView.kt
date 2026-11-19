package com.github.stm7128.hycohyco.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.HomeRecommendFeed
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard

/**
 * ホーム画面「おすすめ」タブ用フィードビュー
 * （ニコニコ公式「おすすめ動画」/recommendations 準拠: おすすめタグ上位抽出＋理由タグ表示）
 */
@Composable
fun HomeRecommendFeedView(
    feed: HomeRecommendFeed,
    isUserLoggedIn: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onOpenShort: (String, List<VideoSummary>) -> Unit,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HycoTheme.colors

    Column(modifier = modifier.fillMaxSize()) {
        // ヘッダー行（タイトル＋パーソナライズ案内＋更新ボタン）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isUserLoggedIn) "あなたへのおすすめ" else "おすすめ動画",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground
                )
                Text(
                    text = if (isUserLoggedIn) {
                        "視聴傾向をもとにおすすめの動画を表示中"
                    } else {
                        "設定からログインすると視聴履歴に合わせて最適化されます"
                    },
                    fontSize = 11.sp,
                    color = colors.onBackgroundVariant
                )
            }
            HomeRefreshButton(
                isLoading = isLoading,
                onClick = onRefresh
            )
        }

        // おすすめのタグ上位一覧（公式同様タップでタグ検索）
        if (feed.recommendedTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                feed.recommendedTags.forEach { tag ->
                    HycoFilterChip(
                        selected = false,
                        onClick = { onTagClick(tag) },
                        label = "# $tag"
                    )
                }
            }
        }

        HycoPullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            when {
                isLoading && feed.videos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }

                errorMessage != null && feed.videos.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onRefresh) { Text("再読み込み") }
                            }
                        }
                    }
                }

                feed.videos.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "おすすめ動画がありません",
                                    color = colors.onBackgroundVariant,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onRefresh) { Text("再読み込み") }
                            }
                        }
                    }
                }

                else -> {
                    val shortQueue = feed.videos.map { it.video }.filter { it.isShort }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(feed.videos, key = { it.video.contentId }) { item ->
                            val video = item.video
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                VideoItemCard(
                                    video = video,
                                    onClick = {
                                        if (video.isShort) {
                                            onOpenShort(video.contentId, shortQueue)
                                        } else {
                                            onPlayVideo(video.contentId)
                                        }
                                    }
                                )
                                if (!item.reasonTag.isNullOrBlank()) {
                                    Text(
                                        text = "おすすめタグ: #${item.reasonTag}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.primary,
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.primary.copy(alpha = 0.1f))
                                            .clickable { onTagClick(item.reasonTag) }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
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
