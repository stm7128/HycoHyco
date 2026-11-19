package com.github.stm7128.hycohyco.ui.library

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.PlaylistVideoItem
import com.github.stm7128.hycohyco.domain.model.VideoSummary
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoAsyncImage
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoTextField
import com.github.stm7128.hycohyco.ui.designsystem.components.VideoItemCard
import com.github.stm7128.hycohyco.ui.player.formatDuration

/**
 * プレイリスト一覧表示ビュー
 */
@Composable
fun PlaylistListView(
    playlists: List<LocalPlaylist>,
    onOpenPlaylist: (LocalPlaylist) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCreating by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        if (isCreating) {
            HycoCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "新しいプレイリスト",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HycoTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HycoTextField(value = newTitle, onValueChange = { newTitle = it }, placeholder = "プレイリスト名...", modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        HycoButton(text = "キャンセル", onClick = { isCreating = false })
                        Spacer(modifier = Modifier.width(8.dp))
                        HycoButton(
                            text = "作成",
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    onCreatePlaylist(newTitle.trim())
                                    newTitle = ""
                                    isCreating = false
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            HycoButton(text = "＋ 新しいプレイリストを作成", onClick = { isCreating = true }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (playlists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "プレイリストはありません",
                    color = HycoTheme.colors.onBackgroundVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(playlists, key = { it.id }) { playlist ->
                    HycoCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { onOpenPlaylist(playlist) }) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null, tint = HycoTheme.colors.primary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlist.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = HycoTheme.colors.onSurface
                                )
                                Text(
                                    text = "${playlist.videoCount} 件の動画",
                                    fontSize = 12.sp,
                                    color = HycoTheme.colors.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "削除", tint = HycoTheme.colors.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

