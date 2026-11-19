package com.github.stm7128.hycohyco.ui.shorts

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.domain.model.CommentNgFilter
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.player.danmaku.DanmakuLayoutEngine
import com.github.stm7128.hycohyco.ui.player.PlayerQualityResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ショート連続視聴のページデータ取得・プリフェッチ・キュー延長・履歴/フォロー同期拡張
 */
internal fun ShortsViewModel.openShortByIdInternal(
    videoId: String,
    context: Context?,
    initialQueue: List<VideoSummary>? = null
) {
    val queue = if (!initialQueue.isNullOrEmpty()) {
        val foundIndex = initialQueue.indexOfFirst { it.contentId.equals(videoId, ignoreCase = true) }
        if (foundIndex >= 0) initialQueue else listOf(createPlaceholderSummary(videoId)) + initialQueue
    } else {
        listOf(createPlaceholderSummary(videoId))
    }
    val targetIndex = queue.indexOfFirst { it.contentId.equals(videoId, ignoreCase = true) }.coerceAtLeast(0)

    updateUiState {
        it.copy(
            isActiveOverlay = true,
            items = queue,
            currentIndex = targetIndex,
            scrollRequestToken = it.scrollRequestToken + 1,
            errorMessage = null
        )
    }
    lastRequestedIndex = -1
    lastRequestedVideoId = null
    onPageSettled(targetIndex, context)

    if (queue.size <= 3) {
        extendQueueFromSeed(videoId)
    }
}

internal fun ShortsViewModel.ensureTabFeedLoadedInternal(context: Context?, forceRefresh: Boolean = false) {
    val current = uiState.value
    if (!forceRefresh && current.items.isNotEmpty()) {
        onPageSettled(current.currentIndex, context)
        return
    }
    viewModelScope.launch {
        updateUiState { it.copy(isLoadingFeed = true, errorMessage = null) }
        videoRepository.getRecommendedShorts(limit = 20)
            .onSuccess { feed ->
                if (feed.isEmpty()) {
                    updateUiState { it.copy(isLoadingFeed = false, errorMessage = "ショート動画が見つかりませんでした") }
                } else {
                    updateUiState {
                        it.copy(
                            isLoadingFeed = false,
                            items = feed,
                            currentIndex = 0,
                            scrollRequestToken = it.scrollRequestToken + 1,
                            errorMessage = null
                        )
                    }
                    lastRequestedIndex = -1
                    lastRequestedVideoId = null
                    onPageSettled(0, context)
                }
            }
            .onFailure { error ->
                updateUiState {
                    it.copy(isLoadingFeed = false, errorMessage = error.localizedMessage ?: "読み込み失敗")
                }
            }
    }
}

internal suspend fun ShortsViewModel.fetchShortPageData(
    videoId: String,
    context: Context?
): ShortPageCache? = coroutineScope {
    try {
        val qualityPref = PlayerQualityResolver.resolveQualityPreference(context, playerPreferencesDataSource)
        val detailDeferred = async { videoRepository.getVideoDetail(videoId) }
        val streamDeferred = async { videoRepository.getStreamSession(videoId, qualityPreference = qualityPref) }
        val commentsDeferred = async { videoRepository.getVideoComments(videoId) }

        val detail = detailDeferred.await().getOrNull() ?: return@coroutineScope null
        val stream = streamDeferred.await().getOrNull()
        val comments = commentsDeferred.await().getOrElse { emptyList() }
        val filtered = CommentNgFilter.filter(comments, latestNgConfig)
        val danmaku = withContext(Dispatchers.Default) {
            DanmakuLayoutEngine.buildRenderItems(filtered, isPortraitMode = true)
        }
        ShortPageCache(detail = detail, streamSession = stream, rawComments = comments, danmakuItems = danmaku)
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }
}

internal fun ShortsViewModel.prefetchNextShort(nextIndex: Int, context: Context?) {
    val nextItem = uiState.value.items.getOrNull(nextIndex) ?: return
    if (getPageCache(nextItem.contentId) != null || !prefetchingIds.add(nextItem.contentId)) return
    viewModelScope.launch {
        try {
            fetchShortPageData(nextItem.contentId, context)?.let { putPageCache(nextItem.contentId, it) }
        } finally {
            prefetchingIds.remove(nextItem.contentId)
        }
    }
}

internal fun ShortsViewModel.extendQueueFromSeed(seedVideoId: String) {
    if (extendJob?.isActive == true) return
    extendJob = viewModelScope.launch {
        val fetched = videoRepository.getShortsPlaylist(seedVideoId).getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: videoRepository.getRecommendedShorts(limit = 20).getOrNull()
            ?: emptyList()
        if (fetched.isEmpty()) return@launch
        updateUiState { state ->
            val existingIds = state.items.mapTo(HashSet()) { it.contentId }
            val additions = fetched.filter { it.contentId.isNotBlank() && existingIds.add(it.contentId) }
            if (additions.isEmpty()) state else state.copy(items = state.items + additions)
        }
    }
}

internal fun ShortsViewModel.saveHistoryIfNeeded(detail: VideoDetail) {
    viewModelScope.launch {
        try {
            libraryRepository.saveWatchProgress(
                videoId = detail.id,
                title = detail.title,
                thumbnailUrl = detail.thumbnailUrl,
                durationMs = detail.durationSeconds * 1000L,
                positionMs = 0L,
                ownerName = detail.ownerName
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {}
    }
}

internal fun ShortsViewModel.refreshOwnerFollowState(ownerId: Long) {
    if (ownerId <= 0L) return
    viewModelScope.launch {
        interactionRepository.isUserFollowed(ownerId).onSuccess { followed ->
            if (uiState.value.activeDetail?.ownerId == ownerId) {
                updateUiState { it.copy(isOwnerFollowed = followed) }
            }
        }
    }
}

internal fun ShortsViewModel.refreshActiveCommentsFilter() {
    val activeId = uiState.value.activeDetail?.id ?: return
    val cached = pageCache[activeId] ?: return
    viewModelScope.launch(Dispatchers.Default) {
        val filtered = CommentNgFilter.filter(cached.rawComments, latestNgConfig)
        val danmaku = DanmakuLayoutEngine.buildRenderItems(filtered, isPortraitMode = true)
        updateUiState { it.copy(comments = filtered, danmakuItems = danmaku) }
    }
}

internal fun createPlaceholderSummary(videoId: String) = VideoSummary(
    contentId = videoId,
    title = "読み込み中...",
    viewCounter = 0,
    commentCounter = 0,
    likeCounter = 0,
    mylistCounter = 0,
    lengthSeconds = 0,
    thumbnailUrl = "",
    startTime = "",
    contentType = "short"
)
