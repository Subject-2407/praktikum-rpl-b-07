package com.scapes.android

import android.app.Application
import com.scapes.di.initializeScapesKoin
import org.koin.android.ext.koin.androidContext

/** Android application entry point. */
class ScapesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeScapesKoin {
            androidContext(this@ScapesApplication)
        }
    }
}
