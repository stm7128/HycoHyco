package com.github.stm7128.hycohyco.domain.model

/**
 * 動画のメタデータドメインモデル
 *
 * @property id 動画ID (例: "sm9")
 * @property title 動画タイトル
 * @property description 概要文・説明文
 * @property durationSeconds 動画の長さ（秒）
 * @property thumbnailUrl サムネイル画像URL
 * @property viewCount 再生数
 * @property commentCount コメント数
 * @property mylistCount マイリスト登録数
 * @property likeCount いいね数
 * @property ownerName 投稿者名またはチャンネル名
 * @property ownerId 投稿者ID
 * @property ownerIconUrl 投稿者アイコン画像URL
 * @property registeredAt 動画投稿日時
 * @property availableQualities 選択可能な画質一覧
 */
data class VideoDetail(
    val id: String,
    val title: String,
    val description: String,
    val durationSeconds: Long,
    val thumbnailUrl: String,
    val viewCount: Long,
    val commentCount: Long,
    val mylistCount: Long,
    val likeCount: Long,
    val ownerName: String,
    val ownerId: Long = 0L,
    val ownerIconUrl: String = "",
    val registeredAt: String = "",
    val availableQualities: List<VideoQualityOption> = emptyList(),
    val isLiked: Boolean = false,
    val isOwnerFollowed: Boolean = false,
    val defaultThreadId: Long? = null,
    val nvCommentServer: String? = null,
    val series: VideoSeriesNav? = null,
    val contentType: String = "long",
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val baseVideo: BaseVideoInfo? = null,
    val tags: List<VideoTag> = emptyList(),
    val hasR18Tag: Boolean = false,
    val isTagEditable: Boolean = false,
    val tagUneditableReason: String? = null,
    val tagEditKey: String? = null
) {
    /** ニコニコショート対象動画かどうか */
    val isShort: Boolean
        get() = contentType.equals("short", ignoreCase = true) || id.startsWith("ss", ignoreCase = true)

    /** 縦長アスペクト比の動画かどうか */
    val isPortraitVideo: Boolean
        get() = (videoHeight > 0 && videoWidth > 0 && videoHeight > videoWidth) ||
            (videoWidth == 0 && videoHeight == 0 && isShort)
}

/**
 * 動画に設定されたタグ情報
 *
 * @property name タグ名
 * @property isCategory カテゴリタグかどうか
 * @property isCategoryCandidate カテゴリ候補タグかどうか
 * @property isNicodicArticleExists ニコニコ大百科記事が存在するか
 * @property isLocked ロックされている（通常ユーザーによる削除・編集不可）タグか
 * @property nicodicUrl ニコニコ大百科APIから解決された正規化記事URL（未解決時は null）
 * @property nicodicSummary ニコニコ大百科APIから取得した記事冒頭サマリー（未取得時は null）
 */
data class VideoTag(
    val name: String,
    val isCategory: Boolean = false,
    val isCategoryCandidate: Boolean = false,
    val isNicodicArticleExists: Boolean = false,
    val isLocked: Boolean = false,
    val nicodicUrl: String? = null,
    val nicodicSummary: String? = null
)

/** タグリスト更新時に、既存のニコニコ大百科判定結果（URL・要約含む）を引き継ぐ */
fun List<VideoTag>.mergeNicodicMetadataFrom(previous: List<VideoTag>): List<VideoTag> {
    if (previous.isEmpty()) return this
    val prevMap = previous.associateBy { it.name }
    return map { tag ->
        val prev = prevMap[tag.name]
        if (prev != null && !tag.isNicodicArticleExists && prev.isNicodicArticleExists) {
            tag.copy(
                isNicodicArticleExists = true,
                nicodicUrl = prev.nicodicUrl,
                nicodicSummary = prev.nicodicSummary
            )
        } else {
            tag
        }
    }
}

/**
 * ショート動画のピン留め本編動画情報 (data.baseVideo)
 */
data class BaseVideoInfo(
    val videoId: String,
    val contentType: String = "long",
    val title: String = "",
    val thumbnailUrl: String = ""
)

/**
 * 動画が所属するシリーズ情報と前後動画ナビゲーション
 */
data class VideoSeriesNav(
    val id: Long,
    val title: String,
    val prev: VideoSeriesItem? = null,
    val next: VideoSeriesItem? = null
)

/**
 * シリーズ内の前後動画情報
 */
data class VideoSeriesItem(
    val id: String,
    val title: String,
    val durationSeconds: Long = 0L,
    val thumbnailUrl: String = ""
)

/**
 * 動画画質オプション
 *
 * @property id 画質ID (例: "video-h264-720p")
 * @property label 表示用ラベル (例: "720p")
 * @property bitRate ビットレート (bps)
 * @property width 幅
 * @property height 高さ
 * @property isAvailable 現在のアカウント/ゲスト権限で利用可能か
 */
data class VideoQualityOption(
    val id: String,
    val label: String,
    val bitRate: Long,
    val width: Int,
    val height: Int,
    val isAvailable: Boolean
)

/**
 * HLS ストリーミング再生セッション情報
 *
 * @property videoId 対象動画ID
 * @property masterPlaylistUrl HLS マスタープレイリスト (.m3u8) のURL
 * @property domandBidCookie 認可に必要な Cookie (domand_bid)
 * @property selectedVideoQualityId 選択中の映像トラックID
 * @property selectedAudioQualityId 選択中の音声トラックID
 */
data class VideoStreamSession(
    val videoId: String,
    val masterPlaylistUrl: String,
    val domandBidCookie: String,
    val selectedVideoQualityId: String,
    val selectedAudioQualityId: String
)
