package com.github.stm7128.hycohyco.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.util.NicoParsedIntent
import com.github.stm7128.hycohyco.domain.util.NicoUrlParser

/**
 * 検索バーに動画 ID や URL が入力された際に表示する直接再生ショートカットカード
 */
@Composable
fun DirectVideoJumpCard(
    query: String,
    onPlayVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenShort: (String) -> Unit = onPlayVideo,
    onOpenUser: (Long) -> Unit = {},
    onOpenMylist: (Long) -> Unit = {},
    onOpenSeries: (Long) -> Unit = {},
) {
    val parsed = NicoUrlParser.parse(query) ?: return
    val label = when (parsed) {
        is NicoParsedIntent.PlayShort -> "ショートを直接再生: ${parsed.videoId}"
        is NicoParsedIntent.PlayVideo -> "動画を直接再生: ${parsed.videoId}"
        is NicoParsedIntent.OpenUser -> "ユーザーページを開く: user/${parsed.userId}"
        is NicoParsedIntent.OpenMylist -> "マイリストを開く: mylist/${parsed.mylistId}"
        is NicoParsedIntent.OpenSeries -> "シリーズを開く: series/${parsed.seriesId}"
    }
    val icon = when (parsed) {
        is NicoParsedIntent.OpenUser -> Icons.Default.AccountCircle
        is NicoParsedIntent.OpenMylist -> Icons.Default.Folder
        is NicoParsedIntent.OpenSeries -> Icons.Default.VideoLibrary
        is NicoParsedIntent.PlayShort, is NicoParsedIntent.PlayVideo -> Icons.Default.PlayCircleOutline
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                when (parsed) {
                    is NicoParsedIntent.PlayShort -> onOpenShort(parsed.videoId)
                    is NicoParsedIntent.PlayVideo -> onPlayVideo(parsed.videoId)
                    is NicoParsedIntent.OpenUser -> onOpenUser(parsed.userId)
                    is NicoParsedIntent.OpenMylist -> onOpenMylist(parsed.mylistId)
                    is NicoParsedIntent.OpenSeries -> onOpenSeries(parsed.seriesId)
                }
            },
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
