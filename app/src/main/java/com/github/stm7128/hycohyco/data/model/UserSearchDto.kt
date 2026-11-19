package com.github.stm7128.hycohyco.data.model

import kotlinx.serialization.Serializable

/**
 * ニコニコ公式 ユーザーキーワード検索 (api.cas.nicovideo.jp) レスポンス DTO
 */
@Serializable
data class CasUserSearchRootDto(
    val meta: CasMetaDto? = null,
    val data: CasUserDataDto? = null
)

@Serializable
data class CasMetaDto(
    val status: Int? = null,
    val totalCount: Int? = null
)

@Serializable
data class CasUserDataDto(
    val users: List<CasUserItemDto> = emptyList()
)

@Serializable
data class CasUserItemDto(
    val id: String = "",
    val nickname: String = "",
    val icons: CasUserIconsDto? = null,
    val description: String = "",
    val followerCount: Int = 0,
    val videoCount: Int = 0
)

@Serializable
data class CasUserIconsDto(
    val urls: Map<String, String> = emptyMap()
)
