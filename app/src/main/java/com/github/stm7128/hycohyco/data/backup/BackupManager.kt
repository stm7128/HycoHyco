package com.github.stm7128.hycohyco.data.backup

import android.content.Context
import android.net.Uri
import com.github.stm7128.hycohyco.data.local.NgPreferencesDataSource
import com.github.stm7128.hycohyco.data.local.PlayerPreferencesDataSource
import com.github.stm7128.hycohyco.data.local.ThemePreferencesDataSource
import com.github.stm7128.hycohyco.data.local.db.HycoDatabaseHelper
import com.github.stm7128.hycohyco.data.local.db.importPlaylistsBatch
import com.github.stm7128.hycohyco.data.local.db.importSearchHistoryBatch
import com.github.stm7128.hycohyco.data.local.db.importWatchHistoryBatch
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.domain.model.PlaylistVideoItem
import com.github.stm7128.hycohyco.domain.model.QualityPreference
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.domain.model.WatchHistoryItem
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository
import com.github.stm7128.hycohyco.domain.model.DanmakuAreaMode
import com.github.stm7128.hycohyco.domain.model.DanmakuDisplaySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * アプリ内全データの一括 JSON バックアップおよび復元を司るマネージャー
 */
class BackupManager(
    private val dbHelper: HycoDatabaseHelper,
    private val themePreferences: ThemePreferencesDataSource,
    private val playerPreferences: PlayerPreferencesDataSource,
    private val ngPreferences: NgPreferencesDataSource,
    private val libraryRepository: LibraryRepository? = null,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * 全設定・プレイリスト・視聴履歴・検索履歴を JSON として指定 URI へ書き出す
     */
    suspend fun exportBackup(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val theme = themePreferences.themeSettings.first()
            val ng = ngPreferences.ngConfig.first()
            val gesture = playerPreferences.gestureMode.first()
            val autoplay = playerPreferences.autoplayEnabled.first()
            val doubleTap = playerPreferences.doubleTapSeekSeconds.first()
            val gay = playerPreferences.useNicovideoGay.first()
            val wifiQ = playerPreferences.defaultQualityWifi.first()
            val cellQ = playerPreferences.defaultQualityCellular.first()
            val coexistAudio = playerPreferences.coexistAudio.first()
            val autoEnterPip = playerPreferences.autoEnterPip.first()
            val showShortsTab = playerPreferences.showShortsBottomTab.first()
            val resumePlaybackEnabled = playerPreferences.isResumePlaybackEnabled.first()
            val danmaku = playerPreferences.danmakuSettings.first()

            val playlists = dbHelper.getAllPlaylists().map { pl ->
                val items = dbHelper.getPlaylistVideos(pl.id).map { item ->
                    BackupPlaylistItemDto(
                        videoId = item.videoId,
                        title = item.title,
                        thumbnailUrl = item.thumbnailUrl,
                        durationMs = item.durationMs,
                        orderIndex = item.orderIndex,
                        addedAt = item.addedAt,
                        ownerName = item.ownerName,
                        viewCounter = item.viewCounter,
                        commentCounter = item.commentCounter,
                        likeCounter = item.likeCounter,
                        mylistCounter = item.mylistCounter,
                    )
                }
                BackupPlaylistDto(
                    id = pl.id,
                    title = pl.title,
                    createdAt = pl.createdAt,
                    updatedAt = pl.updatedAt,
                    items = items
                )
            }

            val history = dbHelper.getAllWatchHistory().map { h ->
                BackupWatchHistoryDto(
                    videoId = h.videoId,
                    title = h.title,
                    thumbnailUrl = h.thumbnailUrl,
                    durationMs = h.durationMs,
                    lastPositionMs = h.lastPositionMs,
                    lastWatchedAt = h.lastWatchedAt,
                    ownerName = h.ownerName,
                    viewCounter = h.viewCounter,
                    commentCounter = h.commentCounter,
                    likeCounter = h.likeCounter,
                    mylistCounter = h.mylistCounter,
                )
            }

            val search = dbHelper.getAllSearchHistory().map { s ->
                BackupSearchHistoryDto(
                    keyword = s.keyword,
                    isPinned = s.isPinned,
                    searchedAt = s.searchedAt
                )
            }

            val root = BackupRootDto(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                themeSettings = BackupThemeDto(
                    themeMode = theme.themeMode.name,
                    darkThemeConfig = theme.darkThemeConfig.name,
                    isDynamicColor = theme.isDynamicColor,
                    isAmoledBlack = theme.isAmoledBlack,
                    defaultHomeMode = theme.defaultHomeMode.name,
                ),
                playerSettings = BackupPlayerDto(
                    gestureMode = gesture.name,
                    isAutoplayEnabled = autoplay,
                    doubleTapSeekSeconds = doubleTap,
                    useNicovideoGay = gay,
                    defaultQualityWifi = wifiQ.name,
                    defaultQualityCellular = cellQ.name,
                    coexistAudio = coexistAudio,
                    autoEnterPip = autoEnterPip,
                    showShortsBottomTab = showShortsTab,
                    isResumePlaybackEnabled = resumePlaybackEnabled,
                ),
                danmakuSettings = BackupDanmakuDto(
                    isEnabled = danmaku.isEnabled,
                    alpha = danmaku.alpha,
                    scale = danmaku.scale,
                    speedScale = danmaku.speedScale,
                    areaMode = danmaku.areaMode.name
                ),
                ngConfig = BackupNgDto(
                    hide184Comments = ng.hide184Comments,
                    minScoreThreshold = ng.minScoreThreshold,
                    ngUserIds = ng.ngUserIds,
                    ngWords = ng.ngWords,
                    ngRegexPatterns = ng.ngRegexes.map { it.pattern }
                ),
                playlists = playlists,
                watchHistory = history,
                searchHistory = search
            )

            val jsonString = json.encodeToString(root)
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(jsonString.toByteArray(Charsets.UTF_8))
            } ?: throw IllegalStateException("出力ストリームを開けませんでした")

            playlists.size + history.size
        }
    }

    /**
     * 指定 URI の JSON ファイルからデータを復元する
     */
    suspend fun importBackup(context: Context, uri: Uri): Result<BackupRestoreSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { ins ->
                BufferedReader(InputStreamReader(ins, Charsets.UTF_8)).readText()
            } ?: throw IllegalStateException("入力ストリームを開けませんでした")

            val root = json.decodeFromString<BackupRootDto>(jsonString)

            // 1. テーマ設定の復元
            val themeMode = runCatching { AppThemeMode.valueOf(root.themeSettings.themeMode) }.getOrDefault(AppThemeMode.HYPER_OS)
            val darkConfig = runCatching { DarkThemeConfig.valueOf(root.themeSettings.darkThemeConfig) }.getOrDefault(DarkThemeConfig.FOLLOW_SYSTEM)
            themePreferences.setThemeMode(themeMode)
            themePreferences.setDarkThemeConfig(darkConfig)
            themePreferences.setDynamicColor(root.themeSettings.isDynamicColor)
            themePreferences.setAmoledBlack(root.themeSettings.isAmoledBlack)
            root.themeSettings.defaultHomeMode?.let {
                themePreferences.setDefaultHomeMode(HomeMode.fromName(it))
            }

            // 2. プレイヤー設定の復元
            playerPreferences.setGestureMode(PlayerGestureMode.fromName(root.playerSettings.gestureMode))
            playerPreferences.setAutoplayEnabled(root.playerSettings.isAutoplayEnabled)
            playerPreferences.setDoubleTapSeekSeconds(root.playerSettings.doubleTapSeekSeconds)
            playerPreferences.setUseNicovideoGay(root.playerSettings.useNicovideoGay)
            playerPreferences.setDefaultQualityWifi(QualityPreference.fromName(root.playerSettings.defaultQualityWifi))
            playerPreferences.setDefaultQualityCellular(QualityPreference.fromName(root.playerSettings.defaultQualityCellular))
            playerPreferences.setCoexistAudio(root.playerSettings.coexistAudio)
            playerPreferences.setAutoEnterPip(root.playerSettings.autoEnterPip)
            playerPreferences.setShowShortsBottomTab(root.playerSettings.showShortsBottomTab)
            playerPreferences.setResumePlaybackEnabled(root.playerSettings.isResumePlaybackEnabled)

            // 2.5 弾幕設定の復元
            playerPreferences.setDanmakuSettings(
                DanmakuDisplaySettings(
                    isEnabled = root.danmakuSettings.isEnabled,
                    alpha = root.danmakuSettings.alpha.coerceIn(0.1f, 1.0f),
                    scale = root.danmakuSettings.scale.coerceIn(0.5f, 2.0f),
                    speedScale = root.danmakuSettings.speedScale.coerceIn(0.5f, 2.0f),
                    areaMode = DanmakuAreaMode.fromName(root.danmakuSettings.areaMode)
                )
            )

            // 3. NG設定の復元
            ngPreferences.saveNgConfig(
                CommentNgConfig(
                    minScoreThreshold = root.ngConfig.minScoreThreshold,
                    ngWords = root.ngConfig.ngWords,
                    ngRegexes = root.ngConfig.ngRegexPatterns.mapNotNull { runCatching { Regex(it) }.getOrNull() },
                    ngUserIds = root.ngConfig.ngUserIds,
                    hide184Comments = root.ngConfig.hide184Comments
                )
            )

            // 4. プレイリスト・視聴履歴・検索履歴のトランザクション一括復元
            dbHelper.importPlaylistsBatch(root.playlists)
            dbHelper.importWatchHistoryBatch(root.watchHistory)
            dbHelper.importSearchHistoryBatch(root.searchHistory)

            // 7. 復元完了時に Flow 更新シグナルを発火し、全画面に即座に反映
            libraryRepository?.notifyDataChanged()

            BackupRestoreSummary(
                playlistCount = root.playlists.size,
                watchHistoryCount = root.watchHistory.size,
                searchHistoryCount = root.searchHistory.size,
                ngWordCount = root.ngConfig.ngWords.size + root.ngConfig.ngUserIds.size
            )
        }
    }
}
