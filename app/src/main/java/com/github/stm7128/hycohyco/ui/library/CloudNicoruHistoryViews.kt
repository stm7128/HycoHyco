package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.github.stm7128.hycohyco.domain.model.NicoruHistoryItem
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch

/**
 * ニコられた / ニコった 履歴統合ビュー
 */
@Composable
fun CloudNicoruHistoryView(
    isPassive: Boolean,
    items: List<NicoruHistoryItem>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    errorMessage: String?,
    totalReceiveCount: Int? = null,
    byVideoOwner: Boolean = false,
    onToggleByVideoOwner: (Boolean) -> Unit = {},
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPlayVideoAtPosition: (String, Long?) -> Unit,
    onDeleteNicoruSend: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var deleteTarget by remember { mutableStateOf<NicoruHistoryItem?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        if (isPassive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SentimentSatisfiedAlt, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (totalReceiveCount != null) "合計 ${totalReceiveCount}回" else "ニコられた履歴",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HycoTheme.colors.onBackground
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("投稿者のみ", fontSize = 12.sp, color = HycoTheme.colors.onBackgroundVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    HycoSwitch(checked = byVideoOwner, onCheckedChange = onToggleByVideoOwner)
                    IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新", tint = HycoTheme.colors.onBackgroundVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ニコった履歴 (${items.size}件${if (hasMore) "+" else ""})", fontSize = 12.sp, color = HycoTheme.colors.onBackgroundVariant)
                IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "更新", tint = HycoTheme.colors.onBackgroundVariant, modifier = Modifier.size(18.dp))
                }
            }
        }

        CloudHistoryStateScaffold(
            isEmpty = items.isEmpty(),
            isLoading = isLoading,
            errorMessage = errorMessage,
            emptyText = if (isPassive) "ニコられた履歴がありません" else "ニコった履歴がありません",
            onRetry = onRefresh,
            modifier = Modifier.weight(1f)
        ) {
            val listState = rememberLazyListState()
            EndlessScrollEffect(listState = listState, hasMore = hasMore, isLoadingMore = isLoadingMore, onLoadMore = onLoadMore)

            HycoPullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        NicoruHistoryCard(
                            item = item,
                            isPassive = isPassive,
                            onPlayVideo = { onPlayVideoAtPosition(item.video.contentId, null) },
                            onPlayAtCommentTime = {
                                onPlayVideoAtPosition(item.video.contentId, item.commentVposMs.coerceAtLeast(1L))
                            },
                            onDeleteClick = if (!isPassive) ({ deleteTarget = item }) else null
                        )
                    }
                    pagingFooterItem(isLoadingMore = isLoadingMore, hasMore = hasMore, onLoadMore = onLoadMore)
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("ニコるを取り消す") },
            text = { Text("コメント「${target.commentBody}」へのニコるを解除し、履歴から削除しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    val id = target.id
                    deleteTarget = null
                    onDeleteNicoruSend(id)
                }) { Text("削除する", color = HycoTheme.colors.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("キャンセル") } }
        )
    }
}
