package com.scapes.presentation.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.scapes.domain.model.WallpaperSource
import com.scapes.platform.AndroidDownloadRegistry
import com.scapes.platform.DirectoryPicker
import com.scapes.platform.WallpaperApplier
import com.scapes.presentation.ui.components.IconShell
import com.scapes.presentation.ui.components.WallpaperDetailDialog
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.components.DownloadGlyph
import com.scapes.presentation.ui.components.MenuGlyph
import com.scapes.presentation.ui.components.ResolutionGlyph
import com.scapes.presentation.ui.components.SearchGlyph
import com.scapes.presentation.ui.screens.AndroidCollectionsScreen
import com.scapes.presentation.ui.screens.AndroidHomeScreen
import com.scapes.presentation.ui.screens.AndroidSearchResultsScreen
import com.scapes.presentation.ui.screens.AndroidSettingsScreen
import com.scapes.presentation.ui.screens.FullscreenWallpaperPreview
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.ui.theme.ScapesTypography
import com.scapes.presentation.ui.theme.ThemePreference
import com.scapes.presentation.ui.theme.scapesThemeColors
import com.scapes.presentation.viewmodel.HomeViewModel
import com.scapes.presentation.viewmodel.ScapesViewModel
import com.scapes.presentation.viewmodel.SearchViewModel
import com.scapes.presentation.viewmodel.SettingsViewModel
import com.scapes.shared.generated.resources.scapes_dark
import com.scapes.shared.generated.resources.scapes_light
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

enum class AndroidNavigationTab {
    HOME, COLLECTIONS, SETTINGS
}

