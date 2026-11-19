package com.github.stm7128.hycohyco.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore を用いたテーマ設定の永続化データソース
 *
 * @param dataStore Preferences DataStore インスタンス
 */
class ThemePreferencesDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val KEY_DARK_CONFIG = stringPreferencesKey("dark_theme_config")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("is_dynamic_color")
        val KEY_AMOLED_BLACK = booleanPreferencesKey("is_amoled_black")
        val KEY_FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("is_first_launch_completed")
        val KEY_DEFAULT_HOME_MODE = stringPreferencesKey("default_home_mode")
    }

    /**
     * テーマ設定の変更を監視する Flow
     */
    val themeSettings: Flow<ThemeSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeName = preferences[PreferencesKeys.KEY_THEME_MODE]
            val themeMode = runCatching {
                themeModeName?.let { AppThemeMode.valueOf(it) }
            }.getOrNull() ?: AppThemeMode.HYPER_OS

            val darkConfigName = preferences[PreferencesKeys.KEY_DARK_CONFIG]
            val darkConfig = runCatching {
                darkConfigName?.let { DarkThemeConfig.valueOf(it) }
            }.getOrNull() ?: DarkThemeConfig.FOLLOW_SYSTEM

            val isDynamicColor = preferences[PreferencesKeys.KEY_DYNAMIC_COLOR] ?: true
            val isAmoledBlack = preferences[PreferencesKeys.KEY_AMOLED_BLACK] ?: false
            val isFirstLaunchCompleted = preferences[PreferencesKeys.KEY_FIRST_LAUNCH_COMPLETED] ?: false
            val homeModeName = preferences[PreferencesKeys.KEY_DEFAULT_HOME_MODE]
            val defaultHomeMode = HomeMode.fromName(homeModeName)

            ThemeSettings(
                themeMode = themeMode,
                darkThemeConfig = darkConfig,
                isDynamicColor = isDynamicColor,
                isAmoledBlack = isAmoledBlack,
                isFirstLaunchCompleted = isFirstLaunchCompleted,
                defaultHomeMode = defaultHomeMode
            )
        }

    /**
     * テーマモード（HyperOS / Material You）を保存する
     */
    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_THEME_MODE] = mode.name
        }
    }

    /**
     * ダークテーマ設定を保存する
     */
    suspend fun setDarkThemeConfig(config: DarkThemeConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_DARK_CONFIG] = config.name
        }
    }

    /**
     * Material You ダイナミックカラー設定を保存する
     */
    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_DYNAMIC_COLOR] = enabled
        }
    }

    /**
     * AMOLED ピュアブラック設定を保存する
     */
    suspend fun setAmoledBlack(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_AMOLED_BLACK] = enabled
        }
    }

    /**
     * 初回セットアップ完了状態を保存する
     */
    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_FIRST_LAUNCH_COMPLETED] = true
        }
    }

    /**
     * 起動時のデフォルトホーム画面モード (ランキング / フォロー新着) を保存する
     */
    suspend fun setDefaultHomeMode(mode: HomeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_DEFAULT_HOME_MODE] = mode.name
        }
    }
}
