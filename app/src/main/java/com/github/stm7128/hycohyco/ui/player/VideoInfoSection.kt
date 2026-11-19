package com.github.stm7128.hycohyco.ui.player

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoTagChipGroup

/**
 * 動画詳細情報（タイトル・統計数・投稿者・タグ・概要欄）セクション
 */
@Composable
fun VideoInfoSection(
    detail: VideoDetail,
    textColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    isLiked: Boolean = false,
    likeCount: Long = detail.likeCount,
    isFollowed: Boolean = false,
    isFollowSubmitting: Boolean = false,
    onToggleLike: () -> Unit = {},
    onToggleFollow: () -> Unit = {},
    onPlayVideo: (String) -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onOpenAddToPlaylist: () -> Unit = {},
    onOpenCommentInput: () -> Unit = {},
    onShareVideo: () -> Unit = {},
    onOwnerClick: (Long, String) -> Unit = { _, _ -> },
    onMylistClick: (Long) -> Unit = {},
    onSeriesClick: (Long) -> Unit = {},
    currentPositionMs: Long = 0L,
    isTagSubmitting: Boolean = false,
    onTagClick: (String) -> Unit = {},
    onAddTag: (String) -> Unit = {},
    onDeleteTag: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val openUrlWithPip: (String) -> Unit = { url ->
        (context as? Activity)?.let { act ->
            val w = if (detail.isPortraitVideo) 9 else 16
            val h = if (detail.isPortraitVideo) 16 else 9
            PipHelper.enterPip(act, w, h)
        }
        runCatching { uriHandler.openUri(url) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // タイトル
        Text(
            text = detail.title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            lineHeight = 22.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 統計情報 (再生数・コメント数・いいね数)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatItem(Icons.Default.PlayArrow, "${detail.viewCount} 再生", secondaryColor)
            StatItem(Icons.AutoMirrored.Filled.Comment, "${detail.commentCount}", secondaryColor)
            StatItem(Icons.Default.Favorite, "${detail.likeCount}", secondaryColor)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // アクションボタン列 (いいね！・コメント・リスト保存・共有)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HycoFilterChip(selected = isLiked, onClick = onToggleLike, label = if (isLiked) "♥ $likeCount" else "♡ $likeCount")
            HycoFilterChip(selected = false, onClick = onOpenCommentInput, label = "💬 コメ")
            HycoFilterChip(selected = false, onClick = onOpenAddToPlaylist, label = "+ リスト")
            HycoFilterChip(selected = false, onClick = onShareVideo, label = "⤴ 共有")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 投稿者カード (アイコン画像・ニックネーム・ユーザーID・フォローボタン)
        VideoOwnerCard(
            detail = detail,
            isFollowed = isFollowed,
            isFollowSubmitting = isFollowSubmitting,
            onToggleFollow = onToggleFollow,
            onOwnerClick = onOwnerClick,
            textColor = textColor,
            secondaryColor = secondaryColor,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 動画タグ一覧（タップでタグ検索・長押しで大百科/削除・＋追加）
        VideoTagChipGroup(
            tags = detail.tags,
            isEditable = detail.isTagEditable,
            isSubmitting = isTagSubmitting,
            hasR18Tag = detail.hasR18Tag,
            onTagClick = onTagClick,
            onAddTag = onAddTag,
            onDeleteTag = onDeleteTag,
            onOpenNicodic = openUrlWithPip,
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
        )

        // 概要欄タイムスタンプから自動抽出したチャプターバー
        VideoChapterBar(
            description = detail.description,
            currentPositionMs = currentPositionMs,
            onSeekTo = onSeekTo,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 概要欄カード（動画IDヘッダー & HTMLリッチパース & インラインリンク直タップ）
        DescriptionCard(
            detail = detail,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onPlayVideo = onPlayVideo,
            onSeekTo = onSeekTo,
            onUserClick = { uid -> onOwnerClick(uid, "") },
            onMylistClick = onMylistClick,
            onSeriesClick = onSeriesClick,
            onOpenUrl = openUrlWithPip
        )
    }
}

/** 統計バッジ項目 */
@Composable
private fun StatItem(
    icon: ImageVector,
    text: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = color, fontSize = 12.sp)
    }
}

/** 概要欄カード（ヘッダーに動画ID/日時表示、HTMLパース＆インラインリンク直タップ） */
@Composable
private fun DescriptionCard(
    detail: VideoDetail,
    textColor: Color,
    secondaryColor: Color,
    onPlayVideo: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onUserClick: (Long) -> Unit,
    onMylistClick: (Long) -> Unit,
    onSeriesClick: (Long) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val annotatedDescription = remember(detail.description) {
        DescriptionLinkParser.parseDescription(
            rawHtml = detail.description,
            handlers = DescriptionLinkHandlers(
                onPlayVideo = onPlayVideo,
                onSeekTo = onSeekTo,
                onUserClick = onUserClick,
                onMylistClick = onMylistClick,
                onSeriesClick = onSeriesClick,
                onOpenUrl = onOpenUrl
            )
        )
    }

    HycoCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded },
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = detail.id,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (detail.registeredAt.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatRegisteredDate(detail.registeredAt),
                            color = secondaryColor,
                            fontSize = 12.sp,
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "折りたたむ" else "展開する",
                    tint = secondaryColor,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = annotatedDescription,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 投稿日時文字列の簡易フォーマット */
private fun formatRegisteredDate(isoString: String): String {
    return isoString
        .replace("T", " ")
        .substringBefore("+")
        .substringBefore("Z")
        .take(16)
}
