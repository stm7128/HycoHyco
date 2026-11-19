package com.github.stm7128.hycohyco.data.model

import com.github.stm7128.hycohyco.domain.model.VideoSummary
import kotlinx.serialization.Serializable

// ==========================================
// 1. 公式「あとで見る」API DTO
// ==========================================
@Serializable
data class WatchLaterRootDto(
    val meta: CloudMetaDto? = null,
    val data: WatchLaterDataDto? = null
)

@Serializable
data class WatchLaterDataDto(
    val watchLater: WatchLaterPayloadDto? = null
)

@Serializable
data class WatchLaterPayloadDto(
    val totalCount: Int = 0,
    val hasNext: Boolean = false,
    val items: List<WatchLaterItemDto> = emptyList()
)

@Serializable
data class WatchLaterItemDto(
    val itemId: Long = 0L,
    val addedAt: String = "",
    val video: CloudVideoItemDto? = null
)

// ==========================================
// 2. 公式「マイリスト」API DTO
// ==========================================
@Serializable
data class MylistRootDto(
    val meta: CloudMetaDto? = null,
    val data: MylistDataDto? = null
)

@Serializable
data class MylistDataDto(
    val mylists: List<MylistSummaryDto> = emptyList()
)

@Serializable
data class MylistSummaryDto(
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = false,
    val itemsCount: Int = 0
)

@Serializable
data class MylistDetailRootDto(
    val meta: CloudMetaDto? = null,
    val data: MylistDetailDataDto? = null
)

@Serializable
data class MylistDetailDataDto(
    val mylist: MylistDetailPayloadDto? = null
)

@Serializable
data class MylistDetailPayloadDto(
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    val totalCount: Int = 0,
    val hasNext: Boolean = false,
    val items: List<WatchLaterItemDto> = emptyList()
)

// ==========================================
// 3. 共通動画アイテム & メタ DTO
// ==========================================
@Serializable
data class CloudMetaDto(
    val status: Int = 200,
    val errorCode: String = ""
)

@Serializable
data class CloudVideoOwnerDto(
    val id: String? = null,
    val name: String? = null,
    val iconUrl: String? = null,
    val ownerType: String? = null
)

@Serializable
data class CloudVideoItemDto(
    val id: String = "",
    val title: String = "",
    val registeredAt: String = "",
    val duration: Long = 0L,
    val thumbnail: WatchThumbnailDto? = null,
    val count: WatchCountDto? = null,
    val owner: CloudVideoOwnerDto? = null,
    val playbackPosition: Double? = null,
    val contentType: String? = null
)

// ==========================================
// 4. ユーザー投稿動画 & 投稿者プロフィール DTO
// ==========================================
@Serializable
data class UserVideosRootDto(
    val meta: CloudMetaDto? = null,
    val data: UserVideosDataDto? = null
)

@Serializable
data class UserVideosDataDto(
    val totalCount: Int = 0,
    val items: List<UserVideoItemWrapperDto> = emptyList()
)

@Serializable
data class UserVideoItemWrapperDto(
    val essential: CloudVideoItemDto? = null
)

@Serializable
data class UserDetailRootDto(
    val meta: CloudMetaDto? = null,
    val data: UserDetailDataDto? = null
)

@Serializable
data class UserDetailDataDto(
    val user: UserDetailPayloadDto? = null
)

@Serializable
data class UserDetailPayloadDto(
    val id: Long = 0L,
    val nickname: String = "",
    val description: String = "",
    val icons: UsersMeIconsDto? = null,
    val followerCount: Long = 0L
)

// ==========================================
// 5. フォロー新着タイムライン DTO (新フォロー新着 API 準拠)
// ==========================================
@Serializable
data class FollowActivitiesRootDto(
    val code: String = "",
    val activities: List<FollowActivityDto> = emptyList(),
    val nextCursor: String? = null
)

@Serializable
data class FollowActivityDto(
    val id: String = "",
    val kind: String = "",
    val createdAt: String = "",
    val thumbnailUrl: String? = null,
    val content: FollowActivityContentDto? = null,
    val actor: FollowActivityActorDto? = null
)

@Serializable
data class FollowActivityContentDto(
    val type: String = "",
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val startedAt: String? = null,
    val video: FollowActivityVideoMetaDto? = null
)

@Serializable
data class FollowActivityVideoMetaDto(
    val duration: Int = 0
)

@Serializable
data class FollowActivityActorDto(
    val id: String = "",
    val name: String = "",
    val iconUrl: String? = null,
    val url: String = ""
)

// ==========================================
// 6. ユーザーシリーズ API DTO
// ==========================================
@Serializable
data class UserSeriesRootDto(
    val meta: CloudMetaDto? = null,
    val data: UserSeriesDataDto? = null
)

@Serializable
data class UserSeriesDataDto(
    val items: List<UserSeriesItemDto> = emptyList(),
    val totalCount: Int = 0
)

@Serializable
data class UserSeriesItemDto(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val itemsCount: Int = 0
)

// ==========================================
// 7. フォロー中ユーザー API DTO
// ==========================================
@Serializable
data class FollowingUsersRootDto(
    val meta: CloudMetaDto? = null,
    val data: FollowingUsersDataDto? = null
)

@Serializable
data class FollowingUsersDataDto(
    val items: List<FollowingUserItemDto> = emptyList(),
    val users: List<FollowingUserItemDto> = emptyList(),
    val totalCount: Int = 0
)

// ==========================================
// 8. シリーズ内動画詳細 API DTO
// ==========================================
@Serializable
data class SeriesDetailRootDto(
    val meta: CloudMetaDto? = null,
    val data: SeriesDetailDataDto? = null
)

@Serializable
data class SeriesDetailDataDto(
    val detail: UserSeriesItemDto? = null,
    val items: List<SeriesVideoItemWrapperDto> = emptyList(),
    val totalCount: Int = 0
)

@Serializable
data class SeriesVideoItemWrapperDto(
    val video: CloudVideoItemDto? = null
)

@Serializable
data class FollowingUserItemDto(
    val id: Long = 0L,
    val nickname: String = "",
    val iconUrl: String = "",
    val icons: UsersMeIconsDto? = null,
    val description: String = "",
    val strippedDescription: String = "",
    val followerCount: Long = 0L
)

fun CloudVideoItemDto.toVideoSummary(): VideoSummary {
    val thumb = thumbnail?.largeUrl?.ifBlank { thumbnail.middleUrl ?: thumbnail.url }
        ?: thumbnail?.url.orEmpty()
    return VideoSummary(
        contentId = id,
        title = title,
        viewCounter = count?.view?.toInt() ?: 0,
        commentCounter = count?.comment?.toInt() ?: 0,
        likeCounter = count?.like?.toInt() ?: 0,
        mylistCounter = count?.mylist?.toInt() ?: 0,
        lengthSeconds = duration.toInt(),
        thumbnailUrl = thumb,
        startTime = registeredAt,
        ownerName = owner?.name.orEmpty(),
        ownerId = owner?.id.orEmpty()
    )
}
