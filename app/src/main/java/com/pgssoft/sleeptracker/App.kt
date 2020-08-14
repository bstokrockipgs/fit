package com.pgssoft.sleeptracker

import android.app.Application
import com.pgssoft.sleeptracker.services.di.mainModule
import com.pgssoft.sleeptracker.services.di.viewModelsModule
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        JodaTimeAndroid.init(this)

        startKoin {
            androidContext(this@App)
            modules(listOf(mainModule, viewModelsModule))
        }
    }
}