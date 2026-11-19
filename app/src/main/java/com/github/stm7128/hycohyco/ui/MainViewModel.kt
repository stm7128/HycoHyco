package com.github.stm7128.hycohyco.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stm7128.hycohyco.data.local.PlayerPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.PlayerGestureMode
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.domain.repository.ThemeRepository
import com.github.stm7128.hycohyco.ui.main.MainTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * テーマおよびアプリ共通の UI 状態
 */
sealed interface ThemeUiState {
    /** DataStore 読み込み中 */
    data object Loading : ThemeUiState

    /** 読み込み完了 */
    data class Success(val settings: ThemeSettings) : ThemeUiState
}

/**
 * アプリ全体およびメイン画面の ViewModel
 *
 * @param themeRepository テーマ設定リポジトリ
 */
class MainViewModel(
    private val themeRepository: ThemeRepository,
    private val playerPreferencesDataSource: PlayerPreferencesDataSource? = null,
) : ViewModel() {

    /** 選択中のメインタブ (テーマ再構成時も保持される) */
    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    /** Deep Link 等から開かれているユーザーページ ID */
    private val _openedUserId = MutableStateFlow<Long?>(null)
    val openedUserId: StateFlow<Long?> = _openedUserId.asStateFlow()

    /** Deep Link 等から開かれているマイリスト ID */
    private val _openedMylistId = MutableStateFlow<Long?>(null)
    val openedMylistId: StateFlow<Long?> = _openedMylistId.asStateFlow()

    /** Deep Link 等から開かれているシリーズ ID */
    private val _openedSeriesId = MutableStateFlow<Long?>(null)
    val openedSeriesId: StateFlow<Long?> = _openedSeriesId.asStateFlow()

    /** スライドジェスチャー有効モード */
    val gestureMode: StateFlow<PlayerGestureMode> = (playerPreferencesDataSource?.gestureMode
        ?: kotlinx.coroutines.flow.flowOf(PlayerGestureMode.FULLSCREEN_ONLY))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerGestureMode.FULLSCREEN_ONLY
        )

    /** ボトムバーに「ショート」タブを表示するかどうか */
    val showShortsBottomTab: StateFlow<Boolean> = (playerPreferencesDataSource?.showShortsBottomTab
        ?: kotlinx.coroutines.flow.flowOf(true))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        playerPreferencesDataSource?.showShortsBottomTab
            ?.onEach { visible ->
                if (!visible && _selectedTab.value == MainTab.SHORTS) {
                    _selectedTab.value = MainTab.HOME
                }
            }
            ?.launchIn(viewModelScope)
    }

    /** テーマ状態の StateFlow */
    val uiState: StateFlow<ThemeUiState> = themeRepository.themeSettings
        .map { ThemeUiState.Success(it) as ThemeUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeUiState.Loading
        )

    /**
     * メイン画面のタブを切り替える
     */
    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    /**
     * Deep Link 等で指定されたユーザー ID のユーザー詳細シートを開く
     */
    fun openUserPage(userId: Long) {
        if (userId > 0L) {
            _openedUserId.value = userId
        }
    }

    /**
     * ユーザー詳細シートを閉じる
     */
    fun closeUserPage() {
        _openedUserId.value = null
    }

    /**
     * Deep Link 等で指定されたマイリスト ID のマイリスト詳細シートを開く
     */
    fun openMylist(mylistId: Long) {
        if (mylistId > 0L) {
            _openedMylistId.value = mylistId
        }
    }

    /**
     * マイリスト詳細シートを閉じる
     */
    fun closeMylist() {
        _openedMylistId.value = null
    }

    /**
     * Deep Link 等で指定されたシリーズ ID のシリーズ詳細シートを開く
     */
    fun openSeries(seriesId: Long) {
        if (seriesId > 0L) {
            _openedSeriesId.value = seriesId
        }
    }

    /**
     * シリーズ詳細シートを閉じる
     */
    fun closeSeries() {
        _openedSeriesId.value = null
    }

    /**
     * テーマモード（HyperOS / Material You）を変更する
     */
    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
        }
    }

    /**
     * ダークテーマ設定を変更する
     */
    fun setDarkThemeConfig(config: DarkThemeConfig) {
        viewModelScope.launch {
            themeRepository.setDarkThemeConfig(config)
        }
    }

    /**
     * 壁紙連動ダイナミックカラー (Monet) 設定を変更する
     */
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.setDynamicColor(enabled)
        }
    }

    /**
     * AMOLED ピュアブラック設定を変更する
     */
    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.setAmoledBlack(enabled)
        }
    }

    /**
     * 初回セットアップ完了状態にする
     */
    fun completeFirstLaunch() {
        viewModelScope.launch {
            themeRepository.setFirstLaunchCompleted()
        }
    }

    /**
     * スライドジェスチャー有効モードを変更する
     */
    fun setGestureMode(mode: PlayerGestureMode) {
        viewModelScope.launch {
            playerPreferencesDataSource?.setGestureMode(mode)
        }
    }

    /**
     * 起動時のデフォルトホーム画面モードを変更する
     */
    fun setDefaultHomeMode(mode: HomeMode) {
        viewModelScope.launch {
            themeRepository.setDefaultHomeMode(mode)
        }
    }
}
