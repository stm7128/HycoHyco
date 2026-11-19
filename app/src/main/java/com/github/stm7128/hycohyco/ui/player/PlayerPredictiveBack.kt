package com.github.stm7128.hycohyco.ui.player

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * 展開プレイヤー向け Predictive Back (予測型戻る) 状態
 *
 * Android 14/15 端末のシステム戻るスワイプジェスチャーに追従し、
 * スワイプ進行度に応じてプレイヤー全体のスムーズな縮小・角丸変形・位置移動を制御する。
 */
class PlayerPredictiveBackState {
    val scale = Animatable(1f)
    val cornerRadius = Animatable(0f)
    val translationY = Animatable(0f)

    /**
     * ジェスチャー進行度 (0.0f 〜 1.0f) に応じて視覚効果をリアルタイム更新する
     */
    suspend fun update(progress: Float) {
        val targetScale = 1f - (progress * 0.12f)
        val targetRadius = progress * 28f
        val targetY = progress * 36f
        scale.snapTo(targetScale)
        cornerRadius.snapTo(targetRadius)
        translationY.snapTo(targetY)
    }

    /**
     * アニメーション状態を初期状態へスムーズに復帰する
     */
    suspend fun reset() {
        scale.animateTo(1f, spring())
        cornerRadius.animateTo(0f, spring())
        translationY.animateTo(0f, spring())
    }
}

/**
 * [PlayerPredictiveBackState] のインスタンスを保持する Composable
 */
@Composable
fun rememberPlayerPredictiveBackState(): PlayerPredictiveBackState {
    return remember { PlayerPredictiveBackState() }
}

/**
 * 展開プレイヤー用の Predictive Back ハンドラー
 *
 * @param enabled ハンドラーを有効化するか否か（フルスクリーン時や非展開時は無効）
 * @param state 視覚アニメーション状態
 * @param onComplete スワイプが完了した際のコールバック（ミニプレイヤー化）
 */
@Composable
fun PlayerPredictiveBackHandler(
    enabled: Boolean,
    state: PlayerPredictiveBackState,
    onComplete: () -> Unit
) {
    PredictiveBackHandler(enabled = enabled) { progress: Flow<BackEventCompat> ->
        try {
            progress.collect { backEvent ->
                state.update(backEvent.progress)
            }
            // スワイプが確定して完了した際
            state.reset()
            onComplete()
        } catch (e: CancellationException) {
            // ユーザーが画面端へ指を戻してキャンセルした際
            state.reset()
        }
    }
}

/**
 * [PlayerPredictiveBackState] の進行度に応じたスケール・角丸・位置変形を適用する修飾子
 */
fun Modifier.playerPredictiveBackGraphics(state: PlayerPredictiveBackState): Modifier = this
    .graphicsLayer {
        scaleX = state.scale.value
        scaleY = state.scale.value
        translationY = state.translationY.value
        val radius = state.cornerRadius.value
        clip = radius > 0.5f
        if (radius > 0.5f) {
            shape = RoundedCornerShape(radius.dp)
        }
    }
