package com.github.stm7128.hycohyco.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings

/**
 * 全画面（フルスクリーン）表示時のプレイヤー本体コンテナ
 *
 * サイドパネル展開時は映像領域を左側に縮小し、右側に「関連動画／コメント」の
 * ダークサイドパネルをスライド展開する。パネル表示中のシステム戻るキーは
 * 全画面解除よりも優先してサイドパネルを閉じる。
 */
@Composable
fun FullscreenPlayerContainer(
    isFullscreen: Boolean,
    uiState: PlayerUiState,
    renderSurface: @Composable (
        isSidePanelOpen: Boolean,
        onToggleSidePanel: () -> Unit,
        surfaceModifier: Modifier
    ) -> Unit,
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
    var isSidePanelOpen by remember { mutableStateOf(false) }

    // 全画面終了時はサイドパネルを自動リセットする
    LaunchedEffect(isFullscreen) {
        if (!isFullscreen) {
            isSidePanelOpen = false
        }
    }

    // サイドパネル表示中は全画面解除より先にパネルを閉じる
    BackHandler(enabled = isFullscreen && isSidePanelOpen) {
        isSidePanelOpen = false
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val sidePanelWidth = (maxWidth * 0.34f).coerceIn(270.dp, 360.dp)

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                renderSurface(
                    isSidePanelOpen,
                    { isSidePanelOpen = !isSidePanelOpen },
                    Modifier.fillMaxSize()
                )
            }

            AnimatedVisibility(
                visible = isSidePanelOpen,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            ) {
                FullscreenSidePanel(
                    uiState = uiState,
                    onClosePanel = { isSidePanelOpen = false },
                    onPlayVideo = onPlayVideo,
                    onSeekTo = onSeekTo,
                    onToggleLike = onToggleLike,
                    onOpenCommentInput = onOpenCommentInput,
                    onUpdateDanmakuAlpha = onUpdateDanmakuAlpha,
                    onUpdateDanmakuSettings = onUpdateDanmakuSettings,
                    onUpdateNgConfig = onUpdateNgConfig,
                    onAddNgUserId = onAddNgUserId,
                    onAddNgWord = onAddNgWord,
                    onDeleteComment = onDeleteComment,
                    onToggleNicoru = onToggleNicoru,
                    onLoadPastComments = onLoadPastComments,
                    onResetToLatestComments = onResetToLatestComments,
                    modifier = Modifier.width(sidePanelWidth)
                )
            }
        }
    }
}
