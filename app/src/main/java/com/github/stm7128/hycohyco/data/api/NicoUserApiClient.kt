package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.CasUserSearchRootDto
import com.github.stm7128.hycohyco.data.model.FollowingUsersRootDto
import com.github.stm7128.hycohyco.data.model.MylistRootDto
import com.github.stm7128.hycohyco.data.model.SeriesDetailRootDto
import com.github.stm7128.hycohyco.data.model.UserDetailRootDto
import com.github.stm7128.hycohyco.data.model.UserSeriesRootDto
import com.github.stm7128.hycohyco.data.model.UserVideosRootDto
import com.github.stm7128.hycohyco.data.model.toVideoSummary
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.OwnerProfile
import com.github.stm7128.hycohyco.domain.model.UserSeries
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

/**
 * ニコニコ動画 ユーザー・クリエイター情報 & ユーザー検索 API クライアント
 */
class NicoUserApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    /** 指定した投稿者の公開動画一覧を取得する */
    suspend fun fetchUserVideos(
        userId: Long,
        page: Int = 1,
        pageSize: Int = 25,
        sortKey: String = "registeredAt",
        sortOrder: String = "desc"
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v3/users/$userId/videos") {
            applyNicoHeaders()
            parameter("pageSize", pageSize)
            parameter("page", page)
            parameter("sortKey", sortKey)
            parameter("sortOrder", sortOrder)
        }
        response.ensureSuccess("fetchUserVideos")
        val dto: UserVideosRootDto = decodeResponse(response.bodyAsText())
        dto.data?.items.orEmpty().mapNotNull { it.essential?.toVideoSummary() }
    }

    /** 投稿者の詳細プロフィールを取得する */
    suspend fun fetchOwnerProfile(userId: Long): Result<OwnerProfile> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/$userId") {
            applyNicoHeaders()
        }
        response.ensureSuccess("fetchOwnerProfile")
        val dto: UserDetailRootDto = decodeResponse(response.bodyAsText())
        val user = dto.data?.user ?: throw IllegalStateException("ユーザー情報が見つかりません")
        OwnerProfile(
            id = user.id,
            nickname = user.nickname,
            iconUrl = user.icons?.large?.ifBlank { user.icons.small }.orEmpty(),
            description = user.description,
            followerCount = user.followerCount
        )
    }

    /** 指定した投稿者のシリーズ一覧を取得する */
    suspend fun fetchUserSeries(userId: Long): Result<List<UserSeries>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/$userId/series") {
            applyNicoHeaders()
        }
        response.ensureSuccess("fetchUserSeries")
        val dto: UserSeriesRootDto = decodeResponse(response.bodyAsText())
        dto.data?.items.orEmpty().map {
            UserSeries(it.id, it.title, it.description, it.thumbnailUrl, it.itemsCount)
        }
    }

    /** 指定した投稿者の公開マイリスト一覧を取得する */
    suspend fun fetchUserPublicMylists(userId: Long): Result<List<CloudMylist>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/$userId/mylists") {
            applyNicoHeaders()
        }
        response.ensureSuccess("fetchUserPublicMylists")
        val dto: MylistRootDto = decodeResponse(response.bodyAsText())
        dto.data?.mylists.orEmpty().map {
            CloudMylist(it.id, it.name, it.description, it.isPublic, it.itemsCount)
        }
    }

    /** 指定したユーザーのフォロー中クリエイター一覧を取得する */
    suspend fun fetchFollowingUsers(userId: Long, page: Int = 1, pageSize: Int = 100): Result<List<OwnerProfile>> =
        fetchUserRelationshipList("https://nvapi.nicovideo.jp/v1/users/$userId/following/users", page, pageSize)

    /** 指定したユーザーのフォロワー一覧を取得する */
    suspend fun fetchFollowers(userId: Long, page: Int = 1, pageSize: Int = 100): Result<List<OwnerProfile>> =
        fetchUserRelationshipList("https://nvapi.nicovideo.jp/v1/users/$userId/followed-by/users", page, pageSize)

    private suspend fun fetchUserRelationshipList(endpoint: String, page: Int, pageSize: Int): Result<List<OwnerProfile>> = suspendRunCatching {
        val response = httpClient.get(endpoint) {
            applyNicoHeaders()
            parameter("pageSize", pageSize)
            parameter("page", page)
        }
        response.ensureSuccess("fetchUserRelationshipList")
        val dto: FollowingUsersRootDto = decodeResponse(response.bodyAsText())
        val list = dto.data?.items?.ifEmpty { dto.data.users }.orEmpty()
        list.map {
            val resolvedIcon = it.icons?.large?.ifBlank { it.icons.small } ?: it.iconUrl
            OwnerProfile(
                id = it.id,
                nickname = it.nickname,
                description = it.strippedDescription.ifBlank { it.description },
                iconUrl = resolvedIcon,
                followerCount = it.followerCount
            )
        }
    }

    /** 指定したシリーズに属する動画一覧を取得する */
    suspend fun fetchSeriesVideos(seriesId: Long): Result<List<VideoSummary>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/series/$seriesId") {
            applyNicoHeaders()
        }
        response.ensureSuccess("fetchSeriesVideos")
        val dto: SeriesDetailRootDto = decodeResponse(response.bodyAsText())
        dto.data?.items?.mapNotNull { it.video?.toVideoSummary() }.orEmpty()
    }

    /** 指定したシリーズのメタ情報および登録動画一覧を取得する */
    suspend fun fetchSeriesDetail(seriesId: Long): Result<Pair<UserSeries, List<VideoSummary>>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/series/$seriesId") {
            applyNicoHeaders()
        }
        response.ensureSuccess("fetchSeriesDetail")
        val dto: SeriesDetailRootDto = decodeResponse(response.bodyAsText())
        val data = dto.data ?: throw IllegalStateException("シリーズ情報が見つかりません")
        val detail = data.detail
        val userSeries = UserSeries(
            id = detail?.id?.takeIf { it > 0L } ?: seriesId,
            title = detail?.title?.ifBlank { "シリーズ #$seriesId" } ?: "シリーズ #$seriesId",
            description = detail?.description.orEmpty(),
            thumbnailUrl = detail?.thumbnailUrl.orEmpty(),
            itemsCount = data.totalCount.takeIf { it > 0 } ?: (detail?.itemsCount ?: data.items.size)
        )
        val videos = data.items.mapNotNull { it.video?.toVideoSummary() }
        Pair(userSeries, videos)
    }

    /** ユーザーID直接指定またはキーワード検索でユーザーを検索・解決する */
    suspend fun searchUsers(keyword: String): Result<List<OwnerProfile>> = suspendRunCatching {
        val clean = keyword.trim()
        val directId = clean.removePrefix("user/").toLongOrNull()
        if (directId != null && directId > 0L) {
            val profile = fetchOwnerProfile(directId).getOrThrow()
            return@suspendRunCatching listOf(profile)
        }

        // キーワードによる公式ユーザー検索 API (api.cas.nicovideo.jp)
        val response = httpClient.get("https://api.cas.nicovideo.jp/v1/search/users") {
            applyNicoHeaders()
            parameter("searchWord", clean)
            parameter("limit", 30)
        }
        response.ensureSuccess("searchUsers")
        val dto: CasUserSearchRootDto = decodeResponse(response.bodyAsText())
        dto.data?.users.orEmpty().map { item ->
            val icon = item.icons?.urls?.get("150x150") ?: item.icons?.urls?.get("50x50").orEmpty()
            OwnerProfile(
                id = item.id.toLongOrNull() ?: 0L,
                nickname = item.nickname,
                description = item.description,
                iconUrl = icon,
                followerCount = item.followerCount.toLong()
            )
        }
    }

    private inline fun <reified T> decodeResponse(text: String): T = NicoApiConstants.sharedJsonParser.decodeFromString(text)
}
