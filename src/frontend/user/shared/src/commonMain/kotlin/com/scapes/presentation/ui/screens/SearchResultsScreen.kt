package com.scapes.presentation.ui.screens

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.model.WallpaperFeedState
import com.scapes.presentation.ui.components.SearchResultBar
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.components.WallpaperVisual
import com.scapes.presentation.ui.theme.ScapesThemeColors
import kotlinx.coroutines.delay

@Composable
fun SearchResultsScreen(
    query: String,
    selectedSource: SourceOption,
    feedState: WallpaperFeedState,
    actionStates: Map<String, WallpaperActionState>,
    colors: ScapesThemeColors,
    onQueryChange: (String) -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.base, colors.elevated.copy(alpha = 0.86f), colors.base)
                )
            )
    ) {
        SearchResultBar(
            query = query,
            colors = colors,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onBack = onBack,
        )
        SearchStudioSourceDropdown(
            selectedSource = selectedSource,
            colors = colors,
            onSourceSelected = onSourceSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        ResultHeader(query = query, selectedSource = selectedSource, colors = colors)
        MasonryWallpaperGrid(
            wallpapers = feedState.wallpapers,
            isInitialLoading = feedState.isInitialLoading,
            isLoadingMore = feedState.isLoadingMore,
            endReached = feedState.endReached,
            message = feedState.message,
            actionStates = actionStates,
            colors = colors,
            onLoadMore = onLoadMore,
            onSaveWallpaper = onSaveWallpaper,
            onApplyWallpaper = onApplyWallpaper,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ResultHeader(query: String, selectedSource: SourceOption, colors: ScapesThemeColors) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = query.ifBlank { "Fresh picks" },
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${selectedSource.label} source",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
        )
    }
}

@Composable
private fun MasonryWallpaperGrid(
    wallpapers: List<WallpaperUi>,
    isInitialLoading: Boolean,
    isLoadingMore: Boolean,
    endReached: Boolean,
    message: String?,
    actionStates: Map<String, WallpaperActionState>,
    colors: ScapesThemeColors,
    onLoadMore: () -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val left = wallpapers.filterIndexed { index, _ -> index % 2 == 0 }
    val right = wallpapers.filterIndexed { index, _ -> index % 2 == 1 }
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp),
    ) {
        if (wallpapers.isEmpty()) {
            item {
                SearchStatusPanel(
                    message =
                        when {
                            isInitialLoading -> "Fetching wallpapers..."
                            message != null -> message
                            else -> "No wallpapers found."
                        },
                    colors = colors,
                    loading = isInitialLoading,
                )
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        left.forEach { wallpaper ->
                            MasonryWallpaperCard(
                                wallpaper = wallpaper,
                                actionState = actionStates[wallpaper.wallpaper.id],
                                colors = colors,
                                onSave = { onSaveWallpaper(wallpaper) },
                                onApply = { onApplyWallpaper(wallpaper) },
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f).padding(top = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        right.forEach { wallpaper ->
                            MasonryWallpaperCard(
                                wallpaper = wallpaper,
                                actionState = actionStates[wallpaper.wallpaper.id],
                                colors = colors,
                                onSave = { onSaveWallpaper(wallpaper) },
                                onApply = { onApplyWallpaper(wallpaper) },
                            )
                        }
                    }
                }
            }
        }

        if (wallpapers.isNotEmpty() && isLoadingMore) {
            item {
                SearchStatusPanel(
                    message = "Loading more wallpapers...",
                    colors = colors,
                    loading = true,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        if (wallpapers.isNotEmpty() && message != null && !isLoadingMore) {
            item {
                SearchStatusPanel(
                    message = message,
                    colors = colors,
                    loading = false,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        if (wallpapers.isNotEmpty() && !isInitialLoading && !isLoadingMore && !endReached) {
            item {
                LaunchedEffect(wallpapers.size) { onLoadMore() }
                Spacer(Modifier.height(1.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MasonryWallpaperCard(
    wallpaper: WallpaperUi,
    actionState: WallpaperActionState?,
    colors: ScapesThemeColors,
    onSave: () -> Unit,
    onApply: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by
        animateFloatAsState(
            targetValue = if (pressed) 0.98f else 1f,
            animationSpec = tween(durationMillis = 150, easing = EaseOutCubic),
        )

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(90)
            pressed = false
        }
    }

    Card(
        modifier =
            Modifier.fillMaxWidth()
                .height(wallpaper.height)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .combinedClickable(onClick = { pressed = true }, onLongClick = { pressed = true }),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            WallpaperVisual(
                wallpaper = wallpaper,
                colors = colors,
                modifier = Modifier.fillMaxSize(),
            )
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                WallpaperCardAction(
                    label = if (actionState?.isSaving == true) "Saving" else "Save",
                    enabled = actionState?.isSaving != true && actionState?.isApplying != true,
                    colors = colors,
                    onClick = onSave,
                )
                WallpaperCardAction(
                    label = if (actionState?.isApplying == true) "Applying" else "Apply",
                    enabled = actionState?.isSaving != true && actionState?.isApplying != true,
                    colors = colors,
                    onClick = onApply,
                )
            }
            Column(
                modifier =
                    Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.56f))
                            )
                        )
                        .padding(10.dp)
            ) {
                Text(
                    wallpaper.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    wallpaper.author,
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    wallpaper.resolution,
                    color = Color.White.copy(alpha = 0.74f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                actionState?.message?.let { message ->
                    Text(
                        message,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperCardAction(
    label: String,
    enabled: Boolean,
    colors: ScapesThemeColors,
    onClick: () -> Unit,
) {
    val background =
        if (enabled) colors.base.copy(alpha = 0.82f) else colors.base.copy(alpha = 0.52f)
    val clickableModifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier =
            Modifier.height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(background)
                .border(1.dp, colors.support.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                .then(clickableModifier)
                .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = colors.text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
