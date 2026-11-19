package com.github.stm7128.hycohyco.ui.player

import com.github.stm7128.hycohyco.domain.model.CommentItem
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.player.danmaku.DanmakuRenderItem
import com.github.stm7128.hycohyco.player.model.PlaybackState

/**
 * プレイヤーの表示モード
 */

/**
 * 視聴画面下部の切り替えタブ
 */
enum class PlayerBottomTab(val label: String) {
    COMMENTS("コメント"),
    RECOMMENDS("関連動画")
}

enum class PlayerDisplayMode {
    /** 非表示（動画が選択されていない状態） */
    HIDDEN,

    /** ミニプレイヤーモード（画面下部のバーとして常駐） */
    MINI,

    /** 展開モード（画面全体で動画と詳細を表示） */
    EXPANDED,
}

/**
 * プレイヤー画面の全体 UI 状態
 */
data class PlayerUiState(
    /** 現在の表示モード */
    val displayMode: PlayerDisplayMode = PlayerDisplayMode.HIDDEN,

    /** 現在動画読み込み中かどうか */
    val isLoading: Boolean = false,

    /** コメント読み込み中かどうか */
    val isCommentLoading: Boolean = false,

    /** 再生対象の動画詳細情報 */
    val videoDetail: VideoDetail? = null,

    /** 再生エンジン (Media3) のリアルタイム再生状態 */
    val playbackState: PlaybackState = PlaybackState.Idle,

    /** 生のコメントリスト */
    val comments: List<CommentItem> = emptyList(),

    /** 衝突回避レーン計算済みの弾幕描画アイテム */
    val danmakuRenderItems: List<DanmakuRenderItem> = emptyList(),

    /** 弾幕の表示設定 (ON/OFF, 不透明度, スケール) */
    val danmakuSettings: DanmakuDisplaySettings = DanmakuDisplaySettings(),

    /** コメント NG フィルタ設定 */
    val ngConfig: CommentNgConfig = CommentNgConfig(),

    /** エラーメッセージ（存在する場合） */
    val errorMessage: String? = null,

    /** プレイヤー上のコントロールオーバーレイが表示されているか */
    val isControlsVisible: Boolean = true,

    /** 現在の再生速度倍率 */
    val playbackSpeed: Float = 1.0f,

    /** 1曲リピート再生が有効かどうか */
    val isRepeatOne: Boolean = false,

    /** 選択中の画質ID (null の場合は最高画質自動) */
    val selectedQualityId: String? = null,

    /** 再生設定ボトムシートが表示中かどうか */
    val isSettingsSheetVisible: Boolean = false,

    /** プレイリスト追加ボトムシートが表示中かどうか */
    val isAddToPlaylistSheetVisible: Boolean = false,

    /** 利用可能なローカルプレイリスト一覧 */
    val availablePlaylists: List<LocalPlaylist> = emptyList(),

    /** 連続再生キュー (動画IDのリスト) */
    val playbackQueue: List<String> = emptyList(),

    /** スライドジェスチャー（音量・輝度）の動作モード */
    val gestureMode: PlayerGestureMode = PlayerGestureMode.FULLSCREEN_ONLY,

    /** 現在オフライン保存データから再生中かどうか */
    val isOfflineMode: Boolean = false,

    /** 現在の動画に「いいね！」済みかどうか */
    val isLiked: Boolean = false,

    /** 現在の動画のいいね数 */
    val likeCount: Long = 0L,

    /** いいね送信中フラグ */
    val isLikeSubmitting: Boolean = false,

    /** 投稿者のお礼メッセージダイアログの表示テキスト (null の場合は非表示) */
    val likeThanksMessage: String? = null,

    /** 投稿者をフォロー中かどうか */
    val isOwnerFollowed: Boolean = false,

    /** フォロー通信中フラグ */
    val isFollowSubmitting: Boolean = false,

    /** コメント投稿ボトムシートが表示中かどうか */
    val isCommentInputSheetVisible: Boolean = false,

    /** コメント送信処理中フラグ */
    val isCommentSubmitting: Boolean = false,

    /** 次の動画の自動再生が有効かどうか */
    val isAutoplayEnabled: Boolean = true,

    /** 次の動画の自動再生カウントダウン残り秒数 (null の場合はカウントダウン非表示) */
    val nextVideoCountdownSeconds: Int? = null,

    /** 左右ダブルタップのシーク秒数 (デフォルト: 10秒) */
    val doubleTapSeekSeconds: Int = 10,

    /** ユーザーが作成した公式マイリスト一覧 (リスト保存シート用) */
    val userMylists: List<com.github.stm7128.hycohyco.domain.model.CloudMylist> = emptyList(),

    /** プレイリスト/マイリスト操作完了・エラーメッセージ */
    val playlistMessage: String? = null,

    /** いいね・フォロー・コメント等の一時的操作フィードバック・エラー通知メッセージ */
    val userFeedbackMessage: String? = null,

    /** 共有時に nicovideo.gay を使用するかどうか */
    val useNicovideoGay: Boolean = false,

    /** 視聴画面下部の選択中タブ (コメント / 関連動画) */
    val selectedBottomTab: PlayerBottomTab = PlayerBottomTab.COMMENTS,

    /** 関連動画 (レコメンド) リスト */
    val recommendVideos: List<VideoSummary> = emptyList(),

    /** 関連動画読み込み中かどうか */
    val isRecommendLoading: Boolean = false,

    /** スリープタイマーの動作モード */
    val sleepTimerMode: com.github.stm7128.hycohyco.player.SleepTimerMode = com.github.stm7128.hycohyco.player.SleepTimerMode.OFF,

    /** スリープタイマー自動停止までの残り秒数 */
    val sleepTimerRemainingSeconds: Int? = null,

    /** 他アプリの音と共存して同時再生するかどうか */
    val isCoexistAudio: Boolean = false,

    /** ホーム移動時に自動で PiP 小窓化するかどうか (false の場合はバックグラウンド＋スーパー島/通知へ直行) */
    val isAutoEnterPip: Boolean = true,

    /** 動画再生時に前回の続きから自動レジューム再生するかどうか */
    val isResumePlaybackEnabled: Boolean = true,

    /** 過去ログコメント表示中の対象日付ラベル (例: "2020/05/01", null = 最新コメント) */
    val pastLogDateLabel: String? = null,

    /** 動画タグの追加・削除通信中フラグ */
    val isTagSubmitting: Boolean = false,
)
