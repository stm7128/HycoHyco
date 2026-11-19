package com.github.stm7128.hycohyco.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.OwnerProfile
import com.github.stm7128.hycohyco.domain.model.SearchHistoryItem
import com.github.stm7128.hycohyco.domain.model.SearchSort
import com.github.stm7128.hycohyco.domain.model.SearchTarget
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository
import com.github.stm7128.hycohyco.domain.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 検索画面の UI 状態
 */
sealed interface SearchUiState {
    /** 検索前 (初期状態) */
    data object Idle : SearchUiState

    /** 検索中 */
    data object Loading : SearchUiState

    /** 検索結果表示 (動画) */
    data class Success(
        val videos: List<VideoSummary>,
        val hasMore: Boolean = true,
        val isLoadingMore: Boolean = false
    ) : SearchUiState

    /** 検索結果表示 (ユーザー) */
    data class UserSuccess(val users: List<OwnerProfile>) : SearchUiState

    /** 検索結果 0 件 */
    data object Empty : SearchUiState

    /** エラー発生 */
    data class Error(val message: String) : SearchUiState
}

/**
 * 検索画面の ViewModel
 */
class SearchViewModel(
    private val videoRepository: VideoRepository,
    private val libraryRepository: LibraryRepository,
    private val cloudLibraryRepository: CloudLibraryRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _target = MutableStateFlow(SearchTarget.KEYWORD)
    val target: StateFlow<SearchTarget> = _target.asStateFlow()

    private val _sort = MutableStateFlow(SearchSort.VIEW_COUNT)
    val sort: StateFlow<SearchSort> = _sort.asStateFlow()

    private val _isShortsTagSearch = MutableStateFlow(false)
    val isShortsTagSearch: StateFlow<Boolean> = _isShortsTagSearch.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistoryItem>> = _searchHistory.asStateFlow()

    init {
        libraryRepository.getSearchHistory()
            .onEach { history -> _searchHistory.value = history }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        _isShortsTagSearch.value = false
    }

    fun onTargetChange(newTarget: SearchTarget) {
        _target.value = newTarget
        _isShortsTagSearch.value = false
        if (_query.value.isNotBlank()) {
            search()
        }
    }

    fun onSortChange(newSort: SearchSort) {
        _sort.value = newSort
        if (_query.value.isNotBlank()) {
            search()
        }
    }

    private var currentOffset = 0
    private var currentShortPage = 1
    private var searchJob: kotlinx.coroutines.Job? = null
    private var loadMoreJob: kotlinx.coroutines.Job? = null
    private val pageSize = 30

    /** 検索を実行する（keepExistingContent = true の場合は既存一覧を保持したまま再取得） */
    fun search(keepExistingContent: Boolean = false) {
        val currentQuery = _query.value.trim()
        if (currentQuery.isBlank()) {
            _isRefreshing.value = false
            _uiState.value = SearchUiState.Idle
            return
        }

        val canKeep = keepExistingContent &&
            (_uiState.value is SearchUiState.Success || _uiState.value is SearchUiState.UserSuccess)
        loadMoreJob?.cancel()
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (canKeep) {
                _isRefreshing.value = true
            } else {
                _isRefreshing.value = false
                _uiState.value = SearchUiState.Loading
            }
            currentOffset = 0
            currentShortPage = 1
            libraryRepository.recordSearch(currentQuery)

            if (_target.value == SearchTarget.USER) {
                cloudLibraryRepository.searchUsers(currentQuery).onSuccess { users ->
                    _isRefreshing.value = false
                    _uiState.value = if (users.isEmpty()) SearchUiState.Empty else SearchUiState.UserSuccess(users)
                }.onFailure { error ->
                    _isRefreshing.value = false
                    if (!canKeep) {
                        _uiState.value = SearchUiState.Error(error.localizedMessage ?: "ユーザー検索に失敗しました")
                    }
                }
                return@launch
            }

            if (_target.value == SearchTarget.SHORTS) {
                videoRepository.searchShorts(
                    query = currentQuery,
                    sort = _sort.value,
                    page = 1,
                    pageSize = pageSize,
                    isTagSearch = _isShortsTagSearch.value
                ).onSuccess { list ->
                    currentShortPage = 1
                    val hasMore = list.size >= pageSize
                    _isRefreshing.value = false
                    _uiState.value = if (list.isEmpty()) SearchUiState.Empty else SearchUiState.Success(list, hasMore = hasMore)
                }.onFailure { error ->
                    _isRefreshing.value = false
                    if (!canKeep) {
                        _uiState.value = SearchUiState.Error(error.localizedMessage ?: "ショート検索に失敗しました")
                    }
                }
                return@launch
            }

            videoRepository.searchVideos(
                query = currentQuery,
                target = _target.value,
                sort = _sort.value,
                offset = 0,
                limit = pageSize
            ).onSuccess { list ->
                currentOffset = list.size
                val hasMore = list.size >= pageSize
                _isRefreshing.value = false
                _uiState.value = if (list.isEmpty()) SearchUiState.Empty else SearchUiState.Success(list, hasMore = hasMore)
            }.onFailure { error ->
                _isRefreshing.value = false
                if (!canKeep) {
                    _uiState.value = SearchUiState.Error(error.localizedMessage ?: "検索に失敗しました")
                }
            }
        }
    }

    /** Pull-to-Refresh 用に現在の検索結果を保ったまま再検索する */
    fun refresh() = search(keepExistingContent = true)

    /** 次ページ（さらに30件）を追加取得する */
    fun loadNextPage() {
        val currentState = _uiState.value as? SearchUiState.Success ?: return
        if (!currentState.hasMore || currentState.isLoadingMore) return
        val currentQuery = _query.value.trim()
        if (currentQuery.isBlank() || _target.value == SearchTarget.USER) return

        _uiState.value = currentState.copy(isLoadingMore = true)
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            val nextResult = if (_target.value == SearchTarget.SHORTS) {
                videoRepository.searchShorts(
                    query = currentQuery,
                    sort = _sort.value,
                    page = currentShortPage + 1,
                    pageSize = pageSize,
                    isTagSearch = _isShortsTagSearch.value
                )
            } else {
                videoRepository.searchVideos(
                    query = currentQuery,
                    target = _target.value,
                    sort = _sort.value,
                    offset = currentOffset,
                    limit = pageSize
                )
            }
            nextResult.onSuccess { nextList ->
                val updatedList = (currentState.videos + nextList).distinctBy { it.contentId }
                if (_target.value == SearchTarget.SHORTS) {
                    currentShortPage += 1
                } else {
                    currentOffset += nextList.size
                }
                val hasMore = nextList.size >= pageSize
                _uiState.value = currentState.copy(
                    videos = updatedList,
                    hasMore = hasMore,
                    isLoadingMore = false
                )
            }.onFailure { error ->
                android.util.Log.e("SearchViewModel", "次ページ取得失敗", error)
                _uiState.value = currentState.copy(isLoadingMore = false)
            }
        }
    }

    /** 特定のタグを直接指定して検索する（ショート時は tag= パラメータでショートタグ検索を実行） */
    fun searchTag(tag: String, isShorts: Boolean = false) {
        val trimmed = tag.trim()
        if (trimmed.isBlank()) return
        _query.value = trimmed
        _isShortsTagSearch.value = isShorts
        _target.value = if (isShorts) SearchTarget.SHORTS else SearchTarget.TAG_EXACT
        search()
    }

    /** 検索履歴のピン留めを切り替える */
    fun togglePinSearch(keyword: String) {
        viewModelScope.launch { libraryRepository.togglePinSearch(keyword) }
    }

    /** 検索履歴を 1 件削除する */
    fun deleteSearchHistory(keyword: String) {
        viewModelScope.launch { libraryRepository.deleteSearch(keyword) }
    }

    /** 検索履歴を全消去する (ピン留めは維持) */
    fun clearSearchHistory() {
        viewModelScope.launch { libraryRepository.clearSearchHistory() }
    }
}
