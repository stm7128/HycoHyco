package com.github.stm7128.hycohyco.player.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.github.stm7128.hycohyco.player.NicoPlaybackManager
import com.github.stm7128.hycohyco.ui.MainActivity
import com.github.stm7128.hycohyco.ui.widget.HycoWidgetUpdater
import org.koin.android.ext.android.inject

/**
 * バックグラウンド再生・メディア通知コントロール・HyperOS スーパー島表示・ホーム画面ウィジェット連携を司る Foreground Service
 *
 * Media3 の [MediaSessionService] を継承し、以下を実行する：
 * 1. [HycoMediaNotificationProvider] を設定し、`MediaStyle` 通知と通知チャネルを管理
 * 2. 通知・スーパー島タップ時に [MainActivity] を開く `sessionActivity` ([PendingIntent]) を設定
 * 3. `addSession` を明示的に呼び出し、Media3 内部の通知管理ライフサイクルを即時有効化
 * 4. 再生状態の変更を検知してホーム画面ウィジェット ([HycoWidgetUpdater]) へ非同期配信
 */
class HycoPlaybackService : MediaSessionService() {

    private val playbackManager: NicoPlaybackManager by inject()
    private var mediaSession: MediaSession? = null

    private val widgetPlayerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateWidget()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateWidget()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateWidget()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. カスタム通知プロバイダーを登録（セッション追加より先に設定）
        setMediaNotificationProvider(HycoMediaNotificationProvider(this))

        // 2. 通知やスーパー島（Dynamic Island）タップ時にアプリ画面へ復帰する PendingIntent
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. ExoPlayer と紐づく MediaSession を構築し、MediaSessionService に明示登録
        val session = MediaSession.Builder(this, playbackManager.exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .setPeriodicPositionUpdateEnabled(true)
            .build()

        mediaSession = session
        addSession(session)

        // 4. ホーム画面ウィジェット連携用リスナー登録
        playbackManager.exoPlayer.addListener(widgetPlayerListener)
        updateWidget()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        // ユーザーがタスク一覧からアプリアイコンをスワイプ終了した場合
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        playbackManager.exoPlayer.removeListener(widgetPlayerListener)
        HycoWidgetUpdater.updatePlaybackState(
            context = this,
            isPlaying = false,
            hasMedia = false,
            title = "",
            artist = "",
            videoId = ""
        )
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun updateWidget() {
        val player = playbackManager.exoPlayer
        val isPlaying = player.isPlaying
        val hasMedia = player.mediaItemCount > 0
        val mediaItem = player.currentMediaItem
        val title = mediaItem?.mediaMetadata?.title?.toString() ?: ""
        val artist = mediaItem?.mediaMetadata?.artist?.toString() ?: ""
        val videoId = mediaItem?.mediaId ?: ""

        HycoWidgetUpdater.updatePlaybackState(
            context = this,
            isPlaying = isPlaying,
            hasMedia = hasMedia,
            title = title,
            artist = artist,
            videoId = videoId
        )
    }
}
