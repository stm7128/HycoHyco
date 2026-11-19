package com.github.stm7128.hycohyco.domain.model

/**
 * 検索ターゲット種別
 *
 * @property apiValue スナップショット検索 API の targets パラメータ値
 * @property displayName UI 表示用ラベル
 */
enum class SearchTarget(val apiValue: String, val displayName: String) {
    KEYWORD("title,description,tags", "キーワード"),
    TAG_EXACT("tagsExact", "タグ"),
    SHORTS("short", "ショート"),
    USER("user", "ユーザー")
}

/**
 * 検索・ランキングのソート順 (スナップショット検索 API v2 準拠 全14項目)
 *
 * @property apiValue スナップショット検索 API の _sort パラメータ値
 * @property displayName UI 表示用ラベル
 */
enum class SearchSort(val apiValue: String, val displayName: String) {
    START_TIME_DESC("-startTime", "投稿日時が新しい順"),
    START_TIME_ASC("+startTime", "投稿日時が古い順"),
    VIEW_COUNT("-viewCounter", "再生数が多い順"),
    VIEW_COUNT_ASC("+viewCounter", "再生数が少ない順"),
    COMMENT_TIME_DESC("-lastCommentTime", "コメントが新しい順"),
    COMMENT_TIME_ASC("+lastCommentTime", "コメントが古い順"),
    COMMENT_COUNT("-commentCounter", "コメント数が多い順"),
    COMMENT_COUNT_ASC("+commentCounter", "コメント数が少ない順"),
    LIKE_COUNT("-likeCounter", "いいね！数が多い順"),
    LIKE_COUNT_ASC("+likeCounter", "いいね！数が少ない順"),
    MYLIST_COUNT("-mylistCounter", "マイリスト数が多い順"),
    MYLIST_COUNT_ASC("+mylistCounter", "マイリスト数が少ない順"),
    DURATION_DESC("-lengthSeconds", "再生時間が長い順"),
    DURATION_ASC("+lengthSeconds", "再生時間が短い順");

    companion object {
        val VIEW_COUNT_DESC = VIEW_COUNT
        val START_TIME = START_TIME_DESC
    }
}

/**
 * ホーム画面で表示するランキング・トレンドジャンル
 *
 * @property genreTag ニコニコ動画のカテゴリタグ名
 * @property displayName UI 表示用ラベル
 */
enum class RankingGenre(val genreTag: String, val displayName: String) {
    ALL("ニコニコ動画", "総合"),
    ENTERTAINMENT("エンターテイメント", "エンタメ"),
    MUSIC("音楽・サウンド", "音楽"),
    GAME("ゲーム", "ゲーム"),
    ANIME("アニメ", "アニメ"),
    VOCALOID("VOCALOID", "VOCALOID"),
    COMMENTARY("解説・講座", "解説・講座"),
    OTHER("例のアレ", "例のアレ")
}

/**
 * ランキングの集計期間
 *
 * @property displayName UI 表示用ラベル
 * @property daysAgo 何日前以降の投稿を対象とするか (null の場合は全期間)
 */
enum class RankingTerm(val displayName: String, val daysAgo: Int?) {
    HOT("急上昇", 3),
    WEEKLY("週間", 7),
    MONTHLY("月間", 30),
    TOTAL("全期間", null)
}
