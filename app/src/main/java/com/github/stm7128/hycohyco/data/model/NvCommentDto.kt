package com.github.stm7128.hycohyco.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * nvcomment API (/v1/threads) へのリクエスト DTO
 *
 * @property params 取得対象スレッドとターゲット情報
 * @property threadKey 認証用 JWT トークン (threadKey)
 * @property additionals 追加パラメータ（空オブジェクト）
 */
@Serializable
data class NvCommentRequestDto(
    val params: NvCommentParamsDto,
    val threadKey: String,
    val additionals: JsonObject? = null
)

/**
 * nvcomment API (/v1/threads) のルートレスポンス DTO
 */
@Serializable
data class NvCommentResponseRootDto(
    val meta: NvCommentMetaDto? = null,
    val data: NvCommentDataDto? = null
)

@Serializable
data class NvCommentMetaDto(
    val status: Int = 0
)

@Serializable
data class NvCommentDataDto(
    val threads: List<NvCommentThreadDto> = emptyList()
)

/**
 * スレッド情報 DTO (owner, main, easy)
 */
@Serializable
data class NvCommentThreadDto(
    val id: String = "",
    val fork: String = "",
    val commentCount: Int = 0,
    val comments: List<NvCommentItemDto> = emptyList()
)

/**
 * コメント単体の DTO
 *
 * @property id コメント固有ID
 * @property no コメント番号
 * @property vposMs 動画開始からの再生位置 (ミリ秒)
 * @property body コメント本文
 * @property commands コマンド配列 (例: ["184", "shita", "red", "big"])
 * @property userId 匿名化ユーザーIDまたは184ハッシュ
 * @property isPremium プレミアム会員かどうか
 * @property score 荒らし判定等のNGスコア（負の値が大きいほど荒らしリスク高）
 * @property postedAt 投稿日時 (ISO8601)
 * @property nicoruCount ニコル数
 */
@Serializable
data class NvCommentItemDto(
    val id: String = "",
    val no: Long = 0L,
    val vposMs: Long = 0L,
    val body: String = "",
    val commands: List<String> = emptyList(),
    val userId: String = "",
    val isPremium: Boolean = false,
    val score: Int = 0,
    val postedAt: String = "",
    val nicoruCount: Int = 0,
    val nicoruId: String? = null,
    val isMyPost: Boolean = false
)

/**
 * ニコるキー (nicoruKey) レスポンス DTO
 */
@Serializable
data class NicoruKeyResponseDto(
    val meta: NvCommentMetaDto? = null,
    val data: NicoruKeyDataDto? = null
)

@Serializable
data class NicoruKeyDataDto(
    val nicoruKey: String = ""
)

/**
 * ニコる送信リクエスト DTO
 */
@Serializable
data class NicoruRequestDto(
    val videoId: String,
    val fork: String = "main",
    val no: Long,
    val content: String,
    val nicoruKey: String
)

/**
 * ニコる送信レスポンス DTO
 */
@Serializable
data class NicoruResponseRootDto(
    val meta: NvCommentMetaDto? = null,
    val data: NicoruResponseDataDto? = null
)

@Serializable
data class NicoruResponseDataDto(
    val nicoruId: String = "",
    val nicoruCount: Int = 0
)
