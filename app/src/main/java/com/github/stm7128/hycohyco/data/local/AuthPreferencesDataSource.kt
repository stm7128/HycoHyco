package com.github.stm7128.hycohyco.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.stm7128.hycohyco.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * ニコニコログインセッションおよびアカウント情報を DataStore で永続化するデータソース
 */
class AuthPreferencesDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val KEY_USER_SESSION = stringPreferencesKey("auth_user_session")
        val KEY_USER_ID = longPreferencesKey("auth_user_id")
        val KEY_NICKNAME = stringPreferencesKey("auth_nickname")
        val KEY_ICON_URL = stringPreferencesKey("auth_icon_url")
        val KEY_IS_PREMIUM = booleanPreferencesKey("auth_is_premium")
    }

    /**
     * 現在のログインセッション情報を監視する Flow
     */
    val userSession: Flow<UserSession> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSession(
                userId = preferences[PreferencesKeys.KEY_USER_ID] ?: 0L,
                nickname = preferences[PreferencesKeys.KEY_NICKNAME].orEmpty(),
                iconUrl = preferences[PreferencesKeys.KEY_ICON_URL].orEmpty(),
                isPremium = preferences[PreferencesKeys.KEY_IS_PREMIUM] ?: false,
                userSessionCookie = preferences[PreferencesKeys.KEY_USER_SESSION].orEmpty()
            )
        }

    /**
     * ログインセッションを永続化する
     */
    suspend fun saveSession(session: UserSession) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_USER_SESSION] = session.userSessionCookie
            preferences[PreferencesKeys.KEY_USER_ID] = session.userId
            preferences[PreferencesKeys.KEY_NICKNAME] = session.nickname
            preferences[PreferencesKeys.KEY_ICON_URL] = session.iconUrl
            preferences[PreferencesKeys.KEY_IS_PREMIUM] = session.isPremium
        }
    }

    /**
     * ログアウト処理: セッション情報をすべて消去する
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.KEY_USER_SESSION)
            preferences.remove(PreferencesKeys.KEY_USER_ID)
            preferences.remove(PreferencesKeys.KEY_NICKNAME)
            preferences.remove(PreferencesKeys.KEY_ICON_URL)
            preferences.remove(PreferencesKeys.KEY_IS_PREMIUM)
        }
    }
}
