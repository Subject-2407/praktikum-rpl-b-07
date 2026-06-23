package com.scapes.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.LandingFeedState
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.ui.components.AndroidCategoryTabs
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.viewmodel.SearchViewModel
import org.koin.compose.koinInject
import androidx.compose.ui.Alignment
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
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
    val refreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    val hasFinishedLoading = landingFeedState.sections.none { it.isLoading }
    LaunchedEffect(hasFinishedLoading) {
        if (hasFinishedLoading) {
            isRefreshing = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.base)
    ) {
        AndroidCategoryTabs(
            categories = categories,
            activeCategorySlug = activeCategorySlug,
            colors = colors,
            isDarkMode = isDarkMode,
            enabledSources = enabledSources,
            selectedSource = selectedSource,
            onFeedSelected = onFeedSelected,
            onCategorySelected = onCategorySelected,
            onSourceSelected = onSourceSelected,
        )

        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

        Box(modifier = Modifier.weight(1f)) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                state = refreshState,
                onRefresh = {
                    isRefreshing = true
                    onFeedSelected()
                },
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator(
                        state = refreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = colors.surface,
                        color = colors.amber
                    )
                }
            ) {
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
}
