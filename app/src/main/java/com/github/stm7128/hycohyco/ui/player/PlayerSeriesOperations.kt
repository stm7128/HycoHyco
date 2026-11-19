package com.github.stm7128.hycohyco.ui.player

import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * プレイヤー画面におけるシリーズ巡回 & YouTube風次枠自動再生の操作拡張
 */

/**
 * 自動再生の有効/無効を切り替える
 */
fun PlayerViewModel.setAutoplayEnabled(enabled: Boolean) {
    viewModelScope.launch {
        playerPreferencesDataSource?.setAutoplayEnabled(enabled)
        updatePlayerUiState { it.copy(isAutoplayEnabled = enabled) }
    }
}

/**
 * 自動再生の有効/無効をトグル切り替えする
 */
fun PlayerViewModel.toggleAutoplay() {
    setAutoplayEnabled(!uiState.value.isAutoplayEnabled)
}

/**
 * 動画再生終了時に呼び出され、自動再生が有効であれば次枠カウントダウンを開始する
 */
fun PlayerViewModel.handleSeriesPlaybackEnded() {
    if (!uiState.value.isAutoplayEnabled) return
    val nextVideo = uiState.value.videoDetail?.series?.next ?: return
    startNextVideoCountdown()
}

/**
 * 次のシリーズ動画のカウントダウンを開始する
 *
 * @param durationSec カウントダウン秒数 (デフォルト: 5秒)
 * @param context Android コンテキスト
 */
fun PlayerViewModel.startNextVideoCountdown(durationSec: Int = 5, context: Context? = null) {
    seriesCountdownJob?.cancel()
    seriesCountdownJob = viewModelScope.launch {
        for (sec in durationSec downTo 1) {
            updatePlayerUiState { it.copy(nextVideoCountdownSeconds = sec) }
            delay(1000L)
        }
        updatePlayerUiState { it.copy(nextVideoCountdownSeconds = null) }
        playNextSeriesVideo(context)
    }
}

/**
 * 次の動画への自動再生カウントダウンを中止する
 */
fun PlayerViewModel.cancelNextVideoCountdown() {
    seriesCountdownJob?.cancel()
    seriesCountdownJob = null
    updatePlayerUiState { it.copy(nextVideoCountdownSeconds = null) }
}

/**
 * シリーズの次の動画を直ちに再生する
 */
fun PlayerViewModel.playNextSeriesVideo(context: Context? = null) {
    val nextVideo = uiState.value.videoDetail?.series?.next ?: return
    cancelNextVideoCountdown()
    playVideo(videoId = nextVideo.id, context = context)
}

/**
 * シリーズの前の動画を直ちに再生する
 */
fun PlayerViewModel.playPrevSeriesVideo(context: Context? = null) {
    val prevVideo = uiState.value.videoDetail?.series?.prev ?: return
    cancelNextVideoCountdown()
    playVideo(videoId = prevVideo.id, context = context)
}
