package com.github.stm7128.hycohyco.domain.model

/**
 * コメント NG 判定の設定
 *
 * @property minScoreThreshold 許容する最小 NG スコア（これ未満のスコアは非表示）
 * @property ngWords 除外するキーワード（部分一致）
 * @property ngRegexes 除外する正規表現パターン
 * @property ngUserIds 除外するユーザーIDリスト
 * @property hide184Comments 184（匿名）コメントを非表示にするか
 */
data class CommentNgConfig(
    val minScoreThreshold: Int = -1000,
    val ngWords: List<String> = emptyList(),
    val ngRegexes: List<Regex> = emptyList(),
    val ngUserIds: List<String> = emptyList(),
    val hide184Comments: Boolean = false
)

/**
 * ニコニココメントの NG 判定エンジン
 */
object CommentNgFilter {

    /**
     * 単一のコメントが NG ルールに抵触しているか判定する
     *
     * @param comment 対象コメント
     * @param config NG 設定
     * @return NG に該当する場合は true
     */
    fun isNg(comment: CommentItem, config: CommentNgConfig): Boolean {
        // 1. スコア閾値判定（スコアが閾値未満の場合は荒らしコメントとして除外）
        if (comment.score < config.minScoreThreshold) {
            return true
        }

        // 2. 184 非表示判定
        if (config.hide184Comments && comment.is184) {
            return true
        }

        // 3. ユーザー ID 判定
        if (comment.userId.isNotEmpty() && config.ngUserIds.contains(comment.userId)) {
            return true
        }

        // 4. NG ワード（部分一致）判定
        for (word in config.ngWords) {
            if (word.isNotEmpty() && comment.body.contains(word, ignoreCase = true)) {
                return true
            }
        }

        // 5. 正規表現判定
        for (regex in config.ngRegexes) {
            if (regex.containsMatchIn(comment.body)) {
                return true
            }
        }

        return false
    }

    /**
     * コメントリストに対して一括で NG フィルタを適用する
     *
     * @param comments 元のコメントリスト
     * @param config NG 設定
     * @return フィルタ通過後のコメントリスト
     */
    fun filter(comments: List<CommentItem>, config: CommentNgConfig): List<CommentItem> {
        return comments.filterNot { isNg(it, config) }
    }
}
