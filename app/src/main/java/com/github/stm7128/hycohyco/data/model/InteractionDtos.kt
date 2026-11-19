package com.github.stm7128.hycohyco.data.model

import kotlinx.serialization.Serializable

/**
 * いいね！送信 API レスポンス DTO
 */
@Serializable
data class LikeResponseDto(
    val meta: InteractionMetaDto? = null,
    val data: LikeDataDto? = null
)

@Serializable
data class InteractionMetaDto(
    val status: Int = 200,
    val errorCode: String = ""
)

@Serializable
data class LikeDataDto(
    val thanksMessage: String? = null
)

/**
 * コメント投稿用 postKey 取得レスポンス DTO
 */
@Serializable
data class PostKeyResponseDto(
    val meta: InteractionMetaDto? = null,
    val data: PostKeyDataDto? = null
)

@Serializable
data class PostKeyDataDto(
    val postKey: String = ""
)

/**
 * コメント投稿リクエスト DTO
 */
@Serializable
data class PostCommentRequestDto(
    val videoId: String = "",
    val body: String,
    val commands: List<String> = emptyList(),
    val vposMs: Long = 0L,
    val postKey: String = ""
)

/**
 * コメント投稿レスポンス DTO
 */
@Serializable
data class PostCommentResponseDto(
    val meta: InteractionMetaDto? = null,
    val data: PostCommentDataDto? = null
)

@Serializable
data class PostCommentDataDto(
    val id: String = "",
    val no: Long = 0L,
    val shouldRefreshKey: Boolean = false
)

/**
 * ユーザーフォロー状態一括判定 API レスポンス DTO
 */
@Serializable
data class UserFolloweesBulkResponseDto(
    val meta: InteractionMetaDto? = null,
    val data: UserFolloweesBulkDataDto? = null
)

@Serializable
data class UserFolloweesBulkDataDto(
    val followees: Map<String, UserFolloweeStatusDto> = emptyMap()
)

@Serializable
data class UserFolloweeStatusDto(
    val following: Boolean = false
)

/**
 * コメント削除用 deleteKey 取得レスポンス DTO
 */
@Serializable
data class DeleteKeyResponseDto(
    val meta: InteractionMetaDto? = null,
    val data: DeleteKeyDataDto? = null
)

@Serializable
data class DeleteKeyDataDto(
    val deleteKey: String = ""
)

/**
 * コメント削除 (comment-comment-owner-deletions) リクエスト DTO
 */
@Serializable
data class DeleteCommentRequestDto(
    val videoId: String,
    val targets: List<DeleteCommentTargetDto>,
    val fork: String,
    val language: String,
    val deleteKey: String
)

@Serializable
data class DeleteCommentTargetDto(
    val no: Long,
    val operation: String
)

