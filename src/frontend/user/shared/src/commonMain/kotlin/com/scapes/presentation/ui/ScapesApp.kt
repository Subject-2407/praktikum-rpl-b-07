package com.scapes.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.platform.DirectoryPicker
import com.scapes.presentation.ui.components.WallpaperDetailDialog
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.screens.CollectionsScreen
import com.scapes.presentation.ui.screens.HomeScreen
import com.scapes.presentation.ui.screens.SearchResultsScreen
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
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var snackbarIsError by remember { mutableStateOf(false) }
    val previousMessages = remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
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

    LaunchedEffect(wallpaperActionStates) {
        val currentMessages = wallpaperActionStates.mapValues { it.value.message }
        val newMessages = currentMessages.filter { (id, msg) -> msg != null && msg != previousMessages.value[id] }
        previousMessages.value = currentMessages
        
        val newEntry = newMessages.entries.firstOrNull()
        newEntry?.let { (id, msg) ->
            snackbarIsError = wallpaperActionStates[id]?.isError == true
            snackbarHostState.showSnackbar(msg!!)
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
        // Shared settings callbacks for the inline dropdown panel
        val settingsChooseDownloadFolder: () -> Unit = {
            scope.launch {
                directoryPicker
                    .chooseDirectory(settingsState.downloadFolderInput)
                    ?.let(settingsViewModel::updateDownloadFolderInput)
            }
        }

        Box(Modifier.fillMaxSize().background(colors.base)) {
            when {
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
                        settingsState = settingsState,
                        onSettingsInputChange = settingsViewModel::updateApiKeyInput,
                        onSettingsSave = settingsViewModel::saveApiKey,
                        onSettingsRemove = settingsViewModel::removeApiKey,
                        onSettingsDownloadFolderChange = settingsViewModel::updateDownloadFolderInput,
                        onSettingsChooseDownloadFolder = settingsChooseDownloadFolder,
                        onSettingsDownloadOrganizationChange = settingsViewModel::updateDownloadOrganization,
                        onSettingsSaveDownloadSettings = settingsViewModel::saveDownloadSettings,
                        onSettingsLoad = settingsViewModel::load,
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
                        onSaveWallpaper = searchViewModel::deleteWallpaper,
                        onApplyWallpaper = searchViewModel::applyWallpaper,
                        settingsState = settingsState,
                        onSettingsInputChange = settingsViewModel::updateApiKeyInput,
                        onSettingsSave = settingsViewModel::saveApiKey,
                        onSettingsRemove = settingsViewModel::removeApiKey,
                        onSettingsDownloadFolderChange = settingsViewModel::updateDownloadFolderInput,
                        onSettingsChooseDownloadFolder = settingsChooseDownloadFolder,
                        onSettingsDownloadOrganizationChange = settingsViewModel::updateDownloadOrganization,
                        onSettingsSaveDownloadSettings = settingsViewModel::saveDownloadSettings,
                        onSettingsLoad = settingsViewModel::load,
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
                        onLogoClick = {
                            viewModel.showHome()
                            homeViewModel.load(viewModel.uiState.value.selectedSource)
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
                        settingsState = settingsState,
                        onSettingsInputChange = settingsViewModel::updateApiKeyInput,
                        onSettingsSave = settingsViewModel::saveApiKey,
                        onSettingsRemove = settingsViewModel::removeApiKey,
                        onSettingsDownloadFolderChange = settingsViewModel::updateDownloadFolderInput,
                        onSettingsChooseDownloadFolder = settingsChooseDownloadFolder,
                        onSettingsDownloadOrganizationChange = settingsViewModel::updateDownloadOrganization,
                        onSettingsSaveDownloadSettings = settingsViewModel::saveDownloadSettings,
                        onSettingsLoad = settingsViewModel::load,
                    )
            }

            selectedWallpaper?.let { wallpaper ->
                WallpaperDetailDialog(
                    wallpaper = wallpaper,
                    actionState = wallpaperActionStates[wallpaper.wallpaper.id],
                    colors = colors,
                    onDismiss = { selectedWallpaper = null },
                    onSave = {
                        if (state.showCollections) {
                            searchViewModel.deleteWallpaper(wallpaper)
                            selectedWallpaper = null
                        } else {
                            searchViewModel.saveWallpaper(wallpaper)
                        }
                    },
                    onApply = { searchViewModel.applyWallpaper(wallpaper) },
                    isAnyApplying = wallpaperActionStates.values.any { it.isApplying },
                    isSaved = state.showCollections,
                    onTagClick = { tag ->
                        selectedWallpaper = null
                        val query = viewModel.showResults(tag)
                        searchViewModel.search(query, state.selectedSource)
                    }
                )
            }

            androidx.compose.material3.SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp),
                snackbar = { data ->
                    val bgColor = if (snackbarIsError) Color(0xFFE53935) else colors.accent
                    val contentColor = if (snackbarIsError || !isDarkMode) Color.White else Color(0xFF222222)
                    
                    androidx.compose.material3.Surface(
                        color = bgColor,
                        contentColor = contentColor,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            if (snackbarIsError) {
                                // Simple X glyph for error
                                androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
                                    val stroke = 2.dp.toPx()
                                    drawLine(contentColor, androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.3f), androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.7f), strokeWidth = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    drawLine(contentColor, androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.3f), androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.7f), strokeWidth = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                }
                            } else {
                                com.scapes.presentation.ui.components.CheckGlyph(color = contentColor)
                            }
                            androidx.compose.material3.Text(
                                text = data.visuals.message,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = contentColor
                            )
                        }
                    }
                }
            )
        }
    }

    PlatformBackHandler(
        enabled = state.showResults || state.showCollections
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
