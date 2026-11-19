package com.github.stm7128.hycohyco.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.domain.model.QualityPreference
import com.github.stm7128.hycohyco.domain.model.DanmakuAreaMode
import com.github.stm7128.hycohyco.domain.model.DanmakuDisplaySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * プレイヤーおよび弾幕関連の環境設定（ジェスチャー・自動再生・弾幕描画等）を保持する DataStore データソース
 */
class PlayerPreferencesDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val KEY_GESTURE_MODE = stringPreferencesKey("player_gesture_mode")
        val KEY_AUTOPLAY_ENABLED = booleanPreferencesKey("is_autoplay_enabled")
        val KEY_DOUBLE_TAP_SEEK_SECONDS = intPreferencesKey("double_tap_seek_seconds")
        val KEY_USE_NICOVIDEO_GAY = booleanPreferencesKey("use_nicovideo_gay")
        val KEY_DEFAULT_QUALITY_WIFI = stringPreferencesKey("default_quality_wifi")
        val KEY_DEFAULT_QUALITY_CELLULAR = stringPreferencesKey("default_quality_cellular")
        val KEY_COEXIST_AUDIO = booleanPreferencesKey("coexist_audio_focus")
        val KEY_AUTO_ENTER_PIP = booleanPreferencesKey("auto_enter_pip")
        val KEY_SHOW_SHORTS_BOTTOM_TAB = booleanPreferencesKey("show_shorts_bottom_tab")
        val KEY_RESUME_PLAYBACK_ENABLED = booleanPreferencesKey("is_resume_playback_enabled")

        // 弾幕表示設定キー
        val KEY_DANMAKU_ENABLED = booleanPreferencesKey("danmaku_enabled")
        val KEY_DANMAKU_ALPHA = floatPreferencesKey("danmaku_alpha")
        val KEY_DANMAKU_SCALE = floatPreferencesKey("danmaku_scale")
        val KEY_DANMAKU_SPEED_SCALE = floatPreferencesKey("danmaku_speed_scale")
        val KEY_DANMAKU_AREA_MODE = stringPreferencesKey("danmaku_area_mode")
    }

    private val safeData: Flow<Preferences> = dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }

    /** スライドジェスチャーの有効モード Flow */
    val gestureMode: Flow<PlayerGestureMode> = safeData
        .map { PlayerGestureMode.fromName(it[PreferencesKeys.KEY_GESTURE_MODE]) }
        .distinctUntilChanged()

    /** 次の動画の自動再生が有効かどうか Flow (デフォルト: true) */
    val autoplayEnabled: Flow<Boolean> = safeData
        .map { it[PreferencesKeys.KEY_AUTOPLAY_ENABLED] ?: true }
        .distinctUntilChanged()

    /** 左右ダブルタップのシーク秒数 Flow (デフォルト: 10秒) */
    val doubleTapSeekSeconds: Flow<Int> = safeData
        .map { it[PreferencesKeys.KEY_DOUBLE_TAP_SEEK_SECONDS] ?: 10 }
        .distinctUntilChanged()

    /** 共有時に nicovideo.gay を使用するかどうか Flow (デフォルト: false) */
    val useNicovideoGay: Flow<Boolean> = safeData
        .map { it[PreferencesKeys.KEY_USE_NICOVIDEO_GAY] ?: false }
        .distinctUntilChanged()

    /** Wi-Fi 接続時のデフォルト画質 Flow (デフォルト: AUTO) */
    val defaultQualityWifi: Flow<QualityPreference> = safeData
        .map { QualityPreference.fromName(it[PreferencesKeys.KEY_DEFAULT_QUALITY_WIFI]) }
        .distinctUntilChanged()

    /** モバイルデータ通信時のデフォルト画質 Flow (デフォルト: P360) */
    val defaultQualityCellular: Flow<QualityPreference> = safeData
        .map { prefs ->
            val name = prefs[PreferencesKeys.KEY_DEFAULT_QUALITY_CELLULAR]
            if (name != null) QualityPreference.fromName(name) else QualityPreference.P360
        }
        .distinctUntilChanged()

    /** 他アプリと音声を共存（同時再生）させるかどうか Flow (デフォルト: false) */
    val coexistAudio: Flow<Boolean> = safeData
        .map { it[PreferencesKeys.KEY_COEXIST_AUDIO] ?: false }
        .distinctUntilChanged()

    /** ホーム画面移動時に自動で PiP 小窓へ移行するかどうか Flow (デフォルト: true) */
    val autoEnterPip: Flow<Boolean> = safeData
        .map { it[PreferencesKeys.KEY_AUTO_ENTER_PIP] ?: true }
        .distinctUntilChanged()

    /** ボトムバーに「ショート」タブを表示するかどうか Flow (デフォルト: true) */
    val showShortsBottomTab: Flow<Boolean> = safeData
        .map { it[PreferencesKeys.KEY_SHOW_SHORTS_BOTTOM_TAB] ?: true }
        .distinctUntilChanged()

    /** 前回の続きから自動レジューム再生するかどうか Flow (デフォルト: true) */
    val isResumePlaybackEnabled: Flow<Boolean> = safeData
        .map { it[PreferencesKeys.KEY_RESUME_PLAYBACK_ENABLED] ?: true }
        .distinctUntilChanged()

    /** 弾幕表示・描画設定 Flow (デフォルト: 表示ON, 透明度90%, 等倍, 全画面) */
    val danmakuSettings: Flow<DanmakuDisplaySettings> = safeData
        .map { prefs ->
            DanmakuDisplaySettings(
                isEnabled = prefs[PreferencesKeys.KEY_DANMAKU_ENABLED] ?: true,
                alpha = (prefs[PreferencesKeys.KEY_DANMAKU_ALPHA] ?: 0.90f).coerceIn(0.1f, 1.0f),
                scale = (prefs[PreferencesKeys.KEY_DANMAKU_SCALE] ?: 1.0f).coerceIn(0.5f, 2.0f),
                speedScale = (prefs[PreferencesKeys.KEY_DANMAKU_SPEED_SCALE] ?: 1.0f).coerceIn(0.5f, 2.0f),
                areaMode = DanmakuAreaMode.fromName(prefs[PreferencesKeys.KEY_DANMAKU_AREA_MODE])
            )
        }
        .distinctUntilChanged()

    suspend fun setGestureMode(mode: PlayerGestureMode) {
        dataStore.edit { it[PreferencesKeys.KEY_GESTURE_MODE] = mode.name }
    }

    suspend fun setAutoplayEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.KEY_AUTOPLAY_ENABLED] = enabled }
    }

    suspend fun setDoubleTapSeekSeconds(seconds: Int) {
        dataStore.edit { it[PreferencesKeys.KEY_DOUBLE_TAP_SEEK_SECONDS] = seconds }
    }

    suspend fun setUseNicovideoGay(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.KEY_USE_NICOVIDEO_GAY] = enabled }
    }

    suspend fun setDefaultQualityWifi(quality: QualityPreference) {
        dataStore.edit { it[PreferencesKeys.KEY_DEFAULT_QUALITY_WIFI] = quality.name }
    }

    suspend fun setDefaultQualityCellular(quality: QualityPreference) {
        dataStore.edit { it[PreferencesKeys.KEY_DEFAULT_QUALITY_CELLULAR] = quality.name }
    }

    suspend fun setCoexistAudio(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.KEY_COEXIST_AUDIO] = enabled }
    }

    suspend fun setAutoEnterPip(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.KEY_AUTO_ENTER_PIP] = enabled }
    }

    suspend fun setShowShortsBottomTab(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.KEY_SHOW_SHORTS_BOTTOM_TAB] = enabled }
    }

    suspend fun setResumePlaybackEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.KEY_RESUME_PLAYBACK_ENABLED] = enabled }
    }

    /** 弾幕表示・描画設定を一括保存する */
    suspend fun setDanmakuSettings(settings: DanmakuDisplaySettings) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.KEY_DANMAKU_ENABLED] = settings.isEnabled
            prefs[PreferencesKeys.KEY_DANMAKU_ALPHA] = settings.alpha.coerceIn(0.1f, 1.0f)
            prefs[PreferencesKeys.KEY_DANMAKU_SCALE] = settings.scale.coerceIn(0.5f, 2.0f)
            prefs[PreferencesKeys.KEY_DANMAKU_SPEED_SCALE] = settings.speedScale.coerceIn(0.5f, 2.0f)
            prefs[PreferencesKeys.KEY_DANMAKU_AREA_MODE] = settings.areaMode.name
        }
    }
}
