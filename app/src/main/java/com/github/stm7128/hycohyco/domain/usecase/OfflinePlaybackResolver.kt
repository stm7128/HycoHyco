package com.github.stm7128.hycohyco.domain.usecase

import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.repository.DownloadRepository
import java.io.File

/**
 * ローカルに保存されたオフライン動画の再生データ
 */
data class OfflinePlaybackData(
    val videoId: String,
    val title: String,
    val durationMs: Long,
    val ownerName: String,
    val videoFilePath: String?,
    val audioFilePath: String?,
    val masterPlaylistPath: String? = null,
    val comments: List<CommentItem>,
    val isAudioOnly: Boolean
)

/**
 * 保存済み動画の存在チェックおよびオフライン再生データの解決を行う UseCase
 */
class OfflinePlaybackResolver(
    private val downloadRepository: DownloadRepository
) {
    /**
     * 指定された動画IDのオフライン再生データを取得する (未保存またはファイルが存在しない場合は null)
     */
    fun resolve(videoId: String): OfflinePlaybackData? {
        val item = downloadRepository.getDownload(videoId) ?: return null
        if (item.status != DownloadStatus.COMPLETED) return null

        val mediaPath = item.mediaFilePath ?: return null
        val mediaFile = File(mediaPath)
        val downloadDir = mediaFile.parentFile ?: return null

        val videoFile = File(downloadDir, "video.mp4")
        val audioFile = File(downloadDir, "audio.m4a")
        val masterFile = File(downloadDir, "master.m3u8")

        val hasVideo = videoFile.exists() && videoFile.length() > 0L
        val hasAudio = audioFile.exists() && audioFile.length() > 0L
        val hasMaster = masterFile.exists() && masterFile.length() > 0L

        if (!hasVideo && !hasAudio && !hasMaster) return null

        // コメントのローカル復元 (Repository 経由で取得)
        val comments = downloadRepository.loadLocalComments(item.commentsFilePath)

        return OfflinePlaybackData(
            videoId = item.videoId,
            title = item.title,
            durationMs = item.durationMs,
            ownerName = item.ownerName,
            videoFilePath = if (hasVideo) videoFile.absolutePath else null,
            audioFilePath = if (hasAudio) audioFile.absolutePath else null,
            masterPlaylistPath = if (hasMaster) masterFile.absolutePath else null,
            comments = comments,
            isAudioOnly = item.downloadType == DownloadType.AUDIO_ONLY_AND_DANMAKU || !hasVideo
        )
    }
}
