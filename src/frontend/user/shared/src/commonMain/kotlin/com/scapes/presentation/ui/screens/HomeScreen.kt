package com.scapes.presentation.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.LandingFeedState
import com.scapes.presentation.model.LandingSectionState
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.ui.components.ChevronDownGlyph
import com.scapes.presentation.ui.components.HomeAppBar
import com.scapes.presentation.ui.components.LoadingGlyph
import com.scapes.presentation.ui.components.SearchInput
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.components.WallpaperVisual
import com.scapes.presentation.ui.theme.ScapesThemeColors

@Composable
fun HomeScreen(
    query: String,
    selectedSource: SourceOption,
    landingFeedState: LandingFeedState,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onQueryChange: (String) -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
    onQuickSearch: (String) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.base,
                            colors.support.copy(alpha = if (isDarkMode) 0.2f else 0.12f),
                            colors.base,
                        )
                    )
                ),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            HomeAppBar(
                colors = colors,
                isDarkMode = isDarkMode,
                onOpenMenu = onOpenMenu,
                onSearch = onSearch,
            )
        }
        item {
            SearchStudio(
                query = query,
                selectedSource = selectedSource,
                colors = colors,
                onQueryChange = onQueryChange,
                onSourceSelected = onSourceSelected,
                onSearch = onSearch,
            )
        }
        item {
            CategoryCarouselFeed(
                feedState = landingFeedState,
                colors = colors,
                onQuickSearch = onQuickSearch,
            )
        }
    }
}

@Composable
private fun SearchStudio(
    query: String,
    selectedSource: SourceOption,
    colors: ScapesThemeColors,
    onQueryChange: (String) -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    onSearch: () -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 10.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.surface,
                            colors.support.copy(alpha = 0.22f),
                            colors.amber.copy(alpha = 0.18f),
                        )
                    )
                )
                .border(1.dp, colors.support.copy(alpha = 0.34f), RoundedCornerShape(8.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Find a workspace mood in seconds.",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
        )
        SearchInput(
            query = query,
            onQueryChange = onQueryChange,
            colors = colors,
            onSearch = onSearch,
            modifier = Modifier.fillMaxWidth(),
        )
        SearchStudioSourceDropdown(
            selectedSource = selectedSource,
            colors = colors,
            onSourceSelected = onSourceSelected,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Search wallpaper",
                color = colors.base,
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(colors.text)
                        .clickable(onClick = onSearch)
                        .padding(horizontal = 16.dp, vertical = 11.dp),
            )
        }
    }
}

@Composable
fun SearchStudioSourceDropdown(
    selectedSource: SourceOption,
    colors: ScapesThemeColors,
    onSourceSelected: (SourceOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.base.copy(alpha = 0.72f))
                    .border(1.dp, colors.support.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier =
                    Modifier.size(9.dp)
                        .clip(CircleShape)
                        .background(sourceColor(selectedSource.source, colors))
            )
            Text(
                text = selectedSource.label,
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ChevronDownGlyph(colors.secondaryText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface),
        ) {
            SourceOption.defaults().forEach { source ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(9.dp)
                                        .clip(CircleShape)
                                        .background(sourceColor(source.source, colors))
                            )
                            Text(source.label, color = colors.text)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSourceSelected(source)
                    },
                )
            }
        }
    }
}

@Composable
private fun CategoryCarouselFeed(
    feedState: LandingFeedState,
    colors: ScapesThemeColors,
    onQuickSearch: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        feedState.sections.forEach { section ->
            CategoryCarouselSection(
                section = section,
                colors = colors,
                onQuickSearch = onQuickSearch,
            )
        }
    }
}

@Composable
private fun CategoryCarouselSection(
    section: LandingSectionState,
    colors: ScapesThemeColors,
    onQuickSearch: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = section.title, style = MaterialTheme.typography.titleLarge, color = colors.text)
        if (section.wallpapers.isEmpty()) {
            SearchStatusPanel(
                message =
                    when {
                        section.isLoading -> "Fetching ${section.title}..."
                        section.message != null -> section.message
                        else -> "No wallpapers found."
                    },
                colors = colors,
                loading = section.isLoading,
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                section.wallpapers.forEachIndexed { index, wallpaper ->
                    CarouselWallpaperCard(
                        wallpaper = wallpaper,
                        colors = colors,
                        height = if (index % 3 == 1) 228.dp else 194.dp,
                        onClick = { onQuickSearch(wallpaper.title) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarouselWallpaperCard(
    wallpaper: WallpaperUi,
    colors: ScapesThemeColors,
    height: Dp,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier.width(156.dp)
                .height(height)
                .combinedClickable(onClick = onClick, onLongClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            WallpaperVisual(
                wallpaper = wallpaper,
                colors = colors,
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = wallpaper.title,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                        .padding(8.dp),
            )
        }
    }
}

@Composable
fun SearchStatusPanel(
    message: String,
    colors: ScapesThemeColors,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.84f))
                .border(1.dp, colors.support.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (loading) {
            LoadingGlyph(colors.text)
        }
        Text(text = message, color = colors.text, style = MaterialTheme.typography.bodyMedium)
    }
}

fun sourceColor(source: WallpaperSource, colors: ScapesThemeColors): Color =
    when (source) {
        WallpaperSource.SCAPES_API -> colors.text
        WallpaperSource.PEXELS -> colors.text
        WallpaperSource.UNSPLASH -> colors.text
        WallpaperSource.PIXABAY -> colors.text
    }
