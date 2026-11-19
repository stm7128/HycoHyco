package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.api.NicoInteractionApiClient
import com.github.stm7128.hycohyco.data.local.AuthPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.LikeResult
import com.github.stm7128.hycohyco.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.first

/**
 * ニコニコ双方向インタラクションリポジトリの実装クラス
 */
class InteractionRepositoryImpl(
    private val interactionApiClient: NicoInteractionApiClient,
    private val authPreferencesDataSource: AuthPreferencesDataSource
) : InteractionRepository {

    private suspend fun getSessionCookie(): String {
        return authPreferencesDataSource.userSession.first().formattedCookieHeader
    }

    override suspend fun likeVideo(videoId: String): Result<LikeResult> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.failure(IllegalStateException("「いいね！」を送信するにはログインが必要です"))
        }
        return interactionApiClient.likeVideo(videoId, cookie)
    }

    override suspend fun unlikeVideo(videoId: String): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.failure(IllegalStateException("「いいね！」を解除するにはログインが必要です"))
        }
        return interactionApiClient.unlikeVideo(videoId, cookie)
    }

    override suspend fun followUser(userId: Long): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.failure(IllegalStateException("フォローするにはログインが必要です"))
        }
        return interactionApiClient.followUser(userId, cookie)
    }

    override suspend fun unfollowUser(userId: Long): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.failure(IllegalStateException("フォロー解除するにはログインが必要です"))
        }
        return interactionApiClient.unfollowUser(userId, cookie)
    }

    override suspend fun isUserFollowed(userId: Long): Result<Boolean> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.success(false)
        }
        return interactionApiClient.checkIsFollowing(userId, cookie)
    }

    override suspend fun postComment(
        videoId: String,
        threadId: Long,
        serverUrl: String,
        body: String,
        commands: List<String>,
        vposMs: Long
    ): Result<CommentItem> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.failure(IllegalStateException("コメントを投稿するにはログインが必要です"))
        }
        val postKeyResult = interactionApiClient.getPostKey(threadId, cookie)
        val postKey = postKeyResult.getOrElse { return Result.failure(it) }

        return interactionApiClient.postComment(
            serverUrl = serverUrl,
            threadId = threadId,
            videoId = videoId,
            body = body,
            commands = commands,
            vposMs = vposMs,
            postKey = postKey,
            userSessionCookie = cookie
        )
    }

    override suspend fun deleteComment(
        videoId: String,
        threadId: Long,
        serverUrl: String,
        commentId: String,
        commentNo: Long
    ): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.failure(IllegalStateException("コメントを削除するにはログインが必要です"))
        }
        val deleteKeyResult = interactionApiClient.getDeleteKey(threadId, cookie)
        val deleteKey = deleteKeyResult.getOrElse { return Result.failure(it) }

        return interactionApiClient.deleteComment(
            serverUrl = serverUrl,
            threadId = threadId,
            videoId = videoId,
            commentId = commentId,
            commentNo = commentNo,
            deleteKey = deleteKey,
            userSessionCookie = cookie
        )
    }

    override suspend fun postNicoru(
        videoId: String,
        threadId: Long,
        serverUrl: String,
        commentNo: Long,
        commentBody: String,
        fork: String
    ): Result<Pair<String, Int>> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.failure(IllegalStateException("ニコるにはログインが必要です"))
        }
        val nicoruKeyResult = interactionApiClient.getNicoruKey(threadId, cookie, fork)
        val nicoruKey = nicoruKeyResult.getOrElse { return Result.failure(it) }

        return interactionApiClient.postNicoru(
            serverUrl = serverUrl,
            threadId = threadId,
            videoId = videoId,
            commentNo = commentNo,
            commentBody = commentBody,
            nicoruKey = nicoruKey,
            fork = fork
        )
    }

    override suspend fun deleteNicoru(nicoruId: String): Result<Unit> {
        val cookie = getSessionCookie()
        if (cookie.isBlank()) {
            return Result.failure(IllegalStateException("ニコる解除にはログインが必要です"))
        }
        return interactionApiClient.deleteNicoru(nicoruId, cookie)
    }
}
