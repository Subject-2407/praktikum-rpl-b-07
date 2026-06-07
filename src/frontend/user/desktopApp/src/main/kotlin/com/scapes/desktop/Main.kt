package com.scapes.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.scapes.presentation.ui.ScapesApp

/**
 * Desktop Compose entry point.
 */
fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Scapes",
        ) {
            ScapesApp()
        }
    }
