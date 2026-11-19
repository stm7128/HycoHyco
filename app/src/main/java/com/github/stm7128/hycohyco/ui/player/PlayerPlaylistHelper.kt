package com.github.stm7128.hycohyco.ui.player

import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository

/**
 * プレイヤー画面におけるプレイリスト追加操作を担うヘルパー
 */
object PlayerPlaylistHelper {

    suspend fun addVideoToPlaylist(
        libraryRepository: LibraryRepository,
        playlistId: String,
        detail: VideoDetail
    ) {
        libraryRepository.addVideoToPlaylist(
            playlistId = playlistId,
            videoId = detail.id,
            title = detail.title,
            thumbnailUrl = detail.thumbnailUrl,
            durationMs = detail.durationSeconds * 1000L,
            ownerName = detail.ownerName,
            viewCounter = detail.viewCount.toInt(),
            commentCounter = detail.commentCount.toInt(),
            likeCounter = detail.likeCount.toInt(),
            mylistCounter = detail.mylistCount.toInt()
        )
    }

    suspend fun createAndAdd(
        libraryRepository: LibraryRepository,
        title: String,
        detail: VideoDetail
    ) {
        val newId = libraryRepository.createPlaylist(title)
        addVideoToPlaylist(libraryRepository, newId, detail)
    }
}
