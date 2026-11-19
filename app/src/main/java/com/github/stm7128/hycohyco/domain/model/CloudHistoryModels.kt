package com.github.stm7128.hycohyco.domain.model

/**
 * クラウド履歴のページネーション結果コンテナ
 */
data class CloudHistoryPage<T>(
    val items: List<T>,
    val nextCursor: String? = null,
    val hasNext: Boolean = false
)

/**
 * ニコニコアカウント クラウド視聴履歴アイテム
 */
data class CloudWatchHistoryItem(
    val itemId: String,
    val viewedAt: String,
    val playbackPositionSec: Long,
    val video: VideoSummary
) {
    /** 視聴進捗率 (0.0f .. 1.0f) */
    val progressFraction: Float
        get() {
            val dur = video.lengthSeconds.toFloat()
            if (dur <= 0f || playbackPositionSec <= 0L) return 0f
            return (playbackPositionSec / dur).coerceIn(0f, 1f)
        }

    /** 視聴日時の表示テキスト */
    val formattedViewedDate: String
        get() = formatIsoDateShort(viewedAt)
}

/**
 * ニコニコアカウント いいね！履歴アイテム
 */
data class CloudLikeHistoryItem(
    val likedAt: String,
    val thanksMessage: String?,
    val video: VideoSummary
) {
    /** いいね日時の表示テキスト */
    val formattedLikedDate: String
        get() = formatIsoDateShort(likedAt)
}

/**
 * ニコニコアカウント ニコられた / ニコった 履歴アイテム
 */
data class NicoruHistoryItem(
    val id: String,
    val commentBody: String,
    val commentVposMs: Long,
    val nicoruCount: Int,
    val createdAt: String,
    val commentCreatedAt: String,
    val isVideoOwnerNicoru: Boolean,
    val isFeatured: Boolean,
    val video: VideoSummary
) {
    /** ニコる発生日の表示テキスト */
    val formattedCreatedDate: String
        get() = formatIsoDateShort(createdAt)

    /** コメント投稿日の表示テキスト */
    val formattedCommentDate: String
        get() = formatIsoDateShort(commentCreatedAt)
}

private fun formatIsoDateShort(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        isoString.substringBefore("T").substringBefore(" ").replace("-", "/")
    } catch (_: Exception) {
        ""
    }
}
