package com.github.stm7128.hycohyco.ui.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.stm7128.hycohyco.domain.model.SearchTarget
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.util.NicoParsedIntent
import com.github.stm7128.hycohyco.domain.util.NicoUrlParser
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoTextField
import com.github.stm7128.hycohyco.ui.user.UserDetailSheet
import org.koin.androidx.compose.koinViewModel

/**
 * 動画・タグ検索画面
 */
@Composable
fun SearchScreen(
    onPlayVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenShort: (String, List<VideoSummary>) -> Unit = { id, _ -> onPlayVideo(id) },
    onOpenMylist: (Long) -> Unit = {},
    onOpenSeries: (Long) -> Unit = {},
    viewModel: SearchViewModel = koinViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val target by viewModel.target.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val isShortsTagSearch by viewModel.isShortsTagSearch.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var selectedUser by remember { mutableStateOf<Pair<Long, String>?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // 検索入力バー
        HycoTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = "動画タイトル・タグを入力",
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "検索")
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "クリア")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    when (val parsed = NicoUrlParser.parse(query)) {
                        is NicoParsedIntent.PlayShort -> onOpenShort(parsed.videoId, emptyList())
                        is NicoParsedIntent.PlayVideo -> onPlayVideo(parsed.videoId)
                        is NicoParsedIntent.OpenUser -> selectedUser = Pair(parsed.userId, "")
                        is NicoParsedIntent.OpenMylist -> onOpenMylist(parsed.mylistId)
                        is NicoParsedIntent.OpenSeries -> onOpenSeries(parsed.seriesId)
                        else -> viewModel.search()
                    }
                }
            )
        )

        // 動画 ID / URL / ユーザー・マイリスト・シリーズ URL 入力時の直接遷移ショートカットカード
        DirectVideoJumpCard(
            query = query,
            onPlayVideo = onPlayVideo,
            onOpenShort = { onOpenShort(it, emptyList()) },
            onOpenUser = { userId -> selectedUser = Pair(userId, "") },
            onOpenMylist = onOpenMylist,
            onOpenSeries = onOpenSeries
        )

        // フィルタチップ (キーワード/タグ & ソート順セレクター)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchTarget.entries.forEach { t ->
                HycoFilterChip(
                    selected = target == t,
                    onClick = { viewModel.onTargetChange(t) },
                    label = if (t == SearchTarget.SHORTS && isShortsTagSearch) "ショート(タグ)" else t.displayName
                )
            }

            if (target != SearchTarget.USER) {
                Spacer(modifier = Modifier.width(4.dp))
                SearchSortSelectorButton(
                    selectedSort = sort,
                    onSortSelected = { viewModel.onSortChange(it) }
                )
            }
        }

        // 検索結果コンテンツ
        SearchResultsContent(
            uiState = uiState,
            isRefreshing = isRefreshing,
            target = target,
            searchHistory = searchHistory,
            onSelectKeyword = { keyword ->
                focusManager.clearFocus()
                viewModel.onQueryChange(keyword)
                viewModel.search()
            },
            onClearAllHistory = { viewModel.clearSearchHistory() },
            onSelectPopularTag = { tag ->
                focusManager.clearFocus()
                viewModel.searchTag(tag)
            },
            onRetry = { viewModel.search() },
            onRefresh = { viewModel.refresh() },
            onLoadMore = { viewModel.loadNextPage() },
            onPlayVideo = onPlayVideo,
            onOpenShort = onOpenShort,
            onUserClick = { id, name -> selectedUser = Pair(id, name) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }

    selectedUser?.let { (userId, nickname) ->
        UserDetailSheet(
            userId = userId,
            initialNickname = nickname,
            onDismissRequest = { selectedUser = null },
            onVideoClick = { videoId ->
                selectedUser = null
                onPlayVideo(videoId)
            }
        )
    }
}
