package com.scapes.presentation.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scapes.domain.model.DownloadOrganization
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.SettingsUiState
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.model.WallpaperFeedState
import com.scapes.presentation.ui.components.CategoryTabs
import com.scapes.presentation.ui.components.SearchResultBar
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.components.WallpaperImageCard
import com.scapes.presentation.ui.components.WallpaperSkeletonCard
import com.scapes.presentation.ui.theme.ScapesThemeColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    isCollectionsActive: Boolean = false,
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
    onCollectionsSelected: () -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
    onBack: () -> Unit,
    settingsState: SettingsUiState? = null,
    onSettingsInputChange: ((WallpaperSource, String) -> Unit)? = null,
    onSettingsSave: ((WallpaperSource) -> Unit)? = null,
    onSettingsRemove: ((WallpaperSource) -> Unit)? = null,
    onSettingsDownloadFolderChange: ((String) -> Unit)? = null,
    onSettingsChooseDownloadFolder: (() -> Unit)? = null,
    onSettingsDownloadOrganizationChange: ((DownloadOrganization) -> Unit)? = null,
    onSettingsSaveDownloadSettings: (() -> Unit)? = null,
    onSettingsLoad: (() -> Unit)? = null,
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
            isCollectionsActive = isCollectionsActive,
            colors = colors,
            isDarkMode = isDarkMode,
            onFeedSelected = onFeedSelected,
            onCategorySelected = onCategorySelected,
            onCollectionsSelected = onCollectionsSelected,
            settingsState = settingsState,
            onSettingsInputChange = onSettingsInputChange,
            onSettingsSave = onSettingsSave,
            onSettingsRemove = onSettingsRemove,
            onSettingsDownloadFolderChange = onSettingsDownloadFolderChange,
            onSettingsChooseDownloadFolder = onSettingsChooseDownloadFolder,
            onSettingsDownloadOrganizationChange = onSettingsDownloadOrganizationChange,
            onSettingsSaveDownloadSettings = onSettingsSaveDownloadSettings,
            onSettingsLoad = onSettingsLoad,
        )
        Box(modifier = Modifier.weight(1f)) {
            WallpaperMasonryGrid(
                headerTitle = feedState.query.ifBlank { "Fresh picks" },
                headerSubtitle = "On ${selectedSource.label}",
                wallpapers = feedState.wallpapers,
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
internal fun WallpaperGridHeader(
    title: String,
    subtitle: String,
    colors: ScapesThemeColors,
) {
    Column(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
        )
    }
}

@Composable
internal fun WallpaperMasonryGrid(
    headerTitle: String,
    headerSubtitle: String,
    wallpapers: List<WallpaperUi>,
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
                WallpaperGridHeader(title = headerTitle, subtitle = headerSubtitle, colors = colors)
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
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val progress =
        (gridState.firstVisibleItemIndex.toFloat() / (totalItems - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
    val visibleFraction =
        (gridState.layoutInfo.visibleItemsInfo.size.toFloat() / totalItems.toFloat())
            .coerceIn(0.08f, 0.3f)
    val alpha by
        animateFloatAsState(
            if (gridState.isScrollInProgress || isHovered) 0.92f else 0.12f,
            label = "grid-scrollbar-alpha",
        )

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(end = 6.dp, top = 12.dp, bottom = 12.dp)
                .width(12.dp)
                .pointerHoverIcon(PointerIcon.Hand)
                .hoverable(interactionSource)
    ) {
        val maxIndex = (totalItems - 1).coerceAtLeast(0)
        val thumbHeight = (maxHeight * visibleFraction).coerceAtLeast(44.dp)
        val travel = (maxHeight - thumbHeight).coerceAtLeast(0.dp) * progress
        fun scrollToTrackPosition(trackY: Float) {
            if (maxIndex <= 0 || maxHeight.value <= 0f) return
            val targetProgress = (trackY / with(density) { maxHeight.toPx() }).coerceIn(0f, 1f)
            val targetIndex = (targetProgress * maxIndex).roundToInt()
            scope.launch { gridState.scrollToItem(targetIndex) }
        }

        Box(
            modifier =
                Modifier.fillMaxSize()
                    .pointerInput(totalItems) {
                        detectTapGestures { offset -> scrollToTrackPosition(offset.y) }
                    }
                    .pointerInput(totalItems) {
                        detectVerticalDragGestures(
                            onDragStart = { offset -> scrollToTrackPosition(offset.y) },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                                scrollToTrackPosition(change.position.y)
                            },
                        )
                    }
        ) {
            Box(
                modifier =
                    Modifier.align(Alignment.Center)
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(Color.Gray.copy(alpha = 0.12f * alpha), RoundedCornerShape(999.dp))
            )
            Box(
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .width(4.dp)
                        .height(thumbHeight)
                        .offset(y = travel)
                        .background(Color.Gray.copy(alpha = 0.58f * alpha), RoundedCornerShape(999.dp))
            )
        }
    }
}
