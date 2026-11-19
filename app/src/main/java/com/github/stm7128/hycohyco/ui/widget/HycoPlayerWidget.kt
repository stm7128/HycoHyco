package com.github.stm7128.hycohyco.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.github.stm7128.hycohyco.R
import com.github.stm7128.hycohyco.ui.MainActivity

/**
 * ホーム画面から再生中の動画情報確認および再生/一時停止・10秒スキップ操作を行う Glance ウィジェット
 * (Jetpack Compose Glance 1.1.1 準拠)
 */
class HycoPlayerWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val isPlaying = prefs[HycoPlayerWidgetKeys.KEY_IS_PLAYING] ?: false
            val hasMedia = prefs[HycoPlayerWidgetKeys.KEY_HAS_MEDIA] ?: false
            val title = prefs[HycoPlayerWidgetKeys.KEY_TITLE] ?: ""
            val artist = prefs[HycoPlayerWidgetKeys.KEY_ARTIST] ?: ""
            val videoId = prefs[HycoPlayerWidgetKeys.KEY_VIDEO_ID] ?: ""

            val openAppAction = actionStartActivity<MainActivity>()

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .cornerRadius(16.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. アプリアイコン（タップでアプリ起動）
                        Box(
                            modifier = GlanceModifier
                                .size(46.dp)
                                .cornerRadius(12.dp)
                                .background(GlanceTheme.colors.primaryContainer)
                                .clickable(openAppAction),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.mipmap.ic_launcher),
                                contentDescription = "HycoHyco",
                                modifier = GlanceModifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(10.dp))

                        // 2. 動画情報エリア（タップでアプリ復帰）
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                                .clickable(openAppAction),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (hasMedia && title.isNotBlank()) title else "動画を再生していません",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = if (hasMedia) {
                                    if (artist.isNotBlank()) artist else (if (videoId.isNotBlank()) videoId else "再生中")
                                } else {
                                    "タップして HycoHyco を開く"
                                },
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // 3. メディア操作ボタン群（10秒戻る, 再生/一時停止, 10秒進む）
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 10秒巻き戻し
                            val rewIntent = Intent(context, HycoPlayerWidgetReceiver::class.java).apply {
                                action = HycoPlayerWidgetKeys.ACTION_SEEK_BACKWARD
                            }
                            Box(
                                modifier = GlanceModifier
                                    .size(34.dp)
                                    .cornerRadius(17.dp)
                                    .clickable(actionSendBroadcast(rewIntent)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(android.R.drawable.ic_media_rew),
                                    contentDescription = "10秒戻る",
                                    modifier = GlanceModifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(4.dp))

                            // 再生 / 一時停止
                            val toggleIntent = Intent(context, HycoPlayerWidgetReceiver::class.java).apply {
                                action = HycoPlayerWidgetKeys.ACTION_TOGGLE_PLAY_PAUSE
                            }
                            val playPauseIconRes = if (isPlaying) {
                                android.R.drawable.ic_media_pause
                            } else {
                                android.R.drawable.ic_media_play
                            }
                            Box(
                                modifier = GlanceModifier
                                    .size(40.dp)
                                    .cornerRadius(20.dp)
                                    .background(GlanceTheme.colors.primary)
                                    .clickable(actionSendBroadcast(toggleIntent)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(playPauseIconRes),
                                    contentDescription = if (isPlaying) "一時停止" else "再生",
                                    modifier = GlanceModifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(4.dp))

                            // 10秒早送り
                            val ffIntent = Intent(context, HycoPlayerWidgetReceiver::class.java).apply {
                                action = HycoPlayerWidgetKeys.ACTION_SEEK_FORWARD
                            }
                            Box(
                                modifier = GlanceModifier
                                    .size(34.dp)
                                    .cornerRadius(17.dp)
                                    .clickable(actionSendBroadcast(ffIntent)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(android.R.drawable.ic_media_ff),
                                    contentDescription = "10秒進む",
                                    modifier = GlanceModifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
