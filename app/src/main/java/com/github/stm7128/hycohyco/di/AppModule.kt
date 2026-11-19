package com.github.stm7128.hycohyco.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.stm7128.hycohyco.data.api.HycoHttpClientProvider
import com.github.stm7128.hycohyco.data.api.NicoAuthApiClient
import com.github.stm7128.hycohyco.data.backup.BackupManager
import com.github.stm7128.hycohyco.data.api.NicoCloudApiClient
import com.github.stm7128.hycohyco.data.api.NicoCloudHistoryApiClient
import com.github.stm7128.hycohyco.data.api.NicoCommentApiClient
import com.github.stm7128.hycohyco.data.api.NicoDicApiClient
import com.github.stm7128.hycohyco.data.api.NicoInteractionApiClient
import com.github.stm7128.hycohyco.data.api.NicoSearchApiClient
import com.github.stm7128.hycohyco.data.api.NicoRecommendApiClient
import com.github.stm7128.hycohyco.data.api.NicoShortsApiClient
import com.github.stm7128.hycohyco.data.api.NicoTagApiClient
import com.github.stm7128.hycohyco.data.api.NicoUserApiClient
import com.github.stm7128.hycohyco.data.api.NicoVideoApiClient
import com.github.stm7128.hycohyco.data.api.NicoWatchApiClient
import com.github.stm7128.hycohyco.data.download.OfflineDownloadManager
import com.github.stm7128.hycohyco.data.download.OfflineDownloader
import io.ktor.client.HttpClient
import okhttp3.OkHttpClient
import com.github.stm7128.hycohyco.data.local.AuthPreferencesDataSource
import com.github.stm7128.hycohyco.data.local.NgPreferencesDataSource
import com.github.stm7128.hycohyco.data.local.PlayerPreferencesDataSource
import com.github.stm7128.hycohyco.data.local.ThemePreferencesDataSource
import com.github.stm7128.hycohyco.data.local.db.DownloadDao
import com.github.stm7128.hycohyco.data.local.db.HycoDatabaseHelper
import com.github.stm7128.hycohyco.data.repository.AuthRepositoryImpl
import com.github.stm7128.hycohyco.data.repository.CloudLibraryRepositoryImpl
import com.github.stm7128.hycohyco.data.repository.DownloadRepositoryImpl
import com.github.stm7128.hycohyco.data.repository.InteractionRepositoryImpl
import com.github.stm7128.hycohyco.data.repository.LibraryRepositoryImpl
import com.github.stm7128.hycohyco.data.repository.ThemeRepositoryImpl
import com.github.stm7128.hycohyco.data.repository.VideoRepositoryImpl
import com.github.stm7128.hycohyco.domain.repository.AuthRepository
import com.github.stm7128.hycohyco.domain.repository.CloudLibraryRepository
import com.github.stm7128.hycohyco.domain.repository.DownloadRepository
import com.github.stm7128.hycohyco.domain.repository.InteractionRepository
import com.github.stm7128.hycohyco.domain.repository.LibraryRepository
import com.github.stm7128.hycohyco.domain.repository.ThemeRepository
import com.github.stm7128.hycohyco.domain.repository.VideoRepository
import com.github.stm7128.hycohyco.domain.usecase.OfflinePlaybackResolver
import com.github.stm7128.hycohyco.player.NicoPlaybackManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val PREFERENCES_NAME = "hycohyco_preferences"

/**
 * Context に紐づく DataStore デリゲート
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

/**
 * アプリ全体の基本 DI モジュール
 */
val appModule = module {
    // DataStore
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }

    // データソース
    single {
        ThemePreferencesDataSource(dataStore = get())
    }

    single {
        NgPreferencesDataSource(dataStore = get())
    }

    single {
        PlayerPreferencesDataSource(dataStore = get())
    }

    single {
        AuthPreferencesDataSource(dataStore = get())
    }

    // ローカル SQLite DB
    single {
        HycoDatabaseHelper(context = androidContext())
    }

    // バックアップマネージャー
    single {
        BackupManager(
            dbHelper = get(),
            themePreferences = get(),
            playerPreferences = get(),
            ngPreferences = get(),
            libraryRepository = get()
        )
    }

    // オフラインダウンロード DAO & リポジトリ
    single {
        DownloadDao(dbHelper = get())
    }

    single<DownloadRepository> {
        DownloadRepositoryImpl(downloadDao = get())
    }

    // 共通通信エンジン (OkHttpClient & Ktor HttpClient シングルトン)
    single<OkHttpClient> {
        HycoHttpClientProvider.sharedOkHttpClient
    }

    single<HttpClient> {
        HycoHttpClientProvider.sharedKtorClient
    }

    // オフラインダウンロードコア & マネージャー
    single {
        OfflineDownloader(
            context = androidContext(),
            watchApiClient = get(),
            commentApiClient = get(),
            authPreferencesDataSource = get(),
            okHttpClient = get()
        )
    }

    single {
        OfflineDownloadManager(
            context = androidContext(),
            downloadRepository = get(),
            offlineDownloader = get()
        )
    }

    single {
        OfflinePlaybackResolver(
            downloadRepository = get()
        )
    }

    // ライブラリリポジトリ
    single<LibraryRepository> {
        LibraryRepositoryImpl(dbHelper = get())
    }

    // リポジトリ
    single<ThemeRepository> {
        ThemeRepositoryImpl(preferencesDataSource = get())
    }

    // API クライアント (共通 HttpClient を注入)
    single {
        NicoWatchApiClient(httpClient = get())
    }

    single {
        NicoCommentApiClient(httpClient = get())
    }

    single {
        NicoSearchApiClient(httpClient = get())
    }

    single {
        NicoAuthApiClient(httpClient = get())
    }

    single {
        NicoCloudApiClient(httpClient = get())
    }

    single {
        NicoCloudHistoryApiClient(httpClient = get())
    }

    single {
        NicoUserApiClient(httpClient = get())
    }

    single {
        NicoInteractionApiClient(httpClient = get())
    }

    single {
        NicoRecommendApiClient(httpClient = get())
    }

    single {
        NicoShortsApiClient(httpClient = get())
    }

    single {
        NicoTagApiClient(httpClient = get())
    }

    single {
        NicoDicApiClient(httpClient = get())
    }

    single {
        NicoVideoApiClient(httpClient = get())
    }

    single<InteractionRepository> {
        InteractionRepositoryImpl(
            interactionApiClient = get(),
            authPreferencesDataSource = get()
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authApiClient = get(),
            authPreferencesDataSource = get()
        )
    }

    single<CloudLibraryRepository> {
        CloudLibraryRepositoryImpl(
            cloudApiClient = get(),
            userApiClient = get(),
            authPreferencesDataSource = get(),
            cloudHistoryApiClient = get()
        )
    }

    // 動画リポジトリ
    single<VideoRepository> {
        VideoRepositoryImpl(
            apiClient = get(),
            commentApiClient = get(),
            searchApiClient = get(),
            authPreferencesDataSource = get(),
            recommendApiClient = get(),
            shortsApiClient = get(),
            tagApiClient = get(),
            dicApiClient = get(),
            videoApiClient = get(),
        )
    }

    // 再生マネージャー (Media3 ExoPlayer)
    single {
        NicoPlaybackManager(context = androidContext())
    }

    // スリープタイマーマネージャー
    single {
        val manager: NicoPlaybackManager = get()
        val timer = com.github.stm7128.hycohyco.player.SleepTimerManager(playbackManager = manager)
        manager.sleepTimerManager = timer
        timer
    }
}
