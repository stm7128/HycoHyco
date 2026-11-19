package com.github.stm7128.hycohyco.data.download

import android.content.Context
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.domain.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * オフラインダウンロードを統合管理するシングルトンマネージャー
 */
class OfflineDownloadManager(
    private val context: Context,
    private val downloadRepository: DownloadRepository,
    private val offlineDownloader: OfflineDownloader
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val allDownloads: Flow<List<DownloadedVideoItem>> = downloadRepository.getAllDownloads()

    /**
     * ダウンロードを予約してサービスを開始する
     */
    fun enqueueDownload(
        videoId: String,
        title: String,
        durationMs: Long,
        ownerName: String = "",
        thumbnailUrl: String = "",
        downloadType: DownloadType = DownloadType.VIDEO_AND_DANMAKU
    ) {
        val item = DownloadedVideoItem(
            videoId = videoId,
            title = title,
            durationMs = durationMs,
            ownerName = ownerName,
            thumbnailUrl = thumbnailUrl,
            downloadType = downloadType,
            status = DownloadStatus.QUEUED,
            progressPercent = 0
        )
        downloadRepository.insertOrUpdate(item)

        // フォアグラウンドサービス起動
        OfflineDownloadService.startDownload(
            context = context,
            videoId = videoId,
            title = title,
            downloadType = downloadType
        )
    }

    /**
     * ダウンロードを中止する
     */
    fun cancelDownload(videoId: String) {
        OfflineDownloadService.cancelDownload(context, videoId)
    }

    /**
     * 保存済みダウンロード（実ファイルおよび DB レコード）を完全削除する
     */
    fun deleteDownload(videoId: String) {
        scope.launch {
            // 実ファイル・ディレクトリを削除
            offlineDownloader.deleteDownloadedFiles(videoId)
            // DB レコードを削除
            downloadRepository.deleteDownload(videoId)
        }
    }

    /**
     * 指定動画が保存完了しているかチェック
     */
    fun isDownloaded(videoId: String): Boolean {
        val item = downloadRepository.getDownload(videoId)
        return item?.status == DownloadStatus.COMPLETED
    }

    /**
     * 指定動画のダウンロード情報を取得
     */
    fun getDownload(videoId: String): DownloadedVideoItem? {
        return downloadRepository.getDownload(videoId)
    }

    /**
     * 指定動画のダウンロード状態を監視する Flow
     */
    fun getDownloadFlow(videoId: String): Flow<DownloadedVideoItem?> {
        return downloadRepository.getDownloadFlow(videoId)
    }

    /**
     * 保存済みダウンロードの合計消費容量 (バイト)
     */
    fun getTotalSizeBytes(): Long {
        return downloadRepository.getTotalDownloadSizeBytes()
    }
}
