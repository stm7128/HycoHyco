package com.github.stm7128.hycohyco.ui.player

import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.VideoTag
import com.github.stm7128.hycohyco.domain.model.mergeNicodicMetadataFrom
import kotlinx.coroutines.launch

/**
 * 展開プレイヤーにおける動画タグの追加・削除および大百科バッジ非同期補完操作拡張
 */
fun PlayerViewModel.addTag(tagName: String) = mutateTag(tagName, isDelete = false)

fun PlayerViewModel.deleteTag(tagName: String) = mutateTag(tagName, isDelete = true)

/**
 * ニコニコ大百科 API を非同期照会し、大百科記事が存在するタグへ「百」バッジ・URL・要約を反映する
 */
internal fun PlayerViewModel.enrichTagsWithNicodic(videoId: String, tags: List<VideoTag>) {
    if (tags.isEmpty()) return
    viewModelScope.launch {
        val enriched = videoRepository.enrichTagsWithNicodic(tags)
        updatePlayerUiState { state ->
            val current = state.videoDetail
            if (current?.id == videoId) {
                state.copy(videoDetail = current.copy(tags = enriched))
            } else {
                state
            }
        }
    }
}

private fun PlayerViewModel.mutateTag(tagName: String, isDelete: Boolean) {
    val detail = uiState.value.videoDetail ?: return
    val trimmed = tagName.trim()
    if (trimmed.isEmpty() || uiState.value.isTagSubmitting) return
    if (uiState.value.isOfflineMode) {
        updatePlayerUiState { it.copy(userFeedbackMessage = "オフライン再生中はタグを編集できません") }
        return
    }

    viewModelScope.launch {
        updatePlayerUiState { it.copy(isTagSubmitting = true) }
        val result = if (isDelete) {
            videoRepository.deleteVideoTag(detail.id, trimmed, detail.tagEditKey)
        } else {
            videoRepository.addVideoTag(detail.id, trimmed, detail.tagEditKey)
        }
        result.onSuccess { updatedTags ->
            val merged = updatedTags.mergeNicodicMetadataFrom(detail.tags)
            updatePlayerUiState { state ->
                val current = state.videoDetail
                val nextDetail = if (current?.id == detail.id) current.copy(tags = merged) else current
                state.copy(
                    videoDetail = nextDetail,
                    isTagSubmitting = false,
                    userFeedbackMessage = if (isDelete) {
                        "タグ「$trimmed」を削除しました"
                    } else {
                        "タグ「$trimmed」を追加しました"
                    }
                )
            }
            enrichTagsWithNicodic(detail.id, merged)
        }.onFailure { err ->
            updatePlayerUiState {
                it.copy(
                    isTagSubmitting = false,
                    userFeedbackMessage = err.localizedMessage ?: "タグの更新に失敗しました"
                )
            }
        }
    }
}
