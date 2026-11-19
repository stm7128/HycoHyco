package com.github.stm7128.hycohyco.data.download

import java.util.Locale

/**
 * ダウンロードした fMP4 メディア用のローカル HLS (BYTERANGE VOD) プレイリスト生成ユーティリティ
 */
object LocalHlsPlaylistGenerator {

    /** セグメントのバイト範囲と再生時間情報 */
    data class ByteRangeSegment(
        val durationSeconds: Double,
        val byteLength: Long,
        val byteOffset: Long
    )

    /**
     * 単一メディアストリーム（映像または音声）の VOD プレイリスト (video.m3u8 または audio.m3u8) の内容を生成する
     *
     * @param mediaFileName メディアファイル名 (例: "video.mp4", "audio.m4a")
     * @param initByteLength 初期化セグメント (ftyp/moov) のバイト長 (0 の場合は出力しない)
     * @param segments 各メディアセグメントのバイト範囲と再生時間リスト
     * @param targetDuration 最大セグメント長（秒）
     * @return HLS メディアプレイリスト文字列
     */
    fun generateMediaPlaylist(
        mediaFileName: String,
        initByteLength: Long,
        segments: List<ByteRangeSegment>,
        targetDuration: Int = 6
    ): String {
        val sb = StringBuilder()
        sb.append("#EXTM3U\n")
        sb.append("#EXT-X-VERSION:4\n")
        sb.append("#EXT-X-TARGETDURATION:$targetDuration\n")
        sb.append("#EXT-X-MEDIA-SEQUENCE:0\n")
        sb.append("#EXT-X-PLAYLIST-TYPE:VOD\n")

        if (initByteLength > 0L) {
            sb.append("#EXT-X-MAP:URI=\"$mediaFileName\",BYTERANGE=\"$initByteLength@0\"\n")
        }

        for (seg in segments) {
            sb.append("#EXTINF:${String.format(Locale.US, "%.6f", seg.durationSeconds)},\n")
            sb.append("#EXT-X-BYTERANGE:${seg.byteLength}@${seg.byteOffset}\n")
            sb.append("$mediaFileName\n")
        }

        sb.append("#EXT-X-ENDLIST\n")
        return sb.toString()
    }

    /**
     * 映像ストリームと音声ストリームを結合するマスタープレイリスト (master.m3u8) の内容を生成する
     *
     * @param hasVideo 映像ストリームが存在するか
     * @param videoPlaylistName 映像プレイリストファイル名
     * @param audioPlaylistName 音声プレイリストファイル名
     * @param bandwidth 推定帯域幅 (bps)
     * @return HLS マスタープレイリスト文字列
     */
    fun generateMasterPlaylist(
        hasVideo: Boolean,
        videoPlaylistName: String = "video.m3u8",
        audioPlaylistName: String = "audio.m3u8",
        bandwidth: Long = 2000000L
    ): String {
        val sb = StringBuilder()
        sb.append("#EXTM3U\n")
        sb.append("#EXT-X-VERSION:4\n")

        if (hasVideo) {
            sb.append("#EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID=\"audio\",NAME=\"audio\",DEFAULT=YES,AUTOSELECT=YES,URI=\"$audioPlaylistName\"\n")
            sb.append("#EXT-X-STREAM-INF:BANDWIDTH=$bandwidth,AUDIO=\"audio\"\n")
            sb.append("$videoPlaylistName\n")
        } else {
            sb.append("#EXT-X-STREAM-INF:BANDWIDTH=128000\n")
            sb.append("$audioPlaylistName\n")
        }

        return sb.toString()
    }
}
