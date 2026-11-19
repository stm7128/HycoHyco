package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.TagMutationRootDto
import com.github.stm7128.hycohyco.domain.model.VideoTag
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

/**
 * タグ編集キー期限切れを示す例外（自動再取得リトライ用）
 */
class TagKeyExpiredException(message: String = "タグ編集キーの有効期限が切れました") :
    IllegalStateException(message)

/**
 * ニコニコ動画タグ追加・削除 API クライアント (/v2/videos/{watchId}/tags)
 */
class NicoTagApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    companion object {
        private const val BASE_URL = "https://nvapi.nicovideo.jp/v2/videos"
    }

    /**
     * 動画に新規タグを追加する (POST /v2/videos/{watchId}/tags?tag={tag})
     */
    suspend fun addTag(
        watchId: String,
        tag: String,
        tagEditKey: String,
        userSessionCookie: String
    ): Result<List<VideoTag>> = suspendRunCatching {
        val trimmedTag = tag.trim()
        require(trimmedTag.isNotEmpty()) { "追加するタグ名を入力してください" }
        val response = httpClient.post("$BASE_URL/$watchId/tags") {
            parameter("tag", trimmedTag)
            applyNicoHeaders(userSessionCookie)
            header("X-Tag-Edit-Key", tagEditKey)
            header("Referer", "https://www.nicovideo.jp/watch/$watchId")
        }
        parseTagMutationResponse(response, trimmedTag, isDelete = false)
    }

    /**
     * 動画から既存タグを削除する (DELETE /v2/videos/{watchId}/tags?tag={tag})
     */
    suspend fun deleteTag(
        watchId: String,
        tag: String,
        tagEditKey: String,
        userSessionCookie: String
    ): Result<List<VideoTag>> = suspendRunCatching {
        val trimmedTag = tag.trim()
        require(trimmedTag.isNotEmpty()) { "削除するタグが指定されていません" }
        val response = httpClient.delete("$BASE_URL/$watchId/tags") {
            parameter("tag", trimmedTag)
            applyNicoHeaders(userSessionCookie)
            header("X-Tag-Edit-Key", tagEditKey)
            header("Referer", "https://www.nicovideo.jp/watch/$watchId")
        }
        parseTagMutationResponse(response, trimmedTag, isDelete = true)
    }

    private suspend fun parseTagMutationResponse(
        response: HttpResponse,
        tag: String,
        isDelete: Boolean
    ): List<VideoTag> {
        val body = response.bodyAsText()
        val dto = try {
            NicoApiConstants.sharedJsonParser.decodeFromString<TagMutationRootDto>(body)
        } catch (_: Exception) {
            null
        }
        val errorCode = dto?.meta?.errorCode
        if (!response.status.isSuccess()) {
            if (errorCode == "KEY_EXPIRED") {
                throw TagKeyExpiredException()
            }
            throw IllegalStateException(formatTagErrorMessage(errorCode, tag, isDelete, response.status.value))
        }
        val rawItems = dto?.data?.tags ?: dto?.data?.items
            ?: return emptyList()
        return rawItems.mapNotNull { it.toDomainOrNull() }
    }

    private fun formatTagErrorMessage(
        errorCode: String?,
        tag: String,
        isDelete: Boolean,
        httpStatus: Int
    ): String = when (errorCode) {
        "TAG_TOO_LONG" -> "最大文字数を超えているため追加できません"
        "TAG_RESERVED" -> "タグ「$tag」は登録できません"
        "INVALID_PARAMETER" -> "利用できない文字が含まれています"
        "TAG_ALREADY_REGISTERED" -> "タグ「$tag」はすでに登録されています"
        "TAG_LIMIT_EXCEEDED" -> "タグの登録数が上限に達しているため追加できません"
        "TAG_LOCKED" -> "このタグはロックされているため削除できません"
        "FORBIDDEN", "UNAUTHORIZED" -> "タグを編集する権限がありません（ログイン状態をご確認ください）"
        else -> if (isDelete) {
            "タグの削除に失敗しました (HTTP $httpStatus)"
        } else {
            "タグの追加に失敗しました (HTTP $httpStatus)"
        }
    }
}
