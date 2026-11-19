package com.github.stm7128.hycohyco.ui.designsystem.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.VideoSortOrder
import kotlinx.coroutines.launch

/**
 * ニコニコ公式準拠の並び替え（ソート）セレクターバー
 *
 * @param selectedSort 現在選択されているソート順
 * @param onSortSelected ソート順が選択された時のコールバック
 * @param sortOptions 選択肢リスト（デフォルトはユーザー投稿動画用全14項目）
 * @param totalCount リストの総件数（表示不要な場合は null）
 * @param isWatchLater 「あとで見る」画面用の表示ラベルに切り替えるか
 * @param modifier 修飾子
 */
@Composable
fun VideoSortBar(
    selectedSort: VideoSortOrder,
    onSortSelected: (VideoSortOrder) -> Unit,
    modifier: Modifier = Modifier,
    sortOptions: List<VideoSortOrder> = VideoSortOrder.USER_VIDEO_SORT_ORDERS,
    totalCount: Int? = null,
    isWatchLater: Boolean = false
) {
    var showSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (totalCount != null) {
            Text(
                text = "全 $totalCount 件",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        // ソート選択トリガーボタン
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { showSheet = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedSort.getLabel(isWatchLater),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showSheet) {
        VideoSortBottomSheet(
            selectedSort = selectedSort,
            options = sortOptions,
            isWatchLater = isWatchLater,
            onDismiss = { showSheet = false },
            onSortSelected = { order ->
                showSheet = false
                onSortSelected(order)
            }
        )
    }
}

/**
 * ニコニコ公式準拠の並び替え選択ボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSortBottomSheet(
    selectedSort: VideoSortOrder,
    options: List<VideoSortOrder>,
    isWatchLater: Boolean,
    onDismiss: () -> Unit,
    onSortSelected: (VideoSortOrder) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "並び替え",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(options, key = { it.name }) { order ->
                    val isSelected = order == selectedSort
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    onSortSelected(order)
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = order.getLabel(isWatchLater),
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "選択中",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
