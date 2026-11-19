package com.github.stm7128.hycohyco.data.model

import com.github.stm7128.hycohyco.domain.model.VideoSummary
import kotlinx.serialization.Serializable

/**
 * ニコニコ公式 動画基本情報 API (`GET https://nvapi.nicovideo.jp/v1/videos?watchIds={id}`) のレスポンス DTO
 * スナップショット検索 API 未反映の最新投稿動画の 4大指標（再生数・コメント・いいね・マイリスト）をリアルタイムに補完する
 */
@Serializable
data class NicoVideoEssentialRootDto(
    val meta: NicoVideoMetaDto? = null,
    val data: NicoVideoEssentialDataDto? = null
)

@Serializable
data class NicoVideoMetaDto(
    val status: Int = 0,
    val errorCode: String? = null
)

@Serializable
data class NicoVideoEssentialDataDto(
    val items: List<NicoVideoEssentialItemDto> = emptyList()
)

@Serializable
data class NicoVideoEssentialItemDto(
    val watchId: String = "",
    val video: NicoVideoEssentialPayloadDto? = null
)

@Serializable
data class NicoVideoEssentialPayloadDto(
    val id: String = "",
    val title: String = "",
    val contentType: String? = null,
    val registeredAt: String = "",
    val duration: Long = 0L,
    val count: NicoVideoCountDto? = null,
    val thumbnail: NicoVideoThumbnailDto? = null,
    val owner: NicoVideoOwnerDto? = null
) {
    fun toVideoSummary(): VideoSummary {
        val thumb = thumbnail?.nHdUrl
            ?: thumbnail?.largeUrl
            ?: thumbnail?.middleUrl
            ?: thumbnail?.listingUrl
            ?: thumbnail?.url
            .orEmpty()
        return VideoSummary(
            contentId = id,
            title = title,
            viewCounter = count?.view ?: 0,
            commentCounter = count?.comment ?: 0,
            likeCounter = count?.like ?: 0,
            mylistCounter = count?.mylist ?: 0,
            lengthSeconds = duration.toInt(),
            thumbnailUrl = thumb,
            startTime = registeredAt,
            ownerName = owner?.name.orEmpty(),
            ownerId = owner?.id.orEmpty(),
            contentType = contentType ?: "long"
        )
    }
}

@Serializable
data class NicoVideoCountDto(
    val view: Int = 0,
    val comment: Int = 0,
    val mylist: Int = 0,
    val like: Int = 0
)

@Serializable
data class NicoVideoThumbnailDto(
    val url: String? = null,
    val middleUrl: String? = null,
    val largeUrl: String? = null,
    val listingUrl: String? = null,
    val nHdUrl: String? = null,
    val shortUrl: String? = null
)

@Serializable
data class NicoVideoOwnerDto(
    val id: String = "",
    val name: String = "",
    val iconUrl: String? = null
)
