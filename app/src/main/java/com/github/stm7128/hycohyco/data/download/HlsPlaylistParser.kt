package com.github.stm7128.hycohyco.data.download

/**
 * HLS (.m3u8) プレイリスト解析用ユーティリティ
 */
object HlsPlaylistParser {

    /** 暗号化キー情報 (AES-128) */
    data class EncryptionKeyInfo(
        val method: String,
        val keyUrl: String,
        val ivHex: String?
    )

    /** サブプレイリスト情報 */
    data class StreamInfo(
        val url: String,
        val bandwidth: Long = 0L,
        val resolution: String = "",
        val isAudioOnly: Boolean = false
    )

    /** セグメント項目 (URL と正確な再生時間) */
    data class SegmentItem(
        val url: String,
        val durationSeconds: Double
    )

    /** メディアセグメント一覧情報 */
    data class MediaSegments(
        val initSegmentUrl: String?,
        val segmentUrls: List<String>,
        val segments: List<SegmentItem> = emptyList(),
        val keyInfo: EncryptionKeyInfo? = null,
        val mediaSequence: Long = 0L,
        val targetDuration: Int = 6
    )

    /**
     * マスタープレイリストから映像ストリームURLと音声ストリームURLの両方を抽出する
     *
     * @return Pair<videoStreamUrl?, audioStreamUrl?>
     */
    fun parseMasterStreams(
        masterPlaylistContent: String,
        baseUrl: String
    ): Pair<String?, String?> {
        val lines = masterPlaylistContent.lines()
        val videoStreams = mutableListOf<StreamInfo>()
        val audioStreams = mutableListOf<StreamInfo>()

        var expectingStreamUri = false
        var currentBandwidth = 0L
        var currentResolution = ""

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.startsWith("#EXT-X-STREAM-INF:")) {
                currentBandwidth = extractAttribute(line, "BANDWIDTH")?.toLongOrNull() ?: 0L
                currentResolution = extractAttribute(line, "RESOLUTION") ?: ""
                expectingStreamUri = true
            } else if (expectingStreamUri && line.isNotBlank() && !line.startsWith("#")) {
                val resolvedUrl = resolveUrl(baseUrl, line)
                videoStreams.add(
                    StreamInfo(
                        url = resolvedUrl,
                        bandwidth = currentBandwidth,
                        resolution = currentResolution,
                        isAudioOnly = false
                    )
                )
                currentBandwidth = 0L
                currentResolution = ""
                expectingStreamUri = false
            } else if (line.startsWith("#EXT-X-MEDIA:TYPE=AUDIO")) {
                val uri = extractAttribute(line, "URI")
                if (uri != null) {
                    audioStreams.add(
                        StreamInfo(
                            url = resolveUrl(baseUrl, uri),
                            bandwidth = 128000L,
                            resolution = "audio",
                            isAudioOnly = true
                        )
                    )
                }
            }
        }

        // 映像: 720p を優先し、なければ最高ビットレート
        val selectedVideo = videoStreams.firstOrNull { it.resolution.contains("720") }?.url
            ?: videoStreams.maxByOrNull { it.bandwidth }?.url
            ?: videoStreams.firstOrNull()?.url

        // 音声: 最初の音声ストリーム
        val selectedAudio = audioStreams.firstOrNull()?.url

        return Pair(selectedVideo, selectedAudio)
    }

    /**
     * マスタープレイリストから最適なストリーム URL を抽出する (後方互換)
     */
    fun parseMasterPlaylist(
        masterPlaylistContent: String,
        baseUrl: String,
        preferAudioOnly: Boolean = false
    ): String? {
        val (video, audio) = parseMasterStreams(masterPlaylistContent, baseUrl)
        return if (preferAudioOnly) audio ?: video else video ?: audio
    }

    /**
     * メディアプレイリストから初期化セグメント、暗号化キー、全セグメントURLおよび再生時間を抽出する
     */
    fun parseMediaPlaylist(mediaPlaylistContent: String, baseUrl: String): MediaSegments {
        val lines = mediaPlaylistContent.lines()
        var initSegmentUrl: String? = null
        var keyInfo: EncryptionKeyInfo? = null
        var mediaSequence = 0L
        var targetDuration = 6
        var currentDuration = 6.0
        val segmentUrls = mutableListOf<String>()
        val segments = mutableListOf<SegmentItem>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXT-X-TARGETDURATION:")) {
                targetDuration = trimmed.substringAfter(":").trim().toIntOrNull() ?: 6
            } else if (trimmed.startsWith("#EXT-X-MEDIA-SEQUENCE:")) {
                mediaSequence = trimmed.substringAfter(":").trim().toLongOrNull() ?: 0L
            } else if (trimmed.startsWith("#EXTINF:")) {
                currentDuration = trimmed.substringAfter(":").substringBefore(",").trim().toDoubleOrNull() ?: 6.0
            } else if (trimmed.startsWith("#EXT-X-KEY:")) {
                val method = extractAttribute(trimmed, "METHOD") ?: "NONE"
                val uri = extractAttribute(trimmed, "URI")
                val iv = extractAttribute(trimmed, "IV")
                if (method == "AES-128" && uri != null) {
                    keyInfo = EncryptionKeyInfo(
                        method = method,
                        keyUrl = resolveUrl(baseUrl, uri),
                        ivHex = iv
                    )
                }
            } else if (trimmed.startsWith("#EXT-X-MAP:")) {
                val uri = extractAttribute(trimmed, "URI")
                if (uri != null) {
                    initSegmentUrl = resolveUrl(baseUrl, uri)
                }
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                val resolved = resolveUrl(baseUrl, trimmed)
                segmentUrls.add(resolved)
                segments.add(SegmentItem(url = resolved, durationSeconds = currentDuration))
            }
        }

        return MediaSegments(
            initSegmentUrl = initSegmentUrl,
            segmentUrls = segmentUrls,
            segments = segments,
            keyInfo = keyInfo,
            mediaSequence = mediaSequence,
            targetDuration = targetDuration
        )
    }

    private fun extractAttribute(line: String, attributeName: String): String? {
        val pattern = Regex("""$attributeName=(?:"([^"]*)"|([^,\s]+))""")
        val match = pattern.find(line) ?: return null
        return match.groupValues[1].ifEmpty { match.groupValues[2] }
    }

    fun resolveUrl(baseUrl: String, relativeOrAbsoluteUrl: String): String {
        if (relativeOrAbsoluteUrl.startsWith("http://") || relativeOrAbsoluteUrl.startsWith("https://")) {
            return relativeOrAbsoluteUrl
        }
        val cleanBase = baseUrl.substringBefore("?").substringBeforeLast("/")
        val query = if (baseUrl.contains("?")) "?" + baseUrl.substringAfter("?") else ""
        return "$cleanBase/$relativeOrAbsoluteUrl$query"
    }
}
