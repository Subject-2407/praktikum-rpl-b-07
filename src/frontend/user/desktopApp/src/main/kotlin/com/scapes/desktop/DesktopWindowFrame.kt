package com.scapes.desktop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import com.scapes.presentation.ui.ScapesApp
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.ui.theme.scapesThemeColors
import androidx.compose.foundation.shape.RoundedCornerShape
import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Rectangle
import java.awt.Window

@Composable
fun DesktopWindowFrame(window: Window, windowState: WindowState, onCloseRequest: () -> Unit) {
    var isDarkTheme by remember { mutableStateOf(false) }
    var restoredBounds by remember { mutableStateOf<Rectangle?>(null) }
    val colors = scapesThemeColors(isDarkTheme)
    val windowShape = RoundedCornerShape(18.dp)
    val isMaximized = restoredBounds != null

    Surface(
        modifier =
            Modifier.fillMaxSize()
                .clip(windowShape)
                .background(Color.Transparent),
        shape = windowShape,
        color = colors.base,
    ) {
        Box(Modifier.fillMaxSize()) {
            ScapesApp(
                topBarModifier =
                    desktopTitleBarModifier(
                        window = window,
                        isMaximized = isMaximized,
                        onToggleMaximize = {
                            restoredBounds = toggleWindowMaximize(window, restoredBounds)
                        },
                    ),
                windowControls = {
                    DesktopWindowControls(
                        window = window,
                        windowState = windowState,
                        isMaximized = isMaximized,
                        colors = colors,
                        onToggleMaximize = {
                            restoredBounds = toggleWindowMaximize(window, restoredBounds)
                        },
                        onCloseRequest = onCloseRequest,
                    )
                },
                onResolvedThemeChange = { isDarkTheme = it },
            )
        }
    }
}

private fun desktopTitleBarModifier(
    window: Window,
    isMaximized: Boolean,
    onToggleMaximize: () -> Unit,
): Modifier {
    return Modifier
            .pointerInput(isMaximized) {
                detectTapGestures(
                    onDoubleTap = { onToggleMaximize() }
                )
            }
            .pointerInput(isMaximized) {
                var pointerStart: Point? = null
                var windowStart: Point? = null

                detectDragGestures(
                    onDragStart = {
                        if (!isMaximized) {
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
}

@Composable
private fun DesktopWindowControls(
    window: Window,
    windowState: WindowState,
    isMaximized: Boolean,
    colors: ScapesThemeColors,
    onToggleMaximize: () -> Unit,
    onCloseRequest: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WindowActionButton(
            onClick = { windowState.isMinimized = true },
            hoverColor = colors.support.copy(alpha = 0.22f),
        ) { _ ->
            MinimizeIcon(color = colors.text)
        }

        WindowActionButton(
            onClick = onToggleMaximize,
            hoverColor = colors.support.copy(alpha = 0.22f),
        ) { _ ->
            MaximizeIcon(color = colors.text, isMaximized = isMaximized)
        }

        WindowActionButton(onClick = onCloseRequest, hoverColor = Color(0xFFC42B1C)) { isHovered ->
            CloseIcon(color = if (isHovered) Color.White else colors.text)
        }
    }
}

private fun toggleWindowMaximize(window: Window, restoredBounds: Rectangle?): Rectangle? {
    return if (restoredBounds == null) {
        val targetBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        val currentBounds = Rectangle(window.bounds)
        window.setLocation(targetBounds.x, targetBounds.y)
        window.setSize(targetBounds.width, targetBounds.height)
        currentBounds
    } else {
        window.bounds = restoredBounds
        null
    }
}

@Composable
private fun WindowActionButton(
    onClick: () -> Unit,
    hoverColor: Color,
    content: @Composable (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    TextButton(
        onClick = onClick,
        modifier = Modifier.width(38.dp).height(34.dp),
        interactionSource = interactionSource,
        shape = RectangleShape,
        colors =
            ButtonDefaults.textButtonColors(
                containerColor = if (isHovered) hoverColor else Color.Transparent
            ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content(isHovered) }
    }
}

@Composable
private fun MinimizeIcon(color: Color) {
    Canvas(Modifier.size(14.dp)) {
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
    Canvas(Modifier.size(14.dp)) {
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
    Canvas(Modifier.size(14.dp)) {
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
