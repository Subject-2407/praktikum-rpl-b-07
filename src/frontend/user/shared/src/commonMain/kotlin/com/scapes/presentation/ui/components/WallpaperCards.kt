package com.scapes.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.ui.theme.ScapesThemeColors
import kotlinx.coroutines.launch

private const val HoldApplyMillis = 1800

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperImageCard(
    wallpaper: WallpaperUi,
    actionState: WallpaperActionState?,
    colors: ScapesThemeColors,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    aspectRatioOverride: Float? = null,
    showActions: Boolean = true,
    enableLongPress: Boolean = true,
    onOpenDetail: () -> Unit,
    onSave: () -> Unit,
    onApply: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val holdProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isHolding by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)
    val aspectRatio = aspectRatioOverride ?: wallpaper.aspectRatio()

    LaunchedEffect(actionState?.isApplying) {
        if (actionState?.isApplying != true && !isHolding) {
            holdProgress.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .aspectRatio(if (featured) maxOf(aspectRatio, 1.55f) else aspectRatio)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .combinedClickable(onClick = onOpenDetail, onLongClick = {})
            .then(
                if (enableLongPress) {
                    Modifier.pointerInput(wallpaper.wallpaper.id) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitPointerEvent().changes.firstOrNull { it.pressed }
                                if (down == null) continue
                                isHolding = true
                                val job = scope.launch {
                                    holdProgress.snapTo(0f)
                                    holdProgress.animateTo(1f, tween(HoldApplyMillis))
                                    if (isHolding) onApply()
                                }
                                do {
                                    val event = awaitPointerEvent()
                                } while (event.changes.any { it.pressed })
                                isHolding = false
                                job.cancel()
                                if (holdProgress.value < 1f) {
                                    scope.launch { holdProgress.snapTo(0f) }
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                }
            ),
    ) {
        WallpaperVisual(wallpaper = wallpaper, colors = colors, modifier = Modifier.fillMaxSize())

        AnimatedVisibility(
            visible = isHovered || actionState?.isApplying == true || actionState?.isSaving == true,
            enter = fadeIn(animationSpec = tween(150)) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(animationSpec = tween(120)) + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            WallpaperCardOverlay(
                wallpaper = wallpaper,
                actionState = actionState,
                colors = colors,
                showActions = showActions,
                onSave = onSave,
                onApply = onApply,
            )
        }

        if (enableLongPress && (isHolding || actionState?.isApplying == true)) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.24f))
                        .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth(holdProgress.value.coerceIn(0f, 1f))
                                .height(4.dp)
                                .background(colors.amber)
                    )
                }
            }
        }
    }
}

@Composable
fun WallpaperSkeletonCard(
    colors: ScapesThemeColors,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    aspectRatioOverride: Float? = null,
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier =
            modifier
                .clip(shape)
                .aspectRatio(
                    when {
                        featured -> 1.95f
                        aspectRatioOverride != null -> aspectRatioOverride
                        else -> 0.86f
                    }
                ),
    ) {
        NeutralPlaceholderSurface(colors = colors, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun WallpaperCardOverlay(
    wallpaper: WallpaperUi,
    actionState: WallpaperActionState?,
    colors: ScapesThemeColors,
    showActions: Boolean,
    onSave: () -> Unit,
    onApply: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.68f))
                    )
                )
                .padding(10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = wallpaper.author,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = wallpaper.resolution,
                color = Color.White.copy(alpha = 0.76f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
        if (showActions) {
            WallpaperCardAction(
                enabled = actionState?.isSaving != true && actionState?.isApplying != true,
                isLoading = actionState?.isSaving == true,
                progress = if (actionState?.isSaving == true) actionState.downloadProgress else null,
                colors = colors,
                icon = { color -> DownloadGlyph(color) },
                onClick = onSave,
            )
            WallpaperCardAction(
                enabled = actionState?.isSaving != true && actionState?.isApplying != true,
                isLoading = actionState?.isApplying == true,
                progress = if (actionState?.isApplying == true) actionState.downloadProgress else null,
                colors = colors,
                icon = { color -> ApplyGlyph(color) },
                onClick = onApply,
            )
        }
    }
}

