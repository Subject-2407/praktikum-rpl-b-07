package com.scapes.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.scapes.data.local.DownloadedWallpaperStore
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.model.WallpaperFeedState
import com.scapes.presentation.ui.components.AndroidMasonryGrid
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.viewmodel.SearchViewModel
import org.koin.compose.koinInject

@Composable
fun AndroidCollectionsScreen(
    modifier: Modifier = Modifier,
    query: String,
    selectedSource: SourceOption,
    feedState: WallpaperFeedState,
    actionStates: Map<String, WallpaperActionState>,
    searchRecommendations: List<SearchRecommendation>,
    isLoadingRecommendations: Boolean,
    categories: List<WallpaperCategory>,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
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
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
    searchViewModel: SearchViewModel = koinInject()
) {

    LaunchedEffect(Unit) {
        // load collections
        searchViewModel.loadCollections()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.base)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidMasonryGrid(
                headerTitle = "",
                headerSubtitle = "",
                wallpapers = feedState.wallpapers,
                isInitialLoading = feedState.isInitialLoading,
                isLoadingMore = false,
                endReached = true,
                message = feedState.message,
                actionStates = actionStates,
                colors = colors,
                onLoadMore = {},
                onOpenWallpaper = onOpenWallpaper,
                onSaveWallpaper = onSaveWallpaper,
                onApplyWallpaper = onApplyWallpaper,
                modifier = Modifier.fillMaxSize(),
                showDesktopScrollIndicator = false,
            )
        }
    }
}
