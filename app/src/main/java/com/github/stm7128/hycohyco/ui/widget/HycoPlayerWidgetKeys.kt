package com.github.stm7128.hycohyco.ui.widget

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * HycoHyco ホーム画面プレイヤーウィジェットで共有する DataStore キーおよびインテントアクション定義
 */
object HycoPlayerWidgetKeys {
    val KEY_IS_PLAYING = booleanPreferencesKey("widget_is_playing")
    val KEY_HAS_MEDIA = booleanPreferencesKey("widget_has_media")
    val KEY_TITLE = stringPreferencesKey("widget_title")
    val KEY_ARTIST = stringPreferencesKey("widget_artist")
    val KEY_VIDEO_ID = stringPreferencesKey("widget_video_id")

    const val ACTION_TOGGLE_PLAY_PAUSE = "com.github.stm7128.hycohyco.widget.ACTION_TOGGLE_PLAY_PAUSE"
    const val ACTION_SEEK_BACKWARD = "com.github.stm7128.hycohyco.widget.ACTION_SEEK_BACKWARD"
    const val ACTION_SEEK_FORWARD = "com.github.stm7128.hycohyco.widget.ACTION_SEEK_FORWARD"
}
