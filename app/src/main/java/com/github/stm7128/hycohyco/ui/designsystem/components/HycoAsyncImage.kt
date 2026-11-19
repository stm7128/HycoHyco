package com.github.stm7128.hycohyco.ui.designsystem.components

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.data.api.HycoHttpClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap

/**
 * 端末の最大ヒープメモリの 1/8 を割り当てたサムネイル画像 LRU キャッシュ (16MB〜64MB)
 */
private val maxCacheMemory: Int = ((Runtime.getRuntime().maxMemory() / 8).toInt())
    .coerceIn(16 * 1024 * 1024, 64 * 1024 * 1024)

private val imageMemoryCache = object : LruCache<String, ImageBitmap>(maxCacheMemory) {
    override fun sizeOf(key: String, value: ImageBitmap): Int {
        return value.asAndroidBitmap().allocationByteCount
    }
}

/** 同時リクエスト重複排除用のインフライト Job マップ */
private val inFlightRequests = ConcurrentHashMap<String, Deferred<ImageBitmap?>>()

private val imageHttpClient: HttpClient
    get() = HycoHttpClientProvider.sharedKtorClient

/**
 * 外部ライブラリ非依存の軽量・高速・低メモリ非同期画像 Composable
 *
 * @param url 画像 URL
 * @param contentDescription アクセシビリティ説明
 * @param modifier 修飾子
 * @param contentScale 画像スケーリング方法
 * @param targetMaxWidth 最大デコード幅 (ダウンサンプリング用)
 * @param targetMaxHeight 最大デコード高さ (ダウンサンプリング用)
 */
@Composable
fun HycoAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    targetMaxWidth: Int = 720,
    targetMaxHeight: Int = 720
) {
    val initiallyCached = remember(url) { if (url.isNotBlank()) imageMemoryCache.get(url) else null }
    var imageBitmap by remember(url) { mutableStateOf(initiallyCached) }
    var isLoading by remember(url) { mutableStateOf(imageBitmap == null && url.isNotBlank()) }
    var isError by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url, targetMaxWidth, targetMaxHeight) {
        if (url.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }

        val cached = imageMemoryCache.get(url)
        if (cached != null) {
            imageBitmap = cached
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        isError = false

        val loaded = fetchAndDecodeImage(url, targetMaxWidth, targetMaxHeight)
        if (loaded != null) {
            imageMemoryCache.put(url, loaded)
            imageBitmap = loaded
            isLoading = false
        } else {
            isError = true
            isLoading = false
        }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // メモリキャッシュからの即時読み込み時は Crossfade をスキップしてチラつきを根絶
        if (initiallyCached != null && imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            Crossfade(
                targetState = Triple(imageBitmap, isLoading, isError),
                label = "AsyncImageCrossfade"
            ) { (bmp, loading, error) ->
                when {
                    bmp != null -> {
                        Image(
                            bitmap = bmp,
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = contentScale
                        )
                    }
                    loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                    error -> {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = "画像読み込みエラー",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

/**
 * 重複排除を行いながら画像をダウンロードし、要求サイズへダウンサンプリングしてデコード
 */
private suspend fun fetchAndDecodeImage(
    url: String,
    reqWidth: Int,
    reqHeight: Int
): ImageBitmap? = coroutineScope {
    val existing = inFlightRequests[url]
    if (existing != null) {
        return@coroutineScope existing.await()
    }

    val deferred = async(Dispatchers.IO) {
        try {
            val bytes = imageHttpClient.get(url).readRawBytes()
            decodeSampledBitmap(bytes, reqWidth, reqHeight)
        } catch (_: Exception) {
            null
        } finally {
            inFlightRequests.remove(url)
        }
    }
    inFlightRequests[url] = deferred
    deferred.await()
}

/**
 * inSampleSize によるダウンサンプリングデコード
 */
private fun decodeSampledBitmap(
    bytes: ByteArray,
    reqWidth: Int,
    reqHeight: Int
): ImageBitmap? {
    if (bytes.isEmpty()) return null
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
    } catch (_: Throwable) {
        null
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (reqWidth > 0 && reqHeight > 0 && (height > reqHeight || width > reqWidth)) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize.coerceAtLeast(1)
}
