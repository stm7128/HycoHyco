package com.github.stm7128.hycohyco.domain.model

/**
 * 視聴履歴アイテム
 *
 * @property videoId 動画 ID (例: "sm9")
 * @property title 動画タイトル
 * @property thumbnailUrl サムネイル画像 URL
 * @property durationMs 動画全体の長さ (ミリ秒)
 * @property lastPositionMs 最後に視聴していた再生位置 (ミリ秒)
 * @property lastWatchedAt 最終視聴日時 (エポックミリ秒)
 * @property ownerName 投稿者名
 */
data class WatchHistoryItem(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val durationMs: Long,
    val lastPositionMs: Long,
    val lastWatchedAt: Long,
    val ownerName: String = "",
    val viewCounter: Int = 0,
    val commentCounter: Int = 0,
    val likeCounter: Int = 0,
    val mylistCounter: Int = 0
) {
    /** 視聴進捗率 (0.0f 〜 1.0f) */
    val progressRatio: Float
        get() = if (durationMs > 0L) {
            (lastPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    /** 視聴完了済みとみなすか (残り5秒未満または95%以上) */
    val isCompleted: Boolean
        get() = (durationMs > 0L && lastPositionMs >= durationMs - 5000L) || progressRatio >= 0.95f
}

/**
 * ローカルプレイリスト情報
 *
 * @property id プレイリスト ID (UUID または一意キー)
 * @property title プレイリスト名
 * @property videoCount 登録されている動画件数
 * @property createdAt 作成日時 (エポックミリ秒)
 * @property updatedAt 更新日時 (エポックミリ秒)
 */
data class LocalPlaylist(
    val id: String,
    val title: String,
    val videoCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * プレイリスト内の動画アイテム
 */
data class PlaylistVideoItem(
    val playlistId: String,
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val durationMs: Long,
    val orderIndex: Int,
    val addedAt: Long = System.currentTimeMillis(),
    val ownerName: String = "",
    val viewCounter: Int = 0,
    val commentCounter: Int = 0,
    val likeCounter: Int = 0,
    val mylistCounter: Int = 0
)

/**
 * 検索履歴アイテム
 *
 * @property keyword 検索キーワードまたはタグ名
 * @property isPinned お気に入りピン留めされているか
 * @property searchedAt 最終検索日時 (エポックミリ秒)
 */
data class SearchHistoryItem(
    val keyword: String,
    val isPinned: Boolean = false,
    val searchedAt: Long = System.currentTimeMillis()
)
