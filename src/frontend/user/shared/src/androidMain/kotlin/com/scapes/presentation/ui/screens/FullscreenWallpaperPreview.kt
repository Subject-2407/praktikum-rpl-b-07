package com.scapes.presentation.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.scapes.domain.model.ApplyTarget
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.theme.ScapesThemeColors

enum class PreviewFlowState {
    INITIAL, SELECTING_TARGET, READY_TO_APPLY
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FullscreenWallpaperPreview(
    wallpaper: WallpaperUi,
    colors: ScapesThemeColors,
    isProcessing: Boolean = false,
    progress: Float? = null,
    phaseLabel: String? = null,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onApply: (ApplyTarget, Offset, Float) -> Unit,
    saveLabel: String = "Save"
) {
    var flowState by remember { mutableStateOf(PreviewFlowState.INITIAL) }
    var selectedTarget by remember { mutableStateOf<ApplyTarget?>(null) }
    var uiVisible by remember { mutableStateOf(true) }

    // Transformation state (in Pixels)
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }

    BackHandler(enabled = true) {
        if (isProcessing) return@BackHandler
        when (flowState) {
            PreviewFlowState.INITIAL -> onBack()
            else -> {
                flowState = PreviewFlowState.INITIAL
                selectedTarget = null
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        // Interactive Canvas (Panning & Zooming)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { uiVisible = !uiVisible }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3f)

                        val wallpaperAspect = if (wallpaper.wallpaper.height > 0)
                            wallpaper.wallpaper.width.toFloat() / wallpaper.wallpaper.height.toFloat()
                            else 1f

                        val screenAspect = screenWidthPx / screenHeightPx

                        val scaledImgWidth = if (wallpaperAspect > screenAspect) {
                            screenHeightPx * wallpaperAspect * scale
                        } else {
                            screenWidthPx * scale
                        }

                        val scaledImgHeight = if (wallpaperAspect < screenAspect) {
                            screenWidthPx / wallpaperAspect * scale
                        } else {
                            screenHeightPx * scale
                        }

                        val limitX = ((scaledImgWidth - screenWidthPx) / 2).coerceAtLeast(0f)
                        val limitY = ((scaledImgHeight - screenHeightPx) / 2).coerceAtLeast(0f)

                        val newX = (offset.x + pan.x).coerceIn(-limitX, limitX)
                        val newY = (offset.y + pan.y).coerceIn(-limitY, limitY)

                        offset = Offset(newX, newY)
                    }
                }
        ) {
            SubcomposeAsyncImage(
                model = wallpaper.wallpaper.fullImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.amber)
                    }
                }
            )

            if (flowState != PreviewFlowState.READY_TO_APPLY && uiVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                )
            }
        }

        // UI overlay
        AnimatedVisibility(
            visible = uiVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isProcessing) return@IconButton
                        if (flowState == PreviewFlowState.INITIAL) onBack()
                        else {
                            flowState = PreviewFlowState.INITIAL
                            selectedTarget = null
                        }
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (flowState != PreviewFlowState.READY_TO_APPLY) {
                        IconButton(
                            onClick = { 
                                if (isProcessing) return@IconButton
                                uiVisible = !uiVisible 
                            },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (uiVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle UI",
                                tint = Color.White
                            )
                        }
                    }

                    if (flowState == PreviewFlowState.READY_TO_APPLY) {
                        IconButton(
                            onClick = { 
                                if (!isProcessing) {
                                    selectedTarget?.let { onApply(it, offset, scale) } 
                                }
                            },
                            modifier = Modifier.background(colors.amber, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Apply",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (flowState != PreviewFlowState.READY_TO_APPLY) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = wallpaper.title,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Text(
                            text = "by ${wallpaper.author}",
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${wallpaper.resolution} • ${wallpaper.wallpaper.source}",
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (flowState) {
                        PreviewFlowState.INITIAL -> {
                            Button(
                                onClick = onSave,
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(saveLabel, color = Color.White)
                            }
                            Button(
                                onClick = { flowState = PreviewFlowState.SELECTING_TARGET },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.amber),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text("Apply", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        PreviewFlowState.SELECTING_TARGET, PreviewFlowState.READY_TO_APPLY -> {
                            TargetButton(
                                label = "Home",
                                selected = selectedTarget == ApplyTarget.HOME_SCREEN,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedTarget = ApplyTarget.HOME_SCREEN
                                    flowState = PreviewFlowState.READY_TO_APPLY
                                }
                            )
                            TargetButton(
                                label = "Lock",
                                selected = selectedTarget == ApplyTarget.LOCK_SCREEN,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedTarget = ApplyTarget.LOCK_SCREEN
                                    flowState = PreviewFlowState.READY_TO_APPLY
                                }
                            )
                            TargetButton(
                                label = "Both",
                                selected = selectedTarget == ApplyTarget.BOTH_SCREENS,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedTarget = ApplyTarget.BOTH_SCREENS
                                    flowState = PreviewFlowState.READY_TO_APPLY
                                }
                            )
                        }
                    }
                }
            }
        }

        // Overlay for processing
        if (isProcessing) {
            val animatedProgress by animateFloatAsState(
                targetValue = progress ?: 0f,
                animationSpec = tween(durationMillis = 400),
                label = "applyProgress"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .pointerInput(Unit) {
                        detectTapGestures { } // Block all clicks from passing through
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            color = colors.amber,
                            trackColor = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(88.dp),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    if (phaseLabel != null) {
                        androidx.compose.foundation.layout.Spacer(
                            modifier = Modifier.height(16.dp)
                        )
                        Text(
                            text = phaseLabel,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
