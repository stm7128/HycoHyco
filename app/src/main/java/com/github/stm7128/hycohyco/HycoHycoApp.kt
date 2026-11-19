package com.github.stm7128.hycohyco

import android.app.Application
import com.github.stm7128.hycohyco.di.appModule
import com.github.stm7128.hycohyco.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * HycoHyco アプリケーションクラス
 * アプリ全体のライフサイクル管理および DI (Koin) の初期化を行う。
 */
class HycoHycoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@HycoHycoApp)
            modules(appModule, viewModelModule)
        }
    }
}
