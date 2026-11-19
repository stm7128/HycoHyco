package com.github.stm7128.hycohyco.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.github.stm7128.hycohyco.domain.model.VideoStreamSession
import com.github.stm7128.hycohyco.player.model.PlaybackState
import com.github.stm7128.hycohyco.player.model.durationMs
import com.github.stm7128.hycohyco.player.service.HycoPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * ニコニコ動画 (Domand HLS / ローカル HLS) の再生を制御するマネージャークラス
 */
@OptIn(UnstableApi::class)
class NicoPlaybackManager(private val context: Context) {
    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36 HycoHyco/1.0"
        private const val POSITION_POLL_INTERVAL_MS = 200L
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pollJob: Job? = null
    private var fallbackDurationMs: Long = 0L

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    var sleepTimerManager: SleepTimerManager? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            updateState()
            if (state == Player.STATE_ENDED) {
                sleepTimerManager?.onVideoEnded()
            }
        }
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState()
            if (isPlaying) startPositionPolling() else stopPositionPolling()
        }
        override fun onPlayerError(error: PlaybackException) {
            stopPositionPolling()
            _playbackState.value = PlaybackState.Error(error.localizedMessage ?: "再生エラーが発生しました")
        }
    }

    init {
        exoPlayer.addListener(playerListener)
        setAudioCoexistence(false)
    }

    /**
     * 他アプリの音声と共存（同時再生）するかどうかを設定する
     *
     * @param enabled true: 他アプリの音を止めない (handleAudioFocus=false), false: 通常制御
     */
    fun setAudioCoexistence(enabled: Boolean) {
        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
            .setUsage(androidx.media3.common.C.USAGE_MEDIA)
            .build()
        exoPlayer.setAudioAttributes(audioAttributes, /* handleAudioFocus = */ !enabled)
    }

    /** Domand HLS 再生セッションを準備して再生を開始する */
    fun prepareAndPlay(
        session: VideoStreamSession,
        title: String,
        artist: String = "",
        thumbnailUrl: String? = null,
        artworkData: ByteArray? = null,
        initialPositionMs: Long = 0L
    ) {
        fallbackDurationMs = 0L
        _playbackState.value = PlaybackState.Buffering()
        try { context.startService(Intent(context, HycoPlaybackService::class.java)) } catch (_: Exception) {}

        val requestHeaders = mutableMapOf<String, String>()
        if (session.domandBidCookie.isNotEmpty()) requestHeaders["Cookie"] = session.domandBidCookie

        val httpDataSource = DefaultHttpDataSource.Factory()
            .setUserAgent(USER_AGENT)
            .setDefaultRequestProperties(requestHeaders)
            .setAllowCrossProtocolRedirects(true)

        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(title)
            .setDisplayTitle(title)
            .setArtist(artist)
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
        if (!thumbnailUrl.isNullOrBlank()) metadataBuilder.setArtworkUri(Uri.parse(thumbnailUrl))
        if (artworkData != null) metadataBuilder.setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FRONT_COVER)

        val item = MediaItem.Builder()
            .setUri(session.masterPlaylistUrl)
            .setMediaId(session.videoId)
            .setMediaMetadata(metadataBuilder.build())
            .build()

        val hlsSource = HlsMediaSource.Factory(httpDataSource).setAllowChunklessPreparation(true).createMediaSource(item)
        exoPlayer.setMediaSource(hlsSource)
        if (initialPositionMs > 0L) {
            exoPlayer.seekTo(initialPositionMs)
        }
        exoPlayer.prepare()
        exoPlayer.play()
    }

    /** ローカルに保存された動画・音声ファイルを再生する (ローカル HLS VOD 優先・完全シーク対応) */
    fun prepareAndPlayLocal(
        videoFilePath: String?,
        audioFilePath: String?,
        title: String,
        artist: String = "",
        masterPlaylistPath: String? = null,
        durationMs: Long = 0L
    ) {
        fallbackDurationMs = durationMs
        _playbackState.value = PlaybackState.Buffering(durationMs = durationMs)
        try { context.startService(Intent(context, HycoPlaybackService::class.java)) } catch (_: Exception) {}

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setDisplayTitle(title)
            .setArtist(artist)
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
        val dataSourceFactory = DefaultDataSource.Factory(context)

        // 1. master.m3u8 によるネイティブ HLS VOD 再生 (シーク完全対応)
        if (!masterPlaylistPath.isNullOrBlank() && File(masterPlaylistPath).exists()) {
            val item = MediaItem.Builder().setUri(android.net.Uri.fromFile(File(masterPlaylistPath))).setMediaMetadata(metadata).build()
            val hlsSource = HlsMediaSource.Factory(dataSourceFactory).setAllowChunklessPreparation(true).createMediaSource(item)
            exoPlayer.setMediaSource(hlsSource)
            exoPlayer.prepare()
            exoPlayer.play()
            return
        }

        // 2. レガシー環境向けフォールバック (MergingMediaSource)
        val progressiveFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        val videoSource = videoFilePath?.let { progressiveFactory.createMediaSource(MediaItem.Builder().setUri(android.net.Uri.fromFile(File(it))).setMediaMetadata(metadata).build()) }
        val audioSource = audioFilePath?.let { progressiveFactory.createMediaSource(MediaItem.Builder().setUri(android.net.Uri.fromFile(File(it))).setMediaMetadata(metadata).build()) }

        val finalSource = when {
            videoSource != null && audioSource != null -> MergingMediaSource(videoSource, audioSource)
            videoSource != null -> videoSource
            audioSource != null -> audioSource
            else -> return
        }

        exoPlayer.setMediaSource(finalSource)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun pause() = exoPlayer.pause()

    /**
     * 再生を開始・再開する。
     * 再生終了状態 (STATE_ENDED) や末尾到達時、およびエラー停止状態からの再試行時は先頭位置または再prepareを行う。
     */
    fun play() {
        val duration = exoPlayer.duration
        val isAtEnd = exoPlayer.playbackState == Player.STATE_ENDED ||
            (duration > 0L && exoPlayer.currentPosition >= duration)
        if (isAtEnd) {
            exoPlayer.seekTo(0L)
        }
        if (exoPlayer.playbackState == Player.STATE_IDLE && exoPlayer.mediaItemCount > 0) {
            exoPlayer.prepare()
        }
        exoPlayer.play()
        updateState()
    }

    fun togglePlayPause() = if (exoPlayer.isPlaying) pause() else play()

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs.coerceAtLeast(0L))
        updateState()
    }

    fun seekBy(deltaMs: Long) {
        val totalDur = getResolvedDuration()
        val maxTarget = if (totalDur > 0L) totalDur else Long.MAX_VALUE
        seekTo((exoPlayer.currentPosition + deltaMs).coerceIn(0L, maxTarget))
    }

    val isRepeatOne: Boolean get() = exoPlayer.repeatMode == Player.REPEAT_MODE_ONE
    val currentPlaybackSpeed: Float get() = exoPlayer.playbackParameters.speed

    fun setPlaybackSpeed(speed: Float) = exoPlayer.setPlaybackSpeed(speed.coerceIn(0.25f, 4.0f))
    fun setRepeatOne(enabled: Boolean) {
        exoPlayer.repeatMode = if (enabled) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun stop() {
        stopPositionPolling()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _playbackState.value = PlaybackState.Idle
    }

    fun release() {
        stopPositionPolling()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        _playbackState.value = PlaybackState.Idle
    }

    private fun getResolvedDuration(): Long {
        val dur = exoPlayer.duration
        return if (dur > 0L) dur else fallbackDurationMs
    }

    private fun updateState() {
        when (exoPlayer.playbackState) {
            Player.STATE_IDLE -> {
                if (_playbackState.value !is PlaybackState.Error) _playbackState.value = PlaybackState.Idle
            }
            Player.STATE_BUFFERING -> {
                val cur = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = getResolvedDuration().let { if (it > 0L) it else _playbackState.value.durationMs }
                _playbackState.value = PlaybackState.Buffering(currentPositionMs = cur, durationMs = dur)
            }
            Player.STATE_READY -> {
                _playbackState.value = PlaybackState.Ready(
                    isPlaying = exoPlayer.isPlaying,
                    currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                    durationMs = getResolvedDuration(),
                    bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L)
                )
            }
            Player.STATE_ENDED -> {
                stopPositionPolling()
                _playbackState.value = PlaybackState.Ended
            }
        }
    }

    private fun startPositionPolling() {
        stopPositionPolling()
        pollJob = scope.launch {
            while (isActive) {
                if (exoPlayer.playbackState == Player.STATE_READY) {
                    _playbackState.value = PlaybackState.Ready(
                        isPlaying = exoPlayer.isPlaying,
                        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                        durationMs = getResolvedDuration(),
                        bufferedPositionMs = exoPlayer.bufferedPosition.coerceAtLeast(0L)
                    )
                }
                delay(POSITION_POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopPositionPolling() {
        pollJob?.cancel()
        pollJob = null
    }
}
