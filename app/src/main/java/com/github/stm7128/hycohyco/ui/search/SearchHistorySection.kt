package com.github.stm7128.hycohyco.ui.search

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.SearchHistoryItem
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip

val POPULAR_TAGS = listOf(
    "VOCALOID", "東方", "歌ってみた", "ゲーム実況", "アニメ", "sm9", "レッツゴー！陰陽師"
)

/**
 * 検索履歴（ピン留め含む）および人気タグの一覧セクション
 */
@Composable
fun SearchHistorySection(
    searchHistory: List<SearchHistoryItem>,
    onSelectKeyword: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onSelectPopularTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 1. ピン留め / 検索履歴
        if (searchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "検索履歴 & ピン留め",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onClearAllHistory) {
                    Text("履歴を消去", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                searchHistory.forEach { item ->
                    HycoFilterChip(
                        selected = item.isPinned,
                        onClick = { onSelectKeyword(item.keyword) },
                        label = if (item.isPinned) "📌 ${item.keyword}" else item.keyword
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. 注目のタグ
        Text(
            text = "注目のタグ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            POPULAR_TAGS.forEach { tag ->
                HycoFilterChip(
                    selected = false,
                    onClick = { onSelectPopularTag(tag) },
                    label = tag
                )
            }
        }
    }
}
