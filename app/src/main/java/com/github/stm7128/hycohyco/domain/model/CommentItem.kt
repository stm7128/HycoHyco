package com.github.stm7128.hycohyco.domain.model

/**
 * コメントの表示位置
 */
enum class CommentPosition {
    /** 画面右から左へ流れる標準コメント */
    NAKA,

    /** 画面上部に固定表示されるコメント */
    UE,

    /** 画面下部に固定表示されるコメント */
    SHITA,
}

/**
 * コメントの文字サイズ
 */
enum class CommentSize {
    /** 標準サイズ (仮想キャンバス高さ 27px) */
    MEDIUM,

    /** 大文字 (仮想キャンバス高さ 39px) */
    BIG,

    /** 小文字 (仮想キャンバス高さ 18px) */
    SMALL,
}

/**
 * コメントフォント種別
 */
enum class CommentFont {
    /** 標準フォント (角ゴシック) */
    DEFONT,

    /** 明朝体 */
    MINCHO,
}

/**
 * コメントが属するスレッド種別
 */
enum class CommentFork {
    /** 投稿者コメント */
    OWNER,

    /** 通常コメント */
    MAIN,

    /** かんたんコメント */
    EASY,
}

/**
 * コメント1件を表すドメインモデル
 *
 * @property id コメントID
 * @property no コメント連番
 * @property vposMs 動画再生位置 (ミリ秒)
 * @property body コメント本文
 * @property position 表示位置 (NAKA / UE / SHITA)
 * @property size 文字サイズ (MEDIUM / BIG / SMALL)
 * @property colorInt 描画色 (ARGB 32bit 整数)
 * @property font フォント種別
 * @property isTranslucent 半透明描画フラグ
 * @property is184 匿名投稿フラグ
 * @property fork スレッド種別 (投稿者 / 通常 / かんたん)
 * @property userId 投稿ユーザーID
 * @property score 荒らし判定等のNGスコア
 * @property nicoruCount ニコル数
 */
data class CommentItem(
    val id: String,
    val no: Long,
    val vposMs: Long,
    val body: String,
    val position: CommentPosition = CommentPosition.NAKA,
    val size: CommentSize = CommentSize.MEDIUM,
    val colorInt: Int = 0xFFFFFFFF.toInt(),
    val font: CommentFont = CommentFont.DEFONT,
    val isTranslucent: Boolean = false,
    val is184: Boolean = false,
    val fork: CommentFork = CommentFork.MAIN,
    val userId: String = "",
    val score: Int = 0,
    val nicoruCount: Int = 0,
    val nicoruId: String? = null,
    val isMyComment: Boolean = false
)
