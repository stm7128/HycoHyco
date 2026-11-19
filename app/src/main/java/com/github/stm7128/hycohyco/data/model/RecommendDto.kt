package com.github.stm7128.hycohyco.data.model

import com.github.stm7128.hycohyco.domain.model.HomeRecommendVideo
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import kotlinx.serialization.Serializable

/**
 * ニコニコ公式 レコメンド API (v1/recommend) のルートレスポンス DTO
 */
@Serializable
data class RecommendRootDto(
    val meta: CloudMetaDto? = null,
    val data: RecommendDataDto? = null
)

@Serializable
data class RecommendDataDto(
    val recipeId: String = "",
    val items: List<RecommendItemDto> = emptyList()
)

@Serializable
data class RecommendItemDto(
    val id: String = "",
    val contentType: String = "",
    val content: RecommendVideoContentDto? = null,
    val reason: RecommendReasonDto? = null
) {
    fun toHomeRecommendVideoOrNull(): HomeRecommendVideo? {
        if (contentType != "video" || content == null) return null
        val summary = content.toVideoSummary()
        if (summary.contentId.isBlank()) return null
        val reasonTag = reason?.tag?.trim()?.takeIf { it.isNotEmpty() }
        return HomeRecommendVideo(video = summary, reasonTag = reasonTag)
    }
}

@Serializable
data class RecommendReasonDto(
    val tag: String? = null
)

@Serializable
data class RecommendVideoContentDto(
    val id: String = "",
    val contentType: String = "long",
    val title: String = "",
    val registeredAt: String = "",
    val duration: Long = 0L,
    val thumbnail: RecommendThumbnailDto? = null,
    val count: RecommendCountDto? = null,
    val owner: RecommendOwnerDto? = null
) {
    /**
     * ドメインモデル [VideoSummary] へ変換する
     */
    fun toVideoSummary(): VideoSummary {
        val thumbUrl = thumbnail?.listingUrl
            ?: thumbnail?.largeUrl
            ?: thumbnail?.url
            ?: thumbnail?.shortUrl
            ?: ""

        return VideoSummary(
            contentId = id,
            title = title,
            viewCounter = (count?.view ?: 0L).toInt(),
            commentCounter = (count?.comment ?: 0L).toInt(),
            mylistCounter = (count?.mylist ?: 0L).toInt(),
            likeCounter = (count?.like ?: 0L).toInt(),
            lengthSeconds = duration.toInt(),
            thumbnailUrl = thumbUrl,
            startTime = registeredAt,
            ownerName = owner?.name ?: "",
            ownerId = owner?.id ?: "",
            contentType = contentType,
            shortThumbnailUrl = thumbnail?.shortUrl ?: ""
        )
    }
}

@Serializable
data class RecommendThumbnailDto(
    val url: String = "",
    val listingUrl: String? = null,
    val largeUrl: String? = null,
    val shortUrl: String? = null
)

@Serializable
data class RecommendCountDto(
    val view: Long = 0L,
    val comment: Long = 0L,
    val mylist: Long = 0L,
    val like: Long = 0L
)

@Serializable
data class RecommendOwnerDto(
    val id: String = "",
    val name: String? = null,
    val iconUrl: String? = null
)
