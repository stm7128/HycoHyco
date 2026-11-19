package com.github.stm7128.hycohyco.domain.model

/**
 * いいね！操作結果ドメインモデル
 *
 * @property isLiked いいね状態 (true: いいね済み, false: 解除済み)
 * @property thanksMessage 投稿者からのお礼メッセージ (設定されていない場合は null)
 */
data class LikeResult(
    val isLiked: Boolean,
    val thanksMessage: String? = null
)
