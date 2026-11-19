package com.github.stm7128.hycohyco.domain.repository

import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import kotlinx.coroutines.flow.Flow

/**
 * テーマ設定の取得・更新を司るリポジトリインターフェース
 */
interface ThemeRepository {
    /** テーマ設定の監視 Flow */
    val themeSettings: Flow<ThemeSettings>

    /**
     * テーマモード（HyperOS / Material You）を変更する
     *
     * @param mode 新しいテーマモード
     */
    suspend fun setThemeMode(mode: AppThemeMode)

    /**
     * ダークテーマ設定を変更する
     *
     * @param config 新しいダークテーマ設定
     */
    suspend fun setDarkThemeConfig(config: DarkThemeConfig)

    /**
     * Material You ダイナミックカラーの有効/無効を切り替える
     *
     * @param enabled 有効にする場合は true
     */
    suspend fun setDynamicColor(enabled: Boolean)

    /**
     * AMOLED ピュアブラックモードの有効/無効を切り替える
     *
     * @param enabled 有効にする場合は true
     */
    suspend fun setAmoledBlack(enabled: Boolean)

    /**
     * 初回起動セットアップ完了フラグを更新する
     */
    suspend fun setFirstLaunchCompleted()

    /**
     * 起動時のデフォルトホーム画面モードを変更する
     */
    suspend fun setDefaultHomeMode(mode: HomeMode)
}
