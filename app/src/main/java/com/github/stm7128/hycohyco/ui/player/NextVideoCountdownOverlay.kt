package com.github.stm7128.hycohyco.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton

/**
 * YouTube スタイルの次枠自動再生カウントダウンオーバーレイ
 */
@Composable
fun NextVideoCountdownOverlay(
    countdownSeconds: Int?,
    nextVideo: VideoSeriesItem?,
    onCancel: () -> Unit,
    onPlayNow: () -> Unit,
    modifier: Modifier = Modifier,
    totalCountdownSeconds: Int = 5
) {
    AnimatedVisibility(
        visible = countdownSeconds != null && nextVideo != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        if (nextVideo == null || countdownSeconds == null) return@AnimatedVisibility

        Box(
            modifier = Modifier
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xE61E1E1E))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ヘッダー: カウントダウン表示 & 閉じる(キャンセル)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                            CircularProgressIndicator(
                                progress = { countdownSeconds.toFloat() / totalCountdownSeconds.toFloat() },
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f),
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "$countdownSeconds",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "次の動画を自動再生します",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "キャンセル",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 動画プレビュー & アクション
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HycoAsyncImage(
                        url = nextVideo.thumbnailUrl,
                        contentDescription = nextVideo.title,
                        modifier = Modifier
                            .size(width = 80.dp, height = 45.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = nextVideo.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // アクションボタン (キャンセル / 今すぐ再生)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "キャンセル",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onCancel)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    HycoButton(
                        text = "今すぐ再生",
                        onClick = onPlayNow
                    )
                }
            }
        }
    }
}
