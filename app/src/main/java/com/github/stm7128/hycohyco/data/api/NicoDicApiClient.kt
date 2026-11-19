package com.github.stm7128.hycohyco.data.api

import android.util.LruCache
import com.github.stm7128.hycohyco.domain.model.VideoTag
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Serializable
private data class NicodicArticleDto(
    val id: Long = 0L,
    val title: String = "",
    val summary: String = "",
    val url: String = ""
)

private data class NicodicCacheEntry(
    val exists: Boolean,
    val url: String? = null,
    val summary: String? = null
)

/**
 * ニコニコ大百科 API (api.dic.nicovideo.jp) クライアント
 *
 * 現在の Watch API v3 では `isNicodicArticleExists` が常に false を返すため、
 * 本クライアントが並列・キャッシュ付きで大百科記事の有無（「百」バッジ用）と要約を補完する。
 */
class NicoDicApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    companion object {
        private const val ARTICLE_API_PREFIX = "https://api.dic.nicovideo.jp/v1/articles/article/"
        private const val MAX_CONCURRENCY = 6
        private const val MAX_CACHE_ENTRIES = 1000
    }

    private val articleCache = object : LruCache<String, NicodicCacheEntry>(MAX_CACHE_ENTRIES) {}

    /**
     * 与えられたタグ一覧に対してニコニコ大百科記事の有無・URL・サマリーを並列補完する
     */
    suspend fun resolveTags(tags: List<VideoTag>): List<VideoTag> {
        if (tags.isEmpty()) return tags
        val semaphore = Semaphore(MAX_CONCURRENCY)

        return coroutineScope {
            tags.map { tag ->
                async {
                    if (tag.isNicodicArticleExists && !tag.nicodicUrl.isNullOrBlank()) {
                        return@async tag
                    }
                    val cached = synchronized(articleCache) { articleCache.get(tag.name) }
                    if (cached != null) {
                        return@async tag.applyEntry(cached)
                    }
                    val fetched = semaphore.withPermit { fetchArticleEntry(tag.name) }
                    if (fetched != null) {
                        synchronized(articleCache) { articleCache.put(tag.name, fetched) }
                        tag.applyEntry(fetched)
                    } else {
                        tag
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun fetchArticleEntry(tagName: String): NicodicCacheEntry? {
        return try {
            val encoded = URLEncoder.encode(tagName, "UTF-8").replace("+", "%20")
            val response = httpClient.get("$ARTICLE_API_PREFIX$encoded") {
                header(HttpHeaders.Accept, "application/json")
            }
            when {
                response.status.isSuccess() -> {
                    val dto = NicoApiConstants.sharedJsonParser.decodeFromString<NicodicArticleDto>(response.bodyAsText())
                    NicodicCacheEntry(
                        exists = true,
                        url = dto.url.takeIf { it.isNotBlank() },
                        summary = dto.summary.trim().takeIf { it.isNotBlank() }
                    )
                }
                response.status == HttpStatusCode.NotFound -> {
                    NicodicCacheEntry(exists = false)
                }
                else -> null
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            null
        }
    }

    private fun VideoTag.applyEntry(entry: NicodicCacheEntry): VideoTag {
        return if (entry.exists) {
            copy(
                isNicodicArticleExists = true,
                nicodicUrl = entry.url ?: nicodicUrl,
                nicodicSummary = entry.summary ?: nicodicSummary
            )
        } else {
            this
        }
    }
}
