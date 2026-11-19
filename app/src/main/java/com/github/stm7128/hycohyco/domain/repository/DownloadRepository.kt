package com.github.stm7128.hycohyco.domain.repository

import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.DownloadStatus
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import kotlinx.coroutines.flow.Flow

/**
 * オフラインダウンロード管理リポジトリのインターフェース
 */
interface DownloadRepository {

    /**
     * 保存済み・保存中のすべてのダウンロード一覧を監視する Flow
     */
    fun getAllDownloads(): Flow<List<DownloadedVideoItem>>

    /**
     * 指定動画IDのダウンロード情報を同期取得
     */
    fun getDownload(videoId: String): DownloadedVideoItem?

    /**
     * 指定動画IDのダウンロード状態を監視する Flow
     */
    fun getDownloadFlow(videoId: String): Flow<DownloadedVideoItem?>

    /**
     * ダウンロード情報を新規追加または更新
     */
    fun insertOrUpdate(item: DownloadedVideoItem)

    /**
     * 進捗パーセントを更新 (0〜100)
     */
    fun updateProgress(videoId: String, progressPercent: Int)

    /**
     * ダウンロードステータスとエラーメッセージを更新
     */
    fun updateStatus(
        videoId: String,
        status: DownloadStatus,
        progressPercent: Int = 0,
        errorMessage: String? = null
    )

    /**
     * ダウンロード完了時のパス・ファイル容量を更新
     */
    fun markCompleted(
        videoId: String,
        mediaPath: String,
        commentsPath: String,
        thumbnailPath: String?,
        mediaSize: Long,
        commentsSize: Long
    )

    /**
     * ダウンロード情報を削除 (実ファイルの削除はマネージャー側で行う)
     */
    fun deleteDownload(videoId: String)

    /**
     * 保存済みダウンロードの合計消費容量 (バイト) を取得
     */
    fun getTotalDownloadSizeBytes(): Long

    /**
     * 保存済みローカルコメント JSON ファイルを読み込み、[CommentItem] のリストとして復元する
     *
     * @param commentsFilePath コメント JSON の絶対パス
     * @return 復元されたコメントリスト (vposMs 昇順ソート済み)
     */
    fun loadLocalComments(commentsFilePath: String?): List<CommentItem>
}
