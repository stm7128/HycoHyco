package com.github.stm7128.hycohyco.data.model

import kotlinx.serialization.Serializable

/**
 * ニコニコ watch v3_guest API のルートレスポンス DTO
 */
@Serializable
data class WatchResponseRootDto(
    val meta: WatchMetaDto? = null,
    val data: WatchDataDto? = null
)

@Serializable
data class WatchMetaDto(
    val status: Int = 200,
    val errorCode: String = ""
)

/**
 * watch v3_guest のメインデータ DTO
 */
@Serializable
data class WatchDataDto(
    val reasonCode: String? = null,
    val deletedMessage: String? = null,
    val video: WatchVideoDto? = null,
    val client: WatchClientDto? = null,
    val media: WatchMediaDto? = null,
    val owner: WatchOwnerDto? = null,
    val channel: WatchChannelDto? = null,
    val comment: WatchCommentRootDto? = null,
    val like: WatchLikeDto? = null,
    val series: WatchSeriesDto? = null,
    val baseVideo: WatchBaseVideoDto? = null,
    val tag: WatchTagRootDto? = null
)

@Serializable
data class WatchBaseVideoDto(
    val baseVideoId: String? = null,
    val contentType: String? = null,
    val title: String? = null,
    val thumbnail: WatchBaseThumbnailDto? = null
)

@Serializable
data class WatchBaseThumbnailDto(
    val url: String? = null,
    val middleUrl: String? = null,
    val largeUrl: String? = null,
    val short: String? = null
)

@Serializable
data class WatchLikeDto(
    val isLiked: Boolean = false,
    val count: Long? = null
)

@Serializable
data class WatchSeriesDto(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val video: WatchSeriesVideoNavDto? = null
)

@Serializable
data class WatchSeriesVideoNavDto(
    val prev: WatchSeriesItemDto? = null,
    val next: WatchSeriesItemDto? = null,
    val first: WatchSeriesItemDto? = null
)

@Serializable
data class WatchSeriesItemDto(
    val id: String = "",
    val title: String = "",
    val duration: Long = 0L,
    val thumbnailUrl: String = ""
)

@Serializable
data class WatchCommentRootDto(
    val threads: List<WatchCommentThreadDto> = emptyList(),
    val nvComment: WatchNvCommentDto? = null
)

@Serializable
data class WatchCommentThreadDto(
    val id: Long = 0L,
    val fork: Int = 0,
    val forkLabel: String = "",
    val isDefaultPostTarget: Boolean = false
)

@Serializable
data class WatchNvCommentDto(
    val threadKey: String = "",
    val server: String = "",
    val params: NvCommentParamsDto? = null
)

@Serializable
data class NvCommentParamsDto(
    val targets: List<NvCommentTargetDto> = emptyList(),
    val language: String = "ja-jp"
)

@Serializable
data class NvCommentTargetDto(
    val id: String = "",
    val fork: String = ""
)

@Serializable
data class WatchVideoDto(
    val id: String = "",
    val contentType: String? = "long",
    val title: String = "",
    val description: String = "",
    val duration: Long = 0L,
    val thumbnail: WatchThumbnailDto? = null,
    val count: WatchCountDto? = null,
    val registeredAt: String = "",
    val viewer: WatchVideoViewerDto? = null
)

@Serializable
data class WatchVideoViewerDto(
    val isOwner: Boolean = false,
    val like: WatchLikeDto? = null
)

@Serializable
data class WatchThumbnailDto(
    val url: String = "",
    val middleUrl: String? = null,
    val largeUrl: String? = null,
    val ogp: String? = null,
    val short: String? = null
)

@Serializable
data class WatchCountDto(
    val view: Long = 0L,
    val comment: Long = 0L,
    val mylist: Long = 0L,
    val like: Long = 0L
)

@Serializable
data class WatchClientDto(
    val watchTrackId: String = ""
)

@Serializable
data class WatchMediaDto(
    val domand: WatchDomandDto? = null
)

@Serializable
data class WatchDomandDto(
    val accessRightKey: String = "",
    val videos: List<DomandVideoTrackDto> = emptyList(),
    val audios: List<DomandAudioTrackDto> = emptyList()
)

@Serializable
data class DomandVideoTrackDto(
    val id: String = "",
    val isAvailable: Boolean = false,
    val label: String = "",
    val bitRate: Long = 0L,
    val width: Int = 0,
    val height: Int = 0
)

@Serializable
data class DomandAudioTrackDto(
    val id: String = "",
    val isAvailable: Boolean = false,
    val bitRate: Long = 0L,
    val samplingRate: Int = 0
)

@Serializable
data class WatchOwnerDto(
    val id: Long = 0L,
    val nickname: String = "",
    val iconUrl: String = "",
    val icons: UsersMeIconsDto? = null,
    val viewer: WatchOwnerViewerDto? = null
)

@Serializable
data class WatchOwnerViewerDto(
    val isFollowing: Boolean = false
)

@Serializable
data class WatchChannelDto(
    val id: String = "",
    val name: String = "",
    val iconUrl: String = "",
    val thumbnail: WatchChannelThumbnailDto? = null
)

@Serializable
data class WatchChannelThumbnailDto(
    val url: String = "",
    val smallUrl: String = ""
)

/**
 * ニコニコ watch API のエラー・理由コードを分かりやすい日本語メッセージに変換する
 */
fun formatWatchErrorMessage(
    errorCode: String?,
    reasonCode: String?,
    deletedMessage: String? = null
): String {
    if (!deletedMessage.isNullOrBlank()) {
        val cleanMsg = deletedMessage
            .replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
            .trim()
        return "この動画は削除されています。\n$cleanMsg"
    }

    return when (reasonCode) {
        "RIGHT_HOLDER_DELETE_VIDEO" -> "この動画は権利者の申し立てにより削除されました。"
        "ADMINISTRATOR_DELETE_VIDEO" -> "この動画は運営管理者によって削除されました。"
        "DELETED_VIDEO", "DELETED_POSTER_VIDEO" -> "この動画は投稿者によって削除されました。"
        "DELETED_CHANNEL_VIDEO" -> "チャンネルが閉鎖されたため、この動画は視聴できません。"
        "DELETED_COMMUNITY_VIDEO" -> "コミュニティが削除されたため、この動画は視聴できません。"
        "HIDDEN_VIDEO", "PRIVATE_VIDEO" -> "この動画は非公開に設定されています。"
        "CHANNEL_MEMBER_ONLY" -> "この動画はチャンネル会員限定です。"
        "COMMUNITY_ONLY" -> "この動画はコミュニティ限定です。"
        "PREMIUM_ONLY" -> "この動画はプレミアム会員限定です。"
        "PPV_VIDEO" -> "この動画は有料（PPV）動画です。"
        "HARMFUL_VIDEO" -> "この動画にはセンシティブまたは年齢制限が設定されています。"
        "DOMESTIC_VIDEO", "HIGH_RISK_COUNTRY_VIDEO" -> "この動画はお住まいの地域からは視聴できません（地域制限）。"
        "DEVICE_FILTERING" -> "この端末での再生が制限されている動画です。"
        "NOT_FOUND" -> "動画が見つかりませんでした (削除済みまたは存在しないID)。"
        else -> when (errorCode) {
            "FORBIDDEN" -> "再生アクセスが制限されています (非公開・会員限定または削除済み)。"
            "NOT_FOUND" -> "動画が見つかりませんでした (削除済みまたは存在しないID)。"
            else -> "動画を読み込めませんでした"
        }
    }
}
