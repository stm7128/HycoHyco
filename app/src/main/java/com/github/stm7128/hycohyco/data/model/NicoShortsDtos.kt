package com.github.stm7128.hycohyco.data.model

import kotlinx.serialization.Serializable

/**
 * ショート連続視聴プレイリスト API (/v1/playlist/recipe-id) のルート DTO
 */
@Serializable
data class ShortsPlaylistRootDto(
    val meta: CloudMetaDto? = null,
    val data: ShortsPlaylistDataDto? = null
)

@Serializable
data class ShortsPlaylistDataDto(
    val totalCount: Int = 0,
    val items: List<ShortsPlaylistItemDto> = emptyList()
)

@Serializable
data class ShortsPlaylistItemDto(
    val watchId: String = "",
    val content: RecommendVideoContentDto? = null
)

/**
 * ショート動画検索 API (/v2/search/video?selectContentType=short) のルート DTO
 */
@Serializable
data class ShortsSearchRootDto(
    val meta: CloudMetaDto? = null,
    val data: ShortsSearchDataDto? = null
)

@Serializable
data class ShortsSearchDataDto(
    val searchId: String = "",
    val totalCount: Int = 0,
    val hasNext: Boolean = false,
    val items: List<RecommendVideoContentDto> = emptyList()
)
