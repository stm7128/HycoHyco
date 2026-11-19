package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip

/**
 * チャプター情報
 *
 * @property timeMs 開始時刻 (ミリ秒)
 * @property label チャプターのタイトル・説明
 */
data class ChapterItem(
    val timeMs: Long,
    val label: String
)

/**
 * 概要欄からタイムスタンプ付きチャプター行を抽出するパーサー
 */
object ChapterParser {
    // 例: "#01:23 オープニング" や "12:34 サビ" や "1:15:30 ED"
    private val CHAPTER_LINE_REGEX = Regex("""(?:^|\n).*?(?:#)?(?:(\d{1,2}):)?(\d{1,2}):(\d{2})\s*(.*)""")
    private val TIME_MATCH_REGEX = Regex("""(?:#)?(?:(\d{1,2}):)?(\d{1,2}):(\d{2})""")

    fun parse(rawText: String?): List<ChapterItem> {
        if (rawText.isNullOrBlank()) return emptyList()

        // HTML タグの簡易除去
        val plainText = rawText.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]*>"), " ")

        val lines = plainText.split("\n")
        val chapters = mutableListOf<ChapterItem>()

        for (line in lines) {
            val match = TIME_MATCH_REGEX.find(line) ?: continue
            val timeStr = match.value.removePrefix("#")
            val parts = timeStr.split(":")
            val timeMs = when (parts.size) {
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

            // タイムスタンプ以降の文字列をチャプタータイトルとして抽出
            var label = line.substring(match.range.last + 1).trim()
            if (label.isBlank()) {
                label = line.substring(0, match.range.first).trim()
            }
            if (label.isBlank()) {
                label = timeStr
            }

            chapters.add(ChapterItem(timeMs = timeMs, label = label.take(30)))
        }

        return chapters.distinctBy { it.timeMs }.sortedBy { it.timeMs }
    }
}

/**
 * 概要欄のタイムスタンプ行から自動生成するチャプターバー
 */
@Composable
fun VideoChapterBar(
    description: String?,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters = remember(description) { ChapterParser.parse(description) }
    if (chapters.size < 2) return // 2つ以上ある場合のみチャプターバーを表示

    // 現在再生中のチャプターを特定
    val currentChapter = chapters.lastOrNull { it.timeMs <= currentPositionMs } ?: chapters.first()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            chapters.forEach { chapter ->
                val isSelected = chapter == currentChapter
                HycoFilterChip(
                    selected = isSelected,
                    onClick = { onSeekTo(chapter.timeMs) },
                    label = "${formatDuration(chapter.timeMs)} ${chapter.label}"
                )
            }
        }
    }
}
