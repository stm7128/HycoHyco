package com.github.stm7128.hycohyco.ui.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.ui.designsystem.LocalAppThemeMode
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Playlist
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Settings
import androidx.compose.material3.NavigationBar as MaterialNavigationBar
import androidx.compose.material3.NavigationBarItem as MaterialNavigationBarItem
import androidx.compose.material3.TopAppBar as MaterialTopAppBar
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.SmallTopAppBar as MiuixSmallTopAppBar

/**
 * ナビゲーション用のアイコンペア（HyperOS / Material You）
 */
data class HycoNavIcon(
    val hyperOsIcon: ImageVector,
    val materialIcon: ImageVector
) {
    companion object {
        val Home = HycoNavIcon(
            hyperOsIcon = MiuixIcons.Home,
            materialIcon = Icons.Default.Home
        )
        val Shorts = HycoNavIcon(
            hyperOsIcon = MiuixIcons.Play,
            materialIcon = Icons.Default.Bolt
        )
        val Search = HycoNavIcon(
            hyperOsIcon = MiuixIcons.Search,
            materialIcon = Icons.Default.Search
        )
        val Library = HycoNavIcon(
            hyperOsIcon = MiuixIcons.Playlist,
            materialIcon = Icons.Default.VideoLibrary
        )
        val Settings = HycoNavIcon(
            hyperOsIcon = MiuixIcons.Settings,
            materialIcon = Icons.Default.Settings
        )
    }
}

/**
 * デュアル・デザインシステム対応共通 TopAppBar
 *
 * @param title タイトル文字列
 * @param modifier 修飾子
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HycoTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null
) {
    val themeMode = LocalAppThemeMode.current

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            MiuixSmallTopAppBar(
                title = title,
                modifier = modifier,
                navigationIcon = { navigationIcon?.invoke() }
            )
        }

        AppThemeMode.MATERIAL_YOU -> {
            MaterialTopAppBar(
                title = { Text(text = title) },
                navigationIcon = { navigationIcon?.invoke() },
                modifier = modifier
            )
        }
    }
}

/**
 * デュアル・デザインシステム対応共通 NavigationBar（ボトムバー）
 *
 * @param modifier 修飾子
 * @param content ナビゲーション項目 ([HycoNavBarItemScope] 内で定義)
 */
@Composable
fun HycoBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val themeMode = LocalAppThemeMode.current

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            MiuixNavigationBar(
                modifier = modifier,
                content = content
            )
        }

        AppThemeMode.MATERIAL_YOU -> {
            MaterialNavigationBar(
                modifier = modifier,
                content = content
            )
        }
    }
}

/**
 * デュアル・デザインシステム対応共通 NavigationBarItem
 *
 * @param selected 選択中かどうか
 * @param onClick クリック時のコールバック
 * @param icon ペアアイコン ([HycoNavIcon])
 * @param label 表示ラベル文字列
 * @param modifier 修飾子
 */
@Composable
fun RowScope.HycoBottomBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: HycoNavIcon,
    label: String,
    modifier: Modifier = Modifier
) {
    val themeMode = LocalAppThemeMode.current

    when (themeMode) {
        AppThemeMode.HYPER_OS -> {
            MiuixNavigationBarItem(
                selected = selected,
                onClick = onClick,
                icon = icon.hyperOsIcon,
                label = label,
                modifier = modifier
            )
        }

        AppThemeMode.MATERIAL_YOU -> {
            MaterialNavigationBarItem(
                selected = selected,
                onClick = onClick,
                icon = {
                    Icon(
                        imageVector = icon.materialIcon,
                        contentDescription = label
                    )
                },
                label = { Text(text = label) },
                modifier = modifier
            )
        }
    }
}
