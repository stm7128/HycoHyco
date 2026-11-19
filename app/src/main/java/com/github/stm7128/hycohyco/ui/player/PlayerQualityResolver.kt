package com.github.stm7128.hycohyco.ui.player

import android.content.Context
import com.github.stm7128.hycohyco.data.local.PlayerPreferencesDataSource
import com.github.stm7128.hycohyco.domain.model.QualityPreference
import com.github.stm7128.hycohyco.util.NetworkHelper
import kotlinx.coroutines.flow.first

/**
 * ネットワーク環境 (Wi-Fi / モバイルデータ通信) に応じたデフォルト画質解決ヘルパー
 */
object PlayerQualityResolver {

    /**
     * 現在のネットワーク状態と DataStore 設定に基づき、適用すべき画質プリファレンスを解決する
     *
     * @param context Android コンテキスト
     * @param preferencesDataSource プレイヤー設定データソース
     * @return 解決された [QualityPreference]
     */
    suspend fun resolveQualityPreference(
        context: Context?,
        preferencesDataSource: PlayerPreferencesDataSource?
    ): QualityPreference {
        if (preferencesDataSource == null) return QualityPreference.AUTO

        val isWifi = context?.let { NetworkHelper.isWifiConnected(it) } ?: true
        return if (isWifi) {
            preferencesDataSource.defaultQualityWifi.first()
        } else {
            preferencesDataSource.defaultQualityCellular.first()
        }
    }
}
