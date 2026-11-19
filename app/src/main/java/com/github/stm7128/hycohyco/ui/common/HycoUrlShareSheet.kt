package com.github.stm7128.hycohyco.ui.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme

/**
 * 共有・クリップボード操作ユーティリティ
 */
object HycoShareUtils {
    /** ユーザー公式共有 URL (常に nicovideo.jp を使用) */
    fun userUrl(userId: Long): String = "https://www.nicovideo.jp/user/$userId"

    /** マイリスト公式共有 URL (常に nicovideo.jp を使用) */
    fun mylistUrl(mylistId: Long): String = "https://www.nicovideo.jp/mylist/$mylistId"

    /** シリーズ公式共有 URL (常に nicovideo.jp を使用) */
    fun seriesUrl(seriesId: Long): String = "https://www.nicovideo.jp/series/$seriesId"

    /** テキストを Android 共有 Chooser で共有 */
    fun shareText(context: Context, subject: String, text: String, chooserTitle: String = "共有") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    /** クリップボードへコピーして Toast 表示 */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "$label をコピーしました", Toast.LENGTH_SHORT).show()
    }
}

/**
 * ユーザー / マイリスト共通の URL 共有・クリップボードコピー用ボトムシート
 *
 * @param headerTitle シート上部タイトル（例: "ユーザーを共有", "マイリストを共有"）
 * @param title 共有対象の表示名（例: ユーザー名、マイリスト名）
 * @param url 共有する URL（常に https://www.nicovideo.jp/...）
 * @param rawId コピー用の生 ID（例: "12345"）
 * @param idLabel ID の説明ラベル（例: "ユーザーID", "マイリストID"）
 * @param onDismissRequest シート閉じるコールバック
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HycoUrlShareSheet(
    headerTitle: String,
    title: String,
    url: String,
    rawId: String,
    idLabel: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HycoTheme.colors.onBackground
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "閉じる")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 対象情報カード
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = url,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. 他のアプリへ共有
            ShareActionRow(
                icon = Icons.Default.Share,
                title = "他のアプリへ共有",
                subtitle = "SNS やメッセージアプリで URL を共有",
                onClick = {
                    HycoShareUtils.shareText(
                        context = context,
                        subject = title,
                        text = "$title\n$url",
                        chooserTitle = headerTitle
                    )
                    onDismissRequest()
                }
            )

            // 2. URL をコピー
            ShareActionRow(
                icon = Icons.Default.ContentCopy,
                title = "URL をコピー",
                subtitle = url,
                onClick = {
                    HycoShareUtils.copyToClipboard(context, "$title のURL", url)
                    onDismissRequest()
                }
            )

            // 3. ID をコピー
            ShareActionRow(
                icon = Icons.Default.Numbers,
                title = "$idLabel をコピー",
                subtitle = rawId,
                onClick = {
                    HycoShareUtils.copyToClipboard(context, idLabel, rawId)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun ShareActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
