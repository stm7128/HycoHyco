package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.github.stm7128.hycohyco.domain.model.VideoSeriesItem
import com.github.stm7128.hycohyco.domain.model.VideoSeriesNav
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard

/**
 * 視聴画面のシリーズ巡回セクション (シリーズ名 ＆ 前後動画ナビゲーション)
 */
@Composable
fun SeriesSection(
    series: VideoSeriesNav,
    onPlayVideo: (String) -> Unit,
    textColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    HycoCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // シリーズヘッダー
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "シリーズ: ${series.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 前後の動画カード
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 前の動画
                series.prev?.let { prev ->
                    SeriesNavCard(
                        video = prev,
                        isPrev = true,
                        onClick = { onPlayVideo(prev.id) },
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        modifier = Modifier.weight(1f)
                    )
                } ?: run {
                    if (series.next != null) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // 次の動画
                series.next?.let { next ->
                    SeriesNavCard(
                        video = next,
                        isPrev = false,
                        onClick = { onPlayVideo(next.id) },
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesNavCard(
    video: VideoSeriesItem,
    isPrev: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isPrev) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isPrev) "前の動画" else "次の動画",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HycoAsyncImage(
                url = video.thumbnailUrl,
                contentDescription = video.title,
                modifier = Modifier
                    .size(width = 64.dp, height = 36.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = video.title,
                fontSize = 11.sp,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}
