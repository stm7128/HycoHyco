package com.github.stm7128.hycohyco.domain.model

/**
 * オフラインダウンロード状態
 */
enum class DownloadStatus(val displayName: String) {
    /** 待機中 */
    QUEUED("待機中"),

    /** ダウンロード実行中 */
    DOWNLOADING("保存中"),

    /** 一時停止中 */
    PAUSED("一時停止"),

    /** 保存完了 */
    COMPLETED("保存済み"),

    /** エラー・失敗 */
    FAILED("失敗");

    val isFinished: Boolean
        get() = this == COMPLETED || this == FAILED

    val isDownloading: Boolean
        get() = this == DOWNLOADING

    companion object {
        fun fromName(name: String?): DownloadStatus {
            return entries.firstOrNull { it.name == name } ?: QUEUED
        }
    }
}
