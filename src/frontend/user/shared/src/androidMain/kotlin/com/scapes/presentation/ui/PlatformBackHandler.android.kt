package com.scapes.presentation.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Platform back dispatch will be wired when Android shell behavior is implemented.
}
