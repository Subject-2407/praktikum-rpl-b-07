package com.scapes.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.LandingFeedState
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.viewmodel.SearchViewModel
import org.koin.compose.koinInject

@Composable
fun AndroidHomeScreen(
    modifier: Modifier = Modifier,
    query: String,
    selectedSource: SourceOption,
    landingFeedState: LandingFeedState,
    actionStates: Map<String, WallpaperActionState>,
    categories: List<WallpaperCategory>,
    activeCategorySlug: String?,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onQueryChange: (String) -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    enabledSources: Set<WallpaperSource>,
    onFeedSelected: () -> Unit,
    onCategorySelected: (WallpaperCategory) -> Unit,
    onCollectionsSelected: () -> Unit,
    onSearch: () -> Unit,
    onQuickSearch: (String) -> Unit,
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
    onToggleTheme: () -> Unit,
    searchViewModel: SearchViewModel = koinInject(),
    searchRecommendations: List<SearchRecommendation> = emptyList(),
    isLoadingRecommendations: Boolean = false,
    onRecommendationSelected: (SearchRecommendation) -> Unit = {},
    onDismissRecommendations: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().background(colors.base)
    ) {
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
                        showCardActions = false,
                        enableLongPress = false,
                    )
                }
            }
        }
    }
}
