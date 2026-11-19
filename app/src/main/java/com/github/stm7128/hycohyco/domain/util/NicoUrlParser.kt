package com.github.stm7128.hycohyco.domain.util

/**
 * ニコニコ動画の URL・共有テキスト・クエリの解析結果
 */
sealed interface NicoParsedIntent {
    /**
     * 動画再生
     *
     * @property videoId 動画 ID (例: "sm9", "so12345")
     * @property startPositionMs 再生開始位置 (ミリ秒)
     */
    data class PlayVideo(
        val videoId: String,
        val startPositionMs: Long = 0L
    ) : NicoParsedIntent

    /**
     * ニコニコショート再生
     *
     * @property videoId 動画 ID (例: "ss46708330", "sm12345")
     * @property startPositionMs 再生開始位置 (ミリ秒)
     */
    data class PlayShort(
        val videoId: String,
        val startPositionMs: Long = 0L
    ) : NicoParsedIntent

    /**
     * ユーザープロフィール表示
     *
     * @property userId ユーザー ID (数値)
     */
    data class OpenUser(
        val userId: Long
    ) : NicoParsedIntent

    /**
     * マイリスト詳細表示
     *
     * @property mylistId マイリスト ID (数値)
     */
    data class OpenMylist(
        val mylistId: Long
    ) : NicoParsedIntent

    /**
     * シリーズ詳細表示
     *
     * @property seriesId シリーズ ID (数値)
     */
    data class OpenSeries(
        val seriesId: Long
    ) : NicoParsedIntent
}

/**
 * ニコニコの各種 URL、動画 ID、共有テキストを解析する純粋 Kotlin パーサー
 * (Android フレームワーク `android.net.Uri` に依存せず単体テスト可能)
 */
object NicoUrlParser {

    private val VIDEO_ID_REGEX = Regex("""(?i)\b(sm\d+|so\d+|nm\d+|ss\d+)\b""")
    private val USER_ID_REGEX = Regex("""(?i)\buser/(\d+)\b""")
    private val MYLIST_ID_REGEX = Regex("""(?i)\bmylist/(\d+)\b""")
    private val SERIES_ID_REGEX = Regex("""(?i)\bseries/(\d+)\b""")

    private fun buildVideoIntent(
        videoId: String,
        startPositionMs: Long = 0L,
        forceShort: Boolean = false
    ): NicoParsedIntent {
        val normalized = videoId.lowercase()
        return if (forceShort || normalized.startsWith("ss")) {
            NicoParsedIntent.PlayShort(normalized, startPositionMs)
        } else {
            NicoParsedIntent.PlayVideo(normalized, startPositionMs)
        }
    }

    /**
     * テキスト（URL、共有文字列、検索文字列など）を解析して [NicoParsedIntent] を返す
     */
    fun parse(text: String?): NicoParsedIntent? {
        if (text.isNullOrBlank()) return null
        val trimmed = text.trim()

        // 1. URL 形式の場合のパース
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return parseUrlString(trimmed)
        }

        // 2. テキスト内に URL が含まれている共有文字列の場合 (例: "タイトル https://nico.ms/sm12345")
        val urlMatch = Regex("""https?://[^\s]+""").find(trimmed)
        if (urlMatch != null) {
            val intentFromUrl = parseUrlString(urlMatch.value)
            if (intentFromUrl != null) return intentFromUrl
        }

        // 3. 単体の動画 ID (sm12345, so12345, nm12345, ss12345)
        val videoMatch = VIDEO_ID_REGEX.find(trimmed)
        if (videoMatch != null) {
            return buildVideoIntent(videoMatch.value)
        }

        // 4. ユーザー ID 形式 ("user/12345" または "user:12345")
        val userMatch = USER_ID_REGEX.find(trimmed)
        if (userMatch != null) {
            val uid = userMatch.groupValues[1].toLongOrNull()
            if (uid != null) return NicoParsedIntent.OpenUser(uid)
        }

        // 5. マイリスト ID 形式 ("mylist/12345")
        val mylistMatch = MYLIST_ID_REGEX.find(trimmed)
        if (mylistMatch != null) {
            val mid = mylistMatch.groupValues[1].toLongOrNull()
            if (mid != null) return NicoParsedIntent.OpenMylist(mid)
        }

        // 6. シリーズ ID 形式 ("series/12345")
        val seriesMatch = SERIES_ID_REGEX.find(trimmed)
        if (seriesMatch != null) {
            val sid = seriesMatch.groupValues[1].toLongOrNull()
            if (sid != null) return NicoParsedIntent.OpenSeries(sid)
        }

