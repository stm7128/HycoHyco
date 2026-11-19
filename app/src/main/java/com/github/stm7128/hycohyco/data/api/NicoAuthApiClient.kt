package com.github.stm7128.hycohyco.data.api

import android.util.Log
import com.github.stm7128.hycohyco.data.model.UsersMeResponseDto
import com.github.stm7128.hycohyco.domain.model.NicoUserProfile
import com.github.stm7128.hycohyco.domain.util.suspendRunCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

private const val TAG = "NicoAuthApiClient"

/**
 * ニコニコ動画 アカウントプロフィール取得・検証 API クライアント
 */
class NicoAuthApiClient(
    private val httpClient: HttpClient = HycoHttpClientProvider.sharedKtorClient
) {
    companion object {
        private const val USERS_ME_URL = "https://nvapi.nicovideo.jp/v1/users/me"
    }

    /**
     * ログイン中のユーザープロフィールを取得する
     */
    suspend fun fetchMe(userSessionCookie: String): Result<NicoUserProfile> {
        return suspendRunCatching {
            val response = httpClient.get(USERS_ME_URL) {
                applyNicoHeaders(userSessionCookie)
            }
            response.ensureSuccess("NicoAuthApiClient.fetchMe")

            val rawBody = response.bodyAsText()
            val dto: UsersMeResponseDto = try {
                NicoApiConstants.sharedJsonParser.decodeFromString(rawBody)
            } catch (e: Exception) {
                Log.e(TAG, "JSONデコードエラー: ${e.message}", e)
                throw e
            }

            val data = dto.data ?: throw IllegalStateException("ユーザープロフィールの取得に失敗しました (data == null)")
            val u = data.user
            val resolvedId = if (u != null && u.id > 0L) u.id else data.userId
            val resolvedName = if (u != null && u.nickname.isNotBlank()) u.nickname else data.nickname
            val resolvedDesc = if (u != null) u.description else data.description
            val resolvedPremium = if (u != null) u.isPremium else data.isPremium
            val icons = u?.icons ?: data.icons
            val resolvedIcon = icons?.large?.ifBlank { icons.small } ?: ""

            if (resolvedId <= 0L) {
                throw IllegalStateException("有効なユーザーIDが取得できませんでした (id=$resolvedId)")
            }

            NicoUserProfile(
                userId = resolvedId,
                nickname = resolvedName,
                description = resolvedDesc,
                iconUrl = resolvedIcon,
                isPremium = resolvedPremium
            )
        }.onFailure { e ->
            Log.e(TAG, "fetchMe 失敗: ${e.message}", e)
        }
    }
}
