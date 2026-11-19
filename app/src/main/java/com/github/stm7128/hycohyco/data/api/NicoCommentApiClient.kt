package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.NvCommentParamsDto
import com.github.stm7128.hycohyco.data.model.NvCommentRequestDto
import com.github.stm7128.hycohyco.data.model.NvCommentResponseRootDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * ニコニコ nvcomment API (/v1/threads) クライアント
 *
 * @param httpClient Ktor HTTP クライアント
 */
class NicoCommentApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {

    /**
     * nvcomment API からコメント一覧スレッドを取得する
     *
     * @param server nvcomment サーバーURL (例: "https://public.nvcomment.nicovideo.jp")
     * @param threadKey 認証トークン
     * @param params 取得対象スレッドターゲット
     * @return [NvCommentResponseRootDto]
     */
    suspend fun fetchThreads(
        server: String,
        threadKey: String,
        params: NvCommentParamsDto,
        userSessionCookie: String = "",
        whenTimestampSec: Long? = null
    ): NvCommentResponseRootDto {
        val url = "${server.trimEnd('/')}/v1/threads"

        val additionals = buildJsonObject {
            if (whenTimestampSec != null) {
                put("when", whenTimestampSec)
            }
        }

        val requestBody = NvCommentRequestDto(
            params = params,
            threadKey = threadKey,
            additionals = additionals
        )

        val response = httpClient.post(url) {
            applyNicoHeaders(userSessionCookie)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        response.ensureSuccess("NicoCommentApiClient.fetchThreads")

        return response.body()
    }
}
