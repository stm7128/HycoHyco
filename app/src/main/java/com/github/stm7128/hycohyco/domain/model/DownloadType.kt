package com.github.stm7128.hycohyco.domain.model

/**
 * オフラインダウンロード保存種別
 */
enum class DownloadType(val displayName: String) {
    /** 動画 + コメント弾幕 */
    VIDEO_AND_DANMAKU("動画 + 弾幕コメント"),

    /** 音声のみ + コメント弾幕 (軽量・BGM視聴用) */
    AUDIO_ONLY_AND_DANMAKU("音声のみ + 弾幕コメント");

    companion object {
        fun fromName(name: String?): DownloadType {
            return entries.firstOrNull { it.name == name } ?: VIDEO_AND_DANMAKU
        }
    }
}
