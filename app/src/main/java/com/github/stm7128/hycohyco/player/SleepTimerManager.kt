package com.github.stm7128.hycohyco.player

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

/**
 * スリープタイマーのモード
 */
enum class SleepTimerMode(val label: String, val minutes: Int?) {
    OFF("オフ", null),
    MIN_15("15分", 15),
    MIN_30("30分", 30),
    MIN_60("60分", 60),
    END_OF_VIDEO("この動画の終了時", null)
}

/**
 * スリープタイマー管理マネージャー
 */
class SleepTimerManager(
    private val playbackManager: NicoPlaybackManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private val _currentMode = MutableStateFlow(SleepTimerMode.OFF)
    val currentMode: StateFlow<SleepTimerMode> = _currentMode.asStateFlow()

    private val _remainingSeconds = MutableStateFlow<Int?>(null)
    val remainingSeconds: StateFlow<Int?> = _remainingSeconds.asStateFlow()

    fun setTimer(mode: SleepTimerMode) {
        timerJob?.cancel()
        timerJob = null
        _currentMode.value = mode

        val minutes = mode.minutes
        if (minutes == null) {
            _remainingSeconds.value = null
            return
        }

        val totalSeconds = minutes * 60
        _remainingSeconds.value = totalSeconds

        timerJob = scope.launch {
            var currentSec = totalSeconds
            while (isActive && currentSec > 0) {
                delay(1000L)
                currentSec--
                _remainingSeconds.value = currentSec
            }
            playbackManager.pause()
            _currentMode.value = SleepTimerMode.OFF
            _remainingSeconds.value = null
        }
    }

    /**
     * 動画が終了した際に呼ばれるフック
     */
    fun onVideoEnded() {
        if (_currentMode.value == SleepTimerMode.END_OF_VIDEO) {
            playbackManager.pause()
            _currentMode.value = SleepTimerMode.OFF
            _remainingSeconds.value = null
        }
    }

    fun cancelTimer() {
        setTimer(SleepTimerMode.OFF)
    }
}
