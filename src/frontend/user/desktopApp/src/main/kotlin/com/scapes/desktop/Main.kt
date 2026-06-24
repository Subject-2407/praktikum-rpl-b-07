package com.scapes.desktop

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scapes.di.initializeScapesKoin
import java.awt.Dimension
import javax.imageio.ImageIO

/** Desktop Compose entry point. */
fun main() {
    initializeScapesKoin()

    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)
        val iconStream =
            Thread.currentThread().contextClassLoader
                .getResourceAsStream("composeResources/com.scapes.shared.generated.resources/drawable/scapes_logo.png")
                ?: ClassLoader.getSystemResourceAsStream("composeResources/com.scapes.shared.generated.resources/drawable/scapes_logo.png")
        val icon = iconStream?.let {
            BitmapPainter(ImageIO.read(it).toComposeImageBitmap())
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Scapes",
            state = windowState,
            icon = icon,
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
