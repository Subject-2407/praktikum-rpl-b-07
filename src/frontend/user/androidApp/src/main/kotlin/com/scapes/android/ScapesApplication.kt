package com.scapes.android

import android.app.Application
import com.scapes.di.initializeScapesKoin

/**
 * Android application entry point.
 */
class ScapesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeScapesKoin()
    }
}
