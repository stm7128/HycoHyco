package com.github.stm7128.hycohyco.domain.model

import java.util.Locale

/**
 * ダウンロード保存済み動画のドメインモデル
 */
data class DownloadedVideoItem(
    val videoId: String,
    val title: String,
    val durationMs: Long,
    val ownerName: String = "",
    val thumbnailUrl: String = "",
    val downloadType: DownloadType = DownloadType.VIDEO_AND_DANMAKU,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progressPercent: Int = 0,
    val mediaFilePath: String? = null,
    val commentsFilePath: String? = null,
    val thumbnailFilePath: String? = null,
    val mediaSizeBytes: Long = 0L,
    val commentsSizeBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
) {
    /** メディア + コメントの合計ファイル容量 (バイト) */
    val totalSizeBytes: Long
        get() = mediaSizeBytes + commentsSizeBytes

    /** 人間可読なフォーマット済み合計ファイルサイズ (例: "45.2 MB") */
    val formattedTotalSize: String
        get() = formatFileSize(totalSizeBytes)

    /** フォーマット済み再生時間 (例: "04:15") */
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val hours = minutes / 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes % 60, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0L) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            var size = bytes.toDouble()
            var unitIndex = 0
            while (size >= 1024.0 && unitIndex < units.size - 1) {
                size /= 1024.0
                unitIndex++
            }
            return String.format(Locale.getDefault(), "%.1f %s", size, units[unitIndex])
        }
    }
}
