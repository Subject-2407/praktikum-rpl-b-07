package com.scapes.presentation.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop keyboard/window back behavior will be wired with the platform shell.
}
