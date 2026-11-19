package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard

/**
 * 視聴画面下部に表示する関連動画 (レコメンド) リストセクション
 *
 * @param recommendVideos 関連動画リスト
 * @param isLoading 読み込み中かどうか
 * @param onPlayVideo 動画再生コールバック (videoId)
 * @param secondaryColor サブテキストカラー
 */
@Composable
fun RecommendVideoSection(
    recommendVideos: List<VideoSummary>,
    isLoading: Boolean,
    onPlayVideo: (String) -> Unit,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isLoading && recommendVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (recommendVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "関連動画がありません",
                    color = secondaryColor,
                    fontSize = 13.sp
                )
            }
        } else {
            recommendVideos.forEach { video ->
                VideoItemCard(
                    video = video,
                    onClick = { onPlayVideo(video.contentId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
