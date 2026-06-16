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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.scapes.presentation.ui.components.ScapesDrawer
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
import org.koin.compose.koinInject

@Composable
fun ScapesApp(
    initialThemePreference: ThemePreference = ThemePreference.SYSTEM,
    onThemePreferenceChange: (ThemePreference) -> Unit = {},
    onResolvedThemeChange: (Boolean) -> Unit = {},
    viewModel: ScapesViewModel = koinInject(),
    homeViewModel: HomeViewModel = koinInject(),
    searchViewModel: SearchViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject(),
) {
    val state by viewModel.uiState.collectAsState()
    val landingFeedState by homeViewModel.feedState.collectAsState()
    val searchFeedState by searchViewModel.feedState.collectAsState()
    val wallpaperActionStates by searchViewModel.actionStates.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val systemIsDark = isSystemInDarkTheme()
    val isDarkMode =
        when (state.themePreference) {
            ThemePreference.SYSTEM -> systemIsDark
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
        }
    val colors = scapesThemeColors(isDarkMode)

    LaunchedEffect(initialThemePreference) { viewModel.setThemePreference(initialThemePreference) }

    LaunchedEffect(isDarkMode) { onResolvedThemeChange(isDarkMode) }

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.apiKeyChanges.collect { changedSource ->
            val currentState = viewModel.uiState.value
            if (changedSource == currentState.selectedSource.source) {
                homeViewModel.load(currentState.selectedSource)
                val currentFeed = searchViewModel.feedState.value
                if (currentState.showResults && currentFeed.query.isNotBlank()) {
                    searchViewModel.search(currentFeed.query, currentState.selectedSource)
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
                        colors = colors,
                        onQueryChange = viewModel::onQueryChange,
                        onSourceSelected = { source ->
                            viewModel.selectSource(source)
                            homeViewModel.load(source)
                            val query = viewModel.showResults()
                            searchViewModel.search(query, source)
                        },
                        onSearch = {
                            val query = viewModel.showResults()
                            searchViewModel.search(query, viewModel.uiState.value.selectedSource)
                        },
                        onLoadMore = searchViewModel::loadMore,
                        onSaveWallpaper = searchViewModel::saveWallpaper,
                        onApplyWallpaper = searchViewModel::applyWallpaper,
                        onBack = viewModel::onBack,
                    )

                else ->
                    HomeScreen(
                        query = state.query,
                        selectedSource = state.selectedSource,
                        landingFeedState = landingFeedState,
                        colors = colors,
                        isDarkMode = isDarkMode,
                        onQueryChange = viewModel::onQueryChange,
                        onSourceSelected = { source ->
                            viewModel.selectSource(source)
                            homeViewModel.load(source)
                        },
                        onOpenMenu = viewModel::openMenu,
                        onSearch = {
                            val query = viewModel.showResults()
                            searchViewModel.search(query, viewModel.uiState.value.selectedSource)
                        },
                        onQuickSearch = { quickQuery ->
                            val query = viewModel.showResults(quickQuery)
                            searchViewModel.search(query, viewModel.uiState.value.selectedSource)
                        },
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

    PlatformBackHandler(enabled = state.drawerOpen || state.showSettings || state.showResults) {
        viewModel.onBack()
    }
}
