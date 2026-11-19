package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.SnapshotSearchResponseDto
import com.github.stm7128.hycohyco.domain.model.SearchSort
import com.github.stm7128.hycohyco.domain.model.SearchTarget
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException

/**
 * ニコニコ動画 スナップショット検索 API v2 通信クライアント
 */
class NicoSearchApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    companion object {
        private const val BASE_URL = "https://snapshot.search.nicovideo.jp/api/v2/snapshot/video/contents/search"
        private const val CONTEXT_NAME = "HycoHyco"
        private const val SEARCH_FIELDS = "contentId,title,userId,viewCounter,commentCounter,likeCounter,mylistCounter,lengthSeconds,thumbnailUrl,startTime"
    }

    /**
     * 動画を検索する
     *
     * @param query 検索クエリ
     * @param target 検索ターゲット (キーワード / タグ完全一致)
     * @param sort ソート順
     * @param offset 取得開始オフセット (0 〜)
     * @param limit 取得件数 (1 〜 100)
     * @return 検索結果 DTO
     */
    suspend fun searchVideos(
        query: String,
        target: SearchTarget = SearchTarget.KEYWORD,
        sort: SearchSort = SearchSort.VIEW_COUNT,
        offset: Int = 0,
        limit: Int = 25,
        startTimeGte: String? = null
    ): SnapshotSearchResponseDto {
        return httpClient.get(BASE_URL) {
            parameter("q", query)
            parameter("targets", target.apiValue)
            parameter("fields", SEARCH_FIELDS)
            if (startTimeGte != null) {
                parameter("filters[startTime][gte]", startTimeGte)
            }
            parameter("_sort", sort.apiValue)
            parameter("_offset", offset)
            parameter("_limit", limit)
            parameter("_context", CONTEXT_NAME)
        }.body()
    }

    /**
     * 指定された複数の動画 ID (contentId) に一致する動画メタデータを一括取得する
     * (スナップショット検索 API v2 の filters[contentId] を使用して最大 100 件を一撃取得)
     *
     * @param contentIds 動画 ID リスト (例: ["sm9", "sm1097445"])
     * @return 取得できた動画の [VideoSummary] リスト
     */
    suspend fun fetchVideoSummariesByContentIds(
        contentIds: List<String>
    ): List<VideoSummary> {
        val uniqueIds = contentIds.distinct().filter { it.isNotBlank() }
        if (uniqueIds.isEmpty()) return emptyList()

        val results = mutableListOf<VideoSummary>()
        // スナップショット検索 API の 1 リクエスト上限 100 件ごとにチャンク分割
        for (chunk in uniqueIds.chunked(100)) {
            try {
                val response: SnapshotSearchResponseDto = httpClient.get(BASE_URL) {
                    parameter("q", "")
                    parameter("targets", "title")
                    parameter("_sort", "-viewCounter")
                    chunk.forEachIndexed { index, id ->
                        parameter("filters[contentId][$index]", id)
                    }
                    parameter("fields", SEARCH_FIELDS)
                    parameter("_limit", chunk.size)
                    parameter("_context", CONTEXT_NAME)
                }.body()

                val mapped = response.data.map { it.toDomain() }
                results.addAll(mapped)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                android.util.Log.e("NicoSearchApiClient", "動画メタデータ一括取得失敗: ${chunk.take(3)}...", e)
            }
        }
        return results
    }
}
