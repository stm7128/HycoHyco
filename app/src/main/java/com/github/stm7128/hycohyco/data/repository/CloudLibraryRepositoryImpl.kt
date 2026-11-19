package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.api.NicoCloudApiClient
import com.github.stm7128.hycohyco.data.api.NicoCloudHistoryApiClient
import com.github.stm7128.hycohyco.data.api.NicoUserApiClient
import com.github.stm7128.hycohyco.data.local.AuthPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.CloudHistoryPage
import com.github.stm7128.hycohyco.domain.model.CloudLikeHistoryItem
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.CloudWatchHistoryItem
import com.github.stm7128.hycohyco.domain.model.NicoruHistoryItem
import com.github.stm7128.hycohyco.domain.model.OwnerProfile
import com.github.stm7128.hycohyco.domain.model.UserSeries
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import kotlinx.coroutines.flow.first

/**
 * ニコニコ公式クラウドライブラリリポジトリの実装クラス
 */
class CloudLibraryRepositoryImpl(
    private val cloudApiClient: NicoCloudApiClient,
    private val userApiClient: NicoUserApiClient,
    private val authPreferencesDataSource: AuthPreferencesDataSource,
    private val cloudHistoryApiClient: NicoCloudHistoryApiClient = NicoCloudHistoryApiClient(),
) : CloudLibraryRepository {

    private suspend fun getSessionCookie(): String {
        return authPreferencesDataSource.userSession.first().formattedCookieHeader
    }

    override suspend fun getWatchLaterVideos(page: Int): Result<List<VideoSummary>> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) return Result.failure(IllegalStateException("ログインが必要です"))
        return cloudApiClient.fetchWatchLater(cookie, page = page)
    }

    override suspend fun addWatchLater(videoId: String): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) return Result.failure(IllegalStateException("ログインが必要です"))
        return cloudApiClient.addWatchLater(cookie, videoId)
    }

    override suspend fun deleteWatchLaterItem(itemId: String): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) return Result.failure(IllegalStateException("ログインが必要です"))
        return cloudApiClient.deleteWatchLater(cookie, itemId)
    }

    override suspend fun getMylists(): Result<List<CloudMylist>> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) return Result.failure(IllegalStateException("ログインが必要です"))
        return cloudApiClient.fetchMylists(cookie)
    }

    override suspend fun addMylistItem(mylistId: Long, videoId: String): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) return Result.failure(IllegalStateException("ログインが必要です"))
        return cloudApiClient.addMylistItem(cookie, mylistId = mylistId, videoId = videoId)
    }

    override suspend fun deleteMylistItem(mylistId: Long, itemId: String): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) return Result.failure(IllegalStateException("ログインが必要です"))
        return cloudApiClient.deleteMylistItem(cookie, mylistId = mylistId, itemId = itemId)
    }

    override suspend fun getMylistVideos(mylistId: Long, page: Int): Result<List<VideoSummary>> {
        val cookie = getSessionCookie()
        return cloudApiClient.fetchMylistVideos(cookie, mylistId = mylistId, page = page)
    }

    override suspend fun getMylistDetail(mylistId: Long): Result<Pair<CloudMylist, List<VideoSummary>>> {
        val cookie = getSessionCookie()
        return cloudApiClient.fetchMylistDetail(cookie, mylistId = mylistId)
    }

    override suspend fun getFollowFeed(): Result<List<VideoSummary>> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) return Result.failure(IllegalStateException("ログインが必要です"))
        return cloudApiClient.fetchFollowFeed(cookie)
    }

    override suspend fun getOwnerVideos(
        userId: Long,
        page: Int,
        sortKey: String,
        sortOrder: String
    ): Result<List<VideoSummary>> {
        return userApiClient.fetchUserVideos(userId, page = page, sortKey = sortKey, sortOrder = sortOrder)
    }

    override suspend fun getOwnerProfile(userId: Long): Result<OwnerProfile> {
        return userApiClient.fetchOwnerProfile(userId)
    }

    override suspend fun getOwnerSeries(userId: Long): Result<List<UserSeries>> {
        return userApiClient.fetchUserSeries(userId)
    }

    override suspend fun getSeriesVideos(seriesId: Long): Result<List<VideoSummary>> {
        return userApiClient.fetchSeriesVideos(seriesId)
    }

    override suspend fun getSeriesDetail(seriesId: Long): Result<Pair<UserSeries, List<VideoSummary>>> {
        return userApiClient.fetchSeriesDetail(seriesId)
    }

    override suspend fun getOwnerPublicMylists(userId: Long): Result<List<CloudMylist>> {
        return userApiClient.fetchUserPublicMylists(userId)
    }

    override suspend fun getFollowingUsers(userId: Long, page: Int): Result<List<OwnerProfile>> {
        return userApiClient.fetchFollowingUsers(userId, page = page)
    }

    override suspend fun getFollowers(userId: Long, page: Int): Result<List<OwnerProfile>> {
        return userApiClient.fetchFollowers(userId, page = page)
    }

    override suspend fun searchUsers(keyword: String): Result<List<OwnerProfile>> {
        return userApiClient.searchUsers(keyword)
    }

    private suspend inline fun <T> withCookie(block: (String) -> Result<T>): Result<T> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) return Result.failure(IllegalStateException("ログインが必要です"))
        return block(cookie)
    }

    override suspend fun getCloudWatchHistory(cursor: String?): Result<CloudHistoryPage<CloudWatchHistoryItem>> =
        withCookie { cloudHistoryApiClient.fetchWatchHistory(it, cursor = cursor) }

    override suspend fun deleteCloudWatchHistory(videoId: String?): Result<Unit> =
        withCookie { cloudHistoryApiClient.deleteWatchHistory(it, target = videoId ?: "all") }

    override suspend fun getCloudLikeHistory(page: Int): Result<CloudHistoryPage<CloudLikeHistoryItem>> =
        withCookie { cloudHistoryApiClient.fetchLikeHistory(it, page = page) }

    override suspend fun unlikeCloudVideo(videoId: String): Result<Unit> =
        withCookie { cloudHistoryApiClient.unlikeVideo(it, videoId = videoId) }

    override suspend fun getNicoruReceiveHistory(
        cursor: String?,
        byVideoOwner: Boolean
    ): Result<CloudHistoryPage<NicoruHistoryItem>> =
        withCookie { cloudHistoryApiClient.fetchNicoruReceive(it, cursor = cursor, byVideoOwner = byVideoOwner) }

    override suspend fun getNicoruReceiveCount(): Result<Int> =
        withCookie { cloudHistoryApiClient.fetchNicoruReceiveCount(it) }

    override suspend fun getNicoruSendHistory(cursor: String?): Result<CloudHistoryPage<NicoruHistoryItem>> =
        withCookie { cloudHistoryApiClient.fetchNicoruSend(it, cursor = cursor) }

    override suspend fun deleteNicoruSendItem(nicoruId: String): Result<Unit> =
        withCookie { cloudHistoryApiClient.deleteNicoruSend(it, nicoruId = nicoruId) }
}
