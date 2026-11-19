package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.api.NicoTagApiClient
import com.github.stm7128.hycohyco.data.api.TagKeyExpiredException
import com.github.stm7128.hycohyco.domain.model.VideoTag

/**
 * Watch API キャッシュと連携し、タグ追加・削除時の編集キー補完、
 * 期限切れリトライ、およびキャッシュ破棄を担当するヘルパー
 */
internal class VideoTagMutationHelper(
    private val tagApiClient: NicoTagApiClient,
    private val watchDataCache: WatchDataCache
) {
    suspend fun mutateTag(
        videoId: String,
        tag: String,
        tagEditKey: String?,
        userSessionCookie: String,
        isDelete: Boolean
    ): List<VideoTag> {
        require(userSessionCookie.isNotBlank()) {
            "タグを編集するにはニコニコアカウントへのログインが必要です"
        }

        val resolvedKey = tagEditKey?.takeIf { it.isNotBlank() }
            ?: watchDataCache.getWatchData(videoId, userSessionCookie)
                .first.data?.tag?.edit?.editKey?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("タグ編集キーを取得できませんでした")

        val result = executeCall(videoId, tag, resolvedKey, userSessionCookie, isDelete)
        val updatedTags = result.fold(
            onSuccess = { it },
            onFailure = { err ->
                if (err is TagKeyExpiredException) {
                    val refreshed = watchDataCache.getWatchData(
                        videoId = videoId,
                        userSessionCookie = userSessionCookie,
                        forceRefresh = true
                    )
                    val newKey = refreshed.first.data?.tag?.edit?.editKey?.takeIf { it.isNotBlank() }
                        ?: throw IllegalStateException("新しいタグ編集キーを取得できませんでした")
                    executeCall(videoId, tag, newKey, userSessionCookie, isDelete).getOrThrow()
                } else {
                    throw err
                }
            }
        )

        // 次回の詳細取得時に古いタグ一覧が残らないよう Watch キャッシュを破棄
        watchDataCache.invalidate()
        return updatedTags
    }

    private suspend fun executeCall(
        videoId: String,
        tag: String,
        editKey: String,
        userSessionCookie: String,
        isDelete: Boolean
    ): Result<List<VideoTag>> = if (isDelete) {
        tagApiClient.deleteTag(videoId, tag, editKey, userSessionCookie)
    } else {
        tagApiClient.addTag(videoId, tag, editKey, userSessionCookie)
    }
}
