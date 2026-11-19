package com.github.stm7128.hycohyco.data.api

import android.util.Log
import com.github.stm7128.hycohyco.data.model.DeleteCommentRequestDto
import com.github.stm7128.hycohyco.data.model.DeleteCommentTargetDto
import com.github.stm7128.hycohyco.data.model.LikeResponseDto
import com.github.stm7128.hycohyco.data.model.NicoruKeyResponseDto
import com.github.stm7128.hycohyco.data.model.NicoruRequestDto
import com.github.stm7128.hycohyco.data.model.NicoruResponseRootDto
import com.github.stm7128.hycohyco.data.model.PostCommentRequestDto
import com.github.stm7128.hycohyco.data.model.PostCommentResponseDto
import com.github.stm7128.hycohyco.data.model.PostKeyResponseDto
import com.github.stm7128.hycohyco.data.model.UserFolloweesBulkResponseDto
import com.github.stm7128.hycohyco.domain.model.CommentCommandParser
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.LikeResult
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

/**
 * ニコニコ双方向インタラクション (いいね！・コメント投稿・削除・フォロー等) API クライアント
 */
class NicoInteractionApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient,
    private val json: Json = NicoApiConstants.sharedJsonParser
) {

    /** 動画に「いいね！」を送信する */
    suspend fun likeVideo(videoId: String, userSessionCookie: String): Result<LikeResult> = suspendRunCatching {
        val response = httpClient.post("https://nvapi.nicovideo.jp/v1/users/me/likes/items") {
            applyNicoHeaders(userSessionCookie)
            header("Referer", "https://www.nicovideo.jp/watch/$videoId")
            parameter("videoId", videoId)
        }
        response.ensureSuccess("likeVideo")
        val dto = json.decodeFromString<LikeResponseDto>(response.bodyAsText())
        LikeResult(isLiked = true, thanksMessage = dto.data?.thanksMessage?.takeIf { it.isNotBlank() })
    }

    /** 動画の「いいね！」を解除する */
    suspend fun unlikeVideo(videoId: String, userSessionCookie: String): Result<Unit> = suspendRunCatching {
        val response = httpClient.delete("https://nvapi.nicovideo.jp/v1/users/me/likes/items") {
            applyNicoHeaders(userSessionCookie)
            header("Referer", "https://www.nicovideo.jp/watch/$videoId")
            parameter("videoId", videoId)
        }
        response.ensureSuccess("unlikeVideo")
    }

    /** クリエイターをフォローする */
    suspend fun followUser(userId: Long, userSessionCookie: String): Result<Unit> = suspendRunCatching {
        val url = "https://user-follow-api.nicovideo.jp/v1/user/followees/niconico-users/$userId.json"
        val response = httpClient.post(url) {
            applyNicoHeaders(userSessionCookie)
            header("Referer", "https://www.nicovideo.jp/")
        }
        response.ensureSuccess("followUser")
    }

    /** クリエイターのフォローを解除する */
    suspend fun unfollowUser(userId: Long, userSessionCookie: String): Result<Unit> = suspendRunCatching {
        val url = "https://user-follow-api.nicovideo.jp/v1/user/followees/niconico-users/$userId.json"
        val response = httpClient.delete(url) {
            applyNicoHeaders(userSessionCookie)
            header("Referer", "https://www.nicovideo.jp/")
        }
        response.ensureSuccess("unfollowUser")
    }

    /** クリエイターをフォロー中かどうか判定する */
    suspend fun checkIsFollowing(userId: Long, userSessionCookie: String): Result<Boolean> = suspendRunCatching {
        val response = httpClient.get("https://user-follow-api.nicovideo.jp/v1/user/followees/bulk/niconico-users.json") {
            applyNicoHeaders(userSessionCookie)
            header("Referer", "https://www.nicovideo.jp/")
            parameter("targetIds", userId)
        }
        response.ensureSuccess("checkIsFollowing")
        val dto = json.decodeFromString<UserFolloweesBulkResponseDto>(response.bodyAsText())
        dto.data?.followees?.get(userId.toString())?.following ?: false
    }

    /** コメント投稿用 postKey を取得する */
    suspend fun getPostKey(threadId: Long, userSessionCookie: String): Result<String> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/comment/keys/post") {
            applyNicoHeaders(userSessionCookie, requestWith = null)
            parameter("threadId", threadId)
        }
        response.ensureSuccess("getPostKey")
        val dto = json.decodeFromString<PostKeyResponseDto>(response.bodyAsText())
        dto.data?.postKey ?: throw IllegalStateException("postKeyが含まれていません")
    }

    /** nvcomment API へコメントを投稿する */
    suspend fun postComment(
        serverUrl: String,
        threadId: Long,
        videoId: String,
        body: String,
        commands: List<String>,
        vposMs: Long,
        postKey: String,
        userSessionCookie: String = ""
    ): Result<CommentItem> = suspendRunCatching {
        val baseUrl = serverUrl.trimEnd('/')
        val url = "$baseUrl/v1/threads/$threadId/comments"
        val payload = PostCommentRequestDto(videoId = videoId, body = body, commands = commands, vposMs = vposMs, postKey = postKey)
        val response = httpClient.post(url) {
            applyNicoHeaders(userSessionCookie)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        response.ensureSuccess("postComment")
        val dto = json.decodeFromString<PostCommentResponseDto>(response.bodyAsText())
        val data = dto.data
        val parsed = CommentCommandParser.parse(commands)
        CommentItem(
            id = data?.id?.ifBlank { null } ?: System.currentTimeMillis().toString(),
            no = data?.no ?: 0L,
            vposMs = vposMs,
            body = body,
            position = parsed.position,
            size = parsed.size,
            colorInt = parsed.colorInt,
            font = parsed.font,
            isTranslucent = parsed.isTranslucent,
            is184 = parsed.is184,
            userId = "",
            isMyComment = true
        )
    }

    /** コメント削除用 deleteKey を取得する */
    suspend fun getDeleteKey(threadId: Long, userSessionCookie: String): Result<String> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/comment/keys/delete") {
            applyNicoHeaders(userSessionCookie, requestWith = null)
            parameter("threadId", threadId)
        }
        response.ensureSuccess("getDeleteKey")
        val dto = json.decodeFromString<PostKeyResponseDto>(response.bodyAsText())
        dto.data?.postKey ?: throw IllegalStateException("deleteKeyが含まれていません")
    }

    /** コメントを削除する */
    suspend fun deleteComment(
        serverUrl: String,
        threadId: Long,
        videoId: String,
        commentId: String,
        commentNo: Long,
        deleteKey: String,
        userSessionCookie: String
    ): Result<Unit> = suspendRunCatching {
        val baseUrl = serverUrl.trimEnd('/')
        val url = "$baseUrl/v1/threads/$threadId/comment-comment-owner-deletions"
        val payload = DeleteCommentRequestDto(
            videoId = videoId,
            targets = listOf(DeleteCommentTargetDto(no = commentNo, operation = "DELETE")),
            fork = "main",
            language = "ja-jp",
            deleteKey = deleteKey
        )
        val jsonPayload = json.encodeToString(DeleteCommentRequestDto.serializer(), payload)
        val response = httpClient.put(url) {
            applyNicoHeaders(userSessionCookie)
            header("Referer", "https://www.nicovideo.jp/watch/$videoId")
            contentType(ContentType.Application.Json)
            setBody(jsonPayload)
        }
        response.ensureSuccess("deleteComment")
    }

    /** ニコる用 nicoruKey を取得する */
    suspend fun getNicoruKey(threadId: Long, userSessionCookie: String, fork: String = "main"): Result<String> = suspendRunCatching {
        val response = httpClient.get("https://nvapi.nicovideo.jp/v1/comment/keys/nicoru") {
            applyNicoHeaders(userSessionCookie, requestWith = null)
            parameter("threadId", threadId)
            parameter("fork", fork)
        }
        response.ensureSuccess("getNicoruKey")
        val dto = json.decodeFromString<NicoruKeyResponseDto>(response.bodyAsText())
        dto.data?.nicoruKey?.takeIf { it.isNotBlank() } ?: throw IllegalStateException("nicoruKeyが含まれていません")
    }

    /** コメントをニコる (POST /v1/threads/{threadId}/nicorus?pc=1) */
    suspend fun postNicoru(
        serverUrl: String,
        threadId: Long,
        videoId: String,
        commentNo: Long,
        commentBody: String,
        nicoruKey: String,
        fork: String = "main"
    ): Result<Pair<String, Int>> = suspendRunCatching {
        val baseUrl = serverUrl.trimEnd('/')
        val url = "$baseUrl/v1/threads/$threadId/nicorus"
        val payload = NicoruRequestDto(
            videoId = videoId,
            fork = fork,
            no = commentNo,
            content = commentBody,
            nicoruKey = nicoruKey
        )
        val jsonPayload = json.encodeToString(NicoruRequestDto.serializer(), payload)
        val response = httpClient.post(url) {
            applyNicoHeaders(null)
            parameter("pc", 1)
            header("X-Client-Os-Type", "others")
            contentType(ContentType.Application.Json)
            setBody(jsonPayload)
        }
        response.ensureSuccess("postNicoru")
        val dto = json.decodeFromString<NicoruResponseRootDto>(response.bodyAsText())
        val data = dto.data ?: throw IllegalStateException("ニコるレスポンスデータが空です")
        Pair(data.nicoruId, data.nicoruCount)
    }

    /** ニコるを取り消す (DELETE /v1/users/me/nicoru/send/{nicoruId}) */
    suspend fun deleteNicoru(
        nicoruId: String,
        userSessionCookie: String
    ): Result<Unit> = suspendRunCatching {
        val url = "https://nvapi.nicovideo.jp/v1/users/me/nicoru/send/$nicoruId"
        val response = httpClient.delete(url) {
            applyNicoHeaders(userSessionCookie, referer = "https://www.nicovideo.jp/")
        }
        response.ensureSuccess("deleteNicoru")
    }
}
