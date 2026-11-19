package com.github.stm7128.hycohyco.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip

/**
 * 視聴画面下部の「コメント」／「関連動画」タブ切り替えバー
 *
 * @param selectedTab 現在選択されているタブ
 * @param commentCount コメント総件数
 * @param recommendCount 関連動画件数
 * @param onTabSelected タブ選択コールバック
 */
@Composable
fun PlayerBottomTabs(
    selectedTab: PlayerBottomTab,
    commentCount: Int,
    recommendCount: Int,
    onTabSelected: (PlayerBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HycoFilterChip(
            selected = selectedTab == PlayerBottomTab.COMMENTS,
            onClick = { onTabSelected(PlayerBottomTab.COMMENTS) },
            label = if (commentCount > 0) "コメント ($commentCount)" else "コメント"
        )

        HycoFilterChip(
            selected = selectedTab == PlayerBottomTab.RECOMMENDS,
            onClick = { onTabSelected(PlayerBottomTab.RECOMMENDS) },
            label = if (recommendCount > 0) "関連動画 ($recommendCount)" else "関連動画"
        )
    }
}
