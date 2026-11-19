package com.github.stm7128.hycohyco.domain.repository

import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.LikeResult

/**
 * ニコニコ双方向インタラクション (いいね！・コメント等) リポジトリ
 */
interface InteractionRepository {
    /**
     * 指定した動画に「いいね！」を送信する
     *
     * @param videoId 動画ID
     * @return 送信結果（お礼メッセージ含む）
     */
    suspend fun likeVideo(videoId: String): Result<LikeResult>

    /**
     * 指定した動画の「いいね！」を解除する
     *
     * @param videoId 動画ID
     */
    suspend fun unlikeVideo(videoId: String): Result<Unit>

    /**
     * 指定した投稿者（ユーザー）をフォローする
     *
     * @param userId ユーザーID
     */
    suspend fun followUser(userId: Long): Result<Unit>

    /**
     * 指定した投稿者（ユーザー）のフォローを解除する
     *
     * @param userId ユーザーID
     */
    suspend fun unfollowUser(userId: Long): Result<Unit>

    /**
     * 指定した投稿者（ユーザー）を現在フォローしているか判定する
     *
     * @param userId ユーザーID
     * @return フォロー中なら true
     */
    suspend fun isUserFollowed(userId: Long): Result<Boolean>

    /**
     * 動画へコメントを投稿する
     *
     * @param videoId 動画ID (smXXXX 等)
     * @param threadId コメントスレッドID
     * @param serverUrl nvComment サーバー URL
     * @param body コメント本文
     * @param commands コマンドリスト (例: ["184", "shita", "red"])
     * @param vposMs 再生位置 (ミリ秒)
     * @return 投稿されたコメント情報
     */
    suspend fun postComment(
        videoId: String,
        threadId: Long,
        serverUrl: String,
        body: String,
        commands: List<String>,
        vposMs: Long
    ): Result<CommentItem>

    /**
     * 自分が投稿したコメントを削除する
     *
     * @param videoId 動画ID
     * @param threadId コメントスレッドID
     * @param serverUrl nvComment サーバー URL
     * @param commentId 削除対象コメントID
     * @param commentNo 削除対象コメント番号
     */
    suspend fun deleteComment(
        videoId: String,
        threadId: Long,
        serverUrl: String,
        commentId: String,
        commentNo: Long
    ): Result<Unit>

    /**
     * コメントをニコる (Nicoru)
     *
     * @param videoId 動画ID
     * @param threadId コメントスレッドID
     * @param serverUrl nvComment サーバー URL
     * @param commentNo コメント番号
     * @param commentBody コメント本文
     * @param fork スレッド fork (通常 "main")
     * @return Pair(nicoruId, nicoruCount)
     */
    suspend fun postNicoru(
        videoId: String,
        threadId: Long,
        serverUrl: String,
        commentNo: Long,
        commentBody: String,
        fork: String = "main"
    ): Result<Pair<String, Int>>

    /**
     * ニコるを取り消す (DELETE)
     *
     * @param nicoruId ニコるID
     */
    suspend fun deleteNicoru(nicoruId: String): Result<Unit>
}
