package com.github.stm7128.hycohyco.ui.library

import com.github.stm7128.hycohyco.domain.model.CloudLikeHistoryItem
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.CloudWatchHistoryItem
import com.github.stm7128.hycohyco.domain.model.DownloadedVideoItem
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.NicoruHistoryItem
import com.github.stm7128.hycohyco.domain.model.PlaylistVideoItem
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.domain.model.WatchHistoryItem

/**
 * ライブラリ画面内の大分類タブ種別
 */
enum class LibrarySubTab(val title: String) {
    HISTORY("履歴"),
    MYLIST("マイリスト"),
    DOWNLOADS("ダウンロード")
}

/**
 * 「履歴」サブタブ内の種別 (端末ローカル ＋ アカウント公式履歴4種)
 */
enum class HistorySubTab(val title: String) {
    LOCAL("端末履歴"),
    CLOUD_WATCH("視聴"),
    CLOUD_LIKE("いいね！"),
    CLOUD_NICORU_PASSIVE("ニコられた"),
    CLOUD_NICORU_ACTIVE("ニコった")
}

/**
 * 「マイリスト」タブ内のサブタブ種別 (ローカル / あとで見る / 公式マイリスト)
 */
enum class MylistSubTab(val title: String) {
    LOCAL("ローカル"),
    WATCH_LATER("あとで見る"),
    CLOUD_MYLIST("マイリスト")
}

/**
 * ライブラリ画面の UI 状態
 */
data class LibraryUiState(
    val selectedSubTab: LibrarySubTab = LibrarySubTab.HISTORY,
    val selectedHistorySubTab: HistorySubTab = HistorySubTab.LOCAL,
    val historyItems: List<WatchHistoryItem> = emptyList(),
    val playlists: List<LocalPlaylist> = emptyList(),
    val downloads: List<DownloadedVideoItem> = emptyList(),
    val totalDownloadSizeBytes: Long = 0L,
    val activePlaylist: LocalPlaylist? = null,
    val activePlaylistVideos: List<PlaylistVideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val selectedMylistSubTab: MylistSubTab = MylistSubTab.LOCAL,
    val watchLaterVideos: List<VideoSummary> = emptyList(),
    val cloudMylists: List<CloudMylist> = emptyList(),
    val activeCloudMylist: CloudMylist? = null,
    val activeCloudMylistVideos: List<VideoSummary> = emptyList(),
    val isCloudLoading: Boolean = false,

    // クラウド視聴履歴 (/v2/users/me/watch/history)
    val cloudWatchItems: List<CloudWatchHistoryItem> = emptyList(),
    val cloudWatchNextCursor: String? = null,
    val cloudWatchHasLoaded: Boolean = false,
    val isCloudWatchLoading: Boolean = false,
    val isCloudWatchLoadingMore: Boolean = false,
    val cloudWatchError: String? = null,

    // いいね！履歴 (/v1/users/me/likes)
    val cloudLikeItems: List<CloudLikeHistoryItem> = emptyList(),
    val cloudLikePage: Int = 1,
    val cloudLikeHasMore: Boolean = false,
    val cloudLikeHasLoaded: Boolean = false,
    val isCloudLikeLoading: Boolean = false,
    val isCloudLikeLoadingMore: Boolean = false,
    val cloudLikeError: String? = null,

    // ニコられた履歴 (/v1/users/me/nicoru/receive)
    val nicoruPassiveItems: List<NicoruHistoryItem> = emptyList(),
    val nicoruPassiveNextCursor: String? = null,
    val nicoruPassiveTotalCount: Int? = null,
    val nicoruByVideoOwner: Boolean = false,
    val nicoruPassiveHasLoaded: Boolean = false,
    val isNicoruPassiveLoading: Boolean = false,
    val isNicoruPassiveLoadingMore: Boolean = false,
    val nicoruPassiveError: String? = null,

    // ニコった履歴 (/v1/users/me/nicoru/send)
    val nicoruActiveItems: List<NicoruHistoryItem> = emptyList(),
    val nicoruActiveNextCursor: String? = null,
    val nicoruActiveHasLoaded: Boolean = false,
    val isNicoruActiveLoading: Boolean = false,
    val isNicoruActiveLoadingMore: Boolean = false,
    val nicoruActiveError: String? = null,
    val actionMessage: String? = null,
)
