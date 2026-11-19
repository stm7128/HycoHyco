package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoTextField

private enum class PositionChoice(val label: String, val command: String?) {
    NAKA("通常", null), UE("上", "ue"), SHITA("下", "shita")
}

private enum class SizeChoice(val label: String, val command: String?) {
    SMALL("小", "small"), MEDIUM("標準", null), BIG("大", "big")
}

private enum class ColorChoice(val label: String, val command: String?, val color: Color) {
    WHITE("白", null, Color.White), RED("赤", "red", Color(0xFFFF0000)),
    PINK("桃", "pink", Color(0xFFFF8080)), ORANGE("橙", "orange", Color(0xFFFFC000)),
    YELLOW("黄", "yellow", Color(0xFFFFFF00)), GREEN("緑", "green", Color(0xFF00FF00)),
    CYAN("水", "cyan", Color(0xFF00FFFF)), BLUE("青", "blue", Color(0xFF4080FF)),
    PURPLE("紫", "purple", Color(0xFFC000FF)), BLACK("黒", "black", Color(0xFF1A1A1A))
}

/**
 * ニコニコ完全ネイティブ コメント投稿ボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentInputSheet(
    onDismissRequest: () -> Unit,
    onSubmitComment: (body: String, commands: List<String>) -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf("") }
    var is184 by remember { mutableStateOf(true) }
    var selectedPosition by remember { mutableStateOf(PositionChoice.NAKA) }
    var selectedSize by remember { mutableStateOf(SizeChoice.MEDIUM) }
    var selectedColor by remember { mutableStateOf(ColorChoice.WHITE) }

    val handleSend = {
        if (text.isNotBlank() && !isSubmitting) {
            val commands = mutableListOf<String>()
            if (is184) commands.add("184")
            selectedPosition.command?.let { commands.add(it) }
            selectedSize.command?.let { commands.add(it) }
            selectedColor.command?.let { commands.add(it) }
            onSubmitComment(text.trim(), commands)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "コメントを投稿",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "閉じる",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // リアルタイムプレビュー
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF202020))
                    .padding(horizontal = 16.dp),
                contentAlignment = when (selectedPosition) {
                    PositionChoice.UE -> Alignment.TopCenter
                    PositionChoice.SHITA -> Alignment.BottomCenter
                    PositionChoice.NAKA -> Alignment.Center
                }
            ) {
                val isDarkColor = selectedColor == ColorChoice.BLACK
                Text(
                    text = if (text.isBlank()) "（プレビュー）" else text,
                    color = selectedColor.color,
                    fontSize = when (selectedSize) {
                        SizeChoice.SMALL -> 12.sp
                        SizeChoice.MEDIUM -> 15.sp
                        SizeChoice.BIG -> 20.sp
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = if (isDarkColor) {
                            androidx.compose.ui.graphics.Shadow(
                                color = Color.White,
                                blurRadius = 4f
                            )
                        } else null
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 184トグル
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "184（匿名）で投稿",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ユーザーIDを暗号化して送信します",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HycoSwitch(
                    checked = is184,
                    onCheckedChange = { is184 = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 位置セレクター
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("位置", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
                PositionChoice.values().forEach { pos ->
                    HycoFilterChip(selected = selectedPosition == pos, onClick = { selectedPosition = pos }, label = pos.label)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // サイズセレクター
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("サイズ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
                SizeChoice.values().forEach { sz ->
                    HycoFilterChip(selected = selectedSize == sz, onClick = { selectedSize = sz }, label = sz.label)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // カラーパレット
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorChoice.values().forEach { col ->
                    val isSelected = selectedColor == col
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(col.color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { selectedColor = col },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (col == ColorChoice.WHITE || col == ColorChoice.YELLOW) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // コメント入力 & 送信ボタン
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HycoTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "コメントを入力...",
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = handleSend, enabled = text.isNotBlank() && !isSubmitting) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "送信",
                            tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    }
}
