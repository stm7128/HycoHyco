package com.github.stm7128.hycohyco.data.model

import com.github.stm7128.hycohyco.domain.model.VideoSummary
import kotlinx.serialization.Serializable

/**
 * ニコニコ スナップショット検索 API v2 のレスポンスルート DTO
 */
@Serializable
data class SnapshotSearchResponseDto(
    val meta: SnapshotSearchMetaDto,
    val data: List<SnapshotSearchItemDto> = emptyList()
)

/**
 * スナップショット検索 API のメタ情報 DTO
 */
@Serializable
data class SnapshotSearchMetaDto(
    val status: Int = 200,
    val totalCount: Int = 0,
    val id: String = ""
)

/**
 * スナップショット検索 API の個別動画アイテム DTO
 */
@Serializable
data class SnapshotSearchItemDto(
    val contentId: String,
    val title: String = "",
    val userId: Long? = null,
    val viewCounter: Int = 0,
    val commentCounter: Int = 0,
    val likeCounter: Int = 0,
    val mylistCounter: Int = 0,
    val lengthSeconds: Int = 0,
    val thumbnailUrl: String = "",
    val startTime: String = ""
) {
    /**
     * ドメインモデル [VideoSummary] への変換
     * サムネイル URL は HTTPS に正規化する
     */
    fun toDomain(): VideoSummary {
        val safeThumbnailUrl = if (thumbnailUrl.startsWith("http://")) {
            thumbnailUrl.replaceFirst("http://", "https://")
        } else {
            thumbnailUrl
        }

        val ownerDisplay = if (userId != null && userId > 0) "ID: $userId" else ""

        return VideoSummary(
            contentId = contentId,
            title = title,
            viewCounter = viewCounter,
            commentCounter = commentCounter,
            likeCounter = likeCounter,
            mylistCounter = mylistCounter,
            lengthSeconds = lengthSeconds,
            thumbnailUrl = safeThumbnailUrl,
            startTime = startTime,
            ownerName = ownerDisplay,
            ownerId = userId?.toString().orEmpty()
        )
    }
}
