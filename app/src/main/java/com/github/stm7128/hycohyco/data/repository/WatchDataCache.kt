package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.api.NicoWatchApiClient
import com.github.stm7128.hycohyco.data.model.WatchResponseRootDto
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 動画再生開始時の watch v3 API の短時間インメモリキャッシュ
 * (getVideoDetail / getStreamSession / getVideoComments の重複リクエストを 1 回に統合)
 *
 * ネットワーク通信中に Mutex をロックし続けない非ブロッキングな In-Flight 結合を採用。
 */
class WatchDataCache(
    private val apiClient: NicoWatchApiClient,
    private val ttlMs: Long = 15_000L
) {
    private val stateMutex = Mutex()
    private val inFlightMap = mutableMapOf<String, CompletableDeferred<Pair<WatchResponseRootDto, String>>>()

    private var cachedVideoId: String? = null
    private var cachedCookie: String? = null
    private var cachedTimestamp: Long = 0L
    private var cachedResult: Pair<WatchResponseRootDto, String>? = null

    /**
     * キャッシュがあればそれを返し、なければ watch v3 API を呼び出してキャッシュする
     */
    suspend fun getWatchData(
        videoId: String,
        userSessionCookie: String,
        forceRefresh: Boolean = false
    ): Pair<WatchResponseRootDto, String> {
        val cacheKey = "$videoId:$userSessionCookie"
        val deferredToWait: CompletableDeferred<Pair<WatchResponseRootDto, String>>?

        stateMutex.withLock {
            val now = System.currentTimeMillis()
            val currentCached = cachedResult
            if (!forceRefresh &&
                currentCached != null &&
                cachedVideoId == videoId &&
                cachedCookie == userSessionCookie &&
                (now - cachedTimestamp) < ttlMs
            ) {
                return currentCached
            }

            // 同一動画・Cookie の通信がすでに進行中であればその完了を待つ
            val existingInFlight = inFlightMap[cacheKey]
            if (existingInFlight != null) {
                deferredToWait = existingInFlight
            } else {
                val newDeferred = CompletableDeferred<Pair<WatchResponseRootDto, String>>()
                inFlightMap[cacheKey] = newDeferred
                deferredToWait = null
            }
        }

        // 進行中の通信がある場合は Mutex 外で待機
        if (deferredToWait != null) {
            return deferredToWait.await()
        }

        // 自身がイニシエータとして通信を実行 (Mutex 外で非同期通信)
        return try {
            val result = apiClient.fetchWatchData(videoId, userSessionCookie)
            stateMutex.withLock {
                // 有効な動画メタデータが存在する場合のみキャッシュに保存
                if (result.first.data?.video != null) {
                    cachedVideoId = videoId
                    cachedCookie = userSessionCookie
                    cachedTimestamp = System.currentTimeMillis()
                    cachedResult = result
                }
                val deferred = inFlightMap.remove(cacheKey)
                deferred?.complete(result)
            }
            result
        } catch (e: Throwable) {
            withContext(NonCancellable) {
                stateMutex.withLock {
                    val deferred = inFlightMap.remove(cacheKey)
                    deferred?.completeExceptionally(e)
                }
            }
            throw e
        }
    }

    /**
     * キャッシュを明示的にクリアする
     */
    suspend fun invalidate() = stateMutex.withLock {
        cachedVideoId = null
        cachedCookie = null
        cachedTimestamp = 0L
        cachedResult = null
    }
}
