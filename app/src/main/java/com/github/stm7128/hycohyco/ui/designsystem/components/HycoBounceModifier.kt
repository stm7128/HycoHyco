package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

/**
 * 物理スプリング・バウンス押下アニメーション Modifier
 *
 * タップ押下中にわずかに縮小 (0.96f) し、指を離した瞬間にバネの心地よい物理挙動で元に戻る。
 * Xiaomi HyperOS / M3 Expressive ライクな極上の触感を提供する。
 *
 * @param enabled クリックおよびアニメーションが有効かどうか
 * @param pressedScale 押下時の最小スケール (デフォルト: 0.96f)
 * @param onClick タップ時コールバック
 */
fun Modifier.hycoBounceClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "hycoBounceScale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null, // リップルとバウンスの二重演出を防ぐためカスタムインジケーション
            enabled = enabled,
            onClick = onClick
        )
}
