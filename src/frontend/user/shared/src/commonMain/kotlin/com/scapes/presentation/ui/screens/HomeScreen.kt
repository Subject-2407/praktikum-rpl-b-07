package com.scapes.presentation.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.LandingFeedState
import com.scapes.presentation.model.LandingSectionState
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.ui.components.CategoryTabs
import com.scapes.presentation.ui.components.HomeAppBar
import com.scapes.presentation.ui.components.LoadingGlyph
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.components.WallpaperImageCard
import com.scapes.presentation.ui.components.WallpaperSkeletonCard
import com.scapes.presentation.ui.theme.ScapesThemeColors

@Composable
fun HomeScreen(
    query: String,
    selectedSource: SourceOption,
    landingFeedState: LandingFeedState,
    actionStates: Map<String, WallpaperActionState>,
    searchRecommendations: List<SearchRecommendation>,
    isLoadingRecommendations: Boolean,
    categories: List<WallpaperCategory>,
    activeCategorySlug: String?,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    topBarModifier: Modifier = Modifier,
    windowControls: @Composable (() -> Unit)? = null,
    onQueryChange: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onRecommendationSelected: (SearchRecommendation) -> Unit,
    onDismissRecommendations: () -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    enabledSources: Set<WallpaperSource>,
    onFeedSelected: () -> Unit,
    onCategorySelected: (WallpaperCategory) -> Unit,
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
    onQuickSearch: (String) -> Unit,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(colors.base)
    ) {
        HomeAppBar(
            query = query,
            selectedSource = selectedSource,
            searchRecommendations = searchRecommendations,
            isLoadingRecommendations = isLoadingRecommendations,
            colors = colors,
            isDarkMode = isDarkMode,
            topBarModifier = topBarModifier,
            onOpenMenu = onOpenMenu,
            onQueryChange = onQueryChange,
            onToggleTheme = onToggleTheme,
            onRecommendationSelected = onRecommendationSelected,
            onDismissRecommendations = onDismissRecommendations,
            onSourceSelected = onSourceSelected,
            enabledSources = enabledSources,
            onSearch = onSearch,
            windowControls = windowControls,
        )
        CategoryTabs(
            categories = categories,
            activeCategorySlug = activeCategorySlug,
            colors = colors,
            isDarkMode = isDarkMode,
            onFeedSelected = onFeedSelected,
            onCategorySelected = onCategorySelected,
        )
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    CategoryCarouselFeed(
                        feedState = landingFeedState,
                        actionStates = actionStates,
                        colors = colors,
                        onQuickSearch = onQuickSearch,
                        onOpenWallpaper = onOpenWallpaper,
                        onSaveWallpaper = onSaveWallpaper,
                        onApplyWallpaper = onApplyWallpaper,
                    )
                }
            }
            if (windowControls != null) {
                SubtleVerticalScrollIndicator(
                    listState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }
}

