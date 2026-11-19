package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.CloudLikeHistoryRootDto
import com.github.stm7128.hycohyco.data.model.CloudWatchHistoryRootDto
import com.github.stm7128.hycohyco.data.model.NicoruHistoryRootDto
import com.github.stm7128.hycohyco.data.model.NicoruReceiveCountRootDto
import com.github.stm7128.hycohyco.data.model.toDomain
import com.github.stm7128.hycohyco.domain.model.CloudHistoryPage
import com.github.stm7128.hycohyco.domain.model.CloudLikeHistoryItem
import com.github.stm7128.hycohyco.domain.model.CloudWatchHistoryItem
import com.github.stm7128.hycohyco.domain.model.NicoruHistoryItem
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

/**
 * ニコニコアカウント クラウド各種履歴（視聴・いいね！・ニコられた・ニコった）API クライアント
 */
class NicoCloudHistoryApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    /** クラウド視聴履歴を取得する (GET /v2/users/me/watch/history) */
    suspend fun fetchWatchHistory(
        userSessionCookie: String,
        cursor: String? = null,
        limit: Int = 30
    ): Result<CloudHistoryPage<CloudWatchHistoryItem>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v2/users/me/watch/history") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/history/video")
            parameter("limit", limit)
            if (!cursor.isNullOrBlank()) parameter("cursor", cursor)
        }
        response.ensureSuccess("fetchWatchHistory")
        val body = response.bodyAsText()
        val dto: CloudWatchHistoryRootDto = NicoApiConstants.sharedJsonParser.decodeFromString(body)
        val items = dto.data?.items.orEmpty().mapNotNull { it.toDomain() }
        val next = dto.data?.nextCursor?.takeIf { it.isNotBlank() }
        CloudHistoryPage(items = items, nextCursor = next, hasNext = next != null)
    }

    /** クラウド視聴履歴を削除する (target=smXXXX で個別、target=all で全件削除) */
    suspend fun deleteWatchHistory(
        userSessionCookie: String,
        target: String
    ): Result<Unit> = suspendRunCatching {
        val response = httpClient.delete("https://nvapi.nicovideo.jp/v2/users/me/watch/history") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/history/video")
            parameter("target", target)
        }
        response.ensureSuccess("deleteWatchHistory")
    }

    /** いいね！履歴を取得する (GET /v1/users/me/likes) */
    suspend fun fetchLikeHistory(
        userSessionCookie: String,
        page: Int = 1,
        pageSize: Int = 50
    ): Result<CloudHistoryPage<CloudLikeHistoryItem>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/me/likes") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/history/like")
            parameter("page", page)
            parameter("pageSize", pageSize)
        }
        response.ensureSuccess("fetchLikeHistory")
        val body = response.bodyAsText()
        val dto: CloudLikeHistoryRootDto = NicoApiConstants.sharedJsonParser.decodeFromString(body)
        val items = dto.data?.items.orEmpty().mapNotNull { it.toDomain() }
        val hasNext = dto.data?.summary?.hasNext ?: false
        CloudHistoryPage(items = items, nextCursor = null, hasNext = hasNext)
    }

    /** いいね！を解除する (DELETE /v1/users/me/likes/items?videoId={videoId}) */
    suspend fun unlikeVideo(
        userSessionCookie: String,
        videoId: String
    ): Result<Unit> = suspendRunCatching {
        val response = httpClient.delete("https://nvapi.nicovideo.jp/v1/users/me/likes/items") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/history/like")
            parameter("videoId", videoId)
        }
        response.ensureSuccess("unlikeVideo")
    }

    /** ニコられた履歴を取得する (GET /v1/users/me/nicoru/receive) */
    suspend fun fetchNicoruReceive(
        userSessionCookie: String,
        cursor: String? = null,
        byVideoOwner: Boolean = false,
        limit: Int = 100
    ): Result<CloudHistoryPage<NicoruHistoryItem>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/me/nicoru/receive") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/nicoru/passive")
            parameter("limit", limit)
            parameter("byVideoOwner", byVideoOwner)
            if (!cursor.isNullOrBlank()) parameter("cursor", cursor)
        }
        response.ensureSuccess("fetchNicoruReceive")
        val body = response.bodyAsText()
        val dto: NicoruHistoryRootDto = NicoApiConstants.sharedJsonParser.decodeFromString(body)
        val items = dto.data?.items.orEmpty().mapNotNull { it.toDomain() }
        val next = dto.data?.next?.takeIf { it.isNotBlank() }
        CloudHistoryPage(items = items, nextCursor = next, hasNext = next != null)
    }

    /** ニコられた合計回数を取得する (GET /v1/users/me/nicoru/receive/count) */
    suspend fun fetchNicoruReceiveCount(
        userSessionCookie: String
    ): Result<Int> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/me/nicoru/receive/count") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/nicoru/passive")
        }
        response.ensureSuccess("fetchNicoruReceiveCount")
        val body = response.bodyAsText()
        val dto: NicoruReceiveCountRootDto = NicoApiConstants.sharedJsonParser.decodeFromString(body)
        dto.data?.count ?: 0
    }

    /** ニコった履歴を取得する (GET /v1/users/me/nicoru/send) */
    suspend fun fetchNicoruSend(
        userSessionCookie: String,
        cursor: String? = null,
        limit: Int = 100
    ): Result<CloudHistoryPage<NicoruHistoryItem>> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/users/me/nicoru/send") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/nicoru/active")
            parameter("limit", limit)
            if (!cursor.isNullOrBlank()) parameter("cursor", cursor)
        }
        response.ensureSuccess("fetchNicoruSend")
        val body = response.bodyAsText()
        val dto: NicoruHistoryRootDto = NicoApiConstants.sharedJsonParser.decodeFromString(body)
        val items = dto.data?.items.orEmpty().mapNotNull { it.toDomain() }
        val next = dto.data?.next?.takeIf { it.isNotBlank() }
        CloudHistoryPage(items = items, nextCursor = next, hasNext = next != null)
    }

    /** ニコるを解除（ニコった履歴から削除）する (DELETE /v1/users/me/nicoru/send/{nicoruId}) */
    suspend fun deleteNicoruSend(
        userSessionCookie: String,
        nicoruId: String
    ): Result<Unit> = suspendRunCatching {
        val response = httpClient.delete("https://nvapi.nicovideo.jp/v1/users/me/nicoru/send/$nicoruId") {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/my/nicoru/active")
        }
        response.ensureSuccess("deleteNicoruSend")
    }
}
