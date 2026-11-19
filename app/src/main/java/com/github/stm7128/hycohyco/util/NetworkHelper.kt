package com.github.stm7128.hycohyco.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * 端末のネットワーク接続状態判定ヘルパー
 */
object NetworkHelper {

    /**
     * 現在 Wi-Fi (またはイーサネット/有線LAN) に接続されているかを判定する
     *
     * @param context Android コンテキスト
     * @return Wi-Fi または イーサネット接続中の場合は true、モバイルデータ通信や未接続の場合は false
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
