package com.github.stm7128.hycohyco.domain.model

/**
 * ホーム画面「おすすめ」タブの個別動画項目
 *
 * @property video 動画サマリー情報（4大指標・投稿者情報を含む）
 * @property reasonTag レコメンド選出理由のタグ名（存在する場合）
 */
data class HomeRecommendVideo(
    val video: VideoSummary,
    val reasonTag: String? = null
)

/**
 * ホーム画面「おすすめ」タブのフィード全体モデル
 *
 * @property videos おすすめ動画一覧
 * @property recommendedTags おすすめ動画群から出現頻度順に抽出した上位タグ一覧
 */
data class HomeRecommendFeed(
    val videos: List<HomeRecommendVideo> = emptyList(),
    val recommendedTags: List<String> = emptyList()
)
