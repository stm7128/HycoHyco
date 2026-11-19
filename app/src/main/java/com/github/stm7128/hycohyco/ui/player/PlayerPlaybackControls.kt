package com.github.stm7128.hycohyco.ui.player

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.player.danmaku.DanmakuAreaMode
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PlayerViewModel の再生キュー・ジェスチャー設定・弾幕・NG 設定関連の操作拡張
 */
fun PlayerViewModel.setPlaybackQueue(queue: List<String>) {
    updatePlayerUiState { it.copy(playbackQueue = queue) }
}

fun PlayerViewModel.playNextInQueue(context: Context? = null) {
    val currentId = uiState.value.videoDetail?.id ?: return
    val q = uiState.value.playbackQueue
    val idx = q.indexOf(currentId)
    if (idx in 0 until q.size - 1) playVideo(q[idx + 1], context)
}

private fun PlayerViewModel.persistDanmakuSettings(
    settings: DanmakuDisplaySettings,
    debounceMs: Long = 0L
) {
    updatePlayerUiState { it.copy(danmakuSettings = settings) }
    danmakuSaveJob?.cancel()
    danmakuSaveJob = viewModelScope.launch {
        if (debounceMs > 0L) delay(debounceMs)
        playerPreferencesDataSource?.setDanmakuSettings(settings)
    }
}

fun PlayerViewModel.toggleDanmaku() {
    val current = uiState.value.danmakuSettings
    persistDanmakuSettings(current.copy(isEnabled = !current.isEnabled))
}

fun PlayerViewModel.setDanmakuAlpha(alpha: Float) {
    val current = uiState.value.danmakuSettings
    persistDanmakuSettings(current.copy(alpha = alpha.coerceIn(0.1f, 1.0f)), debounceMs = 150L)
}

fun PlayerViewModel.setDanmakuScale(scale: Float) {
    val current = uiState.value.danmakuSettings
    persistDanmakuSettings(current.copy(scale = scale.coerceIn(0.5f, 2.0f)))
}

fun PlayerViewModel.setDanmakuSpeedScale(speedScale: Float) {
    val current = uiState.value.danmakuSettings
    persistDanmakuSettings(current.copy(speedScale = speedScale.coerceIn(0.5f, 2.0f)))
}

fun PlayerViewModel.setDanmakuAreaMode(areaMode: DanmakuAreaMode) {
    val current = uiState.value.danmakuSettings
    persistDanmakuSettings(current.copy(areaMode = areaMode))
}

fun PlayerViewModel.updateDanmakuSettings(newSettings: DanmakuDisplaySettings) {
    val prev = uiState.value.danmakuSettings
    // スライダー（不透明度）のみの連続変更時はドラッグ中の DataStore I/O 競合を防ぐためデバウンス保存する
    val isAlphaOnlyChange = prev.isEnabled == newSettings.isEnabled &&
        prev.scale == newSettings.scale &&
        prev.speedScale == newSettings.speedScale &&
        prev.areaMode == newSettings.areaMode &&
        prev.alpha != newSettings.alpha
    persistDanmakuSettings(newSettings, debounceMs = if (isAlphaOnlyChange) 150L else 0L)
}

fun PlayerViewModel.updateNgConfig(newConfig: CommentNgConfig) {
    viewModelScope.launch { ngPreferencesDataSource.saveNgConfig(newConfig) }
}

fun PlayerViewModel.addNgUserId(userId: String) {
    viewModelScope.launch { ngPreferencesDataSource.addNgUserId(userId) }
}

fun PlayerViewModel.addNgWord(word: String) {
    viewModelScope.launch { ngPreferencesDataSource.addNgWord(word) }
}

fun PlayerViewModel.setGestureMode(mode: PlayerGestureMode) {
    viewModelScope.launch { playerPreferencesDataSource?.setGestureMode(mode) }
}

fun PlayerViewModel.setDoubleTapSeekSeconds(seconds: Int) {
    viewModelScope.launch { playerPreferencesDataSource?.setDoubleTapSeekSeconds(seconds) }
}

fun PlayerViewModel.setUseNicovideoGay(enabled: Boolean) {
    viewModelScope.launch { playerPreferencesDataSource?.setUseNicovideoGay(enabled) }
}

fun PlayerViewModel.setSleepTimerMode(mode: com.github.stm7128.hycohyco.player.SleepTimerMode) {
    sleepTimerManager?.setTimer(mode)
}

fun PlayerViewModel.setCoexistAudio(enabled: Boolean) {
    viewModelScope.launch { playerPreferencesDataSource?.setCoexistAudio(enabled) }
}

fun PlayerViewModel.setAutoEnterPip(enabled: Boolean) {
    viewModelScope.launch { playerPreferencesDataSource?.setAutoEnterPip(enabled) }
}

fun PlayerViewModel.setResumePlaybackEnabled(enabled: Boolean) {
    viewModelScope.launch { playerPreferencesDataSource?.setResumePlaybackEnabled(enabled) }
}
