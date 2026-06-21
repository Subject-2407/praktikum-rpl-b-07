package com.scapes.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.model.WallpaperFeedState
import com.scapes.presentation.ui.components.IconShell
import com.scapes.presentation.ui.components.SearchInput
import com.scapes.presentation.ui.components.SourceDropdown
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.components.BackGlyph
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.viewmodel.SearchViewModel
import androidx.compose.ui.Alignment
import org.koin.compose.koinInject

@Composable
fun AndroidSearchResultsScreen(
    modifier: Modifier = Modifier,
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
    onBack: () -> Unit,
    searchViewModel: SearchViewModel = koinInject()
) {
    Column(
        modifier = Modifier.fillMaxSize().background(colors.base)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.base.copy(alpha = 0.96f))
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconShell(
                    onClick = onBack,
                    colors = colors,
                ) {
                    BackGlyph(colors.text)
                }

                Box(modifier = Modifier.weight(1f)) {
                    SearchInput(
                        query = query,
                        onQueryChange = onQueryChange,
                        searchRecommendations = searchRecommendations,
                        isLoadingRecommendations = isLoadingRecommendations,
                        colors = colors,
                        onRecommendationSelected = onRecommendationSelected,
                        onDismissRecommendations = onDismissRecommendations,
                        onSearch = onSearch,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pick Sources?",
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
                SourceDropdown(
                    selectedSource = selectedSource,
                    colors = colors,
                    isDarkMode = isDarkMode,
                    enabledSources = enabledSources,
                    onSourceSelected = onSourceSelected,
                    modifier = Modifier.width(118.dp),
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f)
        ) {
            WallpaperMasonryGrid(
                headerTitle = "",
                headerSubtitle = "",
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
                onApplyWallpaper = { wallpaper ->
                    searchViewModel.applyWallpaper(wallpaper)
                },
                modifier = Modifier.fillMaxSize(),
                showDesktopScrollIndicator = false,
            )
        }
    }
}
