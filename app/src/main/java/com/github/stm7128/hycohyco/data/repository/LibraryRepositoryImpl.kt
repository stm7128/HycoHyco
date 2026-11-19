package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.local.db.HycoDatabaseHelper
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.PlaylistVideoItem
import com.github.stm7128.hycohyco.domain.model.SearchHistoryItem
import com.github.stm7128.hycohyco.domain.model.WatchHistoryItem
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * SQLite ヘルパーを用いたライブラリ (履歴・プレイリスト・検索履歴) のリポジトリ実装
 */
class LibraryRepositoryImpl(
    private val dbHelper: HycoDatabaseHelper
) : LibraryRepository {

    // データの変更を購読者へ即座に通知するためのシグナル Flow
    private val historyUpdateSignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val playlistUpdateSignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val searchUpdateSignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    // --- 視聴履歴 ---

    override fun getWatchHistory(): Flow<List<WatchHistoryItem>> = flow {
        historyUpdateSignal.collect {
            emit(dbHelper.getAllWatchHistory())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getLastPosition(videoId: String): Long = withContext(Dispatchers.IO) {
        dbHelper.getLastPosition(videoId)
    }

    override suspend fun saveWatchProgress(
        videoId: String,
        title: String,
        thumbnailUrl: String,
        durationMs: Long,
        positionMs: Long,
        ownerName: String
    ) = withContext(Dispatchers.IO) {
        val item = WatchHistoryItem(
            videoId = videoId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            durationMs = durationMs,
            lastPositionMs = positionMs,
            lastWatchedAt = System.currentTimeMillis(),
            ownerName = ownerName
        )
        dbHelper.insertOrUpdateWatchProgress(item)
        historyUpdateSignal.emit(Unit)
    }

    override suspend fun deleteWatchHistory(videoId: String) = withContext(Dispatchers.IO) {
        dbHelper.deleteWatchHistory(videoId)
        historyUpdateSignal.emit(Unit)
    }

    override suspend fun clearAllWatchHistory() = withContext(Dispatchers.IO) {
        dbHelper.clearWatchHistory()
        historyUpdateSignal.emit(Unit)
    }

    // --- ローカルプレイリスト ---

    override fun getPlaylists(): Flow<List<LocalPlaylist>> = flow {
        playlistUpdateSignal.collect {
            emit(dbHelper.getAllPlaylists())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createPlaylist(title: String): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val playlist = LocalPlaylist(id = id, title = title.trim())
        dbHelper.insertPlaylist(playlist)
        playlistUpdateSignal.emit(Unit)
        id
    }

    override suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        dbHelper.deletePlaylist(playlistId)
        playlistUpdateSignal.emit(Unit)
    }

    override fun getPlaylistVideos(playlistId: String): Flow<List<PlaylistVideoItem>> = flow {
        playlistUpdateSignal.collect {
            emit(dbHelper.getPlaylistVideos(playlistId))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun addVideoToPlaylist(
        playlistId: String,
        videoId: String,
        title: String,
        thumbnailUrl: String,
        durationMs: Long,
        ownerName: String,
        viewCounter: Int,
        commentCounter: Int,
        likeCounter: Int,
        mylistCounter: Int
    ) = withContext(Dispatchers.IO) {
        val currentItems = dbHelper.getPlaylistVideos(playlistId)
        val nextOrder = (currentItems.maxOfOrNull { it.orderIndex } ?: -1) + 1
        val item = PlaylistVideoItem(
            playlistId = playlistId,
            videoId = videoId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            durationMs = durationMs,
            orderIndex = nextOrder,
            ownerName = ownerName,
            viewCounter = viewCounter,
            commentCounter = commentCounter,
            likeCounter = likeCounter,
            mylistCounter = mylistCounter
        )
        dbHelper.insertPlaylistItem(item)
        playlistUpdateSignal.emit(Unit)
    }

    override suspend fun removeVideoFromPlaylist(playlistId: String, videoId: String) = withContext(Dispatchers.IO) {
        dbHelper.deletePlaylistItem(playlistId, videoId)
        playlistUpdateSignal.emit(Unit)
    }

    // --- 検索履歴 & ピン留めタグ ---

    override fun getSearchHistory(): Flow<List<SearchHistoryItem>> = flow {
        searchUpdateSignal.collect {
            emit(dbHelper.getAllSearchHistory())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun recordSearch(keyword: String) = withContext(Dispatchers.IO) {
        val trimmed = keyword.trim()
        if (trimmed.isNotBlank()) {
            dbHelper.insertOrUpdateSearch(trimmed)
            searchUpdateSignal.emit(Unit)
        }
    }

    override suspend fun togglePinSearch(keyword: String) = withContext(Dispatchers.IO) {
        dbHelper.togglePinSearch(keyword)
        searchUpdateSignal.emit(Unit)
    }

    override suspend fun deleteSearch(keyword: String) = withContext(Dispatchers.IO) {
        dbHelper.deleteSearch(keyword)
        searchUpdateSignal.emit(Unit)
    }

    override suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        dbHelper.clearSearchHistory()
        searchUpdateSignal.emit(Unit)
    }

    override suspend fun notifyDataChanged() {
        historyUpdateSignal.emit(Unit)
        playlistUpdateSignal.emit(Unit)
        searchUpdateSignal.emit(Unit)
    }
}
