package com.github.stm7128.hycohyco.player.model

/**
 * プレイヤーの再生状態を表す sealed interface
 */
sealed interface PlaybackState {
    /** 未準備・アイドル状態 */
    data object Idle : PlaybackState

    /** 動画・プレイリストの読み込み・バッファリング中 */
    data class Buffering(
        val currentPositionMs: Long = 0L,
        val durationMs: Long = 0L
    ) : PlaybackState

    /**
     * 再生中または一時停止中（動画の準備完了）
     *
     * @property isPlaying 再生中かどうか
     * @property currentPositionMs 現在の再生位置 (ミリ秒)
     * @property durationMs 動画全体の長さ (ミリ秒)
     * @property bufferedPositionMs バッファ済み位置 (ミリ秒)
     */
    data class Ready(
        val isPlaying: Boolean,
        val currentPositionMs: Long,
        val durationMs: Long,
        val bufferedPositionMs: Long
    ) : PlaybackState

    /** 再生終了 */
    data object Ended : PlaybackState

    /**
     * 再生エラー発生
     *
     * @property message エラーメッセージ
     */
    data class Error(val message: String) : PlaybackState
}

/** 現在再生中かどうか */
val PlaybackState.isPlaying: Boolean
    get() = (this as? PlaybackState.Ready)?.isPlaying == true

/** バッファリング中かどうか */
val PlaybackState.isBuffering: Boolean
    get() = this is PlaybackState.Buffering

/** 現在の再生位置 (ミリ秒) */
val PlaybackState.currentPositionMs: Long
    get() = when (this) {
        is PlaybackState.Ready -> currentPositionMs
        is PlaybackState.Buffering -> currentPositionMs
        else -> 0L
    }

/** 動画全体の長さ (ミリ秒) */
val PlaybackState.durationMs: Long
    get() = when (this) {
        is PlaybackState.Ready -> durationMs
        is PlaybackState.Buffering -> durationMs
        else -> 0L
    }

/** バッファ済み位置 (ミリ秒) */
val PlaybackState.bufferedPositionMs: Long
    get() = (this as? PlaybackState.Ready)?.bufferedPositionMs ?: 0L

