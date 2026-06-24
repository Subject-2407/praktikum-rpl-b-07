package com.scapes.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.platform.DirectoryPicker
import com.scapes.presentation.ui.components.ScapesDrawer
import com.scapes.presentation.ui.components.WallpaperDetailDialog
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.screens.CollectionsScreen
import com.scapes.presentation.ui.screens.HomeScreen
import com.scapes.presentation.ui.screens.SearchResultsScreen
import com.scapes.presentation.ui.screens.SettingsScreen
import com.scapes.presentation.ui.theme.ScapesTypography
import com.scapes.presentation.ui.theme.ThemePreference
import com.scapes.presentation.ui.theme.scapesThemeColors
import com.scapes.presentation.viewmodel.HomeViewModel
import com.scapes.presentation.viewmodel.ScapesViewModel
import com.scapes.presentation.viewmodel.SearchViewModel
import com.scapes.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ScapesApp(
    initialThemePreference: ThemePreference = ThemePreference.SYSTEM,
    topBarModifier: Modifier = Modifier,
    windowControls: @Composable (() -> Unit)? = null,
    onThemePreferenceChange: (ThemePreference) -> Unit = {},
    onResolvedThemeChange: (Boolean) -> Unit = {},
    viewModel: ScapesViewModel = koinInject(),
    homeViewModel: HomeViewModel = koinInject(),
    searchViewModel: SearchViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject(),
    directoryPicker: DirectoryPicker = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    val landingFeedState by homeViewModel.feedState.collectAsState()
    val searchFeedState by searchViewModel.feedState.collectAsState()
    val collectionsFeedState by searchViewModel.collectionsState.collectAsState()
    val wallpaperActionStates by searchViewModel.actionStates.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val systemIsDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    var selectedWallpaper by remember { mutableStateOf<WallpaperUi?>(null) }
    val isDarkMode =
        when (state.themePreference) {
            ThemePreference.SYSTEM -> systemIsDark
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
        }
    val colors = scapesThemeColors(isDarkMode)
    val enabledSources =
        buildSet {
            add(WallpaperSource.SCAPES_API)
            settingsState.forms
                .filter { form -> !form.maskedKey.isNullOrBlank() || form.hasDefaultKey }
                .forEach { form -> add(form.sourceOption.source) }
        }

    LaunchedEffect(initialThemePreference) { viewModel.setThemePreference(initialThemePreference) }

    LaunchedEffect(isDarkMode) { onResolvedThemeChange(isDarkMode) }

    LaunchedEffect(settingsViewModel) { settingsViewModel.load() }

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.apiKeyChanges.collect { changedSource ->
            val currentState = viewModel.uiState.value
            if (changedSource == currentState.selectedSource.source) {
                homeViewModel.load(currentState.selectedSource)
                val currentFeed = searchViewModel.feedState.value
                if (currentState.showResults && currentFeed.query.isNotBlank()) {
                    searchViewModel.search(
                        currentFeed.query,
                        currentState.selectedSource,
                        currentFeed.categorySlug,
                    )
                }
            }
        }
    }

    MaterialTheme(
        colorScheme =
            if (isDarkMode) {
                darkColorScheme(
                    primary = colors.text,
                    secondary = colors.amber,
                    background = colors.base,
                    surface = colors.surface,
                    onPrimary = colors.base,
                    onSurface = colors.text,
                    onBackground = colors.text,
                )
            } else {
                lightColorScheme(
                    primary = colors.text,
                    secondary = colors.amber,
                    background = colors.base,
                    surface = colors.surface,
                    onPrimary = Color.White,
                    onSurface = colors.text,
                    onBackground = colors.text,
                )
            },
        typography = ScapesTypography,
    ) {
        Box(Modifier.fillMaxSize().background(colors.base)) {
            when {
                state.showSettings ->
                    SettingsScreen(
                        state = settingsState,
                        colors = colors,
                        onInputChange = settingsViewModel::updateApiKeyInput,
                        onSave = settingsViewModel::saveApiKey,
                        onRemove = settingsViewModel::removeApiKey,
                        onDownloadFolderChange = settingsViewModel::updateDownloadFolderInput,
                        onChooseDownloadFolder = {
                            scope.launch {
                                directoryPicker
                                    .chooseDirectory(settingsState.downloadFolderInput)
                                    ?.let(settingsViewModel::updateDownloadFolderInput)
                            }
                        },
                        onDownloadOrganizationChange =
                            settingsViewModel::updateDownloadOrganization,
                        onSaveDownloadSettings = settingsViewModel::saveDownloadSettings,
                        onBack = viewModel::onBack,
                    )

                state.showResults ->
                    SearchResultsScreen(
                        query = state.query,
                        selectedSource = state.selectedSource,
                        feedState = searchFeedState,
                        actionStates = wallpaperActionStates,
                        searchRecommendations = state.searchRecommendations,
                        isLoadingRecommendations = state.isLoadingRecommendations,
                        categories = state.categories,
                        activeCategorySlug = state.activeCategorySlug,
                        isCollectionsActive = false,
                        colors = colors,
                        isDarkMode = isDarkMode,
                        topBarModifier = topBarModifier,
                        windowControls = windowControls,
                        onQueryChange = viewModel::onQueryChange,
                        onToggleTheme = {
                            val nextPreference = viewModel.toggleTheme(isDarkMode)
                            onThemePreferenceChange(nextPreference)
                        },
                        onRecommendationSelected = { recommendation ->
                            viewModel.applyRecommendation(recommendation.queryValue)
                            val query = viewModel.showResults(recommendation.queryValue)
                            searchViewModel.search(query, viewModel.uiState.value.selectedSource)
                        },
                        onDismissRecommendations = viewModel::dismissRecommendations,
                        enabledSources = enabledSources,
                        onSourceSelected = { source ->
                            viewModel.selectSource(source)
                            homeViewModel.load(source)
                            val nextState = viewModel.uiState.value
                            val activeCategory =
                                nextState.categories.firstOrNull {
                                    it.slug == nextState.activeCategorySlug
                                }
                            if (activeCategory != null) {
                                searchCategory(
                                    category = activeCategory,
                                    state = nextState,
                                    viewModel = viewModel,
                                    searchViewModel = searchViewModel,
                                )
                            } else {
                                val query = viewModel.showResults()
                                if (query.isNotBlank()) {
                                    searchViewModel.search(query, source)
                                }
                            }
                        },
                        onFeedSelected = {
                            viewModel.showHome()
                            homeViewModel.load(viewModel.uiState.value.selectedSource)
                        },
                        onCategorySelected = { category ->
                            searchCategory(
                                category = category,
                                state = viewModel.uiState.value,
                                viewModel = viewModel,
                                searchViewModel = searchViewModel,
                            )
                        },
                        onCollectionsSelected = {
                            viewModel.showCollections()
                            searchViewModel.loadCollections()
                        },
                        onSearch = {
                            val query = viewModel.showResults()
                            if (query.isNotBlank()) {
                                searchViewModel.search(
                                    query,
                                    viewModel.uiState.value.selectedSource,
                                )
                            }
                        },
                        onLoadMore = searchViewModel::loadMore,
                        onOpenWallpaper = { wallpaper -> selectedWallpaper = wallpaper },
                        onSaveWallpaper = searchViewModel::saveWallpaper,
                        onApplyWallpaper = searchViewModel::applyWallpaper,
                        onBack = {},
                    )

                state.showCollections ->
                    CollectionsScreen(
                        query = state.query,
                        selectedSource = state.selectedSource,
                        feedState = collectionsFeedState,
                        actionStates = wallpaperActionStates,
                        searchRecommendations = state.searchRecommendations,
                        isLoadingRecommendations = state.isLoadingRecommendations,
                        categories = state.categories,
                        colors = colors,
                        isDarkMode = isDarkMode,
                        topBarModifier = topBarModifier,
                        windowControls = windowControls,
                        onQueryChange = viewModel::onQueryChange,
                        onToggleTheme = {
                            val nextPreference = viewModel.toggleTheme(isDarkMode)
                            onThemePreferenceChange(nextPreference)
                        },
                        onRecommendationSelected = { recommendation ->
                            viewModel.applyRecommendation(recommendation.queryValue)
                            val query = viewModel.showResults(recommendation.queryValue)
                            searchViewModel.search(query, viewModel.uiState.value.selectedSource)
                        },
                        onDismissRecommendations = viewModel::dismissRecommendations,
                        onSourceSelected = viewModel::selectSource,
                        enabledSources = enabledSources,
                        onFeedSelected = {
                            viewModel.showHome()
                            homeViewModel.load(viewModel.uiState.value.selectedSource)
                        },
                        onCategorySelected = { category ->
                            searchCategory(
                                category = category,
                                state = viewModel.uiState.value,
                                viewModel = viewModel,
                                searchViewModel = searchViewModel,
                            )
                        },
                        onCollectionsSelected = {
                            viewModel.showCollections()
                            searchViewModel.loadCollections()
                        },
                        onSearch = {
                            val query = viewModel.showResults()
                            if (query.isNotBlank()) {
                                searchViewModel.search(
                                    query,
                                    viewModel.uiState.value.selectedSource,
                                )
                            }
                        },
                        onOpenWallpaper = { wallpaper -> selectedWallpaper = wallpaper },
                        onSaveWallpaper = searchViewModel::saveWallpaper,
                        onApplyWallpaper = searchViewModel::applyWallpaper,
                    )

                else ->
                    HomeScreen(
                        query = state.query,
                        selectedSource = state.selectedSource,
                        landingFeedState = landingFeedState,
                        actionStates = wallpaperActionStates,
                        searchRecommendations = state.searchRecommendations,
                        isLoadingRecommendations = state.isLoadingRecommendations,
                        categories = state.categories,
                        activeCategorySlug = state.activeCategorySlug,
                        isCollectionsActive = false,
                        colors = colors,
                        isDarkMode = isDarkMode,
                        topBarModifier = topBarModifier,
                        windowControls = windowControls,
                        onQueryChange = viewModel::onQueryChange,
                        onToggleTheme = {
                            val nextPreference = viewModel.toggleTheme(isDarkMode)
                            onThemePreferenceChange(nextPreference)
                        },
                        onRecommendationSelected = { recommendation ->
                            viewModel.applyRecommendation(recommendation.queryValue)
                            val query = viewModel.showResults(recommendation.queryValue)
                            searchViewModel.search(query, viewModel.uiState.value.selectedSource)
                        },
                        onDismissRecommendations = viewModel::dismissRecommendations,
                        enabledSources = enabledSources,
                        onSourceSelected = { source ->
                            viewModel.selectSource(source)
                            homeViewModel.load(source)
                        },
                        onFeedSelected = {
                            viewModel.showHome()
                            homeViewModel.load(viewModel.uiState.value.selectedSource)
                        },
                        onCategorySelected = { category ->
                            searchCategory(
                                category = category,
                                state = viewModel.uiState.value,
                                viewModel = viewModel,
                                searchViewModel = searchViewModel,
                            )
                        },
                        onCollectionsSelected = {
                            viewModel.showCollections()
                            searchViewModel.loadCollections()
                        },
                        onOpenMenu = viewModel::openMenu,
                        onSearch = {
                            val query = viewModel.showResults()
                            if (query.isNotBlank()) {
                                searchViewModel.search(
                                    query,
                                    viewModel.uiState.value.selectedSource,
                                )
                            }
                        },
                        onQuickSearch = { quickQuery ->
                            if (viewModel.uiState.value.selectedSource.source == WallpaperSource.SCAPES_API) {
                                val category = viewModel.uiState.value.categories.find { it.slug == quickQuery }
                                if (category != null) {
                                    searchCategory(category, viewModel.uiState.value, viewModel, searchViewModel)
                                } else {
                                    val query = viewModel.showResults(quickQuery)
                                    searchViewModel.search(query, viewModel.uiState.value.selectedSource, categorySlug = quickQuery)
                                }
                            } else {
                                val query = viewModel.showResults(quickQuery)
                                searchViewModel.search(query, viewModel.uiState.value.selectedSource)
                            }
                        },
                        onOpenWallpaper = { wallpaper -> selectedWallpaper = wallpaper },
                        onSaveWallpaper = searchViewModel::saveWallpaper,
                        onApplyWallpaper = searchViewModel::applyWallpaper,
                    )
            }

            selectedWallpaper?.let { wallpaper ->
                WallpaperDetailDialog(
                    wallpaper = wallpaper,
                    actionState = wallpaperActionStates[wallpaper.wallpaper.id],
                    colors = colors,
                    onDismiss = { selectedWallpaper = null },
                    onSave = { searchViewModel.saveWallpaper(wallpaper) },
                    onApply = { searchViewModel.applyWallpaper(wallpaper) },
                    onTagClick = { tag ->
                        selectedWallpaper = null
                        val query = viewModel.showResults(tag)
                        searchViewModel.search(query, state.selectedSource)
                    }
                )
            }

            ScapesDrawer(
                isOpen = state.drawerOpen,
                colors = colors,
                isDarkMode = isDarkMode,
                onThemeToggle = {
                    val nextPreference = viewModel.toggleTheme(isDarkMode)
                    onThemePreferenceChange(nextPreference)
                },
                onHome = viewModel::showHome,
                onSettings = {
                    viewModel.openSettings()
                    settingsViewModel.load()
                },
                onClose = viewModel::closeDrawer,
            )
        }
    }

    PlatformBackHandler(
        enabled = state.drawerOpen || state.showSettings || state.showResults || state.showCollections
    ) {
        viewModel.onBack()
    }
}

private fun searchCategory(
    category: WallpaperCategory,
    state: com.scapes.presentation.model.ScapesUiState,
    viewModel: ScapesViewModel,
    searchViewModel: SearchViewModel,
) {
    viewModel.selectCategory(category)
    val categorySlug =
        if (state.selectedSource.source == WallpaperSource.SCAPES_API) {
            category.slug
        } else {
            null
        }
    searchViewModel.search(category.name, state.selectedSource, categorySlug)
}
