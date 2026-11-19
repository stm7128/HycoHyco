package com.github.stm7128.hycohyco.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.github.stm7128.hycohyco.R
import com.google.common.collect.ImmutableList
import org.json.JSONObject

/**
 * HycoHyco 用カスタムメディア通知プロバイダー
 *
 * Media3 標準の [DefaultMediaNotificationProvider] を委譲ラップし、以下を担う：
 * 1. Android 8.0+ の再生用通知チャネル (`hyco_playback_channel`) の確実な作成
 * 2. 通知小アイコン（アプリアイコン `R.drawable.ic_launcher_foreground`）の適用
 * 3. Xiaomi HyperOS 3「スーパー島（焦点通知 / Dynamic Island）」向けの拡張メタデータ
 *    (`miui.focus.param.media`) の付与
 */
@OptIn(UnstableApi::class)
class HycoMediaNotificationProvider(
    context: Context
) : MediaNotification.Provider {

    companion object {
        const val CHANNEL_ID = "hyco_playback_channel"
        const val NOTIFICATION_ID = 1001
        private const val EXTRA_MIUI_FOCUS_MEDIA = "miui.focus.param.media"
    }

    init {
        ensureNotificationChannel(context)
    }

    private val delegate: DefaultMediaNotificationProvider =
        DefaultMediaNotificationProvider.Builder(context)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.playback_channel_name)
            .setNotificationId(NOTIFICATION_ID)
            .build()
            .apply {
                setSmallIcon(R.drawable.ic_launcher_foreground)
            }

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        val mediaNotification = delegate.createNotification(
            mediaSession,
            customLayout,
            actionFactory,
            onNotificationChangedCallback
        )
        attachHyperOsSuperIslandExtras(mediaNotification.notification, mediaSession)
        return mediaNotification
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        return delegate.handleCustomCommand(session, action, extras)
    }

    /**
     * Android 8.0 以降向けにバックグラウンド再生用の通知チャネルを登録する
     */
    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.playback_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "動画・音声のバックグラウンド再生コントロールおよびスーパー島表示"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Xiaomi HyperOS 3 の「スーパー島（焦点通知）」および島上ドラッグ共有に対応する拡張 JSON を付与する
     */
    private fun attachHyperOsSuperIslandExtras(
        notification: Notification,
        mediaSession: MediaSession
    ) {
        val currentItem = mediaSession.player.currentMediaItem ?: return
        val videoId = currentItem.mediaId.takeIf { it.isNotBlank() } ?: return
        val title = currentItem.mediaMetadata.title?.toString().orEmpty().ifBlank { videoId }
        val artist = currentItem.mediaMetadata.artist?.toString().orEmpty()

        try {
            val shareData = JSONObject().apply {
                put("title", title)
                put("content", artist.ifBlank { "ニコニコ動画" })
                put("shareContent", "https://www.nicovideo.jp/watch/$videoId")
            }
            val paramIsland = JSONObject().apply {
                put("shareData", shareData)
            }
            val paramV2 = JSONObject().apply {
                put("param_island", paramIsland)
            }
            val root = JSONObject().apply {
                put("param_v2", paramV2)
            }
            notification.extras.putString(EXTRA_MIUI_FOCUS_MEDIA, root.toString())
        } catch (_: Exception) {
            // JSON 構築失敗時は標準の MediaStyle 通知として継続
        }
    }
}
