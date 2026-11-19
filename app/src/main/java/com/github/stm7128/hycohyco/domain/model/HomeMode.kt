package com.github.stm7128.hycohyco.domain.model

/**
 * ホーム画面の表示モード (おすすめ / ランキング / フォロー新着 / ショート)
 */
enum class HomeMode(val title: String) {
    RECOMMEND("おすすめ"),
    RANKING("ランキング"),
    FOLLOW("フォロー新着"),
    SHORTS("ショート");

    companion object {
        fun fromName(name: String?): HomeMode {
            return entries.firstOrNull { it.name == name } ?: RANKING
        }
    }
}
