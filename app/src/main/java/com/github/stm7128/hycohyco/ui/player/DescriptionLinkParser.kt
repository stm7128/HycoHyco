package com.github.stm7128.hycohyco.ui.player

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.util.regex.Pattern

/**
 * 概要欄リンクのアクション定義
 */
data class DescriptionLinkHandlers(
    val onPlayVideo: (String) -> Unit = {},
    val onSeekTo: (Long) -> Unit = {},
    val onUserClick: (Long) -> Unit = {},
    val onMylistClick: (Long) -> Unit = {},
    val onSeriesClick: (Long) -> Unit = {},
    val onOpenUrl: (String) -> Unit = {}
)

/**
 * ニコニコ概要欄 HTML の装飾パースおよびインライン直タップリンク生成ユーティリティ
 */
object DescriptionLinkParser {

    private val BR_REGEX = Pattern.compile("""<br\s*/?>""", Pattern.CASE_INSENSITIVE)
    private val TAG_REGEX = Pattern.compile("""<(/?)(\w+)([^>]*)>""", Pattern.CASE_INSENSITIVE)
    private val COLOR_ATTR_REGEX = Pattern.compile("""color=["']?([^"'\s>]+)["']?""", Pattern.CASE_INSENSITIVE)
    private val HREF_ATTR_REGEX = Pattern.compile("""href=["']?([^"'\s>]+)["']?", Pattern.CASE_INSENSITIVE""")

    // インラインリンク検出用正規表現
    private val VIDEO_ID_REGEX = Pattern.compile("""\b(?:sm|so|nm|ss)\d+\b""", Pattern.CASE_INSENSITIVE)
    private val TIME_SEEK_REGEX = Pattern.compile("""(?:#)?(\b\d{1,2}):(\d{2})(?::(\d{2}))?\b""")
    private val USER_LINK_REGEX = Pattern.compile("""(?:https?://(?:www\.)?nicovideo\.jp/)?user/(\d+)""", Pattern.CASE_INSENSITIVE)
    private val MYLIST_LINK_REGEX = Pattern.compile("""(?:https?://(?:www\.)?nicovideo\.jp/)?mylist/(\d+)""", Pattern.CASE_INSENSITIVE)
    private val SERIES_LINK_REGEX = Pattern.compile("""(?:https?://(?:www\.)?nicovideo\.jp/)?series/(\d+)""", Pattern.CASE_INSENSITIVE)
    private val URL_REGEX = Pattern.compile("""https?://[^\s<>"']+""")

    private val LINK_STYLE = TextLinkStyles(
        style = SpanStyle(
            color = Color(0xFF0095FF),
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Medium
        )
    )

    /**
     * 概要欄 HTML 文字列を、装飾スタイルとクリック可能リンクを持つ AnnotatedString へ変換する
     */
    fun parseDescription(
        rawHtml: String,
        handlers: DescriptionLinkHandlers
    ): AnnotatedString {
        if (rawHtml.isBlank()) return AnnotatedString("")

        // 1. 基本的な HTML タグの整形とプレーンテキスト＋スタイル範囲の抽出
        val (plainText, styles) = parseHtmlTags(rawHtml)

        // 2. プレーンテキストからインラインリンクを検出して合成
        return buildAnnotatedString {
            append(plainText)

            // HTMLタグから抽出したスタイルを適用
            styles.forEach { (range, style) ->
                if (range.first in 0 until length && range.last in 0 until length) {
                    addStyle(style, range.first, range.last + 1)
                }
            }

            // インラインリンクを検出して追加
            applyInlineLinks(plainText, handlers)
        }
    }

    private data class StyleSpan(val range: IntRange, val style: SpanStyle)

    private fun parseHtmlTags(html: String): Pair<String, List<StyleSpan>> {
        val brReplaced = BR_REGEX.matcher(html).replaceAll("\n")
        val matcher = TAG_REGEX.matcher(brReplaced)
        val sb = StringBuilder()
        val styles = mutableListOf<StyleSpan>()

        data class OpenTag(val tag: String, val startIdx: Int, val attr: String)
        val tagStack = mutableListOf<OpenTag>()
        var lastIdx = 0

        while (matcher.find()) {
            val textBefore = brReplaced.substring(lastIdx, matcher.start())
            sb.append(decodeEntities(textBefore))

            val isClosing = matcher.group(1) == "/"
            val tagName = matcher.group(2)?.lowercase().orEmpty()
            val attributes = matcher.group(3) ?: ""
            val currentIdx = sb.length

            if (!isClosing) {
                tagStack.add(OpenTag(tagName, currentIdx, attributes))
            } else {
                val openIdx = tagStack.indexOfLast { it.tag == tagName }
                if (openIdx != -1) {
                    val openTag = tagStack.removeAt(openIdx)
                    if (currentIdx > openTag.startIdx) {
                        val spanStyle = createSpanStyle(openTag.tag, openTag.attr)
                        if (spanStyle != null) {
                            styles.add(StyleSpan(openTag.startIdx until currentIdx, spanStyle))
                        }
                    }
                }
            }
            lastIdx = matcher.end()
        }

        if (lastIdx < brReplaced.length) {
            sb.append(decodeEntities(brReplaced.substring(lastIdx)))
        }

        return Pair(sb.toString(), styles)
    }

