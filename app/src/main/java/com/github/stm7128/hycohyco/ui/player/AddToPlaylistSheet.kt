package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.CloudMylist
import com.github.stm7128.hycohyco.domain.model.LocalPlaylist
import com.github.stm7128.hycohyco.domain.model.VideoDetail
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoButton
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoTextField

/**
 * 動画を公式クラウド（あとで見る・マイリスト）またはローカルプレイリストへ保存する統合ボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    videoDetail: VideoDetail?,
    playlists: List<LocalPlaylist>,
    userMylists: List<CloudMylist> = emptyList(),
    playlistMessage: String? = null,
    onDismiss: () -> Unit,
    onAddToWatchLater: () -> Unit = {},
    onAddToMylist: (Long) -> Unit = {},
    onAddToPlaylist: (String) -> Unit = {},
    onCreateAndAdd: (String) -> Unit = {},
    onClearMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (videoDetail == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    text = "動画の保存・リスト登録",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HycoTheme.colors.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "閉じる", tint = HycoTheme.colors.onSurface)
                }
            }

            // メッセージ表示 (追加成功時等)
            if (playlistMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HycoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = HycoTheme.colors.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = playlistMessage, fontSize = 13.sp, color = HycoTheme.colors.onSurface, modifier = Modifier.weight(1f))
                        IconButton(onClick = onClearMessage, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // タブ切り替え: 公式クラウド / 端末ローカル
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("公式クラウド (${userMylists.size + 1})", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("端末ローカル (${playlists.size})", fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // 公式クラウド（あとで見る & マイリスト）
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. あとで見るクイック追加カード
                    item {
                        HycoCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onAddToWatchLater
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = HycoTheme.colors.primary, modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "「あとで見る」に追加", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HycoTheme.colors.onSurface)
                                    Text(text = "公式クラウドアカウントのあとで見るへ保存", fontSize = 12.sp, color = HycoTheme.colors.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // 2. 公式マイリスト一覧
                    item {
                        Text(
                            text = "公式マイリストに追加:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = HycoTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (userMylists.isEmpty()) {
                        item {
                            Text(
                                text = "マイリストがありません（未ログインまたは未作成）",
                                fontSize = 12.sp,
                                color = HycoTheme.colors.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(userMylists, key = { it.id }) { mylist ->
                            HycoCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onAddToMylist(mylist.id) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = HycoTheme.colors.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = mylist.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = HycoTheme.colors.onSurface)
                                        Text(text = "${mylist.itemsCount} 件の動画", fontSize = 12.sp, color = HycoTheme.colors.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // 端末ローカルプレイリスト
                if (isCreatingNew) {
                    HycoCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(text = "新しいプレイリスト", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HycoTheme.colors.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            HycoTextField(value = newTitle, onValueChange = { newTitle = it }, placeholder = "プレイリスト名を入力...", modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                HycoButton(text = "キャンセル", onClick = { isCreatingNew = false })
                                Spacer(modifier = Modifier.width(8.dp))
                                HycoButton(
                                    text = "作成して追加",
                                    onClick = {
                                        if (newTitle.isNotBlank()) {
                                            onCreateAndAdd(newTitle.trim())
                                            isCreatingNew = false
                                            newTitle = ""
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    HycoButton(text = "＋ 新しいプレイリストを作成", onClick = { isCreatingNew = true }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (playlists.isEmpty()) {
                    Text(text = "まだプレイリストがありません。「新しいプレイリストを作成」から作成してください。", fontSize = 13.sp, color = HycoTheme.colors.onSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(playlists, key = { it.id }) { playlist ->
                            HycoCard(modifier = Modifier.fillMaxWidth(), onClick = { onAddToPlaylist(playlist.id) }) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = HycoTheme.colors.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = playlist.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = HycoTheme.colors.onSurface)
                                        Text(text = "${playlist.videoCount} 件の動画", fontSize = 12.sp, color = HycoTheme.colors.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
