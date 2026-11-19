package com.github.stm7128.hycohyco.ui.player

import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.repository.VideoRepository

/**
 * 関連動画 (レコメンド) 取得操作ヘルパー
 */
object PlayerRecommendOperations {

    /**
     * 指定された動画のレコメンド動画一覧を非同期取得する
     *
     * @param videoRepository 動画リポジトリ
     * @param videoId 対象動画ID
     * @param onUpdate 状態更新コールバック (videos, isLoading)
     */
    suspend fun loadRecommendVideos(
        videoRepository: VideoRepository,
        videoId: String,
        onUpdate: (videos: List<VideoSummary>, isLoading: Boolean) -> Unit
    ) {
        onUpdate(emptyList(), true)
        val result = videoRepository.getRecommendVideos(videoId)
        val list = result.getOrDefault(emptyList())
        onUpdate(list, false)
    }
}
