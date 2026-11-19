package com.github.stm7128.hycohyco.player.gesture

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 画面ジェスチャー（輝度・音量・ダブルタップシーク・長押し倍速）を検知・処理するオーバーレイコンテナ
 */
@Composable
fun PlayerGestureBox(
    onToggleControls: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    currentSpeed: Float = 1.0f,
    gestureMode: PlayerGestureMode = PlayerGestureMode.FULLSCREEN_ONLY,
    isFullscreen: Boolean = false,
    onMinimize: (() -> Unit)? = null,
    doubleTapSeekSeconds: Int = 10,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var hideJob by remember { mutableStateOf<Job?>(null) }
    var overlayState by remember { mutableStateOf<PlayerGestureOverlay>(PlayerGestureOverlay.None) }

    // 長押し2倍速状態管理 (通常タップでの 1.0x リセット防止 & 前の再生速度退避)
    var isLongPress2xActive by remember { mutableStateOf(false) }
    var speedBeforeLongPress by remember { mutableFloatStateOf(1.0f) }
    val currentSpeedState by rememberUpdatedState(currentSpeed)

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val activity = remember { context as? Activity }

    // 画面離脱時に画面輝度オーバーライドを確実に解除 (OS設定値へ復帰)
    DisposableEffect(Unit) {
        onDispose {
            activity?.let { act ->
                val lp = act.window.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                act.window.attributes = lp
            }
        }
    }

    // ドラッグ中の初期値保持
    var initialBrightness by remember { mutableFloatStateOf(0.5f) }
    var initialVolume by remember { mutableFloatStateOf(0f) }
    var dragStartX by remember { mutableFloatStateOf(0f) }
    var isDragAllowed by remember { mutableStateOf(false) }
    var verticalDragAccumulator by remember { mutableFloatStateOf(0f) }

    fun showOverlayWithTimeout(overlay: PlayerGestureOverlay, timeoutMs: Long = 1000L) {
        overlayState = overlay
        hideJob?.cancel()
        hideJob = coroutineScope.launch {
            delay(timeoutMs)
            overlayState = PlayerGestureOverlay.None
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // 1. タップ・ダブルタップ・長押し検知
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onToggleControls() },
                    onDoubleTap = { offset ->
                        val isLeftSide = offset.x < size.width / 2f
                        val delta = if (isLeftSide) -(doubleTapSeekSeconds * 1000L) else +(doubleTapSeekSeconds * 1000L)
                        onSeekBy(delta)
                        showOverlayWithTimeout(
                            PlayerGestureOverlay.Seek(if (isLeftSide) -doubleTapSeekSeconds else doubleTapSeekSeconds),
                            timeoutMs = 600L
                        )
                    },
                    onLongPress = {
                        speedBeforeLongPress = currentSpeedState
                        isLongPress2xActive = true
                        onSpeedChange(2.0f)
                        hideJob?.cancel()
                        overlayState = PlayerGestureOverlay.Speed2x
                    },
                    onPress = {
                        val released = tryAwaitRelease()
                        if (released && isLongPress2xActive) {
                            isLongPress2xActive = false
                            onSpeedChange(speedBeforeLongPress)
                            if (overlayState is PlayerGestureOverlay.Speed2x) {
                                overlayState = PlayerGestureOverlay.None
                            }
                        }
                    }
                )
            }
            // 2. 上下ドラッグ（左半分: 画面輝度, 右半分: メディア音量 / 縦画面: 下スワイプ最小化）検知
            .pointerInput(gestureMode, isFullscreen, onMinimize) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        verticalDragAccumulator = 0f
                        // ジェスチャーモード判定
                        val isModeEnabled = when (gestureMode) {
                            PlayerGestureMode.ALWAYS -> true
                            PlayerGestureMode.FULLSCREEN_ONLY -> isFullscreen
                            PlayerGestureMode.DISABLED -> false
                        }
                        // シークバー付近（画面下部 22%）からのドラッグは誤爆防止のため無効化
                        val isAboveSeekBar = offset.y < size.height * 0.78f
                        isDragAllowed = isModeEnabled && isAboveSeekBar

                        if (!isDragAllowed) return@detectVerticalDragGestures

                        dragStartX = offset.x
                        val currentWindowBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
                        initialBrightness = if (currentWindowBrightness >= 0f) {
                            currentWindowBrightness
                        } else {
                            runCatching {
                                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128) / 255f
                            }.getOrDefault(0.5f)
                        }

                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
                        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                        initialVolume = (curVol / maxVol.coerceAtLeast(1f)).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        verticalDragAccumulator = 0f
                        if (isDragAllowed) {
                            showOverlayWithTimeout(overlayState, timeoutMs = 600L)
                        }
                        isDragAllowed = false
                    },
                    onDragCancel = {
                        verticalDragAccumulator = 0f
                        overlayState = PlayerGestureOverlay.None
                        isDragAllowed = false
                    },
                    onVerticalDrag = { change, dragAmount ->
                        // 1. 縦画面時の下スワイプ最小化検知 (下向きドラッグ累計が 45dp 相当以上で発火)
                        if (!isFullscreen && onMinimize != null) {
                            verticalDragAccumulator = (verticalDragAccumulator + dragAmount).coerceAtLeast(0f)
                            if (verticalDragAccumulator > with(density) { 45.dp.toPx() }) {
                                change.consume()
                                verticalDragAccumulator = 0f
                                overlayState = PlayerGestureOverlay.None
                                onMinimize()
                                return@detectVerticalDragGestures
                            }
                            if (verticalDragAccumulator > with(density) { 12.dp.toPx() }) {
                                // 下スワイプ中は音量・輝度ジェスチャーを発動させない (M-05)
                                return@detectVerticalDragGestures
                            }
                        }

                        if (!isDragAllowed) return@detectVerticalDragGestures

                        change.consume()
                        val isLeftSide = dragStartX < size.width / 2f
                        val deltaRatio = -dragAmount / (size.height * 0.75f)

                        if (isLeftSide) {
                            val newBrightness = (initialBrightness + deltaRatio).coerceIn(0.01f, 1.0f)
                            initialBrightness = newBrightness
                            activity?.let { act ->
                                val lp = act.window.attributes
                                lp.screenBrightness = newBrightness
                                act.window.attributes = lp
                            }
                            overlayState = PlayerGestureOverlay.Brightness((newBrightness * 100).roundToInt())
                        } else {
                            val newVolume = (initialVolume + deltaRatio).coerceIn(0f, 1f)
                            initialVolume = newVolume
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val targetVol = (newVolume * maxVol).roundToInt().coerceIn(0, maxVol)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                            overlayState = PlayerGestureOverlay.Volume((newVolume * 100).roundToInt())
                        }
                    }
                )
            }
    ) {
        content()

        // ジェスチャーフィードバック HUD
        PlayerGestureHud(overlayState = overlayState)
    }
}
