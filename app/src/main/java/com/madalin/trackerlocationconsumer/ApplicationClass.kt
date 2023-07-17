package com.madalin.trackerlocationconsumer

import android.app.Application
import com.madalin.trackerlocationconsumer.di.appModule
import com.madalin.trackerlocationconsumer.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()

        // initialize Koin with the defined modules
        startKoin {
            androidLogger() // koin logger
            androidContext(this@ApplicationClass)
            modules(appModule, viewModelModule)
        }
    }
}
