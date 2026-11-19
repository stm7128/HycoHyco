package com.github.stm7128.hycohyco.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.stm7128.hycohyco.data.local.NgPreferencesDataSource
import com.github.stm7128.hycohyco.data.local.PlayerPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.CommentNgConfig
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.domain.model.QualityPreference
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.domain.model.UserSession
import com.github.stm7128.hycohyco.domain.repository.AuthRepository
import com.github.stm7128.hycohyco.player.danmaku.DanmakuDisplaySettings
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoCard
import com.github.stm7128.hycohyco.ui.designsystem.components.HycoPreferenceRow
import com.github.stm7128.hycohyco.ui.user.UserDetailSheet
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class QualitySheetTarget {
    WIFI,
    CELLULAR
}

/**
 * メイン設定画面
 *
 * アカウント、テーマ・外観、グローバルプレイヤー動作、弾幕・NGフィルター、デフォルト画質、バックアップを管理する。
 */
@Composable
fun SettingsScreen(
    themeSettings: ThemeSettings,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onDarkThemeConfigChanged: (DarkThemeConfig) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onAmoledBlackChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDefaultHomeModeChanged: (HomeMode) -> Unit = {},
    gestureMode: PlayerGestureMode = PlayerGestureMode.FULLSCREEN_ONLY,
    onGestureModeChanged: (PlayerGestureMode) -> Unit = {},
    onPlayVideo: (String) -> Unit = {},
    authRepository: AuthRepository = koinInject(),
    playerPreferencesDataSource: PlayerPreferencesDataSource = koinInject(),
    ngPreferencesDataSource: NgPreferencesDataSource = koinInject(),
) {
    val userSession by authRepository.userSession.collectAsState(initial = UserSession())
    val useNicovideoGay by playerPreferencesDataSource.useNicovideoGay.collectAsState(initial = false)
    val defaultQualityWifi by playerPreferencesDataSource.defaultQualityWifi.collectAsState(initial = QualityPreference.AUTO)
    val defaultQualityCellular by playerPreferencesDataSource.defaultQualityCellular.collectAsState(initial = QualityPreference.P360)
    val isAutoplayEnabled by playerPreferencesDataSource.autoplayEnabled.collectAsState(initial = true)
    val isResumePlaybackEnabled by playerPreferencesDataSource.isResumePlaybackEnabled.collectAsState(initial = true)
    val doubleTapSeekSeconds by playerPreferencesDataSource.doubleTapSeekSeconds.collectAsState(initial = 10)
    val isAutoEnterPip by playerPreferencesDataSource.autoEnterPip.collectAsState(initial = true)
    val isCoexistAudio by playerPreferencesDataSource.coexistAudio.collectAsState(initial = false)
    val showShortsBottomTab by playerPreferencesDataSource.showShortsBottomTab.collectAsState(initial = true)
    val danmakuSettings by playerPreferencesDataSource.danmakuSettings.collectAsState(initial = DanmakuDisplaySettings())
    val ngConfig by ngPreferencesDataSource.ngConfig.collectAsState(initial = CommentNgConfig())

    var activeQualitySheet by remember { mutableStateOf<QualitySheetTarget?>(null) }
    var isLoginSheetVisible by remember { mutableStateOf(false) }
    var profileUserId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. ニコニコアカウント設定カード
        Text(
            text = "ニコニコアカウント",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        NicoAccountCard(
            userSession = userSession,
            onLoginClick = { isLoginSheetVisible = true },
            onLogoutClick = { scope.launch { authRepository.logout() } },
            onProfileClick = { userId -> profileUserId = userId }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 外観・テーマ設定
        SettingsThemeSection(
            themeSettings = themeSettings,
            onThemeModeChanged = onThemeModeChanged,
            onDarkThemeConfigChanged = onDarkThemeConfigChanged,
            onDynamicColorChanged = onDynamicColorChanged,
            onAmoledBlackChanged = onAmoledBlackChanged
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. ホーム・プレイヤーグローバル・共有設定
        SettingsGeneralSection(
            defaultHomeMode = themeSettings.defaultHomeMode,
            onDefaultHomeModeChanged = onDefaultHomeModeChanged,
            showShortsBottomTab = showShortsBottomTab,
            onShowShortsBottomTabChanged = { enabled ->
                scope.launch { playerPreferencesDataSource.setShowShortsBottomTab(enabled) }
            },
            gestureMode = gestureMode,
            onGestureModeChanged = onGestureModeChanged,
            doubleTapSeekSeconds = doubleTapSeekSeconds,
            onDoubleTapSeekSecondsChanged = { sec ->
                scope.launch { playerPreferencesDataSource.setDoubleTapSeekSeconds(sec) }
            },
            isAutoplayEnabled = isAutoplayEnabled,
            onAutoplayEnabledChanged = { enabled ->
                scope.launch { playerPreferencesDataSource.setAutoplayEnabled(enabled) }
            },
            isResumePlaybackEnabled = isResumePlaybackEnabled,
            onResumePlaybackEnabledChanged = { enabled ->
                scope.launch { playerPreferencesDataSource.setResumePlaybackEnabled(enabled) }
            },
            isAutoEnterPip = isAutoEnterPip,
            onAutoEnterPipChanged = { enabled ->
                scope.launch { playerPreferencesDataSource.setAutoEnterPip(enabled) }
            },
            isCoexistAudio = isCoexistAudio,
            onCoexistAudioChanged = { enabled ->
                scope.launch { playerPreferencesDataSource.setCoexistAudio(enabled) }
            },
            useNicovideoGay = useNicovideoGay,
            onUseNicovideoGayChanged = { enabled ->
                scope.launch { playerPreferencesDataSource.setUseNicovideoGay(enabled) }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. 弾幕・コメント表示設定
        SettingsDanmakuSection(
            danmakuSettings = danmakuSettings,
            onUpdateDanmakuSettings = { settings ->
                scope.launch { playerPreferencesDataSource.setDanmakuSettings(settings) }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. コメント NG フィルター設定
        SettingsNgFilterSection(
            ngConfig = ngConfig,
            onUpdateNgConfig = { config ->
                scope.launch { ngPreferencesDataSource.saveNgConfig(config) }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 6. デフォルト画質設定
        Text(
            text = "デフォルト画質設定",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        HycoCard {
            Column {
                HycoPreferenceRow(
                    title = "Wi-Fi 接続時の画質",
                    summary = defaultQualityWifi.label,
                    onClick = { activeQualitySheet = QualitySheetTarget.WIFI }
                )
                HycoPreferenceRow(
                    title = "モバイルデータ通信時の画質",
                    summary = defaultQualityCellular.label,
                    onClick = { activeQualitySheet = QualitySheetTarget.CELLULAR }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7. データ管理・バックアップ
        SettingsBackupSection()

        Spacer(modifier = Modifier.height(24.dp))

        // 8. アプリ情報
        SettingsAppInfoSection(themeSettings = themeSettings)
    }

    if (isLoginSheetVisible) {
        LoginSheet(
            authRepository = authRepository,
            onDismissRequest = { isLoginSheetVisible = false },
            onLoginSuccess = { isLoginSheetVisible = false }
        )
    }

    profileUserId?.let { userId ->
        UserDetailSheet(
            userId = userId,
            onDismissRequest = { profileUserId = null },
            onVideoClick = onPlayVideo
        )
    }

    activeQualitySheet?.let { target ->
        val isWifi = target == QualitySheetTarget.WIFI
        QualitySelectorSheet(
            title = if (isWifi) "Wi-Fi 接続時の画質" else "モバイルデータ通信時の画質",
            currentQuality = if (isWifi) defaultQualityWifi else defaultQualityCellular,
            onQualitySelected = { quality ->
                scope.launch {
                    if (isWifi) {
                        playerPreferencesDataSource.setDefaultQualityWifi(quality)
                    } else {
                        playerPreferencesDataSource.setDefaultQualityCellular(quality)
                    }
                }
            },
            onDismissRequest = { activeQualitySheet = null }
        )
    }
}