@Composable
fun WallpaperDetailDialog(
    wallpaper: WallpaperUi,
    actionState: WallpaperActionState?,
    colors: ScapesThemeColors,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onApply: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun dismissWithAnimation() {
        visible = false
        scope.launch {
            kotlinx.coroutines.delay(150)
            onDismiss()
        }
    }

    LaunchedEffect(wallpaper.wallpaper.id) { visible = true }

    Dialog(onDismissRequest = ::dismissWithAnimation) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 20.dp)
        ) {
            val maxDialogWidth = if (maxWidth > 1520.dp) 1360.dp else maxWidth

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.9f),
                exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.96f),
            ) {
                Surface(
                    modifier =
                        Modifier.width(maxDialogWidth)
                            .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                ) {
                    Column(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ResolutionGlyph(
                                    color = colors.secondaryText,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text =
                                        if (wallpaper.wallpaper.width > 0 && wallpaper.wallpaper.height > 0) {
                                            "${wallpaper.wallpaper.width} x ${wallpaper.wallpaper.height}"
                                        } else {
                                            wallpaper.resolution
                                        },
                                    color = colors.secondaryText,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                            Box(
                                modifier =
                                    Modifier.pointerHoverIcon(PointerIcon.Hand)
                                        .clickable(onClick = ::dismissWithAnimation)
                            ) {
                                CloseGlyph(
                                    color = colors.secondaryText,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }

                        Box(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .aspectRatio(wallpaper.aspectRatio().coerceIn(1f, 2.2f))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.base.copy(alpha = 0.7f))
                        ) {
                            WallpaperVisual(
                                wallpaper = wallpaper,
                                colors = colors,
                                modifier = Modifier.fillMaxSize(),
                                imageUrl = wallpaper.wallpaper.previewUrl.ifBlank { null },
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = wallpaper.title,
                                color = colors.text,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            wallpaper.wallpaper.description
                                ?.takeIf { it.isNotBlank() }
                                ?.let { description ->
                                    Text(
                                        text = description,
                                        color = colors.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            Text(
                                text = "${wallpaper.author} on ${wallpaper.wallpaper.source.displayLabel()}",
                                color = colors.secondaryText,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                DetailActionButton(
                                    label = when {
                                        actionState?.isSaving == true && actionState.downloadProgress != null -> "Downloading ${(actionState.downloadProgress * 100).toInt()}%"
                                        actionState?.isSaving == true -> "Saving"
                                        else -> "Save"
                                    },
                                    enabled =
                                        actionState?.isSaving != true &&
                                            actionState?.isApplying != true,
                                    colors = colors,
                                    accent = false,
                                    icon = { color -> DownloadGlyph(color) },
                                    onClick = onSave,
                                )
                                DetailActionButton(
                                    label = when {
                                        actionState?.isApplying == true && actionState.downloadProgress != null -> "Downloading ${(actionState.downloadProgress * 100).toInt()}%"
                                        actionState?.isApplying == true -> "Applying"
                                        else -> "Apply"
                                    },
                                    enabled =
                                        actionState?.isSaving != true &&
                                            actionState?.isApplying != true,
                                    colors = colors,
                                    accent = true,
                                    icon = { color -> ApplyGlyph(color) },
                                    onClick = onApply,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperCardAction(
    enabled: Boolean,
    isLoading: Boolean = false,
    progress: Float? = null,
    colors: ScapesThemeColors,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
) {
    val clickableModifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier =
            Modifier.size(32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.base.copy(alpha = if (enabled) 0.84f else 0.52f))
                .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                .then(clickableModifier),
        contentAlignment = Alignment.Center,
    ) {
        when {
            progress != null -> {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = colors.text,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp)),
                    maxLines = 1,
                )
            }
            isLoading -> {
                LoadingGlyph(colors.text, modifier = Modifier.size(18.dp))
            }
            else -> {
                icon(colors.text)
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    label: String,
    enabled: Boolean,
    colors: ScapesThemeColors,
    accent: Boolean,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
) {
    val clickableModifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier
    val backgroundColor =
        when {
            !enabled -> colors.base.copy(alpha = 0.38f)
            accent -> colors.amber
            else -> colors.base.copy(alpha = 0.76f)
        }
    val contentColor =
        when {
            !enabled -> colors.secondaryText
            accent -> if (colors.text == Color.Black) colors.text else colors.base
            else -> colors.text
        }

    Row(
        modifier =
            Modifier.height(38.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(backgroundColor)
                .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                .then(clickableModifier)
                .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon(contentColor)
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}

private fun WallpaperUi.aspectRatio(): Float =
    if (wallpaper.width > 0 && wallpaper.height > 0) {
        (wallpaper.width.toFloat() / wallpaper.height.toFloat()).coerceIn(0.7f, 2.4f)
    } else {
        16f / 9f
    }

private fun com.scapes.domain.model.WallpaperSource.displayLabel(): String =
    when (this) {
        com.scapes.domain.model.WallpaperSource.PEXELS -> "Pexels"
        com.scapes.domain.model.WallpaperSource.UNSPLASH -> "Unsplash"
        com.scapes.domain.model.WallpaperSource.PIXABAY -> "Pixabay"
        com.scapes.domain.model.WallpaperSource.SCAPES_API -> "Scapes"
    }
