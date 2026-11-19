package com.github.stm7128.hycohyco.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.github.stm7128.hycohyco.domain.util.NicoParsedIntent
import com.github.stm7128.hycohyco.domain.util.NicoUrlParser
import com.github.stm7128.hycohyco.player.NicoPlaybackManager
import com.github.stm7128.hycohyco.player.service.HycoPlaybackService
import com.github.stm7128.hycohyco.ui.designsystem.HycoTheme
import com.github.stm7128.hycohyco.ui.main.MainShellScreen
import com.github.stm7128.hycohyco.ui.player.PipHelper
import com.github.stm7128.hycohyco.ui.player.PlayerViewModel
import com.github.stm7128.hycohyco.ui.search.SearchViewModel
import com.github.stm7128.hycohyco.ui.setup.ThemeSetupScreen
import com.github.stm7128.hycohyco.ui.shorts.ShortsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * PiP (Picture-in-Picture) モード中かどうかを提供する CompositionLocal
 */
val LocalIsInPipMode = staticCompositionLocalOf { false }

/**
 * HycoHyco メインアクティビティ
 *
 * アプリの画面遷移エントリーポイント。テーマ状態を監視し、
 * 初回起動画面 (`ThemeSetupScreen`) とメイン画面 (`MainShellScreen`) を切り替える。
 * また、Android 8.0+ の PiP (Picture-in-Picture) 自動小窓再生を制御する。
 */
class MainActivity : ComponentActivity() {

    private val playbackManager: NicoPlaybackManager by inject()
    private val mainViewModel: MainViewModel by viewModel()
    private val playerViewModel: PlayerViewModel by viewModel()
    private val shortsViewModel: ShortsViewModel by viewModel()
    private val searchViewModel: SearchViewModel by viewModel()
    private var isInPipMode by mutableStateOf(false)
    private var isPipDismissed = false
    private var wasInPipMode = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()
        handleIncomingIntent(intent)

