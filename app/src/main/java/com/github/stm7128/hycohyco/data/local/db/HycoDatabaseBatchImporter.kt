package com.github.stm7128.hycohyco.data.local.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.github.stm7128.hycohyco.data.backup.BackupPlaylistDto
import com.github.stm7128.hycohyco.data.backup.BackupSearchHistoryDto
import com.github.stm7128.hycohyco.data.backup.BackupWatchHistoryDto

/**
 * バックアップ復元時の大量データ用トランザクション一括インポート拡張
 */
fun HycoDatabaseHelper.importWatchHistoryBatch(items: List<BackupWatchHistoryDto>) {
    if (items.isEmpty()) return
    val db = writableDatabase
    db.beginTransaction()
    try {
        val values = ContentValues()
        for (item in items) {
            if (item.videoId.isBlank()) continue
            values.clear()
            values.put("video_id", item.videoId)
            values.put("title", item.title)
            values.put("thumbnail_url", item.thumbnailUrl)
            values.put("duration_ms", item.durationMs)
            values.put("last_position_ms", item.lastPositionMs)
            values.put("last_watched_at", if (item.lastWatchedAt > 0L) item.lastWatchedAt else System.currentTimeMillis())
            values.put("owner_name", item.ownerName)
            values.put("view_counter", item.viewCounter)
            values.put("comment_counter", item.commentCounter)
            values.put("like_counter", item.likeCounter)
            values.put("mylist_counter", item.mylistCounter)
            db.insertWithOnConflict(HycoDatabaseHelper.TABLE_WATCH_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }
}

fun HycoDatabaseHelper.importPlaylistsBatch(playlists: List<BackupPlaylistDto>) {
    if (playlists.isEmpty()) return
    val db = writableDatabase
    db.beginTransaction()
    try {
        val pValues = ContentValues()
        val itemValues = ContentValues()
        for (pl in playlists) {
            val playlistId = pl.id.ifBlank { "pl_${System.currentTimeMillis()}_${(100..999).random()}" }
            val now = System.currentTimeMillis()
            pValues.clear()
            pValues.put("id", playlistId)
            pValues.put("title", pl.title)
            pValues.put("created_at", if (pl.createdAt > 0L) pl.createdAt else now)
            pValues.put("updated_at", if (pl.updatedAt > 0L) pl.updatedAt else now)
            db.insertWithOnConflict(HycoDatabaseHelper.TABLE_PLAYLISTS, null, pValues, SQLiteDatabase.CONFLICT_REPLACE)

            for (item in pl.items) {
                if (item.videoId.isBlank()) continue
                itemValues.clear()
                itemValues.put("playlist_id", playlistId)
                itemValues.put("video_id", item.videoId)
                itemValues.put("title", item.title)
                itemValues.put("thumbnail_url", item.thumbnailUrl)
                itemValues.put("duration_ms", item.durationMs)
                itemValues.put("order_index", item.orderIndex)
                itemValues.put("added_at", if (item.addedAt > 0L) item.addedAt else now)
                itemValues.put("owner_name", item.ownerName)
                itemValues.put("view_counter", item.viewCounter)
                itemValues.put("comment_counter", item.commentCounter)
                itemValues.put("like_counter", item.likeCounter)
                itemValues.put("mylist_counter", item.mylistCounter)
                db.insertWithOnConflict(HycoDatabaseHelper.TABLE_PLAYLIST_ITEMS, null, itemValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }
}

fun HycoDatabaseHelper.importSearchHistoryBatch(items: List<BackupSearchHistoryDto>) {
    if (items.isEmpty()) return
    val db = writableDatabase
    db.beginTransaction()
    try {
        val values = ContentValues()
        val now = System.currentTimeMillis()
        for (item in items) {
            if (item.keyword.isBlank()) continue
            values.clear()
            values.put("keyword", item.keyword)
            values.put("is_pinned", if (item.isPinned) 1 else 0)
            values.put("searched_at", if (item.searchedAt > 0L) item.searchedAt else now)
            db.insertWithOnConflict(HycoDatabaseHelper.TABLE_SEARCH_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }
}
