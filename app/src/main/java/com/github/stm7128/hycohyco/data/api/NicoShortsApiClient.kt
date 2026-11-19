package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.RecommendRootDto
import com.github.stm7128.hycohyco.data.model.ShortsPlaylistRootDto
import com.github.stm7128.hycohyco.data.model.ShortsSearchRootDto
import com.github.stm7128.hycohyco.domain.model.SearchSort
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

/**
 * ニコニコショート (Niconico Shorts) 用フィード・連続視聴プレイリスト・検索 API クライアント
 */
class NicoShortsApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    companion object {
        private const val RECOMMEND_URL = "https://nvapi.nicovideo.jp/v1/recommend"
        private const val PLAYLIST_RECIPE_URL = "https://nvapi.nicovideo.jp/v1/playlist/recipe-id"
        private const val SEARCH_VIDEO_URL = "https://nvapi.nicovideo.jp/v2/search/video"
        private const val SHORTS_RECIPE_ID = "video_short_watch_recommendation"
        private const val SHORTS_RECIPE_VERSION = 1
        private const val SITE = "nicovideo"
    }

    /**
     * おすすめショート動画フィードを取得する (/v1/recommend)
     */
    suspend fun fetchRecommendedShorts(
        userSessionCookie: String = "",
        limit: Int = 20
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val response = httpClient.get(RECOMMEND_URL) {
            parameter("recipeId", SHORTS_RECIPE_ID)
            parameter("recipeVersion", SHORTS_RECIPE_VERSION)
            parameter("site", SITE)
            parameter("limit", limit.coerceIn(1, 50))
            applyNicoHeaders(userSessionCookie)
        }
        response.ensureSuccess("fetchRecommendedShorts")
        val dto = NicoApiConstants.sharedJsonParser.decodeFromString<RecommendRootDto>(response.bodyAsText())
        dto.data?.items.orEmpty().mapNotNull { item ->
            item.content?.toVideoSummary()
        }
    }

    /**
     * 指定のショート動画IDを起点とした連続視聴用キューを取得する (/v1/playlist/recipe-id)
     */
    suspend fun fetchShortsPlaylist(
        seedVideoId: String,
        userSessionCookie: String = ""
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val response = httpClient.get(PLAYLIST_RECIPE_URL) {
            parameter("recipeId", SHORTS_RECIPE_ID)
            parameter("recipeVersion", SHORTS_RECIPE_VERSION)
            parameter("site", SITE)
            parameter("videoId", seedVideoId)
            parameter("currentVideoId", seedVideoId)
            applyNicoHeaders(
                userSessionCookie = userSessionCookie,
                requestWith = "https://www.nicovideo.jp"
            )
            header("Referer", "https://www.nicovideo.jp/shorts/$seedVideoId")
        }
        response.ensureSuccess("fetchShortsPlaylist")
        val dto = NicoApiConstants.sharedJsonParser.decodeFromString<ShortsPlaylistRootDto>(response.bodyAsText())
        dto.data?.items.orEmpty().mapNotNull { item ->
            item.content?.toVideoSummary()
        }
    }

    /**
     * ショート動画をキーワードまたはタグで検索する (/v2/search/video?selectContentType=short)
     */
    suspend fun searchShorts(
        query: String,
        sort: SearchSort = SearchSort.VIEW_COUNT,
        page: Int = 1,
        pageSize: Int = 20,
        userSessionCookie: String = "",
        isTagSearch: Boolean = false
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@suspendRunCatching fetchRecommendedShorts(userSessionCookie, pageSize).getOrThrow()
        }
        val (sortKey, sortOrder) = mapSearchSort(sort)
        val response = httpClient.get(SEARCH_VIDEO_URL) {
            if (isTagSearch) {
                parameter("tag", trimmed)
            } else {
                parameter("keyword", trimmed)
            }
            parameter("sortKey", sortKey)
            parameter("sortOrder", sortOrder)
            parameter("page", page.coerceAtLeast(1))
            parameter("pageSize", pageSize.coerceIn(1, 50))
            parameter("selectContentType", "short")
            applyNicoHeaders(userSessionCookie)
        }
        response.ensureSuccess("searchShorts")
        val dto = NicoApiConstants.sharedJsonParser.decodeFromString<ShortsSearchRootDto>(response.bodyAsText())
        dto.data?.items.orEmpty().map { it.toVideoSummary() }
    }

    private fun mapSearchSort(sort: SearchSort): Pair<String, String> = when (sort) {
        SearchSort.START_TIME_DESC -> "registeredAt" to "desc"
        SearchSort.START_TIME_ASC -> "registeredAt" to "asc"
        SearchSort.VIEW_COUNT -> "viewCount" to "desc"
        SearchSort.VIEW_COUNT_ASC -> "viewCount" to "asc"
        SearchSort.COMMENT_TIME_DESC -> "lastCommentTime" to "desc"
        SearchSort.COMMENT_TIME_ASC -> "lastCommentTime" to "asc"
        SearchSort.COMMENT_COUNT -> "commentCount" to "desc"
        SearchSort.COMMENT_COUNT_ASC -> "commentCount" to "asc"
        SearchSort.LIKE_COUNT -> "likeCount" to "desc"
        SearchSort.LIKE_COUNT_ASC -> "likeCount" to "asc"
        SearchSort.MYLIST_COUNT -> "mylistCount" to "desc"
        SearchSort.MYLIST_COUNT_ASC -> "mylistCount" to "asc"
        SearchSort.DURATION_DESC -> "duration" to "desc"
        SearchSort.DURATION_ASC -> "duration" to "asc"
    }
}
