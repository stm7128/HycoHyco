package com.github.stm7128.hycohyco.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.R
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoTopBar

/**
 * メイン画面シェル用トップバー
 *
 * ホームタブ選択時は「アプリアイコン ＋ HycoHyco」を表示し、
 * それ以外のタブ（検索・ライブラリ・設定）は画面機能名単体を表示する。
 */
@Composable
fun MainShellTopBar(
    selectedTab: MainTab,
    modifier: Modifier = Modifier
) {
    if (selectedTab == MainTab.HOME) {
        HycoTopBar(
            title = "HycoHyco",
            modifier = modifier,
            navigationIcon = {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 2.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "HycoHyco",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        )
    } else {
        HycoTopBar(
            title = selectedTab.title,
            modifier = modifier
        )
    }
}
