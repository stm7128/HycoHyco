package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.NicoVideoEssentialRootDto
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

/**
 * ニコニコ公式 動画基本情報 API (`GET https://nvapi.nicovideo.jp/v1/videos?watchIds={id}`) クライアント
 * スナップショット検索 API 未反映の最新投稿動画の 4大指標をリアルタイムに即座取得する
 */
class NicoVideoApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    companion object {
        private const val BASE_URL = "https://nvapi.nicovideo.jp/v1/videos"
    }

    /**
     * 動画 ID から最新の基本情報（再生数・コメント・いいね・マイリスト・投稿者情報）を取得する
     */
    suspend fun fetchVideoEssential(watchId: String): VideoSummary? = suspendRunCatching {
        val response = httpClient.get(BASE_URL) {
            parameter("watchIds", watchId)
            applyNicoHeaders(null)
            header("Referer", "https://www.nicovideo.jp/watch/$watchId")
        }
        if (!response.status.isSuccess()) return@suspendRunCatching null
        val rootDto = response.body<NicoVideoEssentialRootDto>()
        rootDto.data?.items?.firstOrNull()?.video?.toVideoSummary()
    }.getOrNull()
}
