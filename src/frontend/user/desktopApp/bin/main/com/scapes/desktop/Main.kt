package com.scapes.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scapes.di.initializeScapesKoin
import java.awt.Dimension

/** Desktop Compose entry point. */
fun main() {
    initializeScapesKoin()

    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = "Scapes",
            state = windowState,
            undecorated = true,
            transparent = true,
        ) {
            window.minimumSize = Dimension(1040, 720)
            DesktopWindowFrame(
                window = window,
                windowState = windowState,
                onCloseRequest = ::exitApplication,
            )
        }
    }
}
