package com.github.stm7128.hycohyco.data.model

import com.github.stm7128.hycohyco.domain.model.VideoTag
import kotlinx.serialization.Serializable

/**
 * Watch API (v3 / v3_guest) の data.tag DTO
 */
@Serializable
data class WatchTagRootDto(
    val items: List<WatchTagItemDto> = emptyList(),
    val hasR18Tag: Boolean = false,
    val isPublishedNicoscript: Boolean = false,
    val edit: WatchTagEditDto? = null
)

/**
 * 個別タグ項目 DTO (Watch API / nvapi v2 tags 共通)
 */
@Serializable
data class WatchTagItemDto(
    val name: String = "",
    val isCategory: Boolean = false,
    val isCategoryCandidate: Boolean = false,
    val isNicodicArticleExists: Boolean = false,
    val isLocked: Boolean = false
) {
    /** 空白タグ名を除外してドメインモデルへ変換する */
    fun toDomainOrNull(): VideoTag? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        return VideoTag(
            name = trimmed,
            isCategory = isCategory,
            isCategoryCandidate = isCategoryCandidate,
            isNicodicArticleExists = isNicodicArticleExists,
            isLocked = isLocked
        )
    }
}

/**
 * タグ編集権限・編集キー DTO
 */
@Serializable
data class WatchTagEditDto(
    val isEditable: Boolean = false,
    val uneditableReason: String? = null,
    val editKey: String? = null
)

/**
 * nvapi v2/videos/{watchId}/tags 追加・削除レスポンス DTO
 */
@Serializable
data class TagMutationRootDto(
    val meta: TagMutationMetaDto? = null,
    val data: TagMutationDataDto? = null
)

@Serializable
data class TagMutationMetaDto(
    val status: Int = 200,
    val errorCode: String? = null
)

@Serializable
data class TagMutationDataDto(
    val tags: List<WatchTagItemDto>? = null,
    val items: List<WatchTagItemDto>? = null
)
