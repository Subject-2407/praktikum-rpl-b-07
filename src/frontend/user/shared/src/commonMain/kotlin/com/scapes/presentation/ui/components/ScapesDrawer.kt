package com.scapes.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scapes.presentation.ui.theme.ScapesThemeColors

private val Scrim = Color.Black.copy(alpha = 0.32f)

@Composable
fun ScapesDrawer(
    isOpen: Boolean,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit,
) {
    if (isOpen) {
        Box(modifier = Modifier.fillMaxSize().background(Scrim).clickable { onClose() })
    }
    AnimatedVisibility(
        visible = isOpen,
        enter =
            slideInHorizontally(animationSpec = tween(340, easing = EaseOutCubic)) { -it } +
                fadeIn(),
        exit =
            slideOutHorizontally(animationSpec = tween(240, easing = EaseInCubic)) { -it } +
                fadeOut(),
    ) {
        Surface(
            modifier = Modifier.width(296.dp).fillMaxHeight(),
            color = colors.elevated,
            shadowElevation = 12.dp,
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(WindowInsets.statusBars.asPaddingValues())
                            .height(58.dp)
                            .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    IconShell(onClick = onClose, colors = colors) { CloseGlyph(colors.text) }
                    Text("Menu", style = MaterialTheme.typography.titleMedium, color = colors.text)
                    Spacer(Modifier.weight(1f))
                    IconShell(onClick = onThemeToggle, colors = colors) {
                        if (isDarkMode) {
                            SunGlyph(colors.text)
                        } else {
                            MoonGlyph(colors.text, colors.elevated)
                        }
                    }
                }
                DrawerItem("Home", colors, onHome)
                DrawerItem("Settings", colors, onSettings)
            }
        }
    }
}

@Composable
private fun DrawerItem(label: String, colors: ScapesThemeColors, onClick: () -> Unit) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(55.dp)
                .border(width = 0.5.dp, color = colors.support.copy(alpha = 0.32f))
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(label, color = colors.text, style = MaterialTheme.typography.titleMedium)
    }
}