    private fun createSpanStyle(tag: String, attributes: String): SpanStyle? {
        return when (tag) {
            "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
            "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
            "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
            "s", "strike", "del" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            "font" -> {
                val m = COLOR_ATTR_REGEX.matcher(attributes)
                if (m.find()) {
                    val colorStr = m.group(1).orEmpty()
                    val parsedColor = parseColor(colorStr)
                    if (parsedColor != null) SpanStyle(color = parsedColor) else null
                } else null
            }
            else -> null
        }
    }

    private fun parseColor(colorStr: String): Color? {
        return runCatching {
            val c = android.graphics.Color.parseColor(colorStr)
            Color(c)
        }.getOrNull()
    }

    private fun decodeEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
    }

    private fun AnnotatedString.Builder.applyInlineLinks(
        text: String,
        handlers: DescriptionLinkHandlers
    ) {
        // 1. 動画ID (sm1234 等)
        val videoMatcher = VIDEO_ID_REGEX.matcher(text)
        while (videoMatcher.find()) {
            val id = videoMatcher.group()
            val link = LinkAnnotation.Clickable(
                tag = "video:$id",
                styles = LINK_STYLE,
                linkInteractionListener = { handlers.onPlayVideo(id) }
            )
            addLink(link, videoMatcher.start(), videoMatcher.end())
        }

        // 2. 時間シーク (#01:23 / 1:23 等)
        val timeMatcher = TIME_SEEK_REGEX.matcher(text)
        while (timeMatcher.find()) {
            val p1 = timeMatcher.group(1)?.toLongOrNull() ?: 0L
            val p2 = timeMatcher.group(2)?.toLongOrNull() ?: 0L
            val p3 = timeMatcher.group(3)?.toLongOrNull()
            val totalSeconds = if (p3 != null) (p1 * 3600) + (p2 * 60) + p3 else (p1 * 60) + p2
            val posMs = totalSeconds * 1000L

            val link = LinkAnnotation.Clickable(
                tag = "seek:$posMs",
                styles = LINK_STYLE,
                linkInteractionListener = { handlers.onSeekTo(posMs) }
            )
            addLink(link, timeMatcher.start(), timeMatcher.end())
        }

        // 3. ユーザーリンク (user/12345)
        val userMatcher = USER_LINK_REGEX.matcher(text)
        while (userMatcher.find()) {
            val uid = userMatcher.group(1)?.toLongOrNull() ?: continue
            val link = LinkAnnotation.Clickable(
                tag = "user:$uid",
                styles = LINK_STYLE,
                linkInteractionListener = { handlers.onUserClick(uid) }
            )
            addLink(link, userMatcher.start(), userMatcher.end())
        }

        // 4. マイリストリンク (mylist/12345)
        val mylistMatcher = MYLIST_LINK_REGEX.matcher(text)
        while (mylistMatcher.find()) {
            val mid = mylistMatcher.group(1)?.toLongOrNull() ?: continue
            val link = LinkAnnotation.Clickable(
                tag = "mylist:$mid",
                styles = LINK_STYLE,
                linkInteractionListener = { handlers.onMylistClick(mid) }
            )
            addLink(link, mylistMatcher.start(), mylistMatcher.end())
        }

        // 5. 一般 URL
        val urlMatcher = URL_REGEX.matcher(text)
        while (urlMatcher.find()) {
            val rawUrl = urlMatcher.group()
            if (!rawUrl.contains("/watch/") && !rawUrl.contains("/user/") && !rawUrl.contains("/mylist/")) {
                val link = LinkAnnotation.Clickable(
                    tag = "url:$rawUrl",
                    styles = LINK_STYLE,
                    linkInteractionListener = { handlers.onOpenUrl(rawUrl) }
                )
                addLink(link, urlMatcher.start(), urlMatcher.end())
            }
        }
    }
}
