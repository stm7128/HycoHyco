package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.model.WatchResponseRootDto
import com.github.stm7128.hycohyco.data.model.formatWatchErrorMessage
import com.github.stm7128.hycohyco.domain.model.BaseVideoInfo
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.model.VideoQualityOption
import com.github.stm7128.hycohyco.domain.model.VideoSeriesItem
import com.github.stm7128.hycohyco.domain.model.VideoSeriesNav

/**
 * Watch API のルート DTO からドメインモデル [VideoDetail] へのマッピングヘルパー
 */
object VideoDetailMapper {

    fun mapToVideoDetail(rootDto: WatchResponseRootDto): VideoDetail {
        val payload = rootDto.data
        val videoDto = payload?.video
        if (videoDto == null) {
            val errMsg = formatWatchErrorMessage(
                errorCode = rootDto.meta?.errorCode,
                reasonCode = payload?.reasonCode,
                deletedMessage = payload?.deletedMessage
            )
            throw IllegalStateException(errMsg)
        }

        val thumbnail = videoDto.thumbnail?.ogp
            ?: videoDto.thumbnail?.largeUrl
            ?: videoDto.thumbnail?.middleUrl
            ?: videoDto.thumbnail?.url
            ?: ""

        val qualities = payload.media?.domand?.videos?.map { track ->
            VideoQualityOption(
                id = track.id,
                label = track.label,
                bitRate = track.bitRate,
                width = track.width,
                height = track.height,
                isAvailable = track.isAvailable,
            )
        } ?: emptyList()

        val primaryTrack = qualities.firstOrNull { it.isAvailable } ?: qualities.firstOrNull()
        val videoWidth = primaryTrack?.width ?: 0
        val videoHeight = primaryTrack?.height ?: 0

        val baseVideo = payload.baseVideo?.let { base ->
            val baseId = base.baseVideoId?.takeIf { it.isNotBlank() } ?: return@let null
            val baseThumb = base.thumbnail?.largeUrl
                ?: base.thumbnail?.middleUrl
                ?: base.thumbnail?.url
                ?: base.thumbnail?.short
                ?: ""
            BaseVideoInfo(
                videoId = baseId,
                contentType = base.contentType ?: "long",
                title = base.title ?: "",
                thumbnailUrl = baseThumb
            )
        }

        val ownerName = payload.owner?.nickname
            ?: payload.channel?.name
            ?: "不明な投稿者"

        val ownerIcon = payload.owner?.iconUrl?.ifBlank {
            payload.owner.icons?.large?.ifBlank { payload.owner.icons.small }
        } ?: payload.channel?.iconUrl?.ifBlank {
            payload.channel.thumbnail?.url
        } ?: ""

        val isLiked = videoDto.viewer?.like?.isLiked ?: payload.like?.isLiked ?: false
        val isOwnerFollowed = payload.owner?.viewer?.isFollowing ?: false
        android.util.Log.d(
            "VideoDetailMapper",
            "Watch API like/follow取得: videoId=${videoDto.id}, isLiked=$isLiked, isOwnerFollowed=$isOwnerFollowed, count=${videoDto.count?.like}"
        )

        val defaultThreadId = payload.comment?.threads?.find { it.isDefaultPostTarget }?.id
            ?: payload.comment?.threads?.find { it.forkLabel == "main" }?.id
            ?: payload.comment?.nvComment?.params?.targets?.find { it.fork == "main" }?.id?.toLongOrNull()
        val nvCommentServer = payload.comment?.nvComment?.server?.ifBlank { null }

        val tagRoot = payload.tag
        val tags = tagRoot?.items.orEmpty().mapNotNull { it.toDomainOrNull() }

        return VideoDetail(
            id = videoDto.id,
            title = videoDto.title,
            description = videoDto.description,
            durationSeconds = videoDto.duration,
            thumbnailUrl = thumbnail,
            viewCount = videoDto.count?.view ?: 0L,
            commentCount = videoDto.count?.comment ?: 0L,
            mylistCount = videoDto.count?.mylist ?: 0L,
            likeCount = videoDto.count?.like ?: 0L,
            ownerName = ownerName,
            ownerId = payload.owner?.id ?: 0L,
            ownerIconUrl = ownerIcon,
            registeredAt = videoDto.registeredAt,
            availableQualities = qualities,
            isLiked = isLiked,
            isOwnerFollowed = isOwnerFollowed,
            defaultThreadId = defaultThreadId,
            nvCommentServer = nvCommentServer,
            series = payload.series?.let { s ->
                VideoSeriesNav(
                    id = s.id,
                    title = s.title,
                    prev = s.video?.prev?.let { VideoSeriesItem(it.id, it.title, it.duration, it.thumbnailUrl) },
                    next = s.video?.next?.let { VideoSeriesItem(it.id, it.title, it.duration, it.thumbnailUrl) }
                )
            },
            contentType = videoDto.contentType ?: "long",
            videoWidth = videoWidth,
            videoHeight = videoHeight,
            baseVideo = baseVideo,
            tags = tags,
            hasR18Tag = tagRoot?.hasR18Tag ?: false,
            isTagEditable = tagRoot?.edit?.isEditable ?: false,
            tagUneditableReason = tagRoot?.edit?.uneditableReason?.takeIf { it.isNotBlank() },
            tagEditKey = tagRoot?.edit?.editKey?.takeIf { it.isNotBlank() }
        )
    }
}
