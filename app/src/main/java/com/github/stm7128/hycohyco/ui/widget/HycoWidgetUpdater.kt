package com.github.stm7128.hycohyco.ui.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * ホーム画面プレイヤーウィジェットの状態更新ユーティリティ
 */
object HycoWidgetUpdater {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * 再生状態の変更をホーム画面ウィジェットへ非同期で配信・再描画する
     *
     * @param context コンテキスト
     * @param isPlaying 再生中フラグ
     * @param hasMedia メディアが存在するか（再生中または一時停止中）
     * @param title 動画タイトル
     * @param artist 投稿者名
     * @param videoId 動画ID
     */
    fun updatePlaybackState(
        context: Context,
        isPlaying: Boolean,
        hasMedia: Boolean,
        title: String = "",
        artist: String = "",
        videoId: String = ""
    ) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(appContext)
                val glanceIds = manager.getGlanceIds(HycoPlayerWidget::class.java)
                if (glanceIds.isEmpty()) return@launch

                for (glanceId in glanceIds) {
                    updateAppWidgetState(appContext, glanceId) { prefs ->
                        prefs[HycoPlayerWidgetKeys.KEY_IS_PLAYING] = isPlaying
                        prefs[HycoPlayerWidgetKeys.KEY_HAS_MEDIA] = hasMedia
                        prefs[HycoPlayerWidgetKeys.KEY_TITLE] = title
                        prefs[HycoPlayerWidgetKeys.KEY_ARTIST] = artist
                        prefs[HycoPlayerWidgetKeys.KEY_VIDEO_ID] = videoId
                    }
                    HycoPlayerWidget().update(appContext, glanceId)
                }
            } catch (_: Exception) {
                // ウィジェット更新失敗時のフォールバック (例外を握りつぶしクラッシュを防止)
            }
        }
    }
}
