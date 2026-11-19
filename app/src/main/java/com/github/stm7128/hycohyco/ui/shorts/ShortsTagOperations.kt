package com.github.stm7128.hycohyco.ui.shorts

import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.VideoTag
import com.github.stm7128.hycohyco.domain.model.mergeNicodicMetadataFrom
import kotlinx.coroutines.launch

/**
 * ニコニコショート動画詳細シートにおけるタグ追加・削除および大百科バッジ非同期補完拡張
 */
fun ShortsViewModel.addTag(tagName: String) = mutateShortTag(tagName, isDelete = false)

fun ShortsViewModel.deleteTag(tagName: String) = mutateShortTag(tagName, isDelete = true)

/**
 * ニコニコ大百科 API を非同期照会し、ショート詳細とページキャッシュ内のタグ「百」バッジを更新する
 */
internal fun ShortsViewModel.enrichTagsWithNicodic(videoId: String, tags: List<VideoTag>) {
    if (tags.isEmpty()) return
    viewModelScope.launch {
        val enriched = videoRepository.enrichTagsWithNicodic(tags)
        pageCache[videoId]?.let { cached ->
            pageCache[videoId] = cached.copy(detail = cached.detail.copy(tags = enriched))
        }
        updateUiState { state ->
            val current = state.activeDetail
            if (current?.id == videoId) {
                state.copy(activeDetail = current.copy(tags = enriched))
            } else {
                state
            }
        }
    }
}

private fun ShortsViewModel.mutateShortTag(tagName: String, isDelete: Boolean) {
    val detail = uiState.value.activeDetail ?: return
    val trimmed = tagName.trim()
    if (trimmed.isEmpty() || uiState.value.isTagSubmitting) return

    viewModelScope.launch {
        updateUiState { it.copy(isTagSubmitting = true) }
        val result = if (isDelete) {
            videoRepository.deleteVideoTag(detail.id, trimmed, detail.tagEditKey)
        } else {
            videoRepository.addVideoTag(detail.id, trimmed, detail.tagEditKey)
        }
        result.onSuccess { updatedTags ->
            val merged = updatedTags.mergeNicodicMetadataFrom(detail.tags)
            pageCache[detail.id]?.let { cached ->
                pageCache[detail.id] = cached.copy(detail = cached.detail.copy(tags = merged))
            }
            updateUiState { state ->
                val current = state.activeDetail
                val nextDetail = if (current?.id == detail.id) current.copy(tags = merged) else current
                state.copy(
                    activeDetail = nextDetail,
                    isTagSubmitting = false,
                    feedbackMessage = if (isDelete) {
                        "タグ「$trimmed」を削除しました"
                    } else {
                        "タグ「$trimmed」を追加しました"
                    }
                )
            }
            enrichTagsWithNicodic(detail.id, merged)
        }.onFailure { err ->
            updateUiState {
                it.copy(
                    isTagSubmitting = false,
                    feedbackMessage = err.localizedMessage ?: "タグの更新に失敗しました"
                )
            }
        }
    }
}
