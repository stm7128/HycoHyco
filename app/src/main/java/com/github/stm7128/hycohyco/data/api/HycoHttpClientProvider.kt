package com.github.stm7128.hycohyco.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * アプリ全体で唯一の通信エンジン ([OkHttpClient] および [HttpClient]) を一元管理するプロバイダー
 */
object HycoHttpClientProvider {

    /**
     * アプリ共通の [OkHttpClient] シングルトン
     * (接続プール: 最大8接続、5分間維持。タイムアウト: 接続10秒、読み込み15秒)
     */
    val sharedOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(8, 5, TimeUnit.MINUTES))
            .build()
    }

    /**
     * アプリ共通の Ktor [HttpClient] シングルトン
     * (同一の OkHttpClient エンジン・接続プールを共有し、JSON デシリアライズ・タイムアウトを設定)
     */
    val sharedKtorClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                preconfigured = sharedOkHttpClient
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                        coerceInputValues = true
                        encodeDefaults = true
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000L
                connectTimeoutMillis = 10_000L
                socketTimeoutMillis = 15_000L
            }
        }
    }
}
