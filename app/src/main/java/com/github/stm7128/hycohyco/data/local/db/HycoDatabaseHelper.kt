package com.github.stm7128.hycohyco.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.PlaylistVideoItem
import com.github.stm7128.hycohyco.domain.model.SearchHistoryItem
import com.github.stm7128.hycohyco.domain.model.WatchHistoryItem

/**
 * 視聴履歴・プレイリスト・検索履歴を管理するローカル SQLite ヘルパー
 */
class HycoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "hycohyco_library.db"
        const val DATABASE_VERSION = 5

        // テーブル名
        const val TABLE_WATCH_HISTORY = HycoDatabaseMigrations.TABLE_WATCH_HISTORY
        const val TABLE_PLAYLISTS = HycoDatabaseMigrations.TABLE_PLAYLISTS
        const val TABLE_PLAYLIST_ITEMS = HycoDatabaseMigrations.TABLE_PLAYLIST_ITEMS
        const val TABLE_SEARCH_HISTORY = HycoDatabaseMigrations.TABLE_SEARCH_HISTORY
    }

    override fun onCreate(db: SQLiteDatabase) {
        HycoDatabaseMigrations.onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        HycoDatabaseMigrations.onUpgrade(db, oldVersion, newVersion)
    }

    // --- 視聴履歴 CRUD ---

    fun insertOrUpdateWatchProgress(item: WatchHistoryItem) {
        var vc = item.viewCounter; var cc = item.commentCounter; var lc = item.likeCounter; var mc = item.mylistCounter
        if (vc == 0 && cc == 0 && lc == 0 && mc == 0) {
            readableDatabase.query(TABLE_WATCH_HISTORY, arrayOf("view_counter", "comment_counter", "like_counter", "mylist_counter"), "video_id = ?", arrayOf(item.videoId), null, null, null).use {
                if (it.moveToFirst()) { vc = it.getInt(0); cc = it.getInt(1); lc = it.getInt(2); mc = it.getInt(3) }
            }
        }
        val values = ContentValues().apply {
            put("video_id", item.videoId)
            put("title", item.title)
            put("thumbnail_url", item.thumbnailUrl)
            put("duration_ms", item.durationMs)
            put("last_position_ms", item.lastPositionMs)
            put("last_watched_at", item.lastWatchedAt)
            put("owner_name", item.ownerName)
            put("view_counter", vc)
            put("comment_counter", cc)
            put("like_counter", lc)
            put("mylist_counter", mc)
        }
        writableDatabase.insertWithOnConflict(TABLE_WATCH_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getLastPosition(videoId: String): Long {
        val cursor = readableDatabase.query(
            TABLE_WATCH_HISTORY,
            arrayOf("last_position_ms"),
            "video_id = ?",
            arrayOf(videoId),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getLong(0) else 0L
        }
    }

    fun getAllWatchHistory(): List<WatchHistoryItem> {
        val result = mutableListOf<WatchHistoryItem>()
        readableDatabase.query(TABLE_WATCH_HISTORY, null, null, null, null, null, "last_watched_at DESC").use { c ->
            val vId = c.getColumnIndexOrThrow("video_id"); val tId = c.getColumnIndexOrThrow("title")
            val thId = c.getColumnIndexOrThrow("thumbnail_url"); val dId = c.getColumnIndexOrThrow("duration_ms")
            val pId = c.getColumnIndexOrThrow("last_position_ms"); val wId = c.getColumnIndexOrThrow("last_watched_at")
            val oId = c.getColumnIndexOrThrow("owner_name")
            val vc = c.getColumnIndex("view_counter"); val cc = c.getColumnIndex("comment_counter")
            val lc = c.getColumnIndex("like_counter"); val mc = c.getColumnIndex("mylist_counter")
            while (c.moveToNext()) {
                result.add(
                    WatchHistoryItem(
                        videoId = c.getString(vId), title = c.getString(tId), thumbnailUrl = c.getString(thId),
                        durationMs = c.getLong(dId), lastPositionMs = c.getLong(pId), lastWatchedAt = c.getLong(wId),
                        ownerName = c.getString(oId), viewCounter = if (vc >= 0) c.getInt(vc) else 0,
                        commentCounter = if (cc >= 0) c.getInt(cc) else 0, likeCounter = if (lc >= 0) c.getInt(lc) else 0,
                        mylistCounter = if (mc >= 0) c.getInt(mc) else 0
                    )
                )
            }
        }
        return result
    }

    fun deleteWatchHistory(videoId: String) {
        writableDatabase.delete(TABLE_WATCH_HISTORY, "video_id = ?", arrayOf(videoId))
    }

    fun clearWatchHistory() {
        writableDatabase.delete(TABLE_WATCH_HISTORY, null, null)
    }

    // --- プレイリスト CRUD ---

    fun insertPlaylist(playlist: LocalPlaylist) {
        val values = ContentValues().apply {
            put("id", playlist.id)
            put("title", playlist.title)
            put("created_at", playlist.createdAt)
            put("updated_at", playlist.updatedAt)
        }
        writableDatabase.insertWithOnConflict(TABLE_PLAYLISTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllPlaylists(): List<LocalPlaylist> {
        val result = mutableListOf<LocalPlaylist>()
        val query = """
            SELECT p.id, p.title, p.created_at, p.updated_at, COUNT(pi.video_id) AS video_count
            FROM $TABLE_PLAYLISTS p
            LEFT JOIN $TABLE_PLAYLIST_ITEMS pi ON p.id = pi.playlist_id
            GROUP BY p.id
            ORDER BY p.updated_at DESC
        """.trimIndent()

        val cursor = readableDatabase.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                result.add(
                    LocalPlaylist(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        videoCount = it.getInt(it.getColumnIndexOrThrow("video_count")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at")),
                        updatedAt = it.getLong(it.getColumnIndexOrThrow("updated_at"))
                    )
                )
            }
        }
        return result
    }

    fun deletePlaylist(playlistId: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PLAYLIST_ITEMS, "playlist_id = ?", arrayOf(playlistId))
            db.delete(TABLE_PLAYLISTS, "id = ?", arrayOf(playlistId))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertPlaylistItem(item: PlaylistVideoItem) {
        val values = ContentValues().apply {
            put("playlist_id", item.playlistId)
            put("video_id", item.videoId)
            put("title", item.title)
            put("thumbnail_url", item.thumbnailUrl)
            put("duration_ms", item.durationMs)
            put("order_index", item.orderIndex)
            put("added_at", item.addedAt)
            put("owner_name", item.ownerName)
            put("view_counter", item.viewCounter)
            put("comment_counter", item.commentCounter)
            put("like_counter", item.likeCounter)
            put("mylist_counter", item.mylistCounter)
        }
        writableDatabase.insertWithOnConflict(TABLE_PLAYLIST_ITEMS, null, values, SQLiteDatabase.CONFLICT_REPLACE)

        // プレイリストの updated_at を更新
        val updateValues = ContentValues().apply {
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update(TABLE_PLAYLISTS, updateValues, "id = ?", arrayOf(item.playlistId))
    }

    fun deletePlaylistItem(playlistId: String, videoId: String) {
        writableDatabase.delete(TABLE_PLAYLIST_ITEMS, "playlist_id = ? AND video_id = ?", arrayOf(playlistId, videoId))
    }

    fun getPlaylistVideos(playlistId: String): List<PlaylistVideoItem> {
        val result = mutableListOf<PlaylistVideoItem>()
        val cursor = readableDatabase.query(
            TABLE_PLAYLIST_ITEMS, null, "playlist_id = ?", arrayOf(playlistId), null, null, "order_index ASC, added_at ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val ownerIdx = it.getColumnIndex("owner_name")
                val viewIdx = it.getColumnIndex("view_counter")
                val commentIdx = it.getColumnIndex("comment_counter")
                val likeIdx = it.getColumnIndex("like_counter")
                val mylistIdx = it.getColumnIndex("mylist_counter")
                result.add(
                    PlaylistVideoItem(
                        playlistId = it.getString(it.getColumnIndexOrThrow("playlist_id")),
                        videoId = it.getString(it.getColumnIndexOrThrow("video_id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        thumbnailUrl = it.getString(it.getColumnIndexOrThrow("thumbnail_url")),
                        durationMs = it.getLong(it.getColumnIndexOrThrow("duration_ms")),
                        orderIndex = it.getInt(it.getColumnIndexOrThrow("order_index")),
                        addedAt = it.getLong(it.getColumnIndexOrThrow("added_at")),
                        ownerName = if (ownerIdx >= 0) it.getString(ownerIdx) else "",
                        viewCounter = if (viewIdx >= 0) it.getInt(viewIdx) else 0,
                        commentCounter = if (commentIdx >= 0) it.getInt(commentIdx) else 0,
                        likeCounter = if (likeIdx >= 0) it.getInt(likeIdx) else 0,
                        mylistCounter = if (mylistIdx >= 0) it.getInt(mylistIdx) else 0
                    )
                )
            }
        }
        return result
    }

    // --- 検索履歴 CRUD ---

    fun insertOrUpdateSearch(keyword: String) {
        val existing = getSearchItem(keyword)
        val isPinned = existing?.isPinned ?: false
        val values = ContentValues().apply {
            put("keyword", keyword)
            put("is_pinned", if (isPinned) 1 else 0)
            put("searched_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(TABLE_SEARCH_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun togglePinSearch(keyword: String) {
        val existing = getSearchItem(keyword) ?: return
        setSearchPinned(keyword, !existing.isPinned)
    }

    fun setSearchPinned(keyword: String, isPinned: Boolean) {
        val values = ContentValues().apply { put("is_pinned", if (isPinned) 1 else 0) }
        writableDatabase.update(TABLE_SEARCH_HISTORY, values, "keyword = ?", arrayOf(keyword))
    }

    private fun getSearchItem(keyword: String): SearchHistoryItem? {
        val cursor = readableDatabase.query(TABLE_SEARCH_HISTORY, null, "keyword = ?", arrayOf(keyword), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) {
                SearchHistoryItem(it.getString(it.getColumnIndexOrThrow("keyword")), it.getInt(it.getColumnIndexOrThrow("is_pinned")) == 1, it.getLong(it.getColumnIndexOrThrow("searched_at")))
            } else null
        }
    }

    fun getAllSearchHistory(): List<SearchHistoryItem> {
        val result = mutableListOf<SearchHistoryItem>()
        val cursor = readableDatabase.query(
            TABLE_SEARCH_HISTORY,
            null, null, null, null, null,
            "is_pinned DESC, searched_at DESC LIMIT 30"
        )
        cursor.use {
            while (it.moveToNext()) {
                result.add(
                    SearchHistoryItem(
                        keyword = it.getString(it.getColumnIndexOrThrow("keyword")),
                        isPinned = it.getInt(it.getColumnIndexOrThrow("is_pinned")) == 1,
                        searchedAt = it.getLong(it.getColumnIndexOrThrow("searched_at"))
                    )
                )
            }
        }
        return result
    }

    fun deleteSearch(keyword: String) {
        writableDatabase.delete(TABLE_SEARCH_HISTORY, "keyword = ?", arrayOf(keyword))
    }

    fun clearSearchHistory() {
        writableDatabase.delete(TABLE_SEARCH_HISTORY, "is_pinned = 0", null)
    }
}
