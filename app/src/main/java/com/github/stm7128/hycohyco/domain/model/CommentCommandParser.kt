package com.github.stm7128.hycohyco.domain.model

/**
 * ニコニココメントのコマンド文字列解析結果
 */
data class ParsedCommentCommands(
    val position: CommentPosition = CommentPosition.NAKA,
    val size: CommentSize = CommentSize.MEDIUM,
    val colorInt: Int = 0xFFFFFFFF.toInt(),
    val font: CommentFont = CommentFont.DEFONT,
    val isTranslucent: Boolean = false,
    val is184: Boolean = false
)

/**
 * ニコニコ動画のコメントコマンドパーサー
 */
object CommentCommandParser {

    // ニコニコ公式カラーテーブル
    private val COLOR_MAP = mapOf(
        // 標準カラー
        "white" to 0xFFFFFFFF.toInt(),
        "red" to 0xFFFF0000.toInt(),
        "pink" to 0xFFFF8080.toInt(),
        "orange" to 0xFFFFC000.toInt(),
        "yellow" to 0xFFFFFF00.toInt(),
        "green" to 0xFF00FF00.toInt(),
        "cyan" to 0xFF00FFFF.toInt(),
        "blue" to 0xFF0000FF.toInt(),
        "purple" to 0xFFC000FF.toInt(),
        "black" to 0xFF000000.toInt(),

        // プレミアムカラー
        "white2" to 0xFFCCCC99.toInt(),
        "niconicowhite" to 0xFFCCCC99.toInt(),
        "red2" to 0xFFCC0033.toInt(),
        "truered" to 0xFFCC0033.toInt(),
        "pink2" to 0xFFFF33CC.toInt(),
        "orange2" to 0xFFFF6600.toInt(),
        "passionorange" to 0xFFFF6600.toInt(),
        "yellow2" to 0xFF999900.toInt(),
        "madyellow" to 0xFF999900.toInt(),
        "green2" to 0xFF00CC66.toInt(),
        "elementalgreen" to 0xFF00CC66.toInt(),
        "cyan2" to 0xFF00CCCC.toInt(),
        "blue2" to 0xFF3399FF.toInt(),
        "marineblue" to 0xFF3399FF.toInt(),
        "purple2" to 0xFF6633CC.toInt(),
        "nobleviolet" to 0xFF6633CC.toInt(),
        "black2" to 0xFF666666.toInt()
    )

    /**
     * コマンド文字列リストを解析してプロパティ群を抽出する
     *
     * @param commands コマンドリスト (例: ["184", "shita", "red", "big"])
     * @return [ParsedCommentCommands]
     */
    fun parse(commands: List<String>): ParsedCommentCommands {
        var position = CommentPosition.NAKA
        var size = CommentSize.MEDIUM
        var colorInt = 0xFFFFFFFF.toInt()
        var font = CommentFont.DEFONT
        var isTranslucent = false
        var is184 = false

        for (cmd in commands) {
            val lower = cmd.trim().lowercase()

            when (lower) {
                // 位置
                "ue" -> position = CommentPosition.UE
                "shita" -> position = CommentPosition.SHITA
                "naka" -> position = CommentPosition.NAKA

                // サイズ
                "big" -> size = CommentSize.BIG
                "small" -> size = CommentSize.SMALL
                "medium" -> size = CommentSize.MEDIUM

                // フォント
                "mincho" -> font = CommentFont.MINCHO
                "gothic", "defont" -> font = CommentFont.DEFONT

                // 透過
                "_live", "translucent" -> isTranslucent = true

                // 184
                "184" -> is184 = true

                // カラー解析 (プリセットまたは #HEX)
                else -> {
                    val presetColor = COLOR_MAP[lower]
                    if (presetColor != null) {
                        colorInt = presetColor
                    } else if (lower.startsWith("#")) {
                        try {
                            colorInt = parseHexColor(lower)
                        } catch (_: Exception) {
                            // パース失敗時は色を変更しない
                        }
                    }
                }
            }
        }

        return ParsedCommentCommands(
            position = position,
            size = size,
            colorInt = colorInt,
            font = font,
            isTranslucent = isTranslucent,
            is184 = is184
        )
    }

    /**
     * "#RRGGBB", "#RGB", または "#AARRGGBB" 形式のカラーコードを ARGB Int に変換する
     */
    private fun parseHexColor(hex: String): Int {
        val clean = hex.removePrefix("#")
        return try {
            when (clean.length) {
                3 -> {
                    // #RGB -> #RRGGBB (不透明)
                    val r = clean[0].toString().repeat(2).toInt(16)
                    val g = clean[1].toString().repeat(2).toInt(16)
                    val b = clean[2].toString().repeat(2).toInt(16)
                    (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
                6 -> {
                    // #RRGGBB (不透明)
                    val rgb = clean.toInt(16)
                    (0xFF shl 24) or rgb
                }
                8 -> {
                    // #AARRGGBB
                    clean.toLong(16).toInt()
                }
                else -> 0xFFFFFFFF.toInt()
            }
        } catch (_: Exception) {
            0xFFFFFFFF.toInt()
        }
    }
}
