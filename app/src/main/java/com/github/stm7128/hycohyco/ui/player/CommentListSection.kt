package com.github.stm7128.hycohyco.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.CommentNgFilter
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSlider
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * 展開プレイヤー下部に配置するコメント一覧および弾幕・NG設定セクション
 *
 * 動画時間への自動スクロール追従（オートスクロール）および ON/OFF トグルに対応。
 */
@Composable
fun CommentListSection(
    comments: List<CommentItem>,
    isLoading: Boolean,
    danmakuSettings: DanmakuDisplaySettings,
    ngConfig: CommentNgConfig,
    onSeekTo: (Long) -> Unit,
    onUpdateAlpha: (Float) -> Unit,
    onUpdateNgConfig: (CommentNgConfig) -> Unit,
    textColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    currentPositionMs: Long = 0L,
    isPlaying: Boolean = false,
    onAddNgUserId: (String) -> Unit = {},
    onAddNgWord: (String) -> Unit = {},
    onDeleteComment: (CommentItem) -> Unit = {},
    onToggleNicoru: (CommentItem) -> Unit = {},
    maxHeight: androidx.compose.ui.unit.Dp? = 280.dp,
    onUpdateDanmakuSettings: (DanmakuDisplaySettings) -> Unit = { onUpdateAlpha(it.alpha) },
    pastLogDateLabel: String? = null,
    onLoadPastComments: (Long, String) -> Unit = { _, _ -> },
    onResetToLatestComments: () -> Unit = {},
) {
    var isSettingsExpanded by remember { mutableStateOf(false) }
    var selectedCommentForNg by remember { mutableStateOf<CommentItem?>(null) }
    var commentToDelete by remember { mutableStateOf<CommentItem?>(null) }

    // NG フィルター適用済みコメント一覧 (M-04)
    val filteredComments = remember(comments, ngConfig) {
        CommentNgFilter.filter(comments, ngConfig)
    }

    // 自動スクロール（動画時間追従）状態
    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    var isUserScrollingPaused by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val isDragged by listState.interactionSource.collectIsDraggedAsState()

    // ユーザーが手動スクロールした場合は一時停止
    LaunchedEffect(isDragged) {
        if (isDragged) {
            isUserScrollingPaused = true
        }
    }

    // 現在の再生時間に対応する最新のコメントインデックス
    val currentIndex = remember(filteredComments, currentPositionMs) {
        filteredComments.indexOfLast { it.vposMs <= currentPositionMs }
    }

    // 自動スクロール追従実行 (I-05: 1秒スロットルでチラつきとCPU負荷を低減)
    LaunchedEffect(isAutoScrollEnabled, isUserScrollingPaused, isPlaying) {
        if (!isAutoScrollEnabled || isUserScrollingPaused || !isPlaying) return@LaunchedEffect
        while (isActive) {
            val idx = filteredComments.indexOfLast { it.vposMs <= currentPositionMs }
            if (idx >= 0) {
                val targetIndex = (idx - 1).coerceAtLeast(0)
                if (targetIndex != listState.firstVisibleItemIndex) {
                    listState.animateScrollToItem(targetIndex)
                }
            }
            delay(1000L)
        }
    }

    HycoCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ヘッダー（件数・自動追従トグル・設定展開）
            var showPastLogDialog by remember { mutableStateOf(false) }

            CommentListHeader(
                commentCount = filteredComments.size,
                isLoading = isLoading,
                isSettingsExpanded = isSettingsExpanded,
                isAutoScrollEnabled = isAutoScrollEnabled,
                onToggleSettings = { isSettingsExpanded = !isSettingsExpanded },
                onToggleAutoScroll = {
                    isAutoScrollEnabled = !isAutoScrollEnabled
                    if (isAutoScrollEnabled) {
                        isUserScrollingPaused = false
                    }
                },
                textColor = textColor,
                secondaryColor = secondaryColor,
                isPastLogActive = pastLogDateLabel != null,
                onOpenPastLogPicker = { showPastLogDialog = true }
            )

            // 過去ログ表示中バナー
            if (pastLogDateLabel != null) {
                PastLogActiveBanner(
                    dateLabel = pastLogDateLabel,
                    onResetToLatest = onResetToLatestComments
                )
            }

            if (showPastLogDialog) {
                PastCommentDatePickerDialog(
                    onDateSelected = { ts, label ->
                        showPastLogDialog = false
                        onLoadPastComments(ts, label)
                    },
                    onDismiss = { showPastLogDialog = false }
                )
            }

            // 展開時: 弾幕・NG設定パネル
            AnimatedVisibility(visible = isSettingsExpanded) {
                DanmakuSettingsPanel(
                    danmakuSettings = danmakuSettings,
                    ngConfig = ngConfig,
                    onUpdateDanmakuSettings = onUpdateDanmakuSettings,
                    onUpdateNgConfig = onUpdateNgConfig,
                    textColor = textColor,
                    secondaryColor = secondaryColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // コメントリスト & 追従復帰ボタン
            if (filteredComments.isEmpty() && !isLoading) {
                Text(
                    text = "コメントがありません",
                    color = secondaryColor,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (maxHeight != null) Modifier.heightIn(max = maxHeight) else Modifier.fillMaxHeight())
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(filteredComments, key = { _, comment -> comment.id }) { index, comment ->
                            CommentRowItem(
                                comment = comment,
                                onSeekTo = onSeekTo,
                                onLongClick = { selectedCommentForNg = comment },
                                onDeleteClick = { commentToDelete = comment },
                                onToggleNicoru = onToggleNicoru,
                                textColor = textColor,
                                secondaryColor = secondaryColor,
                                isCurrentPlaying = (index == currentIndex)
                            )
                            HorizontalDivider(
                                color = secondaryColor.copy(alpha = 0.1f),
                                thickness = 0.5.dp
                            )
                        }
                    }

                    // 手動スクロール中の一時停止時: 「↓ 再生位置へ追従」復帰ピル
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isAutoScrollEnabled && isUserScrollingPaused && currentIndex >= 0,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0095FF),
                            shadowElevation = 4.dp,
                            modifier = Modifier.clickable {
                                isUserScrollingPaused = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "再生位置へ追従",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // コメント長押し時の NG 登録ボトムシート
    CommentNgSheet(
        comment = selectedCommentForNg,
        onDismiss = { selectedCommentForNg = null },
        onAddNgUserId = onAddNgUserId,
        onAddNgWord = onAddNgWord,
    )

    // 自コメント削除確認ダイアログ
    CommentDeleteDialog(
        comment = commentToDelete,
        onDismissRequest = { commentToDelete = null },
        onConfirmDelete = onDeleteComment
    )
}
