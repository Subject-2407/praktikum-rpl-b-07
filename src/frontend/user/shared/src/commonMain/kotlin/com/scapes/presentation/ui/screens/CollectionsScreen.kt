package com.scapes.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.scapes.presentation.ui.theme.ScapesThemeColors

@Composable
fun CollectionsScreen(
    query: String,
    selectedSource: SourceOption,
    feedState: WallpaperFeedState,
    actionStates: Map<String, WallpaperActionState>,
    searchRecommendations: List<SearchRecommendation>,
    isLoadingRecommendations: Boolean,
    categories: List<WallpaperCategory>,
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
    onOpenWallpaper: (WallpaperUi) -> Unit,
    onSaveWallpaper: (WallpaperUi) -> Unit,
    onApplyWallpaper: (WallpaperUi) -> Unit,
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
    Column(Modifier.fillMaxSize().background(colors.base)) {
        SearchResultBar(
            query = query,
            selectedSource = selectedSource,
            colors = colors,
            isDarkMode = isDarkMode,
            topBarModifier = topBarModifier,
            onQueryChange = onQueryChange,
            searchRecommendations = searchRecommendations,
            isLoadingRecommendations = isLoadingRecommendations,
            onRecommendationSelected = onRecommendationSelected,
            onDismissRecommendations = onDismissRecommendations,
            onSourceSelected = onSourceSelected,
            enabledSources = enabledSources,
            onToggleTheme = onToggleTheme,
            onSearch = onSearch,
            onBack = {},
            windowControls = windowControls,
        )
        CategoryTabs(
            categories = categories,
            activeCategorySlug = null,
            isCollectionsActive = true,
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
            val isAnyApplying = remember(actionStates) { actionStates.values.any { it.isApplying } }
            WallpaperMasonryGrid(
                headerTitle = "Collections",
                headerSubtitle = "Saved locally",
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
                showDesktopScrollIndicator = windowControls != null,
                isAnyApplying = isAnyApplying,
                isCollection = true,
            )
        }
    }
}
