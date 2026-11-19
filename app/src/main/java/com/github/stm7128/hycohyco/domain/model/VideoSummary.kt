package com.github.stm7128.hycohyco.domain.model

/**
 * 動画一覧・検索結果・ランキング表示用の軽量動画ドメインモデル
 *
 * @property contentId 動画ID (例: "sm9")
 * @property title 動画タイトル
 * @property viewCounter 再生数
 * @property commentCounter コメント数
 * @property likeCounter いいね数
 * @property mylistCounter マイリスト数
 * @property lengthSeconds 動画の長さ (秒)
 * @property thumbnailUrl サムネイル画像URL
 * @property startTime 投稿日時 (ISO 8601 文字列)
 */
data class VideoSummary(
    val contentId: String,
    val title: String,
    val viewCounter: Int,
    val commentCounter: Int,
    val likeCounter: Int,
    val mylistCounter: Int,
    val lengthSeconds: Int,
    val thumbnailUrl: String,
    val startTime: String,
    val ownerName: String = "",
    val ownerId: String = "",
    val cloudItemId: Long? = null,
    val contentType: String = "long",
    val shortThumbnailUrl: String = ""
) {
    /** ニコニコショート対象動画かどうか */
    val isShort: Boolean
        get() = contentType.equals("short", ignoreCase = true) || contentId.startsWith("ss", ignoreCase = true)
    /** 再生時間 "mm:ss" または "hh:mm:ss" の文字列表現 */
    val formattedDuration: String
        get() {
            val hours = lengthSeconds / 3600
            val minutes = (lengthSeconds % 3600) / 60
            val seconds = lengthSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    /** 再生数の略記 (例: 12500 -> "1.2万") */
    val formattedViewCount: String
        get() = formatCounter(viewCounter)

    /** コメント数の略記 */
    val formattedCommentCount: String
        get() = formatCounter(commentCounter)

    /** いいね数の略記 */
    val formattedLikeCount: String
        get() = formatCounter(likeCounter)

    /** マイリスト数の略記 */
    val formattedMylistCount: String
        get() = formatCounter(mylistCounter)

    /** 投稿日時の日付表現 (例: "2024/11/09") */
    val formattedRelativeTime: String
        get() {
            if (startTime.isBlank()) return ""
            return try {
                val datePart = startTime.substringBefore("T").substringBefore(" ")
                datePart.replace("-", "/")
            } catch (_: Exception) {
                ""
            }
        }

    private fun formatCounter(count: Int): String {
        return when {
            count >= 100_000_000 -> String.format("%.1f億", count / 100_000_000f)
            count >= 10_000 -> String.format("%.1f万", count / 10_000f)
            else -> count.toString()
        }
    }
}
