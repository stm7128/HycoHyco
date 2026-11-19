package com.github.stm7128.hycohyco.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.hycoBounceClick

/**
 * ホーム画面の「ショート」タブ用 9:16 縦型2列グリッドフィード
 */
@Composable
fun HomeShortsFeedView(
    videos: List<VideoSummary>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onOpenShort: (videoId: String, queue: List<VideoSummary>) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HycoTheme.colors

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "おすすめのニコニコショート",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
            HomeRefreshButton(
                isLoading = isLoading,
                onClick = onRefresh
            )
        }

        HycoPullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            when {
                isLoading && videos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
                errorMessage != null && videos.isEmpty() -> {
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
                videos.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ショート動画がありません",
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(videos, key = { it.contentId }) { video ->
                            HomeShortGridCard(
                                video = video,
                                onClick = { onOpenShort(video.contentId, videos) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeShortGridCard(
    video: VideoSummary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black)
            .hycoBounceClick(onClick = onClick)
    ) {
        HycoAsyncImage(
            url = video.shortThumbnailUrl.ifBlank { video.thumbnailUrl },
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.82f))
                    )
                )
        )

        Surface(
            color = Color.Black.copy(alpha = 0.58f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = video.formattedViewCount,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )
            if (video.ownerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = video.ownerName,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
