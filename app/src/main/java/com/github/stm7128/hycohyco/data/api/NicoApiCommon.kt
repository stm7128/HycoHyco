package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.NvCommentResponseRootDto
import com.github.stm7128.hycohyco.domain.model.CommentCommandParser
import com.github.stm7128.hycohyco.domain.model.CommentFork
import com.github.stm7128.hycohyco.domain.model.CommentItem
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * ニコニコ API 通信用共通定数
 */
object NicoApiConstants {
    const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36 HycoHyco/1.0"
    const val FRONTEND_ID = "6"
    const val FRONTEND_VERSION = "0"
    const val REQUEST_WITH = "https://www.nicovideo.jp"

    val sharedJsonParser: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        coerceInputValues = true
        encodeDefaults = true
    }
}

/**
 * ニコニコ公式 API 向け標準ヘッダーを一括適用する
 */
fun HttpRequestBuilder.applyNicoHeaders(
    userSessionCookie: String? = null,
    referer: String? = null,
    frontendId: String = NicoApiConstants.FRONTEND_ID,
    frontendVersion: String = NicoApiConstants.FRONTEND_VERSION,
    requestWith: String? = NicoApiConstants.REQUEST_WITH
) {
    header("User-Agent", NicoApiConstants.USER_AGENT)
    header("X-Frontend-Id", frontendId)
    header("X-Frontend-Version", frontendVersion)
    if (!requestWith.isNullOrBlank()) {
        header("X-Request-With", requestWith)
        header("Origin", requestWith)
    }
    if (!referer.isNullOrBlank()) {
        header("Referer", referer)
    }
    if (!userSessionCookie.isNullOrBlank()) {
        val cookieValue = if (userSessionCookie.startsWith("user_session=")) {
            userSessionCookie
        } else {
            "user_session=$userSessionCookie"
        }
        header("Cookie", cookieValue)
    }
}

/**
 * ニコニコ API 通信例外
 */
class NicoApiException(val statusCode: Int, message: String) : Exception(message)

/**
 * HTTP レスポンスステータスが 2xx 成功でない場合に例外をスローする
 */
fun HttpResponse.ensureSuccess(endpointName: String = "API") {
    if (!status.isSuccess()) {
        throw NicoApiException(
            statusCode = status.value,
            message = "[$endpointName] HTTP 通信エラー: ${status.value} ${status.description}"
        )
    }
}

/**
 * NvCommentResponseRootDto からドメイン層の CommentItem リストへの共通マッピング
 */
fun NvCommentResponseRootDto.toDomainComments(): List<CommentItem> {
    val threads = data?.threads ?: return emptyList()
    val allComments = mutableListOf<CommentItem>()

    for (thread in threads) {
        val fork = when (thread.fork) {
            "owner" -> CommentFork.OWNER
            "easy" -> CommentFork.EASY
            else -> CommentFork.MAIN
        }

        for (dto in thread.comments) {
            val parsed = CommentCommandParser.parse(dto.commands)
            allComments.add(
                CommentItem(
                    id = dto.id,
                    no = dto.no,
                    vposMs = dto.vposMs,
                    body = dto.body,
                    position = parsed.position,
                    size = parsed.size,
                    colorInt = parsed.colorInt,
                    font = parsed.font,
                    isTranslucent = parsed.isTranslucent,
                    is184 = parsed.is184,
                    fork = fork,
                    userId = dto.userId,
                    score = dto.score,
                    nicoruCount = dto.nicoruCount,
                    nicoruId = dto.nicoruId,
                    isMyComment = dto.isMyPost
                )
            )
        }
    }

    return allComments.sortedBy { it.vposMs }
}
