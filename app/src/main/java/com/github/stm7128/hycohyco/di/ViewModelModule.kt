package com.github.stm7128.hycohyco.di

import com.github.stm7128.hycohyco.ui.MainViewModel
import com.github.stm7128.hycohyco.ui.home.HomeViewModel
import com.github.stm7128.hycohyco.ui.library.LibraryViewModel
import com.github.stm7128.hycohyco.ui.player.PlayerViewModel
import com.github.stm7128.hycohyco.ui.search.SearchViewModel
import com.github.stm7128.hycohyco.ui.shorts.ShortsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * ViewModel 用 Koin DI モジュール
 */
val viewModelModule = module {
    viewModel {
        MainViewModel(
            themeRepository = get(),
            playerPreferencesDataSource = get(),
        )
    }

    viewModel {
        PlayerViewModel(
            videoRepository = get(),
            playbackManager = get(),
            ngPreferencesDataSource = get(),
            libraryRepository = get(),
            playerPreferencesDataSource = get(),
            offlinePlaybackResolver = get(),
            offlineDownloadManager = get(),
            interactionRepository = get(),
            cloudLibraryRepository = getOrNull(),
            sleepTimerManager = getOrNull(),
        )
    }

    viewModel {
        SearchViewModel(
            videoRepository = get(),
            libraryRepository = get(),
            cloudLibraryRepository = get(),
        )
    }

    viewModel {
        HomeViewModel(
            videoRepository = get(),
            authRepository = get(),
            cloudLibraryRepository = get(),
            themeRepository = get(),
        )
    }

    viewModel {
        LibraryViewModel(
            libraryRepository = get(),
            offlineDownloadManager = get(),
            authRepository = get(),
            cloudLibraryRepository = get(),
            videoRepository = get(),
        )
    }

    viewModel {
        ShortsViewModel(
            videoRepository = get(),
            playbackManager = get(),
            ngPreferencesDataSource = get(),
            playerPreferencesDataSource = get(),
            libraryRepository = get(),
            interactionRepository = get(),
            cloudLibraryRepository = getOrNull(),
            offlineDownloadManager = getOrNull(),
        )
    }
}
