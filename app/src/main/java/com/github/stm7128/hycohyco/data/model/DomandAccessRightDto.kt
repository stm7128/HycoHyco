package com.github.stm7128.hycohyco.data.model

import kotlinx.serialization.Serializable

/**
 * Domand access-rights/hls API のリクエストボディ DTO
 *
 * @property outputs 映像トラックIDと音声トラックIDのペア配列 (例: [["video-h264-720p", "audio-aac-192kbps"]])
 */
@Serializable
data class DomandAccessRightRequestDto(
    val outputs: List<List<String>>
)

/**
 * Domand access-rights/hls API のレスポンス DTO
 */
@Serializable
data class DomandAccessRightResponseDto(
    val meta: DomandMetaDto? = null,
    val data: DomandContentDataDto? = null
)

@Serializable
data class DomandMetaDto(
    val status: Int = 0
)

@Serializable
data class DomandContentDataDto(
    val contentUrl: String = ""
)
