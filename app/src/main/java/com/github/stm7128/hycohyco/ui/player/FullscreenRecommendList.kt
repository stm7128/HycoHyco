package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.hycoBounceClick

private val ACCENT_BLUE = Color(0xFF0095FF)
private val MUTED_TEXT = Color(0xFFA6A6A6)

/**
 * 全画面サイドパネル内「関連動画」タブの一覧リスト
 */
@Composable
internal fun SidePanelRecommendList(
    videos: List<VideoSummary>,
    isLoading: Boolean,
    onPlayVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading && videos.isEmpty() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ACCENT_BLUE)
            }
        }

        videos.isEmpty() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "関連動画がありません", color = MUTED_TEXT, fontSize = 13.sp)
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videos, key = { it.contentId }) { video ->
                    FullscreenRecommendCard(
                        video = video,
                        onClick = { onPlayVideo(video.contentId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FullscreenRecommendCard(
    video: VideoSummary,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .hycoBounceClick(onClick = onClick)
            .padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF222222))
        ) {
            HycoAsyncImage(
                url = video.thumbnailUrl,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize()
            )
            if (video.lengthSeconds > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.formattedDuration,
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = video.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (video.ownerName.isNotBlank()) {
                Text(
                    text = video.ownerName,
                    color = MUTED_TEXT,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "再生数",
                    tint = MUTED_TEXT,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = video.formattedViewCount, color = MUTED_TEXT, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Comment,
                    contentDescription = "コメント数",
                    tint = MUTED_TEXT,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = video.formattedCommentCount, color = MUTED_TEXT, fontSize = 11.sp)
            }
        }

        if (video.formattedRelativeTime.isNotBlank()) {
            Text(
                text = video.formattedRelativeTime,
                color = MUTED_TEXT.copy(alpha = 0.8f),
                fontSize = 10.5.sp,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
            )
        }
    }
}
