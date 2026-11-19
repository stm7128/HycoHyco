package com.github.stm7128.hycohyco.domain.repository

import com.github.stm7128.hycohyco.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * ニコニコアカウント認証リポジトリのインターフェース
 */
interface AuthRepository {

    /**
     * 現在のログインセッション状態を監視する Flow
     */
    val userSession: Flow<UserSession>

    /**
     * セッションCookie (user_session) を取り込んでログインを完了する
     */
    suspend fun importSessionCookie(cookieValue: String): Result<UserSession>

    /**
     * ログイン中のユーザー情報を最新化する
     */
    suspend fun refreshProfile(): Result<UserSession>

    /**
     * ログアウトする (DataStore および WebView の Cookie を全消去)
     */
    suspend fun logout()
}
