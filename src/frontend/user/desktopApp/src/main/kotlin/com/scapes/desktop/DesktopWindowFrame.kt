package com.scapes.desktop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.scapes.presentation.ui.ScapesApp
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.ui.theme.scapesThemeColors
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Window

@Composable
fun DesktopWindowFrame(window: Window, windowState: WindowState, onCloseRequest: () -> Unit) {
    var isDarkTheme by remember { mutableStateOf(false) }
    val colors = scapesThemeColors(isDarkTheme)

    Surface(
        modifier =
            Modifier.fillMaxSize()
                .background(colors.base)
                .border(width = 1.dp, color = colors.support.copy(alpha = 0.35f)),
        shape = RectangleShape,
        color = colors.base,
    ) {
        Column(Modifier.fillMaxSize()) {
            DesktopTitleBar(
                window = window,
                windowState = windowState,
                colors = colors,
                onCloseRequest = onCloseRequest,
            )
            Box(Modifier.fillMaxSize()) { ScapesApp(onResolvedThemeChange = { isDarkTheme = it }) }
        }
    }
}

@Composable
private fun DesktopTitleBar(
    window: Window,
    windowState: WindowState,
    colors: ScapesThemeColors,
    onCloseRequest: () -> Unit,
) {
    val isMaximized = windowState.placement == WindowPlacement.Maximized
    val titleBarModifier =
        Modifier.fillMaxWidth()
            .height(44.dp)
            .background(colors.elevated)
            .pointerInput(windowState.placement) {
                detectTapGestures(
                    onDoubleTap = {
                        windowState.placement =
                            if (isMaximized) WindowPlacement.Floating else WindowPlacement.Maximized
                    }
                )
            }
            .pointerInput(windowState.placement) {
                var pointerStart: Point? = null
                var windowStart: Point? = null

                detectDragGestures(
                    onDragStart = {
                        if (windowState.placement == WindowPlacement.Floating) {
                            pointerStart = MouseInfo.getPointerInfo()?.location
                            windowStart = Point(window.location)
                        }
                    }
                ) { _, _ ->
                    val dragStart = pointerStart ?: return@detectDragGestures
                    val origin = windowStart ?: return@detectDragGestures
                    val currentPointer =
                        MouseInfo.getPointerInfo()?.location ?: return@detectDragGestures
                    val deltaX = currentPointer.x - dragStart.x
                    val deltaY = currentPointer.y - dragStart.y
                    window.location = Point(origin.x + deltaX, origin.y + deltaY)
                }
            }

    Row(modifier = titleBarModifier, verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.weight(1f).padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(10.dp).background(colors.amber))
            Text(
                text = "Scapes",
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
        }

        WindowActionButton(
            onClick = { windowState.isMinimized = true },
            hoverColor = colors.support.copy(alpha = 0.22f),
        ) {
            MinimizeIcon(color = colors.text)
        }

        WindowActionButton(
            onClick = {
                windowState.placement =
                    if (isMaximized) WindowPlacement.Floating else WindowPlacement.Maximized
            },
            hoverColor = colors.support.copy(alpha = 0.22f),
        ) {
            MaximizeIcon(color = colors.text, isMaximized = isMaximized)
        }

        WindowActionButton(onClick = onCloseRequest, hoverColor = Color(0xFFC42B1C)) {
            CloseIcon(color = Color.White)
        }
    }
}

@Composable
private fun WindowActionButton(
    onClick: () -> Unit,
    hoverColor: Color,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    TextButton(
        onClick = onClick,
        modifier = Modifier.width(48.dp).height(44.dp),
        interactionSource = interactionSource,
        shape = RectangleShape,
        colors =
            ButtonDefaults.textButtonColors(
                containerColor = if (isHovered) hoverColor else Color.Transparent
            ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun MinimizeIcon(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        drawLine(
            color = color,
            start = Offset(3f, size.height - 4f),
            end = Offset(size.width - 3f, size.height - 4f),
            strokeWidth = 1.8f,
        )
    }
}

@Composable
private fun MaximizeIcon(color: Color, isMaximized: Boolean) {
    Canvas(Modifier.size(16.dp)) {
        if (isMaximized) {
            drawRect(
                color = color,
                topLeft = Offset(4f, 3f),
                size = size.copy(width = size.width - 7f, height = size.height - 7f),
                style = Stroke(width = 1.6f),
            )
            drawRect(
                color = color,
                topLeft = Offset(2f, 5f),
                size = size.copy(width = size.width - 7f, height = size.height - 7f),
                style = Stroke(width = 1.6f),
            )
        } else {
            drawRect(
                color = color,
                topLeft = Offset(2.5f, 2.5f),
                size = size.copy(width = size.width - 5f, height = size.height - 5f),
                style = Stroke(width = 1.6f),
            )
        }
    }
}

@Composable
private fun CloseIcon(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        drawLine(
            color = color,
            start = Offset(3f, 3f),
            end = Offset(size.width - 3f, size.height - 3f),
            strokeWidth = 1.8f,
        )
        drawLine(
            color = color,
            start = Offset(size.width - 3f, 3f),
            end = Offset(3f, size.height - 3f),
            strokeWidth = 1.8f,
        )
    }
}
