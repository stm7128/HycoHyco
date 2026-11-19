package com.github.stm7128.hycohyco.data.repository

import com.github.stm7128.hycohyco.data.local.ThemePreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.AppThemeMode
import com.github.stm7128.hycohyco.domain.model.DarkThemeConfig
import com.github.stm7128.hycohyco.domain.model.HomeMode
import com.github.stm7128.hycohyco.domain.model.ThemeSettings
import com.github.stm7128.hycohyco.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

/**
 * [ThemeRepository] の実装クラス
 *
 * @param preferencesDataSource テーマ設定の永続化データソース
 */
class ThemeRepositoryImpl(
    private val preferencesDataSource: ThemePreferencesDataSource
) : ThemeRepository {

    override val themeSettings: Flow<ThemeSettings> = preferencesDataSource.themeSettings

    override suspend fun setThemeMode(mode: AppThemeMode) {
        preferencesDataSource.setThemeMode(mode)
    }

    override suspend fun setDarkThemeConfig(config: DarkThemeConfig) {
        preferencesDataSource.setDarkThemeConfig(config)
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        preferencesDataSource.setDynamicColor(enabled)
    }

    override suspend fun setAmoledBlack(enabled: Boolean) {
        preferencesDataSource.setAmoledBlack(enabled)
    }

    override suspend fun setFirstLaunchCompleted() {
        preferencesDataSource.setFirstLaunchCompleted()
    }

    override suspend fun setDefaultHomeMode(mode: HomeMode) {
        preferencesDataSource.setDefaultHomeMode(mode)
    }
}
