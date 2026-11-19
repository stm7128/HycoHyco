package com.github.stm7128.hycohyco.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadType
import com.github.stm7128.hycohyco.domain.repository.DownloadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * バックグラウンドで動画・音声・弾幕のダウンロードを実行・通知する Foreground Service
 */
class OfflineDownloadService : Service() {

    private val downloadRepository: DownloadRepository by inject()
    private val offlineDownloader: OfflineDownloader by inject()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var currentDownloadJob: Job? = null
    private var currentVideoId: String? = null

    private data class DownloadTask(val videoId: String, val title: String, val downloadType: DownloadType)
    private val downloadChannel = Channel<DownloadTask>(Channel.UNLIMITED)
    private var queueWorkerJob: Job? = null

    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_ID = "hyco_download_channel"
        const val NOTIFICATION_ID = 4001

        const val ACTION_START_DOWNLOAD = "com.github.stm7128.hycohyco.action.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.github.stm7128.hycohyco.action.CANCEL_DOWNLOAD"

        const val EXTRA_VIDEO_ID = "extra_video_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DOWNLOAD_TYPE = "extra_download_type"

        fun startDownload(
            context: Context,
            videoId: String,
            title: String,
            downloadType: DownloadType
        ) {
            val intent = Intent(context, OfflineDownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_VIDEO_ID, videoId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DOWNLOAD_TYPE, downloadType.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun cancelDownload(context: Context, videoId: String) {
            val intent = Intent(context, OfflineDownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_VIDEO_ID, videoId)
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startQueueWorker()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun startQueueWorker() {
        queueWorkerJob?.cancel()
        queueWorkerJob = serviceScope.launch {
            for (task in downloadChannel) {
                currentVideoId = task.videoId
                try {
                    startForegroundNotification(task.title, 0)
                    downloadRepository.updateStatus(task.videoId, DownloadStatus.DOWNLOADING, 0)

                    val job = launch {
                        val result = offlineDownloader.downloadVideo(
                            videoId = task.videoId,
                            downloadType = task.downloadType,
                            onProgress = { percent ->
                                downloadRepository.updateProgress(task.videoId, percent)
                                notificationManager.notify(NOTIFICATION_ID, buildNotification(task.title, percent))
                            }
                        )

                        downloadRepository.markCompleted(
                            videoId = task.videoId,
                            mediaPath = result.mediaFilePath,
                            commentsPath = result.commentsFilePath,
                            thumbnailPath = result.thumbnailFilePath,
                            mediaSize = result.mediaSizeBytes,
                            commentsSize = result.commentsSizeBytes
                        )

                        showCompletionNotification(task.title)
                    }
                    currentDownloadJob = job
                    job.join()
                } catch (e: CancellationException) {
                    offlineDownloader.deleteDownloadedFiles(task.videoId)
                    downloadRepository.updateStatus(task.videoId, DownloadStatus.FAILED, errorMessage = "中断されました")
                } catch (e: Exception) {
                    offlineDownloader.deleteDownloadedFiles(task.videoId)
                    downloadRepository.updateStatus(
                        task.videoId,
                        DownloadStatus.FAILED,
                        errorMessage = e.localizedMessage ?: "ダウンロードエラー"
                    )
                    showErrorNotification(task.title, e.localizedMessage ?: "保存失敗")
                } finally {
                    currentVideoId = null
                    currentDownloadJob = null
                }
                if (downloadChannel.isEmpty) {
                    break
                }
            }
            stopForegroundAndSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val videoId = intent?.getStringExtra(EXTRA_VIDEO_ID) ?: return START_NOT_STICKY

        if (action == ACTION_CANCEL_DOWNLOAD) {
            if (currentVideoId == videoId) {
                currentDownloadJob?.cancel()
            }
            offlineDownloader.deleteDownloadedFiles(videoId)
            downloadRepository.updateStatus(videoId, DownloadStatus.FAILED, errorMessage = "ユーザーにより中止")
            return START_NOT_STICKY
        }

        if (action == ACTION_START_DOWNLOAD) {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: videoId
            val typeName = intent.getStringExtra(EXTRA_DOWNLOAD_TYPE)
            val downloadType = DownloadType.fromName(typeName)

            startForegroundNotification(title, 0)
            if (queueWorkerJob?.isActive != true) {
                startQueueWorker()
            }
            downloadChannel.trySend(DownloadTask(videoId, title, downloadType))
        }

        return START_NOT_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        super.onTimeout(startId, fgsType)
        currentDownloadJob?.cancel()
        currentVideoId?.let { offlineDownloader.deleteDownloadedFiles(it) }
        stopForegroundAndSelf()
    }

    private fun startForegroundNotification(title: String, progress: Int) {
        val notification = buildNotification(title, progress)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(title: String, progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("動画・弾幕を保存中")
            .setContentText("$title ($progress%)")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun showCompletionNotification(title: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ダウンロード完了")
            .setContentText("$title の保存が完了しました")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showErrorNotification(title: String, error: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ダウンロード失敗")
            .setContentText("$title: $error")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "オフラインダウンロード",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "動画・音声・弾幕のダウンロード進捗通知"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun stopForegroundAndSelf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadChannel.close()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
