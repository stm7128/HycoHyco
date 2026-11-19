package com.github.stm7128.hycohyco.data.repository

import android.webkit.CookieManager
import com.github.stm7128.hycohyco.data.api.NicoAuthApiClient
import com.github.stm7128.hycohyco.data.local.AuthPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.UserSession
import com.github.stm7128.hycohyco.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * ニコニコアカウント認証リポジトリの実装クラス
 */
class AuthRepositoryImpl(
    private val authApiClient: NicoAuthApiClient,
    private val authPreferencesDataSource: AuthPreferencesDataSource
) : AuthRepository {

    override val userSession: Flow<UserSession> = authPreferencesDataSource.userSession

    override suspend fun importSessionCookie(cookieValue: String): Result<UserSession> {
        val cleanCookie = cookieValue.trim()
            .substringAfter("user_session=")
            .substringBefore(";")
            .trim()

        if (cleanCookie.isBlank()) {
            return Result.failure(IllegalArgumentException("有効な user_session Cookie を入力してください"))
        }

        val fullCookie = "user_session=$cleanCookie"
        val profileResult = authApiClient.fetchMe(fullCookie)
        if (profileResult.isFailure) {
            return Result.failure(profileResult.exceptionOrNull() ?: Exception("セッションの検証に失敗しました"))
        }

        val profile = profileResult.getOrThrow()
        val session = UserSession(
            userId = profile.userId,
            nickname = profile.nickname,
            iconUrl = profile.iconUrl,
            isPremium = profile.isPremium,
            userSessionCookie = fullCookie
        )
        authPreferencesDataSource.saveSession(session)
        return Result.success(session)
    }

    override suspend fun refreshProfile(): Result<UserSession> {
        val current = authPreferencesDataSource.userSession.first()
        if (current.userSessionCookie.isBlank()) {
            return Result.failure(IllegalStateException("ログインしていません"))
        }

        val profileResult = authApiClient.fetchMe(current.userSessionCookie)
        if (profileResult.isFailure) {
            return Result.failure(profileResult.exceptionOrNull() ?: Exception("プロフィール更新失敗"))
        }

        val profile = profileResult.getOrThrow()
        val updated = current.copy(
            userId = profile.userId,
            nickname = profile.nickname,
            iconUrl = profile.iconUrl,
            isPremium = profile.isPremium
        )
        authPreferencesDataSource.saveSession(updated)
        return Result.success(updated)
    }

    override suspend fun logout() {
        authPreferencesDataSource.clearSession()
        try {
            val cm = CookieManager.getInstance()
            cm.removeAllCookies(null)
            cm.flush()
        } catch (_: Exception) {
            // CookieManager 呼び出しエラー時は無視
        }
    }
}
