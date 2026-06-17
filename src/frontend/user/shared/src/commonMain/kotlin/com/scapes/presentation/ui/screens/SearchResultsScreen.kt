package com.scapes.presentation.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.model.WallpaperFeedState
import com.scapes.presentation.ui.components.CategoryTabs
import com.scapes.presentation.ui.components.SearchResultBar
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.components.WallpaperImageCard
import com.scapes.presentation.ui.components.WallpaperSkeletonCard
import com.scapes.presentation.ui.theme.ScapesThemeColors

@Composable
fun SearchResultsScreen(
    query: String,
    selectedSource: SourceOption,
    feedState: WallpaperFeedState,
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
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(colors.base)
    ) {
        SearchResultBar(
            query = query,
            selectedSource = selectedSource,
            colors = colors,
            isDarkMode = isDarkMode,
            topBarModifier = topBarModifier,
            onQueryChange = onQueryChange,
            onToggleTheme = onToggleTheme,
            searchRecommendations = searchRecommendations,
            isLoadingRecommendations = isLoadingRecommendations,
            onRecommendationSelected = onRecommendationSelected,
            onDismissRecommendations = onDismissRecommendations,
            onSourceSelected = onSourceSelected,
            enabledSources = enabledSources,
            onSearch = onSearch,
            onBack = onBack,
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
        Box(modifier = Modifier.weight(1f)) {
            MasonryWallpaperGrid(
                wallpapers = feedState.wallpapers,
                searchedQuery = feedState.query,
                selectedSource = selectedSource,
                isInitialLoading = feedState.isInitialLoading,
                isLoadingMore = feedState.isLoadingMore,
                endReached = feedState.endReached,
                message = feedState.message,
                actionStates = actionStates,
                colors = colors,
                onLoadMore = onLoadMore,
                onOpenWallpaper = onOpenWallpaper,
                onSaveWallpaper = onSaveWallpaper,
                onApplyWallpaper = onApplyWallpaper,
                modifier = Modifier.fillMaxSize(),
                showDesktopScrollIndicator = windowControls != null,
            )
        }
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
            text = "On ${selectedSource.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
        )
    }
}

@Composable
private fun MasonryWallpaperGrid(
    wallpapers: List<WallpaperUi>,
    searchedQuery: String,
    selectedSource: SourceOption,
    isInitialLoading: Boolean,
    isLoadingMore: Boolean,
    endReached: Boolean,
    message: String?,
    actionStates: Map<String, WallpaperActionState>,
    colors: ScapesThemeColors,
    onLoadMore: () -> Unit,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
    modifier: Modifier = Modifier,
    showDesktopScrollIndicator: Boolean = false,
) {
    val listState = rememberLazyStaggeredGridState()
    Box(modifier = modifier) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            columns = StaggeredGridCells.Adaptive(minSize = 212.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp),
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                ResultHeader(query = searchedQuery, selectedSource = selectedSource, colors = colors)
            }
            if (wallpapers.isEmpty() && isInitialLoading) {
                items(8, key = { index -> "skeleton-$index" }) {
                    WallpaperSkeletonCard(colors = colors, modifier = Modifier.fillMaxWidth())
                }
            } else if (wallpapers.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SearchStatusPanel(
                        message =
                            when {
                                message != null -> message
                                else -> "No wallpapers found."
                            },
                        colors = colors,
                        loading = false,
                    )
                }
            } else {
                itemsIndexed(
                    items = wallpapers,
                    key = { index, wallpaper -> "${wallpaper.wallpaper.id}-$index" },
                ) { _, wallpaper ->
                    WallpaperImageCard(
                        wallpaper = wallpaper,
                        actionState = actionStates[wallpaper.wallpaper.id],
                        colors = colors,
                        onOpenDetail = { onOpenWallpaper(wallpaper) },
                        onSave = { onSaveWallpaper(wallpaper) },
                        onApply = { onApplyWallpaper(wallpaper) },
                    )
                }
            }

            if (wallpapers.isNotEmpty() && message != null && !isLoadingMore) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SearchStatusPanel(
                        message = message,
                        colors = colors,
                        loading = false,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }
            }

            if (wallpapers.isNotEmpty() && !isInitialLoading && !isLoadingMore && !endReached) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    LaunchedEffect(wallpapers.size) { onLoadMore() }
                    Spacer(Modifier.height(1.dp))
                }
            }
        }
        if (showDesktopScrollIndicator) {
            SubtleGridScrollIndicator(
                gridState = listState,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun SubtleGridScrollIndicator(
    gridState: LazyStaggeredGridState,
    modifier: Modifier = Modifier,
) {
    val totalItems = gridState.layoutInfo.totalItemsCount
    if (totalItems <= 0) return

    val progress =
        (gridState.firstVisibleItemIndex.toFloat() / (totalItems - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
    val visibleFraction =
        (gridState.layoutInfo.visibleItemsInfo.size.toFloat() / totalItems.toFloat())
            .coerceIn(0.08f, 0.3f)
    val alpha by animateFloatAsState(if (gridState.isScrollInProgress) 1f else 0f, label = "grid-scrollbar-alpha")

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(end = 8.dp, top = 12.dp, bottom = 12.dp)
                .width(3.dp)
    ) {
        val thumbHeight = 240.dp * visibleFraction
        val travel = (240.dp - thumbHeight) * progress
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(thumbHeight)
                    .offset(y = travel)
                    .background(Color.Gray.copy(alpha = 0.38f * alpha), RoundedCornerShape(999.dp))
        )
    }
}
