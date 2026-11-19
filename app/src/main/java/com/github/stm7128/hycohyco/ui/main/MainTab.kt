package com.github.stm7128.hycohyco.ui.main

import com.github.stm7128.hycohyco.ui.designsystem.components.HycoNavIcon

/**
 * メイン画面のナビゲーションタブ定義
 *
 * 各タブに対応する HyperOS / Material You のペアアイコンとタイトルを持つ。
 */
enum class MainTab(
    val title: String,
    val icon: HycoNavIcon
) {
    HOME("ホーム", HycoNavIcon.Home),
    SHORTS("ショート", HycoNavIcon.Shorts),
    SEARCH("検索", HycoNavIcon.Search),
    LIBRARY("ライブラリ", HycoNavIcon.Library),
    SETTINGS("設定", HycoNavIcon.Settings)
}