        setContent {
            val uiState by mainViewModel.uiState.collectAsState()
            val selectedTab by mainViewModel.selectedTab.collectAsState()
            val gestureMode by mainViewModel.gestureMode.collectAsState()
            val showShortsBottomTab by mainViewModel.showShortsBottomTab.collectAsState()
            val openedUserId by mainViewModel.openedUserId.collectAsState()
            val openedMylistId by mainViewModel.openedMylistId.collectAsState()
            val openedSeriesId by mainViewModel.openedSeriesId.collectAsState()

            CompositionLocalProvider(LocalIsInPipMode provides isInPipMode) {
                when (val state = uiState) {
                    is ThemeUiState.Loading -> {
                        // チラつき防止用の初期ローディング画面
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }

                    is ThemeUiState.Success -> {
                        // 選択されたテーマ設定をアプリ全体に適用
                        HycoTheme(themeSettings = state.settings) {
                            Crossfade(
                                targetState = state.settings.isFirstLaunchCompleted,
                                label = "FirstLaunchTransition"
                            ) { isCompleted ->
                                if (isCompleted) {
                                    MainShellScreen(
                                        selectedTab = selectedTab,
                                        onSelectTab = mainViewModel::selectTab,
                                        themeSettings = state.settings,
                                        onThemeModeChanged = mainViewModel::setThemeMode,
                                        onDarkThemeConfigChanged = mainViewModel::setDarkThemeConfig,
                                        onDynamicColorChanged = mainViewModel::setDynamicColor,
                                        onAmoledBlackChanged = mainViewModel::setAmoledBlack,
                                        onDefaultHomeModeChanged = mainViewModel::setDefaultHomeMode,
                                        gestureMode = gestureMode,
                                        onGestureModeChanged = mainViewModel::setGestureMode,
                                        showShortsBottomTab = showShortsBottomTab,
                                        openedUserId = openedUserId,
                                        onCloseUserPage = mainViewModel::closeUserPage,
                                        openedMylistId = openedMylistId,
                                        onCloseMylist = mainViewModel::closeMylist,
                                        onOpenMylist = mainViewModel::openMylist,
                                        openedSeriesId = openedSeriesId,
                                        onCloseSeries = mainViewModel::closeSeries,
                                        onOpenSeries = mainViewModel::openSeries,
                                        playerViewModel = playerViewModel,
                                        shortsViewModel = shortsViewModel,
                                        searchViewModel = searchViewModel,
                                    )
                                } else {
                                    ThemeSetupScreen(
                                        themeSettings = state.settings,
                                        onSelectThemeMode = mainViewModel::setThemeMode,
                                        onConfirm = mainViewModel::completeFirstLaunch
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val textToParse = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.toString()
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        } ?: return

        val parsed = NicoUrlParser.parse(textToParse) ?: return
        when (parsed) {
            is NicoParsedIntent.PlayVideo -> {
                shortsViewModel.closeOverlay()
                playerViewModel.playVideo(
                    videoId = parsed.videoId,
                    context = this,
                    initialPositionMs = if (parsed.startPositionMs > 0L) parsed.startPositionMs else null
                )
            }
            is NicoParsedIntent.PlayShort -> {
                playerViewModel.closePlayer()
                shortsViewModel.openShortById(
                    videoId = parsed.videoId,
                    context = this
                )
            }
            is NicoParsedIntent.OpenUser -> {
                mainViewModel.openUserPage(parsed.userId)
            }
            is NicoParsedIntent.OpenMylist -> {
                mainViewModel.openMylist(parsed.mylistId)
            }
            is NicoParsedIntent.OpenSeries -> {
                mainViewModel.openSeries(parsed.seriesId)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // ショート動画表示中は通常プレイヤーの PiP を抑制
        val isShortsActive = shortsViewModel.uiState.value.isActiveOverlay
        if (isShortsActive) return

        // ホーム画面へ戻った際、動画再生中かつ展開モード・自動PiP有効なら自動で PiP 小窓モードへ遷移
        val pState = playerViewModel.uiState.value
        if (playbackManager.exoPlayer.isPlaying &&
            pState.isAutoEnterPip &&
            pState.displayMode == com.github.stm7128.hycohyco.ui.player.PlayerDisplayMode.EXPANDED
        ) {
            PipHelper.enterPip(this)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        val previouslyInPip = isInPipMode
        isInPipMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            wasInPipMode = true
            isPipDismissed = false
        } else {
            // PiP モードから抜けた際、Activity が CREATED (onStop済み) または終了中であれば、
            // フルスクリーン復帰 (STARTED -> RESUMED) ではなくホーム画面上で PiP 小窓が閉じられたと判定
            if (previouslyInPip &&
                (isFinishing || lifecycle.currentState == androidx.lifecycle.Lifecycle.State.CREATED)
            ) {
                handlePipDismissed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // アプリがフルスクリーンで復帰・再開された場合は PiP 終了フラグをリセット
        wasInPipMode = false
        isPipDismissed = false
    }

    override fun onStop() {
        super.onStop()
        // 画面オフや端末スリープ（ロック画面）による onStop() の場合は PiP 小窓が閉じられたわけではないため、
        // プレイヤーやサービスを強制終了せず、バックグラウンド再生を維持する
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isScreenOn = powerManager?.isInteractive ?: true
        if (!isScreenOn) {
            return
        }

        // ホーム画面上の PiP 小窓は表示中 PAUSED (STARTED) 状態を維持し、
        // ユーザーが 'X' ボタンやスワイプで小窓を閉じた瞬間に onStop() が呼ばれる（isFinishing は false のまま）
        if ((isInPipMode || wasInPipMode) && !isPipDismissed) {
            handlePipDismissed()
        }
    }

    override fun onDestroy() {
        if ((isInPipMode || wasInPipMode) && !isPipDismissed) {
            handlePipDismissed()
        }
        super.onDestroy()
    }

    /**
     * ユーザーが PiP 小窓の 'X' ボタン押下やドラッグで明示的に小窓を閉じた場合の終了処理
     */
    private fun handlePipDismissed() {
        if (isPipDismissed) return
        isPipDismissed = true
        wasInPipMode = false
        playerViewModel.closePlayer()
        try {
            stopService(Intent(this, HycoPlaybackService::class.java))
        } catch (_: Exception) {}
    }
}
