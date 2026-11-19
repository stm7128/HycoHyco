package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import com.github.stm7128.hycohyco.player.model.isPlaying
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode

private val SIDE_PANEL_BG = Color(0xFF141414)
private val ACCENT_BLUE = Color(0xFF0095FF)
private val LIKE_PINK = Color(0xFFFF4081)
private val MUTED_TEXT = Color(0xFFA6A6A6)

/**
 * 全画面再生時に右側から展開されるサイドパネル（関連動画検索・コメント一覧）
 */
@Composable
fun FullscreenSidePanel(
    uiState: PlayerUiState,
    onClosePanel: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleLike: () -> Unit,
    onOpenCommentInput: () -> Unit,
    onUpdateDanmakuAlpha: (Float) -> Unit,
    onUpdateDanmakuSettings: (DanmakuDisplaySettings) -> Unit,
    onUpdateNgConfig: (CommentNgConfig) -> Unit,
    onAddNgUserId: (String) -> Unit,
    onAddNgWord: (String) -> Unit,
    onDeleteComment: (CommentItem) -> Unit,
    onToggleNicoru: (CommentItem) -> Unit,
    onLoadPastComments: (Long, String) -> Unit,
    onResetToLatestComments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeTab by remember { mutableStateOf(PlayerBottomTab.RECOMMENDS) }
    val currentThemeMode = LocalAppThemeMode.current
    val safeEndInsets = WindowInsets.navigationBars.union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.End + WindowInsetsSides.Vertical)

    // 全画面黒背景に馴染むようパネル内部は常にダークテーマを適用する
    HycoTheme(
        themeSettings = ThemeSettings(
            themeMode = currentThemeMode,
            darkThemeConfig = DarkThemeConfig.DARK
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxHeight()
                .background(SIDE_PANEL_BG)
                .windowInsetsPadding(safeEndInsets)
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.1f))
            )

            Column(modifier = Modifier.fillMaxSize()) {
                SidePanelHeader(
                    activeTab = activeTab,
                    recommendCount = uiState.recommendVideos.size,
                    commentCount = uiState.comments.size,
                    isLiked = uiState.isLiked,
                    onSelectTab = { activeTab = it },
                    onToggleLike = onToggleLike,
                    onOpenCommentInput = onOpenCommentInput,
                    onClosePanel = onClosePanel,
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                when (activeTab) {
                    PlayerBottomTab.RECOMMENDS -> {
                        SidePanelRecommendList(
                            videos = uiState.recommendVideos,
                            isLoading = uiState.isRecommendLoading,
                            onPlayVideo = onPlayVideo,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    PlayerBottomTab.COMMENTS -> {
                        CommentListSection(
                            comments = uiState.comments,
                            isLoading = uiState.isCommentLoading,
                            danmakuSettings = uiState.danmakuSettings,
                            ngConfig = uiState.ngConfig,
                            onSeekTo = onSeekTo,
                            onUpdateAlpha = onUpdateDanmakuAlpha,
                            onUpdateNgConfig = onUpdateNgConfig,
                            onUpdateDanmakuSettings = onUpdateDanmakuSettings,
                            textColor = Color.White,
                            secondaryColor = MUTED_TEXT,
                            currentPositionMs = uiState.playbackState.currentPositionMs,
                            isPlaying = uiState.playbackState.isPlaying,
                            onAddNgUserId = onAddNgUserId,
                            onAddNgWord = onAddNgWord,
                            onDeleteComment = onDeleteComment,
                            onToggleNicoru = onToggleNicoru,
                            pastLogDateLabel = uiState.pastLogDateLabel,
                            onLoadPastComments = onLoadPastComments,
                            onResetToLatestComments = onResetToLatestComments,
                            maxHeight = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidePanelHeader(
    activeTab: PlayerBottomTab,
    recommendCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    onSelectTab: (PlayerBottomTab) -> Unit,
    onToggleLike: () -> Unit,
    onOpenCommentInput: () -> Unit,
    onClosePanel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SidePanelTabChip(
                label = if (recommendCount > 0) "関連 ($recommendCount)" else "関連動画",
                selected = activeTab == PlayerBottomTab.RECOMMENDS,
                onClick = { onSelectTab(PlayerBottomTab.RECOMMENDS) }
            )
            SidePanelTabChip(
                label = if (commentCount > 0) "コメ ($commentCount)" else "コメント",
                selected = activeTab == PlayerBottomTab.COMMENTS,
                onClick = { onSelectTab(PlayerBottomTab.COMMENTS) }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleLike, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "いいね",
                    tint = if (isLiked) LIKE_PINK else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onOpenCommentInput, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "コメント投稿",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onClosePanel, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "パネルを閉じる",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SidePanelTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) ACCENT_BLUE else Color.White.copy(alpha = 0.1f),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
