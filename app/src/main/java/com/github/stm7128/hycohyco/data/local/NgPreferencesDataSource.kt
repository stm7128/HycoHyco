package com.github.stm7128.hycohyco.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * コメント NG 設定を DataStore で永続化するデータソース
 */
class NgPreferencesDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val KEY_HIDE_184 = booleanPreferencesKey("ng_hide_184")
        val KEY_MIN_SCORE = intPreferencesKey("ng_min_score")
        val KEY_NG_USER_IDS = stringSetPreferencesKey("ng_user_ids")
        val KEY_NG_WORDS = stringSetPreferencesKey("ng_words")
        val KEY_NG_REGEX_PATTERNS = stringSetPreferencesKey("ng_regex_patterns")
    }

    /**
     * 永続化された NG 設定を監視する Flow
     */
    val ngConfig: Flow<CommentNgConfig> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val hide184 = preferences[PreferencesKeys.KEY_HIDE_184] ?: false
            val minScore = preferences[PreferencesKeys.KEY_MIN_SCORE] ?: -1000
            val userIds = preferences[PreferencesKeys.KEY_NG_USER_IDS].orEmpty().toList()
            val words = preferences[PreferencesKeys.KEY_NG_WORDS].orEmpty().toList()
            val regexStrings = preferences[PreferencesKeys.KEY_NG_REGEX_PATTERNS].orEmpty().toList()
            val regexes = regexStrings.mapNotNull { pattern ->
                runCatching { Regex(pattern) }.getOrNull()
            }

            CommentNgConfig(
                minScoreThreshold = minScore,
                ngWords = words,
                ngRegexes = regexes,
                ngUserIds = userIds,
                hide184Comments = hide184
            )
        }

    /**
     * NG 設定全体を上書き保存する
     */
    suspend fun saveNgConfig(config: CommentNgConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_HIDE_184] = config.hide184Comments
            preferences[PreferencesKeys.KEY_MIN_SCORE] = config.minScoreThreshold
            preferences[PreferencesKeys.KEY_NG_USER_IDS] = config.ngUserIds.toSet()
            preferences[PreferencesKeys.KEY_NG_WORDS] = config.ngWords.toSet()
            preferences[PreferencesKeys.KEY_NG_REGEX_PATTERNS] = config.ngRegexes.map { it.pattern }.toSet()
        }
    }

    /**
     * 特定のユーザー ID を NG リストに追加する
     */
    suspend fun addNgUserId(userId: String) {
        if (userId.isBlank()) return
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_NG_USER_IDS].orEmpty()
            preferences[PreferencesKeys.KEY_NG_USER_IDS] = current + userId
        }
    }

    /**
     * 特定のキーワードを NG リストに追加する
     */
    suspend fun addNgWord(word: String) {
        if (word.isBlank()) return
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.KEY_NG_WORDS].orEmpty()
            preferences[PreferencesKeys.KEY_NG_WORDS] = current + word
        }
    }
}
