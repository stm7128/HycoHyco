package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.FollowActivitiesRootDto
import com.github.stm7128.hycohyco.data.model.MylistDetailRootDto
import com.github.stm7128.hycohyco.data.model.MylistRootDto
import com.github.stm7128.hycohyco.data.model.WatchLaterRootDto
import com.github.stm7128.hycohyco.data.model.toVideoSummary
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

/**
 * ニコニコ動画 公式クラウドライブラリ API クライアント (あとで見る・マイリスト・フォロー新着)
 */
class NicoCloudApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    /** 公式「あとで見る」の動画一覧を取得する (最大1000件自動全件取得) */
    suspend fun fetchWatchLater(
        userSessionCookie: String,
        page: Int = 1,
        pageSize: Int = 100
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val allVideos = mutableListOf<VideoSummary>()
        var curPage = page
        var hasNext = true
        while (hasNext && curPage <= 10) {
            val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/me/watch-later") {
                applyNicoHeaders(userSessionCookie)
                parameter("pageSize", pageSize)
                parameter("page", curPage)
            }
            if (!response.status.isSuccess()) {
                if (curPage == page) response.ensureSuccess("fetchWatchLater")
                break
            }
            val dto: WatchLaterRootDto = decodeResponse(response.bodyAsText())
            allVideos.addAll(
                dto.data?.watchLater?.items.orEmpty().mapNotNull { item ->
                    item.video?.toVideoSummary()?.copy(
                        cloudItemId = item.itemId.takeIf { it > 0L }
                    )
                }
            )
            hasNext = dto.data?.watchLater?.hasNext ?: false
            curPage++
        }
        allVideos
    }

    /** 動画を「あとで見る」に追加する */
    suspend fun addWatchLater(userSessionCookie: String, videoId: String): Result<Unit> = suspendRunCatching {
        val response = httpClient.post("https://nvapi.nicovideo.jp/v1/users/me/watch-later") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/watch/$videoId")
            header(HttpHeaders.ContentType, "application/x-www-form-urlencoded")
            setBody("watchId=$videoId&memo=")
        }
        response.ensureSuccess("addWatchLater")
    }

    /** 動画を「あとで見る」から削除する */
    suspend fun deleteWatchLater(userSessionCookie: String, itemId: String): Result<Unit> = suspendRunCatching {
        val resolvedItemId = if (itemId.toLongOrNull() != null) {
            itemId
        } else {
            val latest = fetchWatchLater(userSessionCookie).getOrThrow()
            latest.firstOrNull { it.contentId == itemId }?.cloudItemId?.toString()
                ?: throw IllegalStateException("削除対象のアイテムIDが見つかりません: $itemId")
        }
        val response = httpClient.delete("https://nvapi.nicovideo.jp/v1/users/me/watch-later") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/watchlater")
            parameter("itemIds", resolvedItemId)
        }
        response.ensureSuccess("deleteWatchLater")
    }

    /** ログインユーザーのマイリスト一覧を取得する */
    suspend fun fetchMylists(userSessionCookie: String): Result<List<CloudMylist>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/me/mylists") {
            applyNicoHeaders(userSessionCookie)
        }
        response.ensureSuccess("fetchMylists")
        val dto: MylistRootDto = decodeResponse(response.bodyAsText())
        dto.data?.mylists.orEmpty().map {
            CloudMylist(it.id, it.name, it.description, it.isPublic, it.itemsCount)
        }
    }

    /** 指定されたマイリスト内の動画一覧を取得する */
    suspend fun fetchMylistVideos(
        userSessionCookie: String = "",
        mylistId: Long,
        page: Int = 1,
        pageSize: Int = 100
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val allVideos = mutableListOf<VideoSummary>()
        var curPage = page
        var hasNext = true
        val isMe = userSessionCookie.isNotBlank()

        while (hasNext && curPage <= 10) {
            val baseUrl = if (isMe) "https://nvapi.nicovideo.jp/v1/users/me/mylists/$mylistId"
                          else "https://nvapi.nicovideo.jp/v2/mylists/$mylistId"
            val res = httpClient.get(baseUrl) {
                applyNicoHeaders(userSessionCookie)
                parameter("pageSize", pageSize)
                parameter("page", curPage)
            }
            val targetRes = if (!res.status.isSuccess() && isMe && curPage == 1) {
                httpClient.get("https://nvapi.nicovideo.jp/v2/mylists/$mylistId") {
                    applyNicoHeaders(userSessionCookie)
                    parameter("pageSize", pageSize)
                    parameter("page", curPage)
                }
            } else res

            if (!targetRes.status.isSuccess()) {
                if (curPage == page) targetRes.ensureSuccess("fetchMylistVideos")
                break
            }
            val dto: MylistDetailRootDto = decodeResponse(targetRes.bodyAsText())
            allVideos.addAll(
                dto.data?.mylist?.items.orEmpty().mapNotNull { item ->
                    item.video?.toVideoSummary()?.copy(
                        cloudItemId = item.itemId.takeIf { it > 0L }
                    )
                }
            )
            hasNext = dto.data?.mylist?.hasNext ?: false
            curPage++
        }
        allVideos
    }

    /** 指定されたマイリストのメタ情報および登録動画一覧を取得する（自分・他人公開両対応） */
    suspend fun fetchMylistDetail(
        userSessionCookie: String = "",
        mylistId: Long
    ): Result<Pair<CloudMylist, List<VideoSummary>>> = suspendRunCatching {
        val isMe = userSessionCookie.isNotBlank()
        val baseUrl = if (isMe) "https://nvapi.nicovideo.jp/v1/users/me/mylists/$mylistId"
                      else "https://nvapi.nicovideo.jp/v2/mylists/$mylistId"
        val res = httpClient.get(baseUrl) {
            applyNicoHeaders(userSessionCookie)
            parameter("pageSize", 100)
            parameter("page", 1)
        }
        val targetRes = if (!res.status.isSuccess() && isMe) {
            httpClient.get("https://nvapi.nicovideo.jp/v2/mylists/$mylistId") {
                applyNicoHeaders(userSessionCookie)
                parameter("pageSize", 100)
                parameter("page", 1)
            }
        } else res

        targetRes.ensureSuccess("fetchMylistDetail")
        val dto: MylistDetailRootDto = decodeResponse(targetRes.bodyAsText())
        val payload = dto.data?.mylist ?: throw IllegalStateException("マイリスト情報が見つかりません")
        val cloudMylist = CloudMylist(
            id = payload.id.takeIf { it > 0L } ?: mylistId,
            name = payload.name.ifBlank { "マイリスト #$mylistId" },
            description = payload.description,
            isPublic = true,
            itemsCount = payload.totalCount
        )
        val videos = payload.items.mapNotNull { item ->
            item.video?.toVideoSummary()?.copy(
                cloudItemId = item.itemId.takeIf { it > 0L }
            )
        }
        Pair(cloudMylist, videos)
    }

    /** 動画を公式マイリストに追加する */
    suspend fun addMylistItem(userSessionCookie: String, mylistId: Long, videoId: String): Result<Unit> = suspendRunCatching {
        val response = httpClient.post("https://nvapi.nicovideo.jp/v1/users/me/mylists/$mylistId/items") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/watch/$videoId")
            parameter("itemId", videoId)
            parameter("description", "")
        }
        response.ensureSuccess("addMylistItem")
    }

    /** 動画を公式マイリストから削除する */
    suspend fun deleteMylistItem(userSessionCookie: String, mylistId: Long, itemId: String): Result<Unit> = suspendRunCatching {
        val resolvedItemId = if (itemId.toLongOrNull() != null) {
            itemId
        } else {
            val latest = fetchMylistVideos(userSessionCookie, mylistId).getOrThrow()
            latest.firstOrNull { it.contentId == itemId }?.cloudItemId?.toString()
                ?: throw IllegalStateException("削除対象のアイテムIDが見つかりません: $itemId")
        }
        val response = httpClient.delete("https://nvapi.nicovideo.jp/v1/users/me/mylists/$mylistId/items") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/mylist/$mylistId")
            parameter("itemIds", resolvedItemId)
        }
        response.ensureSuccess("deleteMylistItem")
    }

    /** フォロー中投稿者の新着動画タイムラインを取得する (最大 maxPages ページ連続自動取得) */
    suspend fun fetchFollowFeed(
        userSessionCookie: String,
        maxPages: Int = 4
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val allVideos = mutableListOf<VideoSummary>()
        var cursor: String? = null
        var pageCount = 0

        while (pageCount < maxPages) {
            val response = httpClient.get("https://api.feed.nicovideo.jp/v1/activities/followings/video") {
                applyNicoHeaders(userSessionCookie)
                parameter("context", "my_timeline")
                if (!cursor.isNullOrBlank()) {
                    parameter("cursor", cursor)
                }
            }
            if (!response.status.isSuccess()) {
                if (pageCount == 0) response.ensureSuccess("fetchFollowFeed")
                break
            }

            val dto: FollowActivitiesRootDto = decodeResponse(response.bodyAsText())
            val videos = dto.activities.mapNotNull { activity ->
                val content = activity.content ?: return@mapNotNull null
                if (content.id.isBlank()) return@mapNotNull null
                VideoSummary(
                    contentId = content.id,
                    title = content.title,
                    viewCounter = 0, commentCounter = 0, likeCounter = 0, mylistCounter = 0,
                    lengthSeconds = content.video?.duration ?: 0,
                    thumbnailUrl = activity.thumbnailUrl.orEmpty(),
                    startTime = content.startedAt ?: activity.createdAt,
                    ownerName = activity.actor?.name.orEmpty(),
                    ownerId = activity.actor?.id.orEmpty()
                )
            }
            allVideos.addAll(videos)

            val next = dto.nextCursor
            if (next.isNullOrBlank() || next == cursor || videos.isEmpty()) {
                break
            }
            cursor = next
            pageCount++
        }

        allVideos.distinctBy { it.contentId }
    }

    private inline fun <reified T> decodeResponse(text: String): T = NicoApiConstants.sharedJsonParser.decodeFromString(text)
}
