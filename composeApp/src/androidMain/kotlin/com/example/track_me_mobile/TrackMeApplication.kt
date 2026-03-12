package com.example.track_me_mobile

import android.app.Application
import com.example.track_me_mobile.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class TrackMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@TrackMeApplication) // ← Передаем контекст
            modules(appModule)
        }
    }
}