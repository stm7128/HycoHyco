package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.api.NicoCommentApiClient
import com.github.stm7128.hycohyco.data.api.NicoDicApiClient
import com.github.stm7128.hycohyco.data.api.NicoRecommendApiClient
import com.github.stm7128.hycohyco.data.api.NicoSearchApiClient
import com.github.stm7128.hycohyco.data.api.NicoShortsApiClient
import com.github.stm7128.hycohyco.data.api.NicoTagApiClient
import com.github.stm7128.hycohyco.data.api.NicoVideoApiClient
import com.github.stm7128.hycohyco.data.api.NicoWatchApiClient
import com.github.stm7128.hycohyco.data.api.toDomainComments
import com.github.stm7128.hycohyco.data.local.AuthPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.*
import com.github.stm7128.hycohyco.domain.repository.VideoRepository
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * [VideoRepository] の実装クラス
 */
class VideoRepositoryImpl(
    private val apiClient: NicoWatchApiClient,
    private val commentApiClient: NicoCommentApiClient = NicoCommentApiClient(),
    private val searchApiClient: NicoSearchApiClient = NicoSearchApiClient(),
    private val authPreferencesDataSource: AuthPreferencesDataSource? = null,
    private val recommendApiClient: NicoRecommendApiClient = NicoRecommendApiClient(),
    private val shortsApiClient: NicoShortsApiClient = NicoShortsApiClient(),
    private val tagApiClient: NicoTagApiClient = NicoTagApiClient(),
    private val dicApiClient: NicoDicApiClient = NicoDicApiClient(),
    private val videoApiClient: NicoVideoApiClient = NicoVideoApiClient(),
    private val watchDataCache: WatchDataCache = WatchDataCache(apiClient),
) : VideoRepository {
    private val tagMutationHelper by lazy {
        VideoTagMutationHelper(tagApiClient = tagApiClient, watchDataCache = watchDataCache)
    }

    private suspend fun getUserSessionCookie(): String {
        return authPreferencesDataSource?.userSession?.first()?.formattedCookieHeader.orEmpty()
    }

    override suspend fun getVideoDetail(videoId: String): Result<VideoDetail> = suspendRunCatching {
        val sessionCookie = getUserSessionCookie()
        val (rootDto, _) = watchDataCache.getWatchData(videoId, userSessionCookie = sessionCookie)
        VideoDetailMapper.mapToVideoDetail(rootDto)
    }

    override suspend fun getStreamSession(
        videoId: String,
        preferredQualityId: String?,
        qualityPreference: QualityPreference,
    ): Result<VideoStreamSession> = suspendRunCatching {
        val sessionCookie = getUserSessionCookie()
        val (rootDto, nicosid) = watchDataCache.getWatchData(videoId, userSessionCookie = sessionCookie)
        val payload = rootDto.data
            ?: throw IllegalStateException("動画メタデータの取得に失敗しました")

        val clientDto = payload.client
            ?: throw IllegalStateException("client.watchTrackId が見つかりません")
        val domandDto = payload.media?.domand
            ?: throw IllegalStateException("media.domand 情報が見つかりません")

        val availableVideos = domandDto.videos.filter { it.isAvailable }
        if (availableVideos.isEmpty()) {
            throw IllegalStateException("再生可能な映像トラックがありません")
        }

        val availableAudios = domandDto.audios.filter { it.isAvailable }
        if (availableAudios.isEmpty()) {
            throw IllegalStateException("再生可能な音声トラックがありません")
        }

        // 音声は最高ビットレートのトラックを選択
        val selectedAudio = availableAudios.maxByOrNull { it.bitRate } ?: availableAudios.first()

        // 映像トラックの決定 & 画質固定フラグ判定
        val (selectedVideo, isQualityFixed) = if (preferredQualityId != null) {
            val matched = availableVideos.find { it.id == preferredQualityId } ?: availableVideos.first()
            matched to true
        } else {
            val targetHeight = qualityPreference.targetHeight
            if (targetHeight == null) {
                // AUTO: 自動ABRに委ねる
                (availableVideos.maxByOrNull { it.height } ?: availableVideos.first()) to false
            } else {
                val matched = availableVideos.filter { it.height <= targetHeight }.maxByOrNull { it.height }
                    ?: availableVideos.minByOrNull { it.height }
                    ?: availableVideos.first()
                matched to true
            }
        }

        // 画質固定時は選択画質ペアのみ要求し、AUTO時は全直積ペアを要求してExoPlayerのABRを動作させる
        val outputs = if (isQualityFixed) {
            listOf(listOf(selectedVideo.id, selectedAudio.id))
        } else {
            availableVideos.flatMap { video ->
                availableAudios.map { audio ->
                    listOf(video.id, audio.id)
                }
            }
        }

        val (contentUrl, domandBidCookie) = apiClient.fetchDomandHlsAccessRight(
            videoId = videoId,
            watchTrackId = clientDto.watchTrackId,
            accessRightKey = domandDto.accessRightKey,
            outputs = outputs,
            nicosid = nicosid,
            userSessionCookie = sessionCookie,
        )

        VideoStreamSession(
            videoId = videoId,
            masterPlaylistUrl = contentUrl,
            domandBidCookie = domandBidCookie,
            selectedVideoQualityId = selectedVideo.id,
            selectedAudioQualityId = selectedAudio.id,
        )
    }

    override suspend fun getVideoComments(
        videoId: String,
        whenTimestampSec: Long?
    ): Result<List<CommentItem>> = suspendRunCatching {
        val sessionCookie = getUserSessionCookie()
        val (rootDto, _) = watchDataCache.getWatchData(videoId, userSessionCookie = sessionCookie)
        val payload = rootDto.data
            ?: throw IllegalStateException("動画メタデータの取得に失敗しました")
        val nvComment = payload.comment?.nvComment
            ?: throw IllegalStateException("nvComment 情報が含まれていません")
        val params = nvComment.params
            ?: throw IllegalStateException("nvComment.params が含まれていません")

        val response = commentApiClient.fetchThreads(
            server = nvComment.server,
            threadKey = nvComment.threadKey,
            params = params,
            userSessionCookie = sessionCookie,
            whenTimestampSec = whenTimestampSec
        )

        response.toDomainComments()
    }

    override suspend fun searchVideos(
        query: String,
        target: SearchTarget,
        sort: SearchSort,
        offset: Int,
        limit: Int
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val response = searchApiClient.searchVideos(
            query = query,
            target = target,
            sort = sort,
            offset = offset,
            limit = limit
        )
        response.data.map { it.toDomain() }
    }

    override suspend fun getGenreRanking(
        genre: RankingGenre,
        term: RankingTerm,
        limit: Int
    ): Result<List<VideoSummary>> = suspendRunCatching {
        val startTimeGte = term.daysAgo?.let { days ->
            ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
                .minusDays(days.toLong())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
        val query = if (genre == RankingGenre.ALL && term.daysAgo != null) "" else genre.genreTag
        val target = if (genre == RankingGenre.ALL && term.daysAgo != null) SearchTarget.KEYWORD else SearchTarget.TAG_EXACT
        val response = searchApiClient.searchVideos(
            query = query, target = target, sort = SearchSort.VIEW_COUNT, offset = 0, limit = limit, startTimeGte = startTimeGte
        )
        response.data.map { it.toDomain() }
    }

    override suspend fun getRecommendVideos(videoId: String): Result<List<VideoSummary>> =
        recommendApiClient.fetchRecommendVideos(videoId)

    override suspend fun getVideoSummaries(videoIds: List<String>): Result<List<VideoSummary>> = suspendRunCatching {
        val snapshotList = searchApiClient.fetchVideoSummariesByContentIds(videoIds)
        val foundIds = snapshotList.map { it.contentId }.toSet()
        val missingIds = videoIds.filter { it !in foundIds }
        if (missingIds.isEmpty()) {
            snapshotList
        } else {
            // スナップショット検索 API 未反映の最新動画を nvapi v1/videos で並列取得して補完
            coroutineScope {
                val fallbackList = missingIds.map { id ->
                    async { videoApiClient.fetchVideoEssential(id) }
                }.awaitAll().filterNotNull()
                snapshotList + fallbackList
            }
        }
    }

    override suspend fun getRecommendedShorts(limit: Int): Result<List<VideoSummary>> {
        return suspendRunCatching {
            val sessionCookie = getUserSessionCookie()
            shortsApiClient.fetchRecommendedShorts(userSessionCookie = sessionCookie, limit = limit).getOrThrow()
        }
    }

    override suspend fun getShortsPlaylist(seedVideoId: String): Result<List<VideoSummary>> {
        return suspendRunCatching {
            val sessionCookie = getUserSessionCookie()
            shortsApiClient.fetchShortsPlaylist(seedVideoId = seedVideoId, userSessionCookie = sessionCookie).getOrThrow()
        }
    }

    override suspend fun searchShorts(
        query: String,
        sort: SearchSort,
        page: Int,
        pageSize: Int,
        isTagSearch: Boolean
    ): Result<List<VideoSummary>> {
        return suspendRunCatching {
            val sessionCookie = getUserSessionCookie()
            shortsApiClient.searchShorts(
                query = query,
                sort = sort,
                page = page,
                pageSize = pageSize,
                userSessionCookie = sessionCookie,
                isTagSearch = isTagSearch
            ).getOrThrow()
        }
    }

    override suspend fun addVideoTag(videoId: String, tag: String, tagEditKey: String?): Result<List<VideoTag>> =
        suspendRunCatching {
            tagMutationHelper.mutateTag(videoId, tag, tagEditKey, getUserSessionCookie(), isDelete = false)
        }

    override suspend fun deleteVideoTag(videoId: String, tag: String, tagEditKey: String?): Result<List<VideoTag>> =
        suspendRunCatching {
            tagMutationHelper.mutateTag(videoId, tag, tagEditKey, getUserSessionCookie(), isDelete = true)
        }

    override suspend fun enrichTagsWithNicodic(tags: List<VideoTag>): List<VideoTag> =
        dicApiClient.resolveTags(tags)

    override suspend fun getHomeRecommendFeed(limit: Int): Result<HomeRecommendFeed> =
        recommendApiClient.fetchHomeRecommendFeed(userSessionCookie = getUserSessionCookie(), limit = limit)
}