@Composable
fun AndroidScapesApp(
    modifier: Modifier = Modifier,
    initialThemePreference: ThemePreference = ThemePreference.SYSTEM,
    windowControls: @Composable (() -> Unit)? = null,
    onThemePreferenceChange: (ThemePreference) -> Unit = {},
    onResolvedThemeChange: (Boolean) -> Unit = {},
    viewModel: ScapesViewModel = koinInject(),
    homeViewModel: HomeViewModel = koinInject(),
    searchViewModel: SearchViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject(),
    directoryPicker: DirectoryPicker = koinInject(),
    wallpaperApplier: WallpaperApplier = koinInject(),
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
    var fullscreenWallpaper by remember { mutableStateOf<WallpaperUi?>(null) }

    var currentAndroidTab by remember { mutableStateOf(AndroidNavigationTab.HOME) }
    var forceShowSearch by remember { mutableStateOf(false) }

    val isDarkMode = when (state.themePreference) {
        ThemePreference.SYSTEM -> systemIsDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    val colors = scapesThemeColors(isDarkMode)
    val context = LocalContext.current

    val enabledSources = setOf(
        WallpaperSource.SCAPES_API,
        WallpaperSource.PEXELS,
        WallpaperSource.UNSPLASH,
        WallpaperSource.PIXABAY
    )

    LaunchedEffect(initialThemePreference) { viewModel.setThemePreference(initialThemePreference) }
    LaunchedEffect(isDarkMode) { onResolvedThemeChange(isDarkMode) }
    LaunchedEffect(settingsViewModel) { settingsViewModel.load() }
    LaunchedEffect(Unit) { viewModel.showHome() }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    scope.launch {
                        kotlinx.coroutines.delay(500)
                        searchViewModel.loadCollections()
                    }
                }
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    BackHandler(enabled = fullscreenWallpaper != null) {
        fullscreenWallpaper = null
    }

    MaterialTheme(
        colorScheme = if (isDarkMode) {
            darkColorScheme(
                primary = colors.text, secondary = colors.amber, background = colors.base,
                surface = colors.surface, onPrimary = colors.base, onSurface = colors.text, onBackground = colors.text,
            )
        } else {
            lightColorScheme(
                primary = colors.text, secondary = colors.amber, background = colors.base,
                surface = colors.surface, onPrimary = Color.White, onSurface = colors.text, onBackground = colors.text,
            )
        },
        typography = ScapesTypography,
    ) {
        val onToggleThemeLambda = {
            val nextPreference = viewModel.toggleTheme(isDarkMode)
            onThemePreferenceChange(nextPreference)
        }

        val showSearchIcon = currentAndroidTab == AndroidNavigationTab.HOME && !state.showResults && !forceShowSearch

        if (state.showResults || forceShowSearch) {
            AndroidSearchResultsScreen(
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
                modifier = modifier,
                windowControls = null,
                onQueryChange = viewModel::onQueryChange,
                onToggleTheme = onToggleThemeLambda,
                onRecommendationSelected = { rec ->
                    viewModel.applyRecommendation(rec.queryValue)
                    val q = viewModel.showResults(rec.queryValue)
                    searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                },
                onDismissRecommendations = viewModel::dismissRecommendations,
                enabledSources = enabledSources,
                onSourceSelected = { src ->
                    viewModel.selectSource(src)
                    homeViewModel.load(src)
                },
                onFeedSelected = {
                    forceShowSearch = false
                    viewModel.showHome()
                    homeViewModel.load(viewModel.uiState.value.selectedSource)
                },
                onCategorySelected = { cat ->
                    viewModel.selectCategory(cat)
                    searchViewModel.search(cat.name, viewModel.uiState.value.selectedSource)
                },
                onCollectionsSelected = {
                    forceShowSearch = false
                    currentAndroidTab = AndroidNavigationTab.COLLECTIONS
                    searchViewModel.loadCollections()
                    viewModel.onBack()
                },
                onSearch = {
                    val q = viewModel.showResults()
                    if (q.isNotBlank()) searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                },
                onLoadMore = searchViewModel::loadMore,
                onOpenWallpaper = { },
                onSaveWallpaper = { wallpaper ->
                    AndroidDownloadRegistry.registerUrl(wallpaper.wallpaper.id, wallpaper.wallpaper.fullImageUrl)
                    searchViewModel.saveWallpaper(wallpaper)
                },
                onApplyWallpaper = { wallpaper -> fullscreenWallpaper = wallpaper },
                onBack = {
                    forceShowSearch = false
                    viewModel.onBack()
                },
            )
        } else {
            AndroidScreenWrapper(
                colors = colors,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleThemeLambda,
                showSearchIcon = showSearchIcon,
                onSearchClick = {
                    viewModel.onQueryChange("")
                    viewModel.showResults("")
                    forceShowSearch = true
                },
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(colors.surface)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val homeActive = currentAndroidTab == AndroidNavigationTab.HOME
                        Column(
                            modifier = Modifier.clickable { currentAndroidTab = AndroidNavigationTab.HOME },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ResolutionGlyph(color = if (homeActive) colors.amber else colors.secondaryText)
                            Text("Explore", fontSize = 11.sp, color = if (homeActive) colors.text else colors.secondaryText)
                        }

                        val collectionsActive = currentAndroidTab == AndroidNavigationTab.COLLECTIONS
                        Column(
                            modifier = Modifier.clickable {
                                currentAndroidTab = AndroidNavigationTab.COLLECTIONS
                                searchViewModel.loadCollections()
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            DownloadGlyph(color = if (collectionsActive) colors.amber else colors.secondaryText)
                            Text("Collections", fontSize = 11.sp, color = if (collectionsActive) colors.text else colors.secondaryText)
                        }

                        val settingsActive = currentAndroidTab == AndroidNavigationTab.SETTINGS
                        Column(
                            modifier = Modifier.clickable {
                                currentAndroidTab = AndroidNavigationTab.SETTINGS
                                settingsViewModel.load()
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            MenuGlyph(color = if (settingsActive) colors.amber else colors.secondaryText)
                            Text("Settings", fontSize = 11.sp, color = if (settingsActive) colors.text else colors.secondaryText)
                        }
                    }
                }
            ) {
                when (currentAndroidTab) {
                    AndroidNavigationTab.SETTINGS -> {
                        AndroidSettingsScreen(
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
                            onDownloadOrganizationChange = settingsViewModel::updateDownloadOrganization,
                            onSaveDownloadSettings = settingsViewModel::saveDownloadSettings,
                            onBack = {},
                        )
                    }

                    AndroidNavigationTab.COLLECTIONS -> {
                        AndroidCollectionsScreen(
                            query = state.query,
                            selectedSource = state.selectedSource,
                            feedState = collectionsFeedState,
                            actionStates = wallpaperActionStates,
                            searchRecommendations = state.searchRecommendations,
                            isLoadingRecommendations = state.isLoadingRecommendations,
                            categories = state.categories,
                            colors = colors,
                            isDarkMode = isDarkMode,
                            modifier = modifier,
                            windowControls = null,
                            onQueryChange = viewModel::onQueryChange,
                            onToggleTheme = onToggleThemeLambda,
                            onRecommendationSelected = { rec ->
                                viewModel.applyRecommendation(rec.queryValue)
                                val q = viewModel.showResults(rec.queryValue)
                                searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onDismissRecommendations = viewModel::dismissRecommendations,
                            onSourceSelected = viewModel::selectSource,
                            enabledSources = enabledSources,
                            onFeedSelected = {
                                currentAndroidTab = AndroidNavigationTab.HOME
                                homeViewModel.load(viewModel.uiState.value.selectedSource)
                            },
                            onCategorySelected = { cat ->
                                currentAndroidTab = AndroidNavigationTab.HOME
                                viewModel.selectCategory(cat)
                                searchViewModel.search(cat.name, viewModel.uiState.value.selectedSource)
                            },
                            onCollectionsSelected = {},
                            onSearch = {
                                val q = viewModel.showResults()
                                if (q.isNotBlank()) searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onOpenWallpaper = { },
                            onSaveWallpaper = { wallpaper ->
                                AndroidDownloadRegistry.registerUrl(wallpaper.wallpaper.id, wallpaper.wallpaper.fullImageUrl)
                                searchViewModel.saveWallpaper(wallpaper)
                            },
                            onApplyWallpaper = { wallpaper -> fullscreenWallpaper = wallpaper },
                        )
                    }

                    AndroidNavigationTab.HOME -> {
                        AndroidHomeScreen(
                            query = state.query,
                            selectedSource = state.selectedSource,
                            landingFeedState = landingFeedState,
                            actionStates = wallpaperActionStates,
                            categories = state.categories,
                            activeCategorySlug = state.activeCategorySlug,
                            colors = colors,
                            isDarkMode = isDarkMode,
                            modifier = modifier,
                            onQueryChange = viewModel::onQueryChange,
                            onToggleTheme = onToggleThemeLambda,
                            searchRecommendations = state.searchRecommendations,
                            isLoadingRecommendations = state.isLoadingRecommendations,
                            onRecommendationSelected = { rec ->
                                viewModel.applyRecommendation(rec.queryValue)
                                val q = viewModel.showResults(rec.queryValue)
                                searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onDismissRecommendations = viewModel::dismissRecommendations,
                            enabledSources = enabledSources,
                            onSourceSelected = { src ->
                                viewModel.selectSource(src)
                                homeViewModel.load(src)
                            },
                            onFeedSelected = { homeViewModel.load(viewModel.uiState.value.selectedSource) },
                            onCategorySelected = { cat ->
                                viewModel.selectCategory(cat)
                                searchViewModel.search(cat.name, viewModel.uiState.value.selectedSource)
                            },
                            onCollectionsSelected = {
                                currentAndroidTab = AndroidNavigationTab.COLLECTIONS
                                searchViewModel.loadCollections()
                            },
                            onOpenMenu = {},
                            onSearch = {
                                val q = viewModel.showResults()
                                if (q.isNotBlank()) searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onQuickSearch = { qq ->
                                val q = viewModel.showResults(qq)
                                searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onOpenWallpaper = { },
                            onSaveWallpaper = { wallpaper ->
                                AndroidDownloadRegistry.registerUrl(wallpaper.wallpaper.id, wallpaper.wallpaper.fullImageUrl)
                                searchViewModel.saveWallpaper(wallpaper)
                            },
                            onApplyWallpaper = { wallpaper -> fullscreenWallpaper = wallpaper },
                        )
                    }
                }
            }
        }

        selectedWallpaper?.let { wallpaper ->
            WallpaperDetailDialog(
                wallpaper = wallpaper,
                actionState = wallpaperActionStates[wallpaper.wallpaper.id],
                colors = colors,
                onDismiss = { selectedWallpaper = null },
                onSave = {
                    AndroidDownloadRegistry.registerUrl(wallpaper.wallpaper.id, wallpaper.wallpaper.fullImageUrl)
                    searchViewModel.saveWallpaper(wallpaper)
                },
                onApply = { fullscreenWallpaper = wallpaper },
            )
        }

        fullscreenWallpaper?.let { wallpaper ->
            FullscreenWallpaperPreview(
                wallpaper = wallpaper,
                colors = colors,
                onBack = { fullscreenWallpaper = null },
                onSave = {
                    AndroidDownloadRegistry.registerUrl(wallpaper.wallpaper.id, wallpaper.wallpaper.fullImageUrl)
                    searchViewModel.saveWallpaper(wallpaper)
                },
                onApply = { target, offset, scale ->
                    scope.launch {
                        // register URL because apply flow also triggers a save action
                        AndroidDownloadRegistry.registerUrl(wallpaper.wallpaper.id, wallpaper.wallpaper.fullImageUrl)

                        launch { wallpaperApplier.applyWithPosition(wallpaper, target, offset, scale) }
                        launch { searchViewModel.saveWallpaper(wallpaper) }

                        fullscreenWallpaper = null
                    }
                }
            )
        }
    }
}

@Composable
private fun AndroidScreenWrapper(
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    showSearchIcon: Boolean,
    onSearchClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize().background(colors.base)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.base.copy(alpha = 0.96f))
                .padding(WindowInsets.statusBars.asPaddingValues())
                .height(56.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconShell(onClick = onToggleTheme, colors = colors) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = "Switch Theme",
                    tint = colors.text,
                    modifier = Modifier.size(18.dp),
                )
            }

            androidx.compose.foundation.Image(
                painter = painterResource(
                    if (isDarkMode) com.scapes.shared.generated.resources.Res.drawable.scapes_dark
                    else com.scapes.shared.generated.resources.Res.drawable.scapes_light
                ),
                contentDescription = "Scapes",
                modifier = Modifier.height(28.dp),
            )

            if (showSearchIcon) {
                IconShell(onClick = onSearchClick, colors = colors) {
                    SearchGlyph(colors.text)
                }
            } else {
                Box(modifier = Modifier.size(40.dp))
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        bottomBar()
    }
}
