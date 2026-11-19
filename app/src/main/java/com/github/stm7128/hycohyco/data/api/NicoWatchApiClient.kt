package com.github.stm7128.hycohyco.data.api

import com.github.stm7128.hycohyco.data.model.DomandAccessRightRequestDto
import com.github.stm7128.hycohyco.data.model.DomandAccessRightResponseDto
import com.github.stm7128.hycohyco.data.model.WatchResponseRootDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

/**
 * ニコニコ動画 watch v3 / v3_guest API および Domand HLS アクセス権取得用 API クライアント
 */
class NicoWatchApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    /**
     * 動画のメタデータ (api/watch/v3 または v3_guest) を取得する
     *
     * @param videoId 動画ID (例: "sm9")
     * @param userSessionCookie ログイン中の user_session Cookie文字列 (空の場合は v3_guest を使用)
     * @return Pair<WatchResponseRootDto, nicosidCookie>
     */
    suspend fun fetchWatchData(
        videoId: String,
        userSessionCookie: String = ""
    ): Pair<WatchResponseRootDto, String> {
        val actionTrackId = "HycoHyco_${System.currentTimeMillis()}"
        val endpoint = if (userSessionCookie.isNotBlank()) "v3" else "v3_guest"
        val url = "https://www.nicovideo.jp/api/watch/$endpoint/$videoId"

        val response = httpClient.get(url) {
            parameter("actionTrackId", actionTrackId)
            applyNicoHeaders(userSessionCookie, requestWith = null)
            // Apache の Accept ネゴシエーションによる 406 回避のため */* を指定
            header(HttpHeaders.Accept, "*/*")
        }
        response.ensureSuccess("fetchWatchData")

        // Set-Cookie ヘッダから nicosid を抽出
        val setCookieHeaders = response.headers.getAll(HttpHeaders.SetCookie) ?: emptyList()
        val nicosid = setCookieHeaders
            .firstOrNull { it.contains("nicosid=") }
            ?.substringAfter("nicosid=")
            ?.substringBefore(";")
            ?: ""

        val rawText = response.bodyAsText()
        val responseBody: WatchResponseRootDto = try {
            NicoApiConstants.sharedJsonParser.decodeFromString(rawText)
        } catch (e: Exception) {
            android.util.Log.e("NicoWatchApiClient", "WatchResponseRootDto デコード失敗: $videoId", e)
            throw IllegalStateException("動画情報の解析に失敗しました ($videoId)", e)
        }
        return Pair(responseBody, nicosid)
    }

    /**
     * Domand HLS のアクセス権を取得し、マスタープレイリストURLと認可Cookie (domand_bid) を返す
     *
     * @param videoId 動画ID
     * @param watchTrackId watch API から取得した client.watchTrackId
     * @param accessRightKey watch API から取得した media.domand.accessRightKey
     * @param outputs 映像・音声の全組み合わせ配列 (例: [["video-h264-360p", "audio-aac-128kbps"], ...])
     * @param nicosid v3_guest API で取得したセッションID (nicosid)
     * @return Pair<contentUrl, domandBidCookie>
     */
    suspend fun fetchDomandHlsAccessRight(
        videoId: String,
        watchTrackId: String,
        accessRightKey: String,
        outputs: List<List<String>>,
        nicosid: String = "",
        userSessionCookie: String = ""
    ): Pair<String, String> {
        val url = "https://nvapi.nicovideo.jp/v1/watch/$videoId/access-rights/hls"

        val response = httpClient.post(url) {
            parameter("actionTrackId", watchTrackId)
            applyNicoHeaders(null)
            header("X-Access-Right-Key", accessRightKey)

            val cookies = mutableListOf<String>()
            if (nicosid.isNotEmpty()) cookies.add("nicosid=$nicosid")
            if (userSessionCookie.isNotEmpty()) {
                val clean = if (userSessionCookie.startsWith("user_session=")) userSessionCookie else "user_session=$userSessionCookie"
                cookies.add(clean)
            }
            if (cookies.isNotEmpty()) {
                header(HttpHeaders.Cookie, cookies.joinToString("; "))
            }

            contentType(ContentType.Application.Json)
            setBody(DomandAccessRightRequestDto(outputs = outputs))
        }
        response.ensureSuccess("fetchDomandHlsAccessRight")

        // Set-Cookie ヘッダから domand_bid を抽出
        val setCookieHeaders = response.headers.getAll(HttpHeaders.SetCookie) ?: emptyList()
        val domandBidCookie = setCookieHeaders
            .firstOrNull { it.contains("domand_bid=") }
            ?.substringAfter("domand_bid=")
            ?.substringBefore(";")
            ?.let { "domand_bid=$it" }
            ?: ""

        val responseBody: DomandAccessRightResponseDto = response.body()
        val contentUrl = responseBody.data?.contentUrl
            ?: throw IllegalStateException("Domand HLS contentUrl がレスポンスに含まれていません")

        return Pair(contentUrl, domandBidCookie)
    }
}
