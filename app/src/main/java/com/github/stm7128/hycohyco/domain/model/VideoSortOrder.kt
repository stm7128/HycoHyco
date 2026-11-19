package com.github.stm7128.hycohyco.domain.model

/**
 * ニコニコ公式準拠の動画一覧並び替え（ソート）順序モデル
 *
 * ユーザー投稿動画（14項目）およびマイリスト・あとで見る（20項目）の全並び替えに対応します。
 *
 * @property defaultLabel 標準のUI表示名
 */
enum class VideoSortOrder(val defaultLabel: String) {
    // --- マイリスト・あとで見る専用 ---
    ADDED_AT_DESC("マイリスト登録が新しい順"),
    ADDED_AT_ASC("マイリスト登録が古い順"),
    TITLE_ASC("タイトル昇順"),
    TITLE_DESC("タイトル降順"),
    MEMO_ASC("メモ昇順"),
    MEMO_DESC("メモ降順"),

    // --- 共通 (ユーザー投稿動画 & マイリスト・あとで見る) ---
    REGISTERED_AT_DESC("投稿日時が新しい順"),
    REGISTERED_AT_ASC("投稿日時が古い順"),
    VIEW_COUNT_DESC("再生数が多い順"),
    VIEW_COUNT_ASC("再生数が少ない順"),
    COMMENT_TIME_DESC("コメントが新しい順"),
    COMMENT_TIME_ASC("コメントが古い順"),
    COMMENT_COUNT_DESC("コメント数が多い順"),
    COMMENT_COUNT_ASC("コメント数が少ない順"),
    LIKE_COUNT_DESC("いいね！数が多い順"),
    LIKE_COUNT_ASC("いいね！数が少ない順"),
    MYLIST_COUNT_DESC("マイリスト数が多い順"),
    MYLIST_COUNT_ASC("マイリスト数が少ない順"),
    DURATION_DESC("再生時間が長い順"),
    DURATION_ASC("再生時間が短い順");

    /**
     * 表示コンテキストに応じた適切なラベルを返します
     * （「あとで見る」画面では「マイリスト登録」を「登録」に差し替えます）
     */
    fun getLabel(isWatchLater: Boolean = false): String {
        return if (isWatchLater) {
            when (this) {
                ADDED_AT_DESC -> "登録が新しい順"
                ADDED_AT_ASC -> "登録が古い順"
                else -> defaultLabel
            }
        } else {
            defaultLabel
        }
    }

    companion object {
        /** ユーザー投稿動画用の並び替え項目一覧（全14項目） */
        val USER_VIDEO_SORT_ORDERS = listOf(
            REGISTERED_AT_DESC,
            REGISTERED_AT_ASC,
            VIEW_COUNT_DESC,
            VIEW_COUNT_ASC,
            COMMENT_TIME_DESC,
            COMMENT_TIME_ASC,
            COMMENT_COUNT_DESC,
            COMMENT_COUNT_ASC,
            LIKE_COUNT_DESC,
            LIKE_COUNT_ASC,
            MYLIST_COUNT_DESC,
            MYLIST_COUNT_ASC,
            DURATION_DESC,
            DURATION_ASC
        )

        /** マイリスト・あとで見る用の並び替え項目一覧（全20項目） */
        val MYLIST_SORT_ORDERS = listOf(
            ADDED_AT_DESC,
            ADDED_AT_ASC,
            TITLE_ASC,
            TITLE_DESC,
            MEMO_ASC,
            MEMO_DESC,
            REGISTERED_AT_DESC,
            REGISTERED_AT_ASC,
            VIEW_COUNT_DESC,
            VIEW_COUNT_ASC,
            COMMENT_TIME_DESC,
            COMMENT_TIME_ASC,
            COMMENT_COUNT_DESC,
            COMMENT_COUNT_ASC,
            LIKE_COUNT_DESC,
            LIKE_COUNT_ASC,
            MYLIST_COUNT_DESC,
            MYLIST_COUNT_ASC,
            DURATION_DESC,
            DURATION_ASC
        )
    }
}

/**
 * 動画一覧に対して指定されたソート順序を適用して返します
 */
fun List<VideoSummary>.applySort(sortOrder: VideoSortOrder): List<VideoSummary> {
    return when (sortOrder) {
        VideoSortOrder.ADDED_AT_DESC -> this
        VideoSortOrder.ADDED_AT_ASC -> this.reversed()
        VideoSortOrder.TITLE_ASC -> this.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        VideoSortOrder.TITLE_DESC -> this.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.title })
        VideoSortOrder.MEMO_ASC -> this.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        VideoSortOrder.MEMO_DESC -> this.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.title })
        VideoSortOrder.REGISTERED_AT_DESC -> this.sortedByDescending { it.startTime }
        VideoSortOrder.REGISTERED_AT_ASC -> this.sortedBy { it.startTime }
        VideoSortOrder.VIEW_COUNT_DESC -> this.sortedByDescending { it.viewCounter }
        VideoSortOrder.VIEW_COUNT_ASC -> this.sortedBy { it.viewCounter }
        VideoSortOrder.COMMENT_TIME_DESC -> this.sortedByDescending { it.startTime }
        VideoSortOrder.COMMENT_TIME_ASC -> this.sortedBy { it.startTime }
        VideoSortOrder.COMMENT_COUNT_DESC -> this.sortedByDescending { it.commentCounter }
        VideoSortOrder.COMMENT_COUNT_ASC -> this.sortedBy { it.commentCounter }
        VideoSortOrder.LIKE_COUNT_DESC -> this.sortedByDescending { it.likeCounter }
        VideoSortOrder.LIKE_COUNT_ASC -> this.sortedBy { it.likeCounter }
        VideoSortOrder.MYLIST_COUNT_DESC -> this.sortedByDescending { it.mylistCounter }
        VideoSortOrder.MYLIST_COUNT_ASC -> this.sortedBy { it.mylistCounter }
        VideoSortOrder.DURATION_DESC -> this.sortedByDescending { it.lengthSeconds }
        VideoSortOrder.DURATION_ASC -> this.sortedBy { it.lengthSeconds }
    }
}
