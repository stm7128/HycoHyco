package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPullToRefreshBox

/**
 * クラウド履歴各ビュー共通の状態表示スキャフォールド (ローディング / エラー / 空状態 / コンテンツ)
 */
@Composable
internal fun CloudHistoryStateScaffold(
    isEmpty: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    emptyText: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when {
        isLoading && isEmpty -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(34.dp))
        }
        errorMessage != null && isEmpty -> HycoPullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRetry,
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(errorMessage, color = HycoTheme.colors.error, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRetry) { Text("再試行") }
                    }
                }
            }
        }
        isEmpty -> HycoPullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRetry,
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(emptyText, color = HycoTheme.colors.onBackgroundVariant, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRetry) { Text("更新") }
                    }
                }
            }
        }
        else -> content()
    }
}

/**
 * LazyColumn 末尾スクロールを検知して次ページを自動取得する共通エフェクト
 */
@Composable
internal fun EndlessScrollEffect(
    listState: LazyListState,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore, hasMore, isLoadingMore) {
        if (shouldLoadMore && hasMore && !isLoadingMore) onLoadMore()
    }
}

/**
 * リスト末尾の追加読み込みインジケータ／「さらに読み込む」ボタンアイテム
 */
internal fun LazyListScope.pagingFooterItem(
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit
) {
    if (isLoadingMore) {
        item(key = "_paging_loading_") {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
            }
        }
    } else if (hasMore) {
        item(key = "_paging_more_") {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                TextButton(onClick = onLoadMore) { Text("さらに読み込む", fontSize = 13.sp) }
            }
        }
    }
}
