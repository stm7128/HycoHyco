package com.github.stm7128.hycohyco.domain.repository

import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.PlaylistVideoItem
import com.github.stm7128.hycohyco.domain.model.SearchHistoryItem
import com.github.stm7128.hycohyco.domain.model.WatchHistoryItem
import kotlinx.coroutines.flow.Flow

/**
 * 視聴履歴・ローカルプレイリスト・検索履歴を管理するリポジトリインターフェース
 */
interface LibraryRepository {

    // --- 視聴履歴 ---
    fun getWatchHistory(): Flow<List<WatchHistoryItem>>

    suspend fun getLastPosition(videoId: String): Long

    suspend fun saveWatchProgress(
        videoId: String,
        title: String,
        thumbnailUrl: String,
        durationMs: Long,
        positionMs: Long,
        ownerName: String = ""
    )

    suspend fun deleteWatchHistory(videoId: String)

    suspend fun clearAllWatchHistory()

    // --- ローカルプレイリスト ---
    fun getPlaylists(): Flow<List<LocalPlaylist>>

    suspend fun createPlaylist(title: String): String

    suspend fun deletePlaylist(playlistId: String)

    fun getPlaylistVideos(playlistId: String): Flow<List<PlaylistVideoItem>>

    suspend fun addVideoToPlaylist(
        playlistId: String,
        videoId: String,
        title: String,
        thumbnailUrl: String,
        durationMs: Long,
        ownerName: String = "",
        viewCounter: Int = 0,
        commentCounter: Int = 0,
        likeCounter: Int = 0,
        mylistCounter: Int = 0
    )

    suspend fun removeVideoFromPlaylist(playlistId: String, videoId: String)

    // --- 検索履歴 & ピン留めタグ ---
    fun getSearchHistory(): Flow<List<SearchHistoryItem>>

    suspend fun recordSearch(keyword: String)

    suspend fun togglePinSearch(keyword: String)

    suspend fun deleteSearch(keyword: String)

    suspend fun clearSearchHistory()

    /**
     * バックアップ復元時などに全購読 Flow へデータ更新シグナルを一括送信する
     */
    suspend fun notifyDataChanged()
}
