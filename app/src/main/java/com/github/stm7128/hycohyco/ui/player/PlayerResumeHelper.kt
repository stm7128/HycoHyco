package com.github.stm7128.hycohyco.ui.player

import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository
import com.github.stm7128.hycohyco.player.NicoPlaybackManager
import com.github.stm7128.hycohyco.player.model.PlaybackState
import com.github.stm7128.hycohyco.player.model.currentPositionMs
import com.github.stm7128.hycohyco.player.model.durationMs

/**
 * 視聴履歴・レジューム再生の保存と復元を担うヘルパークラス
 */
object PlayerResumeHelper {

    /**
     * 現在の再生位置を視聴履歴に保存する
     */
    suspend fun saveCurrentProgress(
        libraryRepository: LibraryRepository,
        videoDetail: VideoDetail?,
        playbackState: PlaybackState
    ) {
        val detail = videoDetail ?: return
        val pos = playbackState.currentPositionMs
        val dur = playbackState.durationMs
        // 再生位置が 0L 以下（未再生・バッファリング直後）の場合は、既存のレジューム位置を上書き消去しないよう保護
        val targetPos = if (pos <= 0L) libraryRepository.getLastPosition(detail.id) else pos
        libraryRepository.saveWatchProgress(
            videoId = detail.id,
            title = detail.title,
            thumbnailUrl = detail.thumbnailUrl,
            durationMs = dur,
            positionMs = targetPos,
            ownerName = detail.ownerName
        )
    }

    /**
     * 前回の再生位置を復元しシークする
     *
     * @param isResumeEnabled 自動レジューム再生が有効かどうか（明示的な [initialPositionMs] 指定時は設定に関わらず優先シークする）
     */
    suspend fun restoreResumePosition(
        libraryRepository: LibraryRepository,
        playbackManager: NicoPlaybackManager,
        videoId: String,
        durationMs: Long,
        title: String,
        ownerName: String,
        thumbnailUrl: String,
        initialPositionMs: Long? = null,
        isResumeEnabled: Boolean = true
    ) {
        val targetPos = when {
            initialPositionMs != null && initialPositionMs > 0L -> initialPositionMs
            isResumeEnabled -> {
                val lastPos = libraryRepository.getLastPosition(videoId)
                if (lastPos > 5000L && durationMs > 0 && lastPos < durationMs - 5000L) lastPos else 0L
            }
            else -> 0L
        }

        if (targetPos > 0L) {
            playbackManager.seekTo(targetPos)
        }
        libraryRepository.saveWatchProgress(
            videoId = videoId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            durationMs = durationMs,
            positionMs = targetPos,
            ownerName = ownerName
        )
    }
}
