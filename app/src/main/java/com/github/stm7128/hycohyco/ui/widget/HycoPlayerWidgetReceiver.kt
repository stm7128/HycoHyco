package com.github.stm7128.hycohyco.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.github.stm7128.hycohyco.player.NicoPlaybackManager
import org.koin.core.context.GlobalContext

/**
 * HycoHyco ホーム画面プレイヤーウィジェットの BroadcastReceiver
 *
 * ウィジェットのライフサイクル（配置・更新）およびウィジェット上の再生/一時停止・
 * 10秒シークボタン操作イベントを受信して [NicoPlaybackManager] に中継する。
 */
class HycoPlayerWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = HycoPlayerWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            HycoPlayerWidgetKeys.ACTION_TOGGLE_PLAY_PAUSE -> {
                handleTogglePlayPause(context)
            }
            HycoPlayerWidgetKeys.ACTION_SEEK_BACKWARD -> {
                handleSeek(deltaMs = -10_000L)
            }
            HycoPlayerWidgetKeys.ACTION_SEEK_FORWARD -> {
                handleSeek(deltaMs = 10_000L)
            }
        }
    }

    private fun handleTogglePlayPause(context: Context) {
        try {
            val playbackManager = GlobalContext.getOrNull()?.get<NicoPlaybackManager>() ?: return
            playbackManager.togglePlayPause()

            val player = playbackManager.exoPlayer
            val isPlaying = player.isPlaying
            val hasMedia = player.mediaItemCount > 0
            val mediaItem = player.currentMediaItem

            HycoWidgetUpdater.updatePlaybackState(
                context = context,
                isPlaying = isPlaying,
                hasMedia = hasMedia,
                title = mediaItem?.mediaMetadata?.title?.toString() ?: "",
                artist = mediaItem?.mediaMetadata?.artist?.toString() ?: "",
                videoId = mediaItem?.mediaId ?: ""
            )
        } catch (_: Exception) {
            // プロセス未起動または再生マネージャー取得不可時は安全に無視
        }
    }

    private fun handleSeek(deltaMs: Long) {
        try {
            val playbackManager = GlobalContext.getOrNull()?.get<NicoPlaybackManager>() ?: return
            playbackManager.seekBy(deltaMs)
        } catch (_: Exception) {
            // プロセス未起動または再生マネージャー取得不可時は安全に無視
        }
    }
}
