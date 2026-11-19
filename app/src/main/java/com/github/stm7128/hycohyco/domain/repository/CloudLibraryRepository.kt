package com.github.stm7128.hycohyco.domain.repository

import com.github.stm7128.hycohyco.domain.model.CloudHistoryPage
import com.github.stm7128.hycohyco.domain.model.CloudLikeHistoryItem
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.CloudWatchHistoryItem
import com.github.stm7128.hycohyco.domain.model.NicoruHistoryItem
import com.github.stm7128.hycohyco.domain.model.OwnerProfile
import com.github.stm7128.hycohyco.domain.model.UserSeries
import com.github.stm7128.hycohyco.domain.model.VideoSummary

/**
 * ニコニコ公式クラウドライブラリ（あとで見る、公式マイリスト、フォロー新着、投稿者動画）リポジトリ
 */
interface CloudLibraryRepository {

    /**
     * 公式「あとで見る」の動画一覧を取得する
     */
    suspend fun getWatchLaterVideos(page: Int = 1): Result<List<VideoSummary>>

    /**
     * 動画を公式「あとで見る」に追加する
     */
    suspend fun addWatchLater(videoId: String): Result<Unit>

    /**
     * 動画を公式「あとで見る」から削除する
     */
    suspend fun deleteWatchLaterItem(itemId: String): Result<Unit>

    /**
     * ログインユーザーの公式マイリスト一覧を取得する
     */
    suspend fun getMylists(): Result<List<CloudMylist>>

    /**
     * 動画を公式マイリストに追加する
     */
    suspend fun addMylistItem(mylistId: Long, videoId: String): Result<Unit>

    /**
     * 動画を公式マイリストから削除する
     */
    suspend fun deleteMylistItem(mylistId: Long, itemId: String): Result<Unit>

    /**
     * 指定された公式マイリストの動画一覧を取得する
     */
    suspend fun getMylistVideos(mylistId: Long, page: Int = 1): Result<List<VideoSummary>>

    /**
     * 指定された公式マイリストのメタ情報（タイトル等）および動画一覧を取得する
     */
    suspend fun getMylistDetail(mylistId: Long): Result<Pair<CloudMylist, List<VideoSummary>>>

    /**
     * フォロー中投稿者の新着動画タイムラインを取得する
     */
    suspend fun getFollowFeed(): Result<List<VideoSummary>>

    /**
     * 指定された投稿者の公開動画一覧を取得する (デフォルト: 投稿日時が新しい順)
     */
    suspend fun getOwnerVideos(
        userId: Long,
        page: Int = 1,
        sortKey: String = "registeredAt",
        sortOrder: String = "desc"
    ): Result<List<VideoSummary>>

    /**
     * 投稿者のプロフィールを取得する
     */
    suspend fun getOwnerProfile(userId: Long): Result<OwnerProfile>

    /**
     * 指定された投稿者のシリーズ一覧を取得する
     */
    suspend fun getOwnerSeries(userId: Long): Result<List<UserSeries>>

    /**
     * 指定されたシリーズに属する動画一覧を取得する
     */
    suspend fun getSeriesVideos(seriesId: Long): Result<List<VideoSummary>>

    /**
     * 指定されたシリーズのメタ情報および動画一覧を取得する
     */
    suspend fun getSeriesDetail(seriesId: Long): Result<Pair<UserSeries, List<VideoSummary>>>

    /**
     * 指定された投稿者の公開マイリスト一覧を取得する
     */
    suspend fun getOwnerPublicMylists(userId: Long): Result<List<CloudMylist>>

    /**
     * 指定されたユーザーのフォロー中クリエイター一覧を取得する
     */
    suspend fun getFollowingUsers(userId: Long, page: Int = 1): Result<List<OwnerProfile>>

    /**
     * 指定されたユーザーのフォロワー一覧を取得する
     */
    suspend fun getFollowers(userId: Long, page: Int = 1): Result<List<OwnerProfile>>

    /**
     * ユーザーIDまたはキーワードでユーザーを検索・解決する
     */
    suspend fun searchUsers(keyword: String): Result<List<OwnerProfile>>

    /** クラウド視聴履歴を取得する (カーソルページネーション対応) */
    suspend fun getCloudWatchHistory(cursor: String? = null): Result<CloudHistoryPage<CloudWatchHistoryItem>>

    /** クラウド視聴履歴を削除する (videoId = null 時は全件削除) */
    suspend fun deleteCloudWatchHistory(videoId: String? = null): Result<Unit>

    /** いいね！履歴を取得する */
    suspend fun getCloudLikeHistory(page: Int = 1): Result<CloudHistoryPage<CloudLikeHistoryItem>>

    /** いいね！を解除する */
    suspend fun unlikeCloudVideo(videoId: String): Result<Unit>

    /** ニコられた履歴を取得する */
    suspend fun getNicoruReceiveHistory(cursor: String? = null, byVideoOwner: Boolean = false): Result<CloudHistoryPage<NicoruHistoryItem>>

    /** ニコられた合計回数を取得する */
    suspend fun getNicoruReceiveCount(): Result<Int>

    /** ニコった履歴を取得する */
    suspend fun getNicoruSendHistory(cursor: String? = null): Result<CloudHistoryPage<NicoruHistoryItem>>

    /** ニコった（ニコる）を取り消し・削除する */
    suspend fun deleteNicoruSendItem(nicoruId: String): Result<Unit>
}
