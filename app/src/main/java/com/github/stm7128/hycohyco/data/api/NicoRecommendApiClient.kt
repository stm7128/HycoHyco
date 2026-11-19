package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.RecommendRootDto
import com.github.stm7128.hycohyco.domain.model.HomeRecommendFeed
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * ニコニコ公式 レコメンド (関連動画・ホームおすすめ動画) API クライアント
 */
class NicoRecommendApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    companion object {
        private const val BASE_URL = "https://nvapi.nicovideo.jp/v1/recommend"
        private const val WATCH_RECIPE_ID = "video_watch_recommendation"
        private const val HOME_RECIPE_ID = "video_recommendation_recommend"
        private const val HOME_RECIPE_VERSION = 1
        private const val TOP_RECOMMEND_TAG_LIMIT = 10
        private const val SITE = "nicovideo"
    }

    /**
     * 指定された動画 ID に関連するレコメンド動画一覧を取得する
     */
    suspend fun fetchRecommendVideos(videoId: String): Result<List<VideoSummary>> = suspendRunCatching {
        val response = httpClient.get(BASE_URL) {
            parameter("recipeId", WATCH_RECIPE_ID)
            parameter("videoId", videoId)
            parameter("site", SITE)
            applyNicoHeaders(null)
            header("Referer", "https://www.nicovideo.jp/watch/$videoId")
        }
        response.ensureSuccess("fetchRecommendVideos")

        val rootDto = response.body<RecommendRootDto>()
        val items = rootDto.data?.items ?: emptyList()

        items.mapNotNull { item ->
            if (item.contentType == "video" && item.content != null) {
                item.content.toVideoSummary()
            } else {
                null
            }
        }
    }

    /**
     * ニコニコ公式ホーム「おすすめ動画」(/recommendations) フィードを取得する
     * ログイン時は user_session によりパーソナライズ推薦、ゲスト時は人気推薦が返却される
     */
    suspend fun fetchHomeRecommendFeed(
        userSessionCookie: String = "",
        limit: Int = 50
    ): Result<HomeRecommendFeed> = suspendRunCatching {
        val response = httpClient.get(BASE_URL) {
            parameter("recipeId", HOME_RECIPE_ID)
            parameter("recipeVersion", HOME_RECIPE_VERSION)
            parameter("site", SITE)
            parameter("limit", limit.coerceIn(1, 50))
            applyNicoHeaders(userSessionCookie)
            header("Referer", "https://www.nicovideo.jp/recommendations")
        }
        response.ensureSuccess("fetchHomeRecommendFeed")

        val rootDto = response.body<RecommendRootDto>()
        val videos = rootDto.data?.items.orEmpty().mapNotNull { it.toHomeRecommendVideoOrNull() }
        val topTags = videos
            .mapNotNull { it.reasonTag }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(TOP_RECOMMEND_TAG_LIMIT)
            .map { it.key }

        HomeRecommendFeed(videos = videos, recommendedTags = topTags)
    }
}
