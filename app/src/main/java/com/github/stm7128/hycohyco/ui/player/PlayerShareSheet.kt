package com.github.stm7128.hycohyco.ui.player

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
import androidx.compose.material.icons.filled.Schedule
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
import com.github.stm7128.hycohyco.domain.model.VideoDetail

/**
 * 動画共有・URL/ID コピー用ボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerShareSheet(
    detail: VideoDetail,
    currentPositionMs: Long,
    useNicovideoGay: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentSeconds = (currentPositionMs / 1000L).coerceAtLeast(0L)
    val host = if (useNicovideoGay) "www.nicovideo.gay" else "www.nicovideo.jp"
    val pathType = if (detail.isShort) "shorts" else "watch"
    val baseVideoUrl = "https://$host/$pathType/${detail.id}"
    val timedVideoUrl = "$baseVideoUrl?from=$currentSeconds"

    val shareText = { text: String ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, detail.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "動画を共有"))
        onDismissRequest()
    }

    val copyToClipboard = { label: String, text: String ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "$label をコピーしました", Toast.LENGTH_SHORT).show()
        onDismissRequest()
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
                .padding(bottom = 28.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "動画の共有",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "閉じる")
                }
            }

            // 動画情報プレビュー
            Text(
                text = detail.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${detail.id}  •  ${detail.ownerName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. 通常共有 (タイトル + URL)
            ShareActionRow(
                icon = Icons.Default.Share,
                title = "通常共有",
                description = baseVideoUrl,
                onClick = { shareText("${detail.title} $baseVideoUrl") }
            )

            // 2. 現在の再生位置から共有 (?from=秒数)
            ShareActionRow(
                icon = Icons.Default.Schedule,
                title = "現在の再生位置から共有 (${formatDuration(currentPositionMs)})",
                description = timedVideoUrl,
                onClick = { shareText("${detail.title} (${formatDuration(currentPositionMs)}) $timedVideoUrl") }
            )

            // 3. URL をコピー
            ShareActionRow(
                icon = Icons.Default.ContentCopy,
                title = "URL をコピー",
                description = if (currentSeconds > 5L) "再生時間なし: $baseVideoUrl" else baseVideoUrl,
                onClick = { copyToClipboard("URL", baseVideoUrl) }
            )

            // 4. 動画 ID をコピー
            ShareActionRow(
                icon = Icons.Default.ContentCopy,
                title = "動画 ID をコピー",
                description = detail.id,
                onClick = { copyToClipboard("動画ID", detail.id) }
            )
        }
    }
}

@Composable
private fun ShareActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
