package com.github.stm7128.hycohyco.data.local.db

import android.database.sqlite.SQLiteDatabase

/**
 * SQLite データベースのスキーマ定義およびマイグレーション処理
 */
object HycoDatabaseMigrations {

    const val TABLE_WATCH_HISTORY = "watch_history"
    const val TABLE_PLAYLISTS = "playlists"
    const val TABLE_PLAYLIST_ITEMS = "playlist_items"
    const val TABLE_SEARCH_HISTORY = "search_history"

    fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_WATCH_HISTORY (" +
                "video_id TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "thumbnail_url TEXT NOT NULL, " +
                "duration_ms INTEGER NOT NULL, " +
                "last_position_ms INTEGER NOT NULL, " +
                "last_watched_at INTEGER NOT NULL, " +
                "owner_name TEXT NOT NULL DEFAULT '', " +
                "view_counter INTEGER NOT NULL DEFAULT 0, " +
                "comment_counter INTEGER NOT NULL DEFAULT 0, " +
                "like_counter INTEGER NOT NULL DEFAULT 0, " +
                "mylist_counter INTEGER NOT NULL DEFAULT 0" +
            ")"
        )
        db.execSQL(
            "CREATE TABLE $TABLE_PLAYLISTS (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER NOT NULL" +
            ")"
        )
        db.execSQL(
            "CREATE TABLE $TABLE_PLAYLIST_ITEMS (" +
                "playlist_id TEXT NOT NULL, " +
                "video_id TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "thumbnail_url TEXT NOT NULL, " +
                "duration_ms INTEGER NOT NULL, " +
                "order_index INTEGER NOT NULL, " +
                "added_at INTEGER NOT NULL, " +
                "owner_name TEXT NOT NULL DEFAULT '', " +
                "view_counter INTEGER NOT NULL DEFAULT 0, " +
                "comment_counter INTEGER NOT NULL DEFAULT 0, " +
                "like_counter INTEGER NOT NULL DEFAULT 0, " +
                "mylist_counter INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (playlist_id, video_id)" +
            ")"
        )
        db.execSQL(
            "CREATE TABLE $TABLE_SEARCH_HISTORY (" +
                "keyword TEXT PRIMARY KEY, " +
                "is_pinned INTEGER NOT NULL DEFAULT 0, " +
                "searched_at INTEGER NOT NULL" +
                ")"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_watch_history_last_watched_at " +
                "ON $TABLE_WATCH_HISTORY(last_watched_at DESC)"
        )
        DownloadDao.createTable(db)
    }

    fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            DownloadDao.createTable(db)
        }
        if (oldVersion < 3) {
            val playlistCols = listOf(
                "owner_name TEXT NOT NULL DEFAULT ''",
                "view_counter INTEGER NOT NULL DEFAULT 0",
                "comment_counter INTEGER NOT NULL DEFAULT 0",
                "like_counter INTEGER NOT NULL DEFAULT 0",
                "mylist_counter INTEGER NOT NULL DEFAULT 0"
            )
            playlistCols.forEach { col ->
                runCatching { db.execSQL("ALTER TABLE $TABLE_PLAYLIST_ITEMS ADD COLUMN $col") }
            }
        }
        if (oldVersion < 4) {
            val historyCols = listOf(
                "view_counter INTEGER NOT NULL DEFAULT 0",
                "comment_counter INTEGER NOT NULL DEFAULT 0",
                "like_counter INTEGER NOT NULL DEFAULT 0",
                "mylist_counter INTEGER NOT NULL DEFAULT 0"
            )
            historyCols.forEach { col ->
                runCatching { db.execSQL("ALTER TABLE $TABLE_WATCH_HISTORY ADD COLUMN $col") }
            }
        }
        if (oldVersion < 5) {
            runCatching {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_watch_history_last_watched_at " +
                        "ON $TABLE_WATCH_HISTORY(last_watched_at DESC)"
                )
            }
        }
    }
}
