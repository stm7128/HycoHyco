package com.github.stm7128.hycohyco.data.backup

import kotlinx.serialization.Serializable

/**
 * HycoHyco 全データ一括バックアップ用ルート DTO
 */
@Serializable
data class BackupRootDto(
    /** バックアップフォーマットのバージョン */
    val version: Int = 1,

    /** エクスポート日時のミリ秒タイムスタンプ */
    val exportedAt: Long = System.currentTimeMillis(),

    /** テーマ・外観設定 */
    val themeSettings: BackupThemeDto = BackupThemeDto(),

    /** プレイヤー動作設定 */
    val playerSettings: BackupPlayerDto = BackupPlayerDto(),

    /** 弾幕表示設定 */
    val danmakuSettings: BackupDanmakuDto = BackupDanmakuDto(),

    /** コメント NG フィルター設定 */
    val ngConfig: BackupNgDto = BackupNgDto(),

    /** ローカルプレイリスト一覧 */
    val playlists: List<BackupPlaylistDto> = emptyList(),

    /** 視聴履歴一覧 */
    val watchHistory: List<BackupWatchHistoryDto> = emptyList(),

    /** 検索履歴・ピン留めタグ一覧 */
    val searchHistory: List<BackupSearchHistoryDto> = emptyList(),
)

@Serializable
data class BackupThemeDto(
    val themeMode: String = "HYPER_OS",
    val darkThemeConfig: String = "FOLLOW_SYSTEM",
    val isDynamicColor: Boolean = true,
    val isAmoledBlack: Boolean = false,
    val defaultHomeMode: String? = null,
)

@Serializable
data class BackupPlayerDto(
    val gestureMode: String = "FULLSCREEN_ONLY",
    val isAutoplayEnabled: Boolean = true,
    val doubleTapSeekSeconds: Int = 10,
    val useNicovideoGay: Boolean = false,
    val defaultQualityWifi: String = "AUTO",
    val defaultQualityCellular: String = "P360",
    val coexistAudio: Boolean = false,
    val autoEnterPip: Boolean = true,
    val showShortsBottomTab: Boolean = true,
    val isResumePlaybackEnabled: Boolean = true,
)

@Serializable
data class BackupDanmakuDto(
    val isEnabled: Boolean = true,
    val alpha: Float = 0.90f,
    val scale: Float = 1.0f,
    val speedScale: Float = 1.0f,
    val areaMode: String = "ALL",
)

@Serializable
data class BackupNgDto(
    val hide184Comments: Boolean = false,
    val minScoreThreshold: Int = -1000,
    val ngUserIds: List<String> = emptyList(),
    val ngWords: List<String> = emptyList(),
    val ngRegexPatterns: List<String> = emptyList(),
)

@Serializable
data class BackupPlaylistDto(
    val id: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val items: List<BackupPlaylistItemDto> = emptyList(),
)

@Serializable
data class BackupPlaylistItemDto(
    val videoId: String = "",
    val title: String = "",
    val thumbnailUrl: String = "",
    val durationMs: Long = 0L,
    val orderIndex: Int = 0,
    val addedAt: Long = 0L,
    val ownerName: String = "",
    val viewCounter: Int = 0,
    val commentCounter: Int = 0,
    val likeCounter: Int = 0,
    val mylistCounter: Int = 0,
)

@Serializable
data class BackupWatchHistoryDto(
    val videoId: String = "",
    val title: String = "",
    val thumbnailUrl: String = "",
    val durationMs: Long = 0L,
    val lastPositionMs: Long = 0L,
    val lastWatchedAt: Long = 0L,
    val ownerName: String = "",
    val viewCounter: Int = 0,
    val commentCounter: Int = 0,
    val likeCounter: Int = 0,
    val mylistCounter: Int = 0,
)

@Serializable
data class BackupSearchHistoryDto(
    val keyword: String = "",
    val isPinned: Boolean = false,
    val searchedAt: Long = 0L,
)

/**
 * バックアップ復元結果サマリ
 */
data class BackupRestoreSummary(
    val playlistCount: Int,
    val watchHistoryCount: Int,
    val searchHistoryCount: Int,
    val ngWordCount: Int,
)
