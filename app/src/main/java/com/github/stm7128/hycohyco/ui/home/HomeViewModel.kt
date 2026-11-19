package com.github.stm7128.hycohyco.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.HomeRecommendFeed
import com.github.stm7128.hycohyco.domain.model.RankingGenre
import com.github.stm7128.hycohyco.domain.model.RankingTerm
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.repository.AuthRepository
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import com.github.stm7128.hycohyco.domain.repository.ThemeRepository
import com.github.stm7128.hycohyco.domain.repository.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

sealed interface HomeRankingUiState {
    data object Loading : HomeRankingUiState
    data class Success(val videos: List<VideoSummary>) : HomeRankingUiState
    data class Error(val message: String) : HomeRankingUiState
}

/**
 * ホーム画面の ViewModel (おすすめ / 人気ランキング / フォロー新着 / ショート)
 */
class HomeViewModel(
    private val videoRepository: VideoRepository,
    private val authRepository: AuthRepository? = null,
    private val cloudLibraryRepository: CloudLibraryRepository? = null,
    private val themeRepository: ThemeRepository? = null
) : ViewModel() {

    private val _homeMode = MutableStateFlow(HomeMode.RANKING)
    val homeMode: StateFlow<HomeMode> = _homeMode.asStateFlow()

    private val _selectedGenre = MutableStateFlow(RankingGenre.ALL)
    val selectedGenre: StateFlow<RankingGenre> = _selectedGenre.asStateFlow()

    private val _selectedTerm = MutableStateFlow(RankingTerm.WEEKLY)
    val selectedTerm: StateFlow<RankingTerm> = _selectedTerm.asStateFlow()

    private val _uiState = MutableStateFlow<HomeRankingUiState>(HomeRankingUiState.Loading)
    val uiState: StateFlow<HomeRankingUiState> = _uiState.asStateFlow()

    private val _isRankingRefreshing = MutableStateFlow(false)
    val isRankingRefreshing: StateFlow<Boolean> = _isRankingRefreshing.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _recommendFeed = MutableStateFlow(HomeRecommendFeed())
    val recommendFeed: StateFlow<HomeRecommendFeed> = _recommendFeed.asStateFlow()

    private val _isRecommendLoading = MutableStateFlow(false)
    val isRecommendLoading: StateFlow<Boolean> = _isRecommendLoading.asStateFlow()

    private val _recommendError = MutableStateFlow<String?>(null)
    val recommendError: StateFlow<String?> = _recommendError.asStateFlow()

    private val _followFeed = MutableStateFlow<List<VideoSummary>>(emptyList())
    val followFeed: StateFlow<List<VideoSummary>> = _followFeed.asStateFlow()

    private val _isFollowLoading = MutableStateFlow(false)
    val isFollowLoading: StateFlow<Boolean> = _isFollowLoading.asStateFlow()

    private val _followError = MutableStateFlow<String?>(null)
    val followError: StateFlow<String?> = _followError.asStateFlow()

    private val _shortsFeed = MutableStateFlow<List<VideoSummary>>(emptyList())
    val shortsFeed: StateFlow<List<VideoSummary>> = _shortsFeed.asStateFlow()

    private val _isShortsLoading = MutableStateFlow(false)
    val isShortsLoading: StateFlow<Boolean> = _isShortsLoading.asStateFlow()

    private val _shortsError = MutableStateFlow<String?>(null)
    val shortsError: StateFlow<String?> = _shortsError.asStateFlow()

    private var hasAppliedInitialMode = false
    private var lastObservedSessionCookie: String? = null
    private var rankingJob: Job? = null
    private var recommendJob: Job? = null
    private var followJob: Job? = null
    private var shortsJob: Job? = null

    private val videoSummaryCache = ConcurrentHashMap<String, VideoSummary>()
    private var enrichJob: Job? = null

    init {
        // 設定された初期ホームタブを優先読み込み（ランキング先行通信の無駄を根絶）
        viewModelScope.launch {
            val initialMode = themeRepository?.themeSettings?.first()?.defaultHomeMode ?: HomeMode.RANKING
            _homeMode.value = initialMode
            hasAppliedInitialMode = true
            when (initialMode) {
                HomeMode.RANKING -> loadRanking(RankingGenre.ALL, RankingTerm.WEEKLY)
                HomeMode.RECOMMEND -> loadRecommendFeed()
                HomeMode.FOLLOW -> if (_isUserLoggedIn.value) loadFollowFeed()
                HomeMode.SHORTS -> loadShortsFeed()
            }
        }

        authRepository?.userSession
            ?.onEach { session ->
                val prevCookie = lastObservedSessionCookie
                lastObservedSessionCookie = session.userSessionCookie
                _isUserLoggedIn.value = session.isLoggedIn

                if (prevCookie != null && prevCookie != session.userSessionCookie) {
                    if (_homeMode.value == HomeMode.RECOMMEND || _recommendFeed.value.videos.isNotEmpty()) {
                        loadRecommendFeed()
                    }
                    if (!session.isLoggedIn) {
                        _followFeed.value = emptyList()
                    }
                }
                if (session.isLoggedIn && _homeMode.value == HomeMode.FOLLOW && _followFeed.value.isEmpty()) {
                    loadFollowFeed()
                }
            }
            ?.launchIn(viewModelScope)
    }

    fun selectHomeMode(mode: HomeMode) {
        _homeMode.value = mode
        when (mode) {
            HomeMode.RECOMMEND -> if (_recommendFeed.value.videos.isEmpty()) loadRecommendFeed()
            HomeMode.FOLLOW -> if (_isUserLoggedIn.value && _followFeed.value.isEmpty()) loadFollowFeed()
            HomeMode.SHORTS -> if (_shortsFeed.value.isEmpty()) loadShortsFeed()
            HomeMode.RANKING -> if (_uiState.value !is HomeRankingUiState.Success) loadRanking(_selectedGenre.value, _selectedTerm.value)
        }
    }

    fun selectGenre(genre: RankingGenre) {
        if (_selectedGenre.value == genre && _uiState.value is HomeRankingUiState.Success) return
        _selectedGenre.value = genre
        loadRanking(genre, _selectedTerm.value)
    }

    fun selectTerm(term: RankingTerm) {
        if (_selectedTerm.value == term && _uiState.value is HomeRankingUiState.Success) return
        _selectedTerm.value = term
        loadRanking(_selectedGenre.value, term)
    }

    fun refresh() {
        when (_homeMode.value) {
            HomeMode.RECOMMEND -> loadRecommendFeed()
            HomeMode.RANKING -> loadRanking(
                genre = _selectedGenre.value,
                term = _selectedTerm.value,
                keepExistingContent = _uiState.value is HomeRankingUiState.Success
            )
            HomeMode.FOLLOW -> loadFollowFeed()
            HomeMode.SHORTS -> loadShortsFeed()
        }
    }

    fun loadRecommendFeed() {
        recommendJob?.cancel()
        recommendJob = viewModelScope.launch {
            _isRecommendLoading.value = true
            _recommendError.value = null
            videoRepository.getHomeRecommendFeed(limit = 50)
                .onSuccess { feed ->
                    _recommendFeed.value = feed
                    _isRecommendLoading.value = false
                }
                .onFailure { error ->
                    Log.e("HomeViewModel", "ホームおすすめ取得失敗", error)
                    _recommendError.value = error.localizedMessage ?: "おすすめ動画の読み込みに失敗しました"
                    _isRecommendLoading.value = false
                }
        }
    }

    fun loadShortsFeed() {
        shortsJob?.cancel()
        shortsJob = viewModelScope.launch {
            _isShortsLoading.value = true
            _shortsError.value = null
            videoRepository.getRecommendedShorts(limit = 20)
                .onSuccess { list ->
                    _shortsFeed.value = list
                    _isShortsLoading.value = false
                }
                .onFailure { error ->
                    Log.e("HomeViewModel", "ショートフィード取得失敗", error)
                    _shortsError.value = error.localizedMessage ?: "ショートの読み込みに失敗しました"
                    _isShortsLoading.value = false
                }
        }
    }

    fun loadFollowFeed() {
        followJob?.cancel()
        followJob = viewModelScope.launch {
            _isFollowLoading.value = true
            _followError.value = null
            cloudLibraryRepository?.getFollowFeed()?.onSuccess { list ->
                Log.d("HomeViewModel", "フォロー新着取得成功: ${list.size}件")
                _followFeed.value = list
                _isFollowLoading.value = false
                enrichFollowFeed(list)
            }?.onFailure { error ->
                Log.e("HomeViewModel", "フォロー新着取得失敗", error)
                _followError.value = error.localizedMessage ?: "フォロー新着の読み込みに失敗しました"
                _isFollowLoading.value = false
            } ?: run {
                _isFollowLoading.value = false
            }
        }
    }

    private fun enrichFollowFeed(videos: List<VideoSummary>) {
        if (videos.any { videoSummaryCache.containsKey(it.contentId) }) {
            _followFeed.value = _followFeed.value.map { item ->
                val cached = videoSummaryCache[item.contentId]
                if (cached != null) mergeFollowSummary(item, cached) else item
            }
        }

        val targets = videos.filter { it.viewCounter <= 0 && !videoSummaryCache.containsKey(it.contentId) }
        if (targets.isEmpty()) return

        enrichJob?.cancel()
        enrichJob = viewModelScope.launch {
            videoRepository.getVideoSummaries(targets.map { it.contentId })
                .onSuccess { summaries ->
                    summaries.forEach { s -> videoSummaryCache[s.contentId] = s }
                    val summaryMap = summaries.associateBy { it.contentId }
                    if (summaryMap.isNotEmpty()) {
                        _followFeed.value = _followFeed.value.map { item ->
                            val s = summaryMap[item.contentId] ?: videoSummaryCache[item.contentId]
                            if (s != null) mergeFollowSummary(item, s) else item
                        }
                    }
                }
                .onFailure { error ->
                    Log.e("HomeViewModel", "スナップショット一括補完失敗: ${error.message}", error)
                }
        }
    }

    private fun mergeFollowSummary(item: VideoSummary, summary: VideoSummary): VideoSummary =
        item.copy(
            viewCounter = summary.viewCounter,
            commentCounter = summary.commentCounter,
            likeCounter = summary.likeCounter,
            mylistCounter = summary.mylistCounter,
            ownerName = summary.ownerName.takeIf { it.isNotBlank() && !it.startsWith("ID:") } ?: item.ownerName,
            ownerId = summary.ownerId.ifBlank { item.ownerId }
        )

    private fun loadRanking(
        genre: RankingGenre,
        term: RankingTerm,
        keepExistingContent: Boolean = false
    ) {
        rankingJob?.cancel()
        rankingJob = viewModelScope.launch {
            if (keepExistingContent) {
                _isRankingRefreshing.value = true
            } else {
                _uiState.value = HomeRankingUiState.Loading
            }
            videoRepository.getGenreRanking(genre = genre, term = term, limit = 25)
                .onSuccess { list ->
                    _uiState.value = HomeRankingUiState.Success(list)
                    _isRankingRefreshing.value = false
                }
                .onFailure { error ->
                    _isRankingRefreshing.value = false
                    _uiState.value = HomeRankingUiState.Error(
                        error.localizedMessage ?: "ランキングの取得に失敗しました"
                    )
                }
        }
    }
}
