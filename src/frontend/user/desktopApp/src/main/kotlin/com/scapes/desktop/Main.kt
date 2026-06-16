package com.scapes.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.scapes.di.initializeScapesKoin
import com.scapes.presentation.ui.ScapesApp

/**
 * Desktop Compose entry point.
 */
fun main() {
    initializeScapesKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Scapes",
        ) {
            ScapesApp()
        }
    }
}
