package com.github.stm7128.hycohyco.domain.repository

import com.github.stm7128.hycohyco.domain.model.QualityPreference

import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.HomeRecommendFeed
import com.github.stm7128.hycohyco.domain.model.RankingGenre
import com.github.stm7128.hycohyco.domain.model.SearchSort
import com.github.stm7128.hycohyco.domain.model.SearchTarget
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.model.VideoStreamSession
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.VideoTag

/**
 * ニコニコ動画のメタデータおよび再生セッション取得を司るリポジトリ
 */
interface VideoRepository {

    /**
     * 動画のメタデータを取得する
     *
     * @param videoId 動画ID (例: "sm9")
     * @return 成功時は [VideoDetail]、失敗時は [Result.failure]
     */
    suspend fun getVideoDetail(videoId: String): Result<VideoDetail>

    /**
     * 動画の HLS 再生セッション (Domand HLS) を取得する
     *
     * @param videoId 動画ID
     * @param preferredQualityId 希望する画質ID (null の場合は最高画質を自動選択)
     * @return 成功時は [VideoStreamSession]、失敗時は [Result.failure]
     */
    suspend fun getStreamSession(
        videoId: String,
        preferredQualityId: String? = null,
        qualityPreference: QualityPreference = QualityPreference.AUTO
    ): Result<VideoStreamSession>

    /**
     * 動画のコメント一覧を取得する
     *
     * @param videoId 動画ID
     * @param whenTimestampSec 過去ログ取得用 UNIX タイムスタンプ秒 (null の場合は最新)
     * @return 成功時は [CommentItem] のリスト (vposMs 昇順)、失敗時は [Result.failure]
     */
    suspend fun getVideoComments(
        videoId: String,
        whenTimestampSec: Long? = null
    ): Result<List<CommentItem>>

    /**
     * スナップショット検索 API を用いて動画を検索する
     *
     * @param query 検索クエリ
     * @param target 検索ターゲット (キーワード / タグ完全一致)
     * @param sort ソート順
     * @param offset 取得開始オフセット
     * @param limit 取得件数
     * @return 成功時は [VideoSummary] のリスト、失敗時は [Result.failure]
     */
    suspend fun searchVideos(
        query: String,
        target: SearchTarget = SearchTarget.KEYWORD,
        sort: SearchSort = SearchSort.VIEW_COUNT,
        offset: Int = 0,
        limit: Int = 25
    ): Result<List<VideoSummary>>

    /**
     * 指定されたジャンルの人気ランキング動画一覧を取得する
     *
     * @param genre ランキングジャンル
     * @param limit 取得件数
     * @return 成功時は [VideoSummary] のリスト、失敗時は [Result.failure]
     */
    suspend fun getGenreRanking(
        genre: RankingGenre,
        term: com.github.stm7128.hycohyco.domain.model.RankingTerm = com.github.stm7128.hycohyco.domain.model.RankingTerm.WEEKLY,
        limit: Int = 25
    ): Result<List<VideoSummary>>

    /**
     * 指定された動画に関連するレコメンド動画一覧を取得する
     *
     * @param videoId 対象動画ID
     * @return 成功時は [VideoSummary] のリスト、失敗時は [Result.failure]
     */
    suspend fun getRecommendVideos(videoId: String): Result<List<VideoSummary>>

    /**
     * スナップショット検索 API を用いて複数の動画メタデータ（4大指標等）を一括取得する
     *
     * @param videoIds 対象動画IDリスト
     * @return 成功時は [VideoSummary] のリスト、失敗時は [Result.failure]
     */
    suspend fun getVideoSummaries(videoIds: List<String>): Result<List<VideoSummary>>

    /**
     * おすすめニコニコショート動画フィードを取得する
     */
    suspend fun getRecommendedShorts(limit: Int = 20): Result<List<VideoSummary>>

    /**
     * 指定ショート動画を起点とした連続視聴用キューを取得する
     */
    suspend fun getShortsPlaylist(seedVideoId: String): Result<List<VideoSummary>>

    /**
     * ニコニコショート動画を検索する (selectContentType=short)
     *
     * @param isTagSearch true の場合は keyword ではなく tag パラメータでタグ検索を行う
     */
    suspend fun searchShorts(
        query: String,
        sort: SearchSort = SearchSort.VIEW_COUNT,
        page: Int = 1,
        pageSize: Int = 20,
        isTagSearch: Boolean = false
    ): Result<List<VideoSummary>>

    /**
     * 指定動画に新規タグを追加する（ログイン・編集可能動画かつ tagEditKey 必須）
     */
    suspend fun addVideoTag(
        videoId: String,
        tag: String,
        tagEditKey: String? = null
    ): Result<List<VideoTag>>

    /**
     * 指定動画から一般タグ（非ロック）を削除する
     */
    suspend fun deleteVideoTag(
        videoId: String,
        tag: String,
        tagEditKey: String? = null
    ): Result<List<VideoTag>>

    /**
     * ニコニコ大百科 API を用いてタグ一覧に記事存在状態（「百」バッジ用）・URL・要約を補完する
     */
    suspend fun enrichTagsWithNicodic(tags: List<VideoTag>): List<VideoTag> = tags

    /**
     * ホーム画面「おすすめ」タブ用のパーソナライズ/人気レコメンドフィードを取得する
     */
    suspend fun getHomeRecommendFeed(limit: Int = 50): Result<HomeRecommendFeed>
}
