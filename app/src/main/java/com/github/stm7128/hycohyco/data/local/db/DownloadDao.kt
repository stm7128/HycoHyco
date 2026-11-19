package com.github.stm7128.hycohyco.data.local.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart

/**
 * オフラインダウンロードテーブル (downloaded_videos) の SQLite DAO
 */
class DownloadDao(private val dbHelper: HycoDatabaseHelper) {

    private val changeNotifier = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun observeChanges(): Flow<Unit> = changeNotifier.onStart { emit(Unit) }

    private fun notifyChanged() {
        changeNotifier.tryEmit(Unit)
    }

    fun insertOrUpdate(item: DownloadedVideoItem) {
        val values = ContentValues().apply {
            put(COL_VIDEO_ID, item.videoId)
            put(COL_TITLE, item.title)
            put(COL_DURATION_MS, item.durationMs)
            put(COL_OWNER_NAME, item.ownerName)
            put(COL_THUMBNAIL_URL, item.thumbnailUrl)
            put(COL_DOWNLOAD_TYPE, item.downloadType.name)
            put(COL_STATUS, item.status.name)
            put(COL_PROGRESS_PERCENT, item.progressPercent)
            put(COL_MEDIA_FILE_PATH, item.mediaFilePath)
            put(COL_COMMENTS_FILE_PATH, item.commentsFilePath)
            put(COL_THUMBNAIL_FILE_PATH, item.thumbnailFilePath)
            put(COL_MEDIA_SIZE_BYTES, item.mediaSizeBytes)
            put(COL_COMMENTS_SIZE_BYTES, item.commentsSizeBytes)
            put(COL_CREATED_AT, item.createdAt)
            put(COL_COMPLETED_AT, item.completedAt)
            put(COL_ERROR_MESSAGE, item.errorMessage)
        }
        dbHelper.writableDatabase.insertWithOnConflict(
            TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
        notifyChanged()
    }

    fun updateProgress(videoId: String, progressPercent: Int) {
        val values = ContentValues().apply {
            put(COL_PROGRESS_PERCENT, progressPercent)
            put(COL_STATUS, DownloadStatus.DOWNLOADING.name)
        }
        dbHelper.writableDatabase.update(TABLE_NAME, values, "$COL_VIDEO_ID = ?", arrayOf(videoId))
        notifyChanged()
    }

    fun updateStatus(
        videoId: String,
        status: DownloadStatus,
        progressPercent: Int = 0,
        errorMessage: String? = null
    ) {
        val values = ContentValues().apply {
            put(COL_STATUS, status.name)
            put(COL_PROGRESS_PERCENT, progressPercent)
            put(COL_ERROR_MESSAGE, errorMessage)
        }
        dbHelper.writableDatabase.update(TABLE_NAME, values, "$COL_VIDEO_ID = ?", arrayOf(videoId))
        notifyChanged()
    }

    fun markCompleted(
        videoId: String,
        mediaPath: String,
        commentsPath: String,
        thumbnailPath: String?,
        mediaSize: Long,
        commentsSize: Long
    ) {
        val values = ContentValues().apply {
            put(COL_STATUS, DownloadStatus.COMPLETED.name)
            put(COL_PROGRESS_PERCENT, 100)
            put(COL_MEDIA_FILE_PATH, mediaPath)
            put(COL_COMMENTS_FILE_PATH, commentsPath)
            put(COL_THUMBNAIL_FILE_PATH, thumbnailPath)
            put(COL_MEDIA_SIZE_BYTES, mediaSize)
            put(COL_COMMENTS_SIZE_BYTES, commentsSize)
            put(COL_COMPLETED_AT, System.currentTimeMillis())
            put(COL_ERROR_MESSAGE, null as String?)
        }
        dbHelper.writableDatabase.update(TABLE_NAME, values, "$COL_VIDEO_ID = ?", arrayOf(videoId))
        notifyChanged()
    }

    fun getDownload(videoId: String): DownloadedVideoItem? {
        val cursor = dbHelper.readableDatabase.query(
            TABLE_NAME, null, "$COL_VIDEO_ID = ?", arrayOf(videoId), null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.toDownloadedVideoItem() else null
        }
    }

    fun getAllDownloads(): List<DownloadedVideoItem> {
        val list = mutableListOf<DownloadedVideoItem>()
        val cursor = dbHelper.readableDatabase.query(
            TABLE_NAME, null, null, null, null, null, "$COL_CREATED_AT DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.toDownloadedVideoItem())
            }
        }
        return list
    }

    fun delete(videoId: String) {
        dbHelper.writableDatabase.delete(TABLE_NAME, "$COL_VIDEO_ID = ?", arrayOf(videoId))
        notifyChanged()
    }

