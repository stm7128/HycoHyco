package com.github.stm7128.hycohyco.data.model

import com.github.stm7128.hycohyco.domain.model.CloudLikeHistoryItem
import com.github.stm7128.hycohyco.domain.model.CloudWatchHistoryItem
import com.github.stm7128.hycohyco.domain.model.NicoruHistoryItem
import kotlinx.serialization.Serializable

// ==========================================
// 1. クラウド視聴履歴 API v2 DTO (/v2/users/me/watch/history)
// ==========================================
@Serializable
data class CloudWatchHistoryRootDto(
    val meta: CloudMetaDto? = null,
    val data: CloudWatchHistoryDataDto? = null
)

@Serializable
data class CloudWatchHistoryDataDto(
    val nextCursor: String? = null,
    val items: List<CloudWatchHistoryItemDto> = emptyList()
)

@Serializable
data class CloudWatchHistoryItemDto(
    val itemId: String = "",
    val viewedAt: String = "",
    val playbackPosition: Double? = null,
    val video: CloudVideoItemDto? = null
)

fun CloudWatchHistoryItemDto.toDomain(): CloudWatchHistoryItem? {
    val videoDto = video ?: return null
    if (videoDto.id.isBlank()) return null
    val posSec = (playbackPosition ?: videoDto.playbackPosition ?: 0.0).toLong().coerceAtLeast(0L)
    return CloudWatchHistoryItem(
        itemId = itemId.ifBlank { "${videoDto.id}_$viewedAt" },
        viewedAt = viewedAt,
        playbackPositionSec = posSec,
        video = videoDto.toVideoSummary()
    )
}

// ==========================================
// 2. いいね！履歴 API DTO (/v1/users/me/likes)
// ==========================================
@Serializable
data class CloudLikeHistoryRootDto(
    val meta: CloudMetaDto? = null,
    val data: CloudLikeHistoryDataDto? = null
)

@Serializable
data class CloudLikeHistoryDataDto(
    val items: List<CloudLikeHistoryItemDto> = emptyList(),
    val summary: CloudLikeSummaryDto? = null
)

@Serializable
data class CloudLikeSummaryDto(
    val hasNext: Boolean = false,
    val canGetNextPage: Boolean = false
)

@Serializable
data class CloudLikeHistoryItemDto(
    val likedAt: String = "",
    val thanksMessage: String? = null,
    val video: CloudVideoItemDto? = null
)

fun CloudLikeHistoryItemDto.toDomain(): CloudLikeHistoryItem? {
    val videoDto = video ?: return null
    if (videoDto.id.isBlank()) return null
    return CloudLikeHistoryItem(
        likedAt = likedAt,
        thanksMessage = thanksMessage?.takeIf { it.isNotBlank() },
        video = videoDto.toVideoSummary()
    )
}

// ==========================================
// 3. ニコられた / ニコった 履歴 API DTO
//    (/v1/users/me/nicoru/receive, /v1/users/me/nicoru/send)
// ==========================================
@Serializable
data class NicoruHistoryRootDto(
    val meta: CloudMetaDto? = null,
    val data: NicoruHistoryDataDto? = null
)

@Serializable
data class NicoruHistoryDataDto(
    val next: String? = null,
    val items: List<NicoruHistoryItemDto> = emptyList()
)

@Serializable
data class NicoruHistoryItemDto(
    val id: String = "",
    val count: Int = 0,
    val totalCount: Int = 0,
    val createdAt: String? = null,
    val commentCreatedAt: String? = null,
    val commentBody: String = "",
    val commentVpos: Long = 0L,
    val isVideoOwnerNicoru: Boolean = false,
    val isFeatured: Boolean = false,
    val video: CloudVideoItemDto? = null
)

fun NicoruHistoryItemDto.toDomain(): NicoruHistoryItem? {
    val videoDto = video ?: return null
    if (videoDto.id.isBlank()) return null
    // ニコる API の commentVpos は 10ms 単位 (Math.floor(commentVpos / 100) で秒換算される) のため * 10L でミリ秒化する
    val vposMs = (commentVpos * 10L).coerceAtLeast(0L)
    val resolvedCount = if (count > 0) count else totalCount
    return NicoruHistoryItem(
        id = id.ifBlank { "${videoDto.id}_${commentVpos}_${commentBody.hashCode()}" },
        commentBody = commentBody,
        commentVposMs = vposMs,
        nicoruCount = resolvedCount,
        createdAt = createdAt.orEmpty(),
        commentCreatedAt = commentCreatedAt.orEmpty(),
        isVideoOwnerNicoru = isVideoOwnerNicoru,
        isFeatured = isFeatured,
        video = videoDto.toVideoSummary()
    )
}

@Serializable
data class NicoruReceiveCountRootDto(
    val meta: CloudMetaDto? = null,
    val data: NicoruReceiveCountDataDto? = null
)

@Serializable
data class NicoruReceiveCountDataDto(
    val count: Int = 0
)