        return null
    }

    /**
     * URL 文字列を解析する (Android SDK 非依存)
     */
    fun parseUrlString(urlString: String): NicoParsedIntent? {
        val afterScheme = urlString.substringAfter("://", "")
        if (afterScheme.isEmpty()) return null

        val host = afterScheme.substringBefore("/").substringBefore("?").substringBefore("#").lowercase()
        val pathAndQuery = "/" + afterScheme.substringAfter("/", "")
        val path = pathAndQuery.substringBefore("?").substringBefore("#")
        val query = if (pathAndQuery.contains("?")) {
            pathAndQuery.substringAfter("?").substringBefore("#")
        } else {
            ""
        }

        // 開始時刻 (?from=120 や ?start=120 や ?from=01:23) の抽出
        val queryParams = query.split("&").mapNotNull {
            val idx = it.indexOf('=')
            if (idx > 0) it.substring(0, idx) to it.substring(idx + 1) else null
        }.toMap()
        val fromParam = queryParams["from"] ?: queryParams["start"]
        val startPositionMs = parseStartMs(fromParam)

        // 1. nicovideo.jp (www / sp 含む) および nicovideo.gay (動画のみ)
        if (host.contains("nicovideo.jp") || host.contains("nicovideo.gay")) {
            // ショート動画: /shorts/ssXXXX
            if (path.contains("/shorts/")) {
                val videoId = path.substringAfter("/shorts/").substringBefore("/").substringBefore("?")
                val match = VIDEO_ID_REGEX.find(videoId)
                if (match != null) {
                    return buildVideoIntent(match.value, startPositionMs, forceShort = true)
                }
            }

            // 通常動画: /watch/smXXXX
            if (path.contains("/watch/")) {
                val videoId = path.substringAfter("/watch/").substringBefore("/").substringBefore("?")
                val match = VIDEO_ID_REGEX.find(videoId)
                if (match != null) {
                    return buildVideoIntent(match.value, startPositionMs)
                }
            }

            // ユーザー: /user/123456 (nicovideo.gay は非対応のため公式 nicovideo.jp のみ)
            if (host.contains("nicovideo.jp") && path.contains("/user/")) {
                val userStr = path.substringAfter("/user/").substringBefore("/").substringBefore("?")
                val uid = userStr.toLongOrNull()
                if (uid != null) {
                    return NicoParsedIntent.OpenUser(uid)
                }
            }

            // マイリスト: /mylist/123456 (nicovideo.gay は非対応のため公式 nicovideo.jp のみ)
            if (host.contains("nicovideo.jp") && path.contains("/mylist/")) {
                val mylistStr = path.substringAfter("/mylist/").substringBefore("/").substringBefore("?")
                val mid = mylistStr.toLongOrNull()
                if (mid != null) {
                    return NicoParsedIntent.OpenMylist(mid)
                }
            }

            // シリーズ: /series/123456 (nicovideo.gay は非対応のため公式 nicovideo.jp のみ)
            if (host.contains("nicovideo.jp") && path.contains("/series/")) {
                val seriesStr = path.substringAfter("/series/").substringBefore("/").substringBefore("?")
                val sid = seriesStr.toLongOrNull()
                if (sid != null) {
                    return NicoParsedIntent.OpenSeries(sid)
                }
            }
        }

        // 2. 短縮 URL nico.ms
        if (host == "nico.ms") {
            val segment = path.removePrefix("/").substringBefore("/")

            // nico.ms/smXXXX or ssXXXX
            val videoMatch = VIDEO_ID_REGEX.find(segment)
            if (videoMatch != null) {
                return buildVideoIntent(videoMatch.value, startPositionMs)
            }

            // nico.ms/user/XXXX
            if (path.contains("/user/")) {
                val userStr = path.substringAfter("/user/").substringBefore("/")
                val uid = userStr.toLongOrNull()
                if (uid != null) {
                    return NicoParsedIntent.OpenUser(uid)
                }
            }

            // nico.ms/mylist/XXXX
            if (path.contains("/mylist/")) {
                val mylistStr = path.substringAfter("/mylist/").substringBefore("/")
                val mid = mylistStr.toLongOrNull()
                if (mid != null) {
                    return NicoParsedIntent.OpenMylist(mid)
                }
            }

            // nico.ms/series/XXXX
            if (path.contains("/series/")) {
                val seriesStr = path.substringAfter("/series/").substringBefore("/")
                val sid = seriesStr.toLongOrNull()
                if (sid != null) {
                    return NicoParsedIntent.OpenSeries(sid)
                }
            }
        }

        // 3. パス全体からのフォールバック抽出
        val fallbackVideo = VIDEO_ID_REGEX.find(path)
        if (fallbackVideo != null) {
            return buildVideoIntent(fallbackVideo.value, startPositionMs)
        }

        return null
    }

    /**
     * 秒数または "mm:ss" 形式の開始時間をミリ秒に変換する
     */
    private fun parseStartMs(param: String?): Long {
        if (param.isNullOrBlank()) return 0L
        val trimmed = param.trim()

        // 1. 純粋な秒数 (例: "120" や "120.5")
        trimmed.toDoubleOrNull()?.let {
            return (it * 1000).toLong().coerceAtLeast(0L)
        }

        // 2. "mm:ss" または "hh:mm:ss" 形式 (例: "01:23", "1:15:30")
        val parts = trimmed.split(":")
        return when (parts.size) {
            2 -> {
                val m = parts[0].toLongOrNull() ?: 0L
                val s = parts[1].toLongOrNull() ?: 0L
                (m * 60 + s) * 1000L
            }
            3 -> {
                val h = parts[0].toLongOrNull() ?: 0L
                val m = parts[1].toLongOrNull() ?: 0L
                val s = parts[2].toLongOrNull() ?: 0L
                (h * 3600 + m * 60 + s) * 1000L
            }
            else -> 0L
        }
    }
}
