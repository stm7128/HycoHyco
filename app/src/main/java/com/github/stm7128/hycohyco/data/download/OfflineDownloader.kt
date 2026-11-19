package com.github.stm7128.hycohyco.data.download

import android.content.Context
import com.github.stm7128.hycohyco.data.api.NicoCommentApiClient
import com.github.stm7128.hycohyco.data.api.NicoWatchApiClient
import com.github.stm7128.hycohyco.data.local.AuthPreferencesDataSource
import com.github.stm7128.hycohyco.data.model.DomandAudioTrackDto
import com.github.stm7128.hycohyco.data.model.DomandVideoTrackDto
import com.github.stm7128.hycohyco.data.model.NvCommentResponseRootDto
import com.github.stm7128.hycohyco.domain.model.DownloadType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * ニコニコ動画の HLS ストリーム (AES-128 復号)・コメント・サムネイルを一括保存するダウンローダーコア
 */
class OfflineDownloader(
    private val context: Context,
    private val watchApiClient: NicoWatchApiClient,
    private val commentApiClient: NicoCommentApiClient,
    private val authPreferencesDataSource: AuthPreferencesDataSource? = null,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()
) {

    data class DownloadResult(
        val mediaFilePath: String,
        val commentsFilePath: String,
        val thumbnailFilePath: String?,
        val mediaSizeBytes: Long,
        val commentsSizeBytes: Long
    )

    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true; coerceInputValues = true }

    suspend fun downloadVideo(
        videoId: String,
        downloadType: DownloadType,
        onProgress: (percent: Int) -> Unit
    ): DownloadResult = withContext(Dispatchers.IO) {
        val downloadDir = getDownloadDir(videoId).apply { mkdirs() }
        val isAudioOnly = downloadType == DownloadType.AUDIO_ONLY_AND_DANMAKU
        val videoFile = File(downloadDir, "video.mp4")
        val audioFile = File(downloadDir, "audio.m4a")
        val commentsFile = File(downloadDir, "comments.json")
        val thumbnailFile = File(downloadDir, "thumbnail.jpg")

        onProgress(3)

        // 1. watch データと Domand セッション取得 (ログイン中の場合は user_session を付与)
        val sessionCookie = authPreferencesDataSource?.userSession?.first()?.formattedCookieHeader.orEmpty()
        val (watchRoot, nicosid) = watchApiClient.fetchWatchData(videoId, userSessionCookie = sessionCookie)
        val watchData = watchRoot.data ?: throw IOException("watch データの取得に失敗しました")
        val domand = watchData.media?.domand ?: throw IOException("Domand 配信データが存在しません")
        val clientDto = watchData.client ?: throw IOException("client 情報が存在しません")

        val outputs = buildOutputs(domand.videos, domand.audios, isAudioOnly)
        val (masterPlaylistUrl, domandBid) = watchApiClient.fetchDomandHlsAccessRight(
            videoId = videoId,
            watchTrackId = clientDto.watchTrackId,
            accessRightKey = domand.accessRightKey,
            outputs = outputs,
            nicosid = nicosid,
            userSessionCookie = sessionCookie
        )

        if (masterPlaylistUrl.isBlank()) throw IOException("マスタープレイリストURLの取得に失敗しました")
        onProgress(8)

        // 2. master.m3u8 取得 & 映像・音声ストリーム解決
        val masterContent = fetchStringWithCookie(masterPlaylistUrl, domandBid)
        val (videoStreamUrl, audioStreamUrl) = HlsPlaylistParser.parseMasterStreams(
            masterPlaylistContent = masterContent,
            baseUrl = masterPlaylistUrl
        )

        // 3. メディアダウンロード & AES-128 復号保存 (HLS BYTERANGE 再生対応)
        val videoRes = if (!isAudioOnly && videoStreamUrl != null) {
            downloadAndDecryptStream(videoStreamUrl, domandBid, videoFile) { p ->
                onProgress(10 + (40 * p / 100))
            }
        } else null

        // 音声ストリーム保存 (映像あり: 50%〜85%, 音声のみ: 10%〜85%)
        val targetAudioUrl = audioStreamUrl ?: videoStreamUrl ?: throw IOException("音声ストリームが見つかりません")
        val audioStartProgress = if (isAudioOnly) 10 else 50
        val audioRange = 85 - audioStartProgress
        val audioRes = downloadAndDecryptStream(targetAudioUrl, domandBid, audioFile) { p ->
            onProgress(audioStartProgress + (audioRange * p / 100))
        }

        // ローカル HLS プレイリストの生成・保存 (ExoPlayer ネイティブシーク対応)
        if (videoRes != null) {
            val videoM3u8 = LocalHlsPlaylistGenerator.generateMediaPlaylist(
                mediaFileName = "video.mp4",
                initByteLength = videoRes.initLength,
                segments = videoRes.segments,
                targetDuration = videoRes.targetDuration
            )
            File(downloadDir, "video.m3u8").writeText(videoM3u8)
        }
        if (audioRes != null) {
            val audioM3u8 = LocalHlsPlaylistGenerator.generateMediaPlaylist(
                mediaFileName = "audio.m4a",
                initByteLength = audioRes.initLength,
                segments = audioRes.segments,
                targetDuration = audioRes.targetDuration
            )
            File(downloadDir, "audio.m3u8").writeText(audioM3u8)
        }
        val masterM3u8 = LocalHlsPlaylistGenerator.generateMasterPlaylist(hasVideo = videoRes != null)
        val masterFile = File(downloadDir, "master.m3u8").apply { writeText(masterM3u8) }

        // 4. コメント取得 (85% 〜 95%)
        onProgress(88)
        saveComments(watchData, commentsFile, sessionCookie)
        onProgress(96)

        // 5. サムネイル保存 (96% 〜 100%)
        val savedThumbnailPath = saveThumbnail(watchData, thumbnailFile)
        onProgress(100)

        val totalMediaSize = (if (videoFile.exists()) videoFile.length() else 0L) +
            (if (audioFile.exists()) audioFile.length() else 0L)
        val mainMediaPath = masterFile.absolutePath

        DownloadResult(
            mediaFilePath = mainMediaPath,
            commentsFilePath = commentsFile.absolutePath,
            thumbnailFilePath = savedThumbnailPath,
            mediaSizeBytes = totalMediaSize,
            commentsSizeBytes = commentsFile.length()
        )
    }

    private data class StreamResult(
        val initLength: Long,
        val segments: List<LocalHlsPlaylistGenerator.ByteRangeSegment>,
        val targetDuration: Int
    )

    private suspend fun downloadAndDecryptStream(
        playlistUrl: String,
        domandBid: String,
        targetFile: File,
        onProgress: (percent: Int) -> Unit
    ): StreamResult? {
        val content = fetchStringWithCookie(playlistUrl, domandBid)
        val segments = HlsPlaylistParser.parseMediaPlaylist(content, playlistUrl)
        if (segments.segmentUrls.isEmpty()) return null

        // 暗号化鍵の取得
        val keyBytes = segments.keyInfo?.let { keyInfo ->
            fetchBytesWithCookie(keyInfo.keyUrl, domandBid)
        }

        var initLength = 0L
        val recordedSegments = mutableListOf<LocalHlsPlaylistGenerator.ByteRangeSegment>()
        var currentOffset = 0L

        if (targetFile.exists()) targetFile.delete()
        FileOutputStream(targetFile, true).use { fos ->
            // 初期化セグメント (init.mp4) は平文
            segments.initSegmentUrl?.let { initUrl ->
                val initBytes = fetchBytesWithCookie(initUrl, domandBid)
                fos.write(initBytes)
                initLength = initBytes.size.toLong()
                currentOffset = initLength
            }

            val total = segments.segments.ifEmpty {
                segments.segmentUrls.map { HlsPlaylistParser.SegmentItem(it, 6.0) }
            }
            for (index in total.indices) {
                currentCoroutineContext().ensureActive()
                val segItem = total[index]
                val encryptedBytes = fetchBytesWithCookie(segItem.url, domandBid)

                val decryptedBytes = if (keyBytes != null) {
                    val iv = segments.keyInfo.ivHex?.let { HlsSegmentDecryptor.parseIvHex(it) }
                        ?: HlsSegmentDecryptor.createSequenceIv(segments.mediaSequence + index)
                    HlsSegmentDecryptor.decrypt(encryptedBytes, keyBytes, iv)
                } else {
                    encryptedBytes
                }
                fos.write(decryptedBytes)
                val byteLength = decryptedBytes.size.toLong()
                recordedSegments.add(
                    LocalHlsPlaylistGenerator.ByteRangeSegment(
                        durationSeconds = segItem.durationSeconds,
                        byteLength = byteLength,
                        byteOffset = currentOffset
                    )
                )
                currentOffset += byteLength
                onProgress((index + 1) * 100 / total.size)
            }
        }

        return StreamResult(
            initLength = initLength,
            segments = recordedSegments,
            targetDuration = segments.targetDuration
        )
    }

    private suspend fun saveComments(
        watchData: com.github.stm7128.hycohyco.data.model.WatchDataDto,
        commentsFile: File,
        userSessionCookie: String
    ) {
        val nvComment = watchData.comment?.nvComment
        val commentsDto = try {
            if (nvComment != null && nvComment.params != null) {
                commentApiClient.fetchThreads(
                    server = nvComment.server,
                    threadKey = nvComment.threadKey,
                    params = nvComment.params,
                    userSessionCookie = userSessionCookie
                )
            } else null
        } catch (_: Exception) { null }
        commentsFile.writeText(if (commentsDto != null) json.encodeToString(commentsDto) else "{}")
    }

    private fun saveThumbnail(watchData: com.github.stm7128.hycohyco.data.model.WatchDataDto, thumbnailFile: File): String? {
        val thumb = watchData.video?.thumbnail
        val url = thumb?.short ?: thumb?.largeUrl ?: thumb?.middleUrl ?: thumb?.url ?: ""
        if (url.isBlank()) return null
        return try {
            val bytes = fetchBytesWithCookie(url, "")
            thumbnailFile.writeBytes(bytes)
            thumbnailFile.absolutePath
        } catch (_: Exception) { null }
    }

    private fun buildOutputs(
        videos: List<DomandVideoTrackDto>,
        audios: List<DomandAudioTrackDto>,
        isAudioOnly: Boolean
    ): List<List<String>> {
        val audioId = audios.maxByOrNull { it.bitRate }?.id ?: "audio-aac-128kbps"
        if (isAudioOnly) return listOf(listOf(audioId))
        val videoId = videos.firstOrNull { it.label.contains("720") }?.id
            ?: videos.firstOrNull()?.id ?: "video-h264-720p"
        return listOf(listOf(videoId, audioId))
    }

    private fun fetchStringWithCookie(url: String, cookie: String): String {
        return fetchBytesWithCookie(url, cookie).decodeToString()
    }

    private fun fetchBytesWithCookie(url: String, cookie: String): ByteArray {
        val req = Request.Builder().url(url).apply {
            if (cookie.isNotBlank()) header("Cookie", cookie)
            header("User-Agent", "HycoHyco/1.0")
        }.build()
        okHttpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}: $url")
            return resp.body?.bytes() ?: ByteArray(0)
        }
    }

    fun getDownloadDir(videoId: String): File {
        val baseDir = context.getExternalFilesDir("downloads") ?: File(context.filesDir, "downloads")
        return File(baseDir, videoId)
    }

    fun deleteDownloadedFiles(videoId: String) {
        val dir = getDownloadDir(videoId)
        if (dir.exists()) dir.deleteRecursively()
    }
}
