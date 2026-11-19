package com.github.stm7128.hycohyco.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoFilterChip
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPreferenceRow
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoSwitch

private val DOUBLE_TAP_SEEK_OPTIONS = listOf(5, 10, 15, 30, 60)

/**
 * ホーム画面設定・プレイヤーグローバル動作設定・共有設定セクション
 */
@Composable
fun SettingsGeneralSection(
    defaultHomeMode: HomeMode,
    onDefaultHomeModeChanged: (HomeMode) -> Unit,
    showShortsBottomTab: Boolean,
    onShowShortsBottomTabChanged: (Boolean) -> Unit,
    gestureMode: PlayerGestureMode,
    onGestureModeChanged: (PlayerGestureMode) -> Unit,
    doubleTapSeekSeconds: Int,
    onDoubleTapSeekSecondsChanged: (Int) -> Unit,
    isAutoplayEnabled: Boolean,
    onAutoplayEnabledChanged: (Boolean) -> Unit,
    isResumePlaybackEnabled: Boolean,
    onResumePlaybackEnabledChanged: (Boolean) -> Unit,
    isAutoEnterPip: Boolean,
    onAutoEnterPipChanged: (Boolean) -> Unit,
    isCoexistAudio: Boolean,
    onCoexistAudioChanged: (Boolean) -> Unit,
    useNicovideoGay: Boolean,
    onUseNicovideoGayChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HycoTheme.colors

    Column(modifier = modifier) {
        // 1. ホーム画面設定
        SectionTitle(text = "ホーム画面設定")

        HycoCard {
            Column {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "起動時の表示タブ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "アプリ起動時に最初に開くホーム画面を選択します",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HomeMode.entries.forEach { mode ->
                            HycoFilterChip(
                                selected = defaultHomeMode == mode,
                                onClick = { onDefaultHomeModeChanged(mode) },
                                label = mode.title
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                HycoPreferenceRow(
                    title = "ボトムバーに「ショート」タブを表示",
                    summary = "下部のナビゲーションバーからニコニコショートを直接開けるようにします",
                    trailing = {
                        HycoSwitch(
                            checked = showShortsBottomTab,
                            onCheckedChange = onShowShortsBottomTabChanged
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. プレイヤー操作・再生挙動設定 (グローバル)
        SectionTitle(text = "プレイヤー操作・再生設定")

        HycoCard {
            Column {
                // スライド操作（音量・輝度）
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "スライド操作（音量・輝度）",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = gestureMode.description,
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlayerGestureMode.entries.forEach { mode ->
                            HycoFilterChip(
                                selected = gestureMode == mode,
                                onClick = { onGestureModeChanged(mode) },
                                label = mode.label
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // ダブルタップのスキップ秒数
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "ダブルタップのスキップ秒数",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "画面左右のダブルタップで早送り・巻き戻しする秒数（現在: ${doubleTapSeekSeconds}秒）",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DOUBLE_TAP_SEEK_OPTIONS.forEach { sec ->
                            HycoFilterChip(
                                selected = doubleTapSeekSeconds == sec,
                                onClick = { onDoubleTapSeekSecondsChanged(sec) },
                                label = "${sec}秒"
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.outline.copy(alpha = 0.35f))

                // 次の動画を自動再生
                HycoPreferenceRow(
                    title = "次の動画を自動再生",
                    summary = "シリーズ動画の再生終了時に次の動画をカウントダウン後に自動再生します",
                    trailing = {
                        HycoSwitch(
                            checked = isAutoplayEnabled,
                            onCheckedChange = onAutoplayEnabledChanged
                        )
                    }
                )

                // 前回の続きから再生
                HycoPreferenceRow(
                    title = "前回の続きから再生",
                    summary = "動画を開いたときに前回の再生位置から自動で再開します",
                    trailing = {
                        HycoSwitch(
                            checked = isResumePlaybackEnabled,
                            onCheckedChange = onResumePlaybackEnabledChanged
                        )
                    }
                )

                // ホーム移動時に自動でPiP小窓化
                HycoPreferenceRow(
                    title = "ホーム移動時に自動でPiP小窓化",
                    summary = "オフにすると小窓を出さずバックグラウンド再生＋スーパー島（通知）へ移行します",
                    trailing = {
                        HycoSwitch(
                            checked = isAutoEnterPip,
                            onCheckedChange = onAutoEnterPipChanged
                        )
                    }
                )

                // 他アプリの音と同時再生
                HycoPreferenceRow(
                    title = "他アプリの音と同時再生",
                    summary = "ゲームや音楽アプリの音を止めずに裏で再生（オーディオフォーカスを要求しない）",
                    trailing = {
                        HycoSwitch(
                            checked = isCoexistAudio,
                            onCheckedChange = onCoexistAudioChanged
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 共有設定
        SectionTitle(text = "共有設定")

        HycoCard {
            Column {
                HycoPreferenceRow(
                    title = "共有URLを nicovideo.gay に置換",
                    summary = "X (Twitter) や Discord でサムネイル・埋め込みが表示されるリンクで共有します",
                    trailing = {
                        HycoSwitch(
                            checked = useNicovideoGay,
                            onCheckedChange = onUseNicovideoGayChanged
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}