    fun getTotalSizeBytes(): Long {
        val cursor = dbHelper.readableDatabase.rawQuery(
            "SELECT SUM($COL_MEDIA_SIZE_BYTES + $COL_COMMENTS_SIZE_BYTES) FROM $TABLE_NAME WHERE $COL_STATUS = ?",
            arrayOf(DownloadStatus.COMPLETED.name)
        )
        return cursor.use {
            if (it.moveToFirst()) it.getLong(0) else 0L
        }
    }

    companion object {
        const val TABLE_NAME = "downloaded_videos"
        const val COL_VIDEO_ID = "video_id"
        const val COL_TITLE = "title"
        const val COL_DURATION_MS = "duration_ms"
        const val COL_OWNER_NAME = "owner_name"
        const val COL_THUMBNAIL_URL = "thumbnail_url"
        const val COL_DOWNLOAD_TYPE = "download_type"
        const val COL_STATUS = "status"
        const val COL_PROGRESS_PERCENT = "progress_percent"
        const val COL_MEDIA_FILE_PATH = "media_file_path"
        const val COL_COMMENTS_FILE_PATH = "comments_file_path"
        const val COL_THUMBNAIL_FILE_PATH = "thumbnail_file_path"
        const val COL_MEDIA_SIZE_BYTES = "media_size_bytes"
        const val COL_COMMENTS_SIZE_BYTES = "comments_size_bytes"
        const val COL_CREATED_AT = "created_at"
        const val COL_COMPLETED_AT = "completed_at"
        const val COL_ERROR_MESSAGE = "error_message"

        fun createTable(db: SQLiteDatabase) {
            val sql = """
                CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                    $COL_VIDEO_ID TEXT PRIMARY KEY,
                    $COL_TITLE TEXT NOT NULL,
                    $COL_DURATION_MS INTEGER NOT NULL,
                    $COL_OWNER_NAME TEXT NOT NULL DEFAULT '',
                    $COL_THUMBNAIL_URL TEXT NOT NULL DEFAULT '',
                    $COL_DOWNLOAD_TYPE TEXT NOT NULL,
                    $COL_STATUS TEXT NOT NULL,
                    $COL_PROGRESS_PERCENT INTEGER NOT NULL DEFAULT 0,
                    $COL_MEDIA_FILE_PATH TEXT,
                    $COL_COMMENTS_FILE_PATH TEXT,
                    $COL_THUMBNAIL_FILE_PATH TEXT,
                    $COL_MEDIA_SIZE_BYTES INTEGER NOT NULL DEFAULT 0,
                    $COL_COMMENTS_SIZE_BYTES INTEGER NOT NULL DEFAULT 0,
                    $COL_CREATED_AT INTEGER NOT NULL,
                    $COL_COMPLETED_AT INTEGER,
                    $COL_ERROR_MESSAGE TEXT
                )
            """.trimIndent()
            db.execSQL(sql)
        }
    }
}

private fun android.database.Cursor.toDownloadedVideoItem(): DownloadedVideoItem {
    return DownloadedVideoItem(
        videoId = getString(getColumnIndexOrThrow(DownloadDao.COL_VIDEO_ID)),
        title = getString(getColumnIndexOrThrow(DownloadDao.COL_TITLE)),
        durationMs = getLong(getColumnIndexOrThrow(DownloadDao.COL_DURATION_MS)),
        ownerName = getString(getColumnIndexOrThrow(DownloadDao.COL_OWNER_NAME)),
        thumbnailUrl = getString(getColumnIndexOrThrow(DownloadDao.COL_THUMBNAIL_URL)),
        downloadType = DownloadType.fromName(getString(getColumnIndexOrThrow(DownloadDao.COL_DOWNLOAD_TYPE))),
        status = DownloadStatus.fromName(getString(getColumnIndexOrThrow(DownloadDao.COL_STATUS))),
        progressPercent = getInt(getColumnIndexOrThrow(DownloadDao.COL_PROGRESS_PERCENT)),
        mediaFilePath = getString(getColumnIndexOrThrow(DownloadDao.COL_MEDIA_FILE_PATH)),
        commentsFilePath = getString(getColumnIndexOrThrow(DownloadDao.COL_COMMENTS_FILE_PATH)),
        thumbnailFilePath = getString(getColumnIndexOrThrow(DownloadDao.COL_THUMBNAIL_FILE_PATH)),
        mediaSizeBytes = getLong(getColumnIndexOrThrow(DownloadDao.COL_MEDIA_SIZE_BYTES)),
        commentsSizeBytes = getLong(getColumnIndexOrThrow(DownloadDao.COL_COMMENTS_SIZE_BYTES)),
        createdAt = getLong(getColumnIndexOrThrow(DownloadDao.COL_CREATED_AT)),
        completedAt = if (isNull(getColumnIndexOrThrow(DownloadDao.COL_COMPLETED_AT))) null else getLong(getColumnIndexOrThrow(DownloadDao.COL_COMPLETED_AT)),
        errorMessage = getString(getColumnIndexOrThrow(DownloadDao.COL_ERROR_MESSAGE))
    )
}