@Composable
private fun CategoryCarouselFeed(
    feedState: LandingFeedState,
    actionStates: Map<String, WallpaperActionState>,
    colors: ScapesThemeColors,
    onQuickSearch: (String) -> Unit,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
) {
    val featuredSection = feedState.sections.firstOrNull { it.isFeatured }
    val regularSections = feedState.sections.filterNot { it.isFeatured }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val sectionColumns = if (maxWidth < 980.dp) 1 else 2

        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            featuredSection?.let { section ->
                CategoryCarouselSection(
                    section = section,
                    actionStates = actionStates,
                    colors = colors,
                    onQuickSearch = onQuickSearch,
                    onOpenWallpaper = onOpenWallpaper,
                    onSaveWallpaper = onSaveWallpaper,
                    onApplyWallpaper = onApplyWallpaper,
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                regularSections.chunked(sectionColumns).forEach { sectionRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        sectionRow.forEach { section ->
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryCarouselSection(
                                    section = section,
                                    actionStates = actionStates,
                                    colors = colors,
                                    onQuickSearch = onQuickSearch,
                                    onOpenWallpaper = onOpenWallpaper,
                                    onSaveWallpaper = onSaveWallpaper,
                                    onApplyWallpaper = onApplyWallpaper,
                                )
                            }
                        }
                        repeat(sectionColumns - sectionRow.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCarouselSection(
    section: LandingSectionState,
    actionStates: Map<String, WallpaperActionState>,
    colors: ScapesThemeColors,
    onQuickSearch: (String) -> Unit,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
) {
    Column(
        modifier =
            if (section.isFeatured) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
            },
        verticalArrangement = Arrangement.spacedBy(if (section.isFeatured) 14.dp else 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = section.title,
                style =
                    if (section.isFeatured) {
                        MaterialTheme.typography.headlineLarge
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "See all",
                color = colors.secondaryText,
                style = MaterialTheme.typography.labelLarge,
                modifier =
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable { onQuickSearch(section.searchQuery) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }

        if (section.wallpapers.isEmpty() && section.isLoading) {
            if (section.isFeatured) {
                FeaturedSkeletonCarousel(colors = colors)
            } else {
                CompactSkeletonMasonry(colors = colors)
            }
        } else if (section.wallpapers.isEmpty()) {
            SearchStatusPanel(
                message = section.message ?: "No wallpapers found.",
                colors = colors,
                loading = false,
            )
        } else if (section.isFeatured) {
            FeaturedWallpaperCarousel(
                wallpapers = section.wallpapers,
                actionStates = actionStates,
                colors = colors,
                onOpenWallpaper = onOpenWallpaper,
                onSaveWallpaper = onSaveWallpaper,
                onApplyWallpaper = onApplyWallpaper,
            )
        } else {
            CompactMasonryCarousel(
                wallpapers = section.wallpapers,
                actionStates = actionStates,
                colors = colors,
                onOpenWallpaper = onOpenWallpaper,
                onSaveWallpaper = onSaveWallpaper,
                onApplyWallpaper = onApplyWallpaper,
            )
        }
    }
}

@Composable
private fun SubtleVerticalScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val totalItems = listState.layoutInfo.totalItemsCount
    if (totalItems <= 0) return

    val progress =
        (listState.firstVisibleItemIndex.toFloat() / (totalItems - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
    val visibleFraction =
        (listState.layoutInfo.visibleItemsInfo.size.toFloat() / totalItems.toFloat())
            .coerceIn(0.08f, 0.34f)
    val alpha by animateFloatAsState(if (listState.isScrollInProgress) 1f else 0f, label = "scrollbar-alpha")

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(end = 8.dp, top = 6.dp, bottom = 12.dp)
                .width(3.dp)
                .background(Color.Transparent)
    ) {
        val thumbHeight = 220.dp * visibleFraction
        val travel = (220.dp - thumbHeight) * progress
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(thumbHeight)
                    .offset(y = travel)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Gray.copy(alpha = 0.38f * alpha))
        )
    }
}

@Composable
private fun FeaturedWallpaperCarousel(
    wallpapers: List<WallpaperUi>,
    actionStates: Map<String, WallpaperActionState>,
    colors: ScapesThemeColors,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
) {
    BoxWithConstraints {
        val cardWidth = if (maxWidth < 760.dp) 324.dp else 468.dp

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            wallpapers.forEach { wallpaper ->
                WallpaperImageCard(
                    wallpaper = wallpaper,
                    actionState = actionStates[wallpaper.wallpaper.id],
                    colors = colors,
                    featured = true,
                    modifier = Modifier.width(cardWidth),
                    onOpenDetail = { onOpenWallpaper(wallpaper) },
                    onSave = { onSaveWallpaper(wallpaper) },
                    onApply = { onApplyWallpaper(wallpaper) },
                )
            }
        }
    }
}

@Composable
private fun CompactMasonryCarousel(
    wallpapers: List<WallpaperUi>,
    actionStates: Map<String, WallpaperActionState>,
    colors: ScapesThemeColors,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
) {
    BoxWithConstraints {
        val rowsPerColumn = if (maxWidth < 540.dp) 1 else 2
        val columnWidth = if (rowsPerColumn == 1) 220.dp else (maxWidth - 10.dp) / 2

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            wallpapers.chunked(rowsPerColumn).forEach { columnWallpapers ->
                Column(
                    modifier = Modifier.width(columnWidth),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    columnWallpapers.forEach { wallpaper ->
                        WallpaperImageCard(
                            wallpaper = wallpaper,
                            actionState = actionStates[wallpaper.wallpaper.id],
                            colors = colors,
                            modifier = Modifier.fillMaxWidth(),
                            onOpenDetail = { onOpenWallpaper(wallpaper) },
                            onSave = { onSaveWallpaper(wallpaper) },
                            onApply = { onApplyWallpaper(wallpaper) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedSkeletonCarousel(colors: ScapesThemeColors) {
    BoxWithConstraints {
        val cardWidth = if (maxWidth < 760.dp) 324.dp else 468.dp

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(3) {
                WallpaperSkeletonCard(
                    colors = colors,
                    featured = true,
                    modifier = Modifier.width(cardWidth),
                )
            }
        }
    }
}

@Composable
private fun CompactSkeletonMasonry(colors: ScapesThemeColors) {
    BoxWithConstraints {
        val rowsPerColumn = if (maxWidth < 540.dp) 1 else 2
        val columnWidth = if (rowsPerColumn == 1) 220.dp else (maxWidth - 10.dp) / 2

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(4) {
                Column(
                    modifier = Modifier.width(columnWidth),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    repeat(rowsPerColumn) {
                        WallpaperSkeletonCard(
                            colors = colors,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
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

