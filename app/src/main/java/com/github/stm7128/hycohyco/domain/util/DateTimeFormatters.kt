package com.github.stm7128.hycohyco.domain.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * スレッドセーフかつ高効率な日付・時刻フォーマッター集
 * (SimpleDateFormat の毎度生成による GC 負荷を抑止)
 */
object DateTimeFormatters {
    private val DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.JAPAN)
            .withZone(ZoneId.systemDefault())

    private val BACKUP_FILENAME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmm", Locale.getDefault())
            .withZone(ZoneId.systemDefault())

    /**
     * Unix エポックミリ秒を "yyyy-MM-dd" 形式の文字列へ変換
     */
    fun formatEpochMillisToDate(millis: Long): String {
        if (millis <= 0L) return ""
        return try {
            DATE_FORMATTER.format(Instant.ofEpochMilli(millis))
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * バックアップファイル名用の現在日時文字列 ("yyyyMMdd_HHmm")
     */
    fun formatCurrentForBackup(): String {
        return try {
            BACKUP_FILENAME_FORMATTER.format(Instant.now())
        } catch (_: Exception) {
            "backup"
        }
    }
}
