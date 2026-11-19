package com.github.stm7128.hycohyco.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CommentsDisabled
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 映像再生サーフェス上に重ねて表示する操作オーバーレイコントロール
 */
@Composable
fun VideoPlayerControlsOverlay(
    isVisible: Boolean,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long,
    previewPositionMs: Long?,
    isFullscreen: Boolean,
    isAutoplayEnabled: Boolean,
    isDanmakuEnabled: Boolean,
    onToggleFullscreen: () -> Unit,
    onMinimize: () -> Unit,
    onToggleAutoplay: () -> Unit,
    onOpenSettings: () -> Unit,
    onEnterPip: () -> Unit,
    onToggleDanmaku: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPreviewPositionMs: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    heatmapDensity: FloatArray? = null,
    isSidePanelOpen: Boolean = false,
    onToggleSidePanel: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
            // 上部ツールバー: 戻る/最小化、自動再生、設定、PiP、コメントトグル
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = if (isFullscreen) onToggleFullscreen else onMinimize,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isFullscreen) "全画面解除" else "プレイヤーを最小化",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleAutoplay, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = if (isAutoplayEnabled) "自動再生 ON" else "自動再生 OFF",
                            tint = if (isAutoplayEnabled) Color.White else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = "再生設定", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = onEnterPip, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PiP", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = onToggleDanmaku, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = if (isDanmakuEnabled) Icons.AutoMirrored.Filled.Comment else Icons.Default.CommentsDisabled,
                            contentDescription = "コメント切替",
                            tint = if (isDanmakuEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 中央: 再生 / 一時停止ボタン
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.align(Alignment.Center).size(64.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "一時停止" else "再生",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }

            // 下部: シークバー、再生時間、全画面切り替え
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .align(Alignment.BottomCenter),
            ) {
                val displayPos = previewPositionMs ?: currentPositionMs
                HycoVideoSeekBar(
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    bufferedPositionMs = bufferedPositionMs,
                    onSeekTo = onSeekTo,
                    onPreviewPositionMs = onPreviewPositionMs,
                    heatmapDensity = heatmapDensity,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatDuration(displayPos),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = " / ${formatDuration(durationMs)}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isFullscreen) {
                            Surface(
                                onClick = onToggleSidePanel,
                                shape = RoundedCornerShape(50),
                                color = if (isSidePanelOpen) Color(0xFF0095FF) else Color.White.copy(alpha = 0.16f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                                        contentDescription = if (isSidePanelOpen) "サイドパネルを閉じる" else "関連動画・コメントを開く",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isSidePanelOpen) "閉じる" else "関連・コメ",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        IconButton(onClick = onToggleFullscreen, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFullscreen) "全画面解除" else "全画面表示",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
