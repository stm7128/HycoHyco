package com.github.stm7128.hycohyco.ui.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.OwnerProfile
import com.github.stm7128.hycohyco.domain.model.UserSeries
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * ユーザー詳細画面の状態管理・ページネーションホルダー
 */
class UserDetailState(
    val userId: Long,
    private val repo: CloudLibraryRepository,
    private val coroutineScope: CoroutineScope
) {
    var profile by mutableStateOf<OwnerProfile?>(null)
    var videos by mutableStateOf<List<VideoSummary>>(emptyList())
    var seriesList by mutableStateOf<List<UserSeries>>(emptyList())
    var mylists by mutableStateOf<List<CloudMylist>>(emptyList())
    var followees by mutableStateOf<List<OwnerProfile>>(emptyList())
    var followers by mutableStateOf<List<OwnerProfile>>(emptyList())
    var isLoading by mutableStateOf(true)

    // ドリルダウン表示
    var selectedSeries by mutableStateOf<UserSeries?>(null)
    var selectedMylist by mutableStateOf<CloudMylist?>(null)
    var detailVideos by mutableStateOf<List<VideoSummary>>(emptyList())
    var isDetailLoading by mutableStateOf(false)

    // ページネーション状態
    var videosPage by mutableIntStateOf(1)
    var hasMoreVideos by mutableStateOf(true)
    var isLoadingMoreVideos by mutableStateOf(false)

    var followeesPage by mutableIntStateOf(1)
    var hasMoreFollowees by mutableStateOf(true)
    var isLoadingMoreFollowees by mutableStateOf(false)

    var followersPage by mutableIntStateOf(1)
    var hasMoreFollowers by mutableStateOf(true)
    var isLoadingMoreFollowers by mutableStateOf(false)
    private var detailJob: kotlinx.coroutines.Job? = null
    private var isFolloweesLoaded = false
    private var isFollowersLoaded = false

    fun loadInitial() {
        if (userId <= 0L) return
        coroutineScope.launch {
            isLoading = true
            videosPage = 1
            followeesPage = 1
            followersPage = 1
            isFolloweesLoaded = false
            isFollowersLoaded = false

            coroutineScope {
                val profileDeferred = async { repo.getOwnerProfile(userId) }
                val videosDeferred = async { repo.getOwnerVideos(userId, page = 1) }
                val seriesDeferred = async { repo.getOwnerSeries(userId) }
                val mylistsDeferred = async { repo.getOwnerPublicMylists(userId) }

                profileDeferred.await().onSuccess { profile = it }
                videosDeferred.await().onSuccess {
                    videos = it
                    hasMoreVideos = it.size >= 25
                }
                seriesDeferred.await().onSuccess { seriesList = it }
                mylistsDeferred.await().onSuccess { mylists = it }
            }
            isLoading = false
        }
    }

    /** フォロー中タブ選択時の遅延ロード */
    fun ensureFolloweesLoaded() {
        if (isFolloweesLoaded || isLoadingMoreFollowees) return
        isFolloweesLoaded = true
        coroutineScope.launch {
            isLoadingMoreFollowees = true
            repo.getFollowingUsers(userId, page = 1).fold(
                onSuccess = {
                    followees = it
                    hasMoreFollowees = it.size >= 100
                },
                onFailure = { hasMoreFollowees = false }
            )
            isLoadingMoreFollowees = false
        }
    }

    /** フォロワータブ選択時の遅延ロード */
    fun ensureFollowersLoaded() {
        if (isFollowersLoaded || isLoadingMoreFollowers) return
        isFollowersLoaded = true
        coroutineScope.launch {
            isLoadingMoreFollowers = true
            repo.getFollowers(userId, page = 1).fold(
                onSuccess = {
                    followers = it
                    hasMoreFollowers = it.size >= 100
                },
                onFailure = { hasMoreFollowers = false }
            )
            isLoadingMoreFollowers = false
        }
    }

    fun loadMoreVideos() {
        if (isLoadingMoreVideos || !hasMoreVideos) return
        coroutineScope.launch {
            isLoadingMoreVideos = true
            val nextPage = videosPage + 1
            repo.getOwnerVideos(userId, page = nextPage).fold(
                onSuccess = { newItems ->
                    if (newItems.isEmpty()) {
                        hasMoreVideos = false
                    } else {
                        val existingIds = videos.map { it.contentId }.toSet()
                        videos = videos + newItems.filterNot { it.contentId in existingIds }
                        videosPage = nextPage
                        if (newItems.size < 25) hasMoreVideos = false
                    }
                },
                onFailure = { hasMoreVideos = false }
            )
            isLoadingMoreVideos = false
        }
    }

    fun loadMoreFollowees() {
        if (isLoadingMoreFollowees || !hasMoreFollowees) return
        coroutineScope.launch {
            isLoadingMoreFollowees = true
            val nextPage = followeesPage + 1
            repo.getFollowingUsers(userId, page = nextPage).fold(
                onSuccess = { newItems ->
                    if (newItems.isEmpty()) {
                        hasMoreFollowees = false
                    } else {
                        val existingIds = followees.map { it.id }.toSet()
                        followees = followees + newItems.filterNot { it.id in existingIds }
                        followeesPage = nextPage
                        if (newItems.size < 100) hasMoreFollowees = false
                    }
                },
                onFailure = { hasMoreFollowees = false }
            )
            isLoadingMoreFollowees = false
        }
    }

    fun loadMoreFollowers() {
        if (isLoadingMoreFollowers || !hasMoreFollowers) return
        coroutineScope.launch {
            isLoadingMoreFollowers = true
            val nextPage = followersPage + 1
            repo.getFollowers(userId, page = nextPage).fold(
                onSuccess = { newItems ->
                    if (newItems.isEmpty()) {
                        hasMoreFollowers = false
                    } else {
                        val existingIds = followers.map { it.id }.toSet()
                        followers = followers + newItems.filterNot { it.id in existingIds }
                        followersPage = nextPage
                        if (newItems.size < 100) hasMoreFollowers = false
                    }
                },
                onFailure = { hasMoreFollowers = false }
            )
            isLoadingMoreFollowers = false
        }
    }

    fun selectSeries(series: UserSeries?) {
        detailJob?.cancel()
        selectedSeries = series
        if (series != null) {
            detailJob = coroutineScope.launch {
                isDetailLoading = true
                repo.getSeriesVideos(series.id).fold(
                    onSuccess = { detailVideos = it },
                    onFailure = { detailVideos = emptyList() }
                )
                isDetailLoading = false
            }
        }
    }

    fun selectMylist(mylist: CloudMylist?) {
        detailJob?.cancel()
        selectedMylist = mylist
        if (mylist != null) {
            detailJob = coroutineScope.launch {
                isDetailLoading = true
                repo.getMylistVideos(mylist.id).fold(
                    onSuccess = { detailVideos = it },
                    onFailure = { detailVideos = emptyList() }
                )
                isDetailLoading = false
            }
        }
    }
}

@Composable
fun rememberUserDetailState(
    userId: Long,
    repo: CloudLibraryRepository
): UserDetailState {
    val coroutineScope = rememberCoroutineScope()
    val state = remember(userId) { UserDetailState(userId, repo, coroutineScope) }
    LaunchedEffect(userId) {
        state.loadInitial()
    }
    return state
}
