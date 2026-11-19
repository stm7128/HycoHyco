package com.github.stm7128.hycohyco.data.model

import kotlinx.serialization.Serializable

/**
 * nvapi.nicovideo.jp/v1/users/me のレスポンス DTO
 */
@Serializable
data class UsersMeResponseDto(
    val meta: UsersMeMetaDto? = null,
    val data: UsersMeDataDto? = null
)

@Serializable
data class UsersMeMetaDto(
    val status: Int = 200,
    val errorCode: String = ""
)

@Serializable
data class UsersMeDataDto(
    val user: UsersMeUserDto? = null,
    val userId: Long = 0L,
    val nickname: String = "",
    val description: String = "",
    val isPremium: Boolean = false,
    val icons: UsersMeIconsDto? = null
)

@Serializable
data class UsersMeUserDto(
    val id: Long = 0L,
    val nickname: String = "",
    val description: String = "",
    val isPremium: Boolean = false,
    val icons: UsersMeIconsDto? = null
)

@Serializable
data class UsersMeIconsDto(
    val small: String = "",
    val large: String = ""
)
