package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode

/**
 * 動画一覧・検索結果・ランキング・ライブラリ表示用の共通統一動画カード
 *
 * @param video 動画サマリデータ
 * @param onClick タップ時コールバック
 * @param modifier 修飾子
 * @param progressFraction 視聴進捗割合 (0.0f 〜 1.0f, null なら非表示)
 * @param progressDurationText 視聴進捗時間テキスト (例: "00:31 / 01:49", 指定時は投稿日時の代わりに表示)
 * @param trailingAction 右端に配置するカスタムアクション (削除ボタン等)
 */
@Composable
fun VideoItemCard(
    video: VideoSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progressFraction: Float? = null,
    progressDurationText: String? = null,
    trailingAction: @Composable (() -> Unit)? = null
) {
    val themeMode = LocalAppThemeMode.current
    val isHyperOs = themeMode == AppThemeMode.HYPER_OS

    // HyperOS: Xiaomiスクワークル大角丸(18dp) + フラットコンテナ / Material You: M3標準角丸(12dp) + アウトライン
    val cardCornerRadius = if (isHyperOs) 18.dp else 12.dp
    val thumbnailCornerRadius = if (isHyperOs) 14.dp else 8.dp
    val cardBg = if (isHyperOs) HycoTheme.colors.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val cardBorder = if (isHyperOs) {
        BorderStroke(0.5.dp, HycoTheme.colors.outline.copy(alpha = 0.25f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    }

    val titleColor = HycoTheme.colors.onSurface
    val subtitleColor = HycoTheme.colors.onSurfaceVariant

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cardCornerRadius))
            .hycoBounceClick(onClick = onClick),
        shape = RoundedCornerShape(cardCornerRadius),
        color = cardBg,
        border = cardBorder,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 16:9 サムネイル領域
            Box(
                modifier = Modifier
                    .width(136.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(thumbnailCornerRadius))
            ) {
                HycoAsyncImage(
                    url = video.thumbnailUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxWidth()
                )

                // 動画の長さバッジ (HyperOS: Xiaomiカプセルピル型 / M3: 標準角丸)
                if (video.lengthSeconds > 0) {
                    val badgeShape = if (isHyperOs) RoundedCornerShape(percent = 50) else RoundedCornerShape(4.dp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(color = Color.Black.copy(alpha = 0.75f), shape = badgeShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = video.formattedDuration,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 視聴進捗バー (赤ゲージ)
                if (progressFraction != null && progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                                .height(3.dp)
                                .background(Color(0xFFE53935))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // タイトル & 投稿者 & 統計情報
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 1.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // 1. タイトル (最大 2 行)
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 17.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = titleColor,
                    fontSize = 13.5.sp
                )

                // 2. 投稿者名
                if (video.ownerName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "投稿者",
                            modifier = Modifier.size(12.dp),
                            tint = subtitleColor
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = video.ownerName,
                            fontSize = 11.sp,
                            color = subtitleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 3. 統計情報バッジ (再生数 • コメント数 • いいね数 • マイリスト数 • 投稿日時/進捗)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 再生数
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "再生数",
                            modifier = Modifier.size(12.dp),
                            tint = subtitleColor
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        Text(text = video.formattedViewCount, fontSize = 10.5.sp, color = subtitleColor)
                    }

                    // コメント数
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "コメント数",
                            modifier = Modifier.size(11.dp),
                            tint = subtitleColor
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = video.formattedCommentCount, fontSize = 10.5.sp, color = subtitleColor)
                    }

                    // いいね数
                    if (video.likeCounter > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "いいね数",
                                modifier = Modifier.size(10.5.dp),
                                tint = subtitleColor
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = video.formattedLikeCount, fontSize = 10.5.sp, color = subtitleColor)
                        }
                    }

                    // マイリスト数
                    if (video.mylistCounter > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "マイリスト数",
                                modifier = Modifier.size(10.5.dp),
                                tint = subtitleColor
                            )
                            Spacer(modifier = Modifier.width(1.dp))
                            Text(text = video.formattedMylistCount, fontSize = 10.5.sp, color = subtitleColor)
                        }
                    }

                    // 進捗時間 または 投稿日
                    if (!progressDurationText.isNullOrBlank()) {
                        Text(
                            text = progressDurationText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE53935)
                        )
                    } else if (video.formattedRelativeTime.isNotBlank()) {
                        Text(
                            text = video.formattedRelativeTime,
                            fontSize = 10.sp,
                            color = subtitleColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // 4. 右端カスタムアクション (プレイリスト削除等)
            trailingAction?.let { action ->
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    action()
                }
            }
        }
    }
}
