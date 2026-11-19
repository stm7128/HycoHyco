package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.api.toDomainComments
import com.github.stm7128.hycohyco.data.local.db.DownloadDao
import com.github.stm7128.hycohyco.data.model.NvCommentResponseRootDto
import com.github.stm7128.hycohyco.domain.model.CommentCommandParser
import com.github.stm7128.hycohyco.domain.model.CommentFork
import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.domain.repository.DownloadRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * [DownloadRepository] の実装クラス
 */
class DownloadRepositoryImpl(
    private val downloadDao: DownloadDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DownloadRepository {

    override fun getAllDownloads(): Flow<List<DownloadedVideoItem>> {
        return downloadDao.observeChanges().map {
            downloadDao.getAllDownloads()
        }.flowOn(ioDispatcher)
    }

    override fun getDownload(videoId: String): DownloadedVideoItem? {
        return downloadDao.getDownload(videoId)
    }

    override fun getDownloadFlow(videoId: String): Flow<DownloadedVideoItem?> {
        return downloadDao.observeChanges().map {
            downloadDao.getDownload(videoId)
        }.flowOn(ioDispatcher)
    }

    override fun insertOrUpdate(item: DownloadedVideoItem) {
        downloadDao.insertOrUpdate(item)
    }

    override fun updateProgress(videoId: String, progressPercent: Int) {
        downloadDao.updateProgress(videoId, progressPercent)
    }

    override fun updateStatus(
        videoId: String,
        status: DownloadStatus,
        progressPercent: Int,
        errorMessage: String?
    ) {
        downloadDao.updateStatus(videoId, status, progressPercent, errorMessage)
    }

    override fun markCompleted(
        videoId: String,
        mediaPath: String,
        commentsPath: String,
        thumbnailPath: String?,
        mediaSize: Long,
        commentsSize: Long
    ) {
        downloadDao.markCompleted(
            videoId = videoId,
            mediaPath = mediaPath,
            commentsPath = commentsPath,
            thumbnailPath = thumbnailPath,
            mediaSize = mediaSize,
            commentsSize = commentsSize
        )
    }

    override fun deleteDownload(videoId: String) {
        downloadDao.delete(videoId)
    }

    override fun getTotalDownloadSizeBytes(): Long {
        return downloadDao.getTotalSizeBytes()
    }

    private val commentJsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun loadLocalComments(commentsFilePath: String?): List<CommentItem> {
        if (commentsFilePath.isNullOrBlank()) return emptyList()
        val file = File(commentsFilePath)
        if (!file.exists()) return emptyList()

        return try {
            val content = file.readText()
            val rootDto = commentJsonParser.decodeFromString<NvCommentResponseRootDto>(content)
            rootDto.toDomainComments()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
