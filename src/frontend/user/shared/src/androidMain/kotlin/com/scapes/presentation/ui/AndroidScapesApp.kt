package com.scapes.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
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
import com.scapes.data.local.DownloadedWallpaperStore
import com.scapes.domain.model.WallpaperSource
import com.scapes.platform.DirectoryPicker
import com.scapes.platform.FileSystemProvider
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

private enum class AndroidDestination {
    EXPLORE, SEARCH, COLLECTIONS, SETTINGS
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
    downloadedWallpaperStore: DownloadedWallpaperStore = koinInject(),
    fileSystemProvider: FileSystemProvider = koinInject(),
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
    var backStack by remember { mutableStateOf<List<AndroidDestination>>(emptyList()) }
    var lastExploreBackMillis by remember { mutableStateOf(0L) }

    val isDarkMode = when (state.themePreference) {
        ThemePreference.SYSTEM -> systemIsDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    val colors = scapesThemeColors(isDarkMode)
    val context = LocalContext.current

    fun currentDestination(): AndroidDestination =
        when {
            state.showResults || forceShowSearch -> AndroidDestination.SEARCH
            currentAndroidTab == AndroidNavigationTab.COLLECTIONS -> AndroidDestination.COLLECTIONS
            currentAndroidTab == AndroidNavigationTab.SETTINGS -> AndroidDestination.SETTINGS
            else -> AndroidDestination.EXPLORE
        }

    fun pushCurrentFor(destination: AndroidDestination) {
        val current = currentDestination()
        if (current != destination) {
            backStack = backStack + current
        }
        lastExploreBackMillis = 0L
    }

    fun showDestination(destination: AndroidDestination) {
        forceShowSearch = false
        when (destination) {
            AndroidDestination.EXPLORE -> {
                currentAndroidTab = AndroidNavigationTab.HOME
                viewModel.showHome()
                homeViewModel.load(viewModel.uiState.value.selectedSource)
            }
            AndroidDestination.SEARCH -> {
                currentAndroidTab = AndroidNavigationTab.HOME
                forceShowSearch = true
            }
            AndroidDestination.COLLECTIONS -> {
                currentAndroidTab = AndroidNavigationTab.COLLECTIONS
                viewModel.showHome()
                searchViewModel.loadCollections()
            }
            AndroidDestination.SETTINGS -> {
                currentAndroidTab = AndroidNavigationTab.SETTINGS
                viewModel.showHome()
                settingsViewModel.load()
            }
        }
    }

    fun navigateTo(destination: AndroidDestination) {
        pushCurrentFor(destination)
        showDestination(destination)
    }

    fun handleBack() {
        when {
            fullscreenWallpaper != null -> fullscreenWallpaper = null
            selectedWallpaper != null -> selectedWallpaper = null
            backStack.isNotEmpty() -> {
                val destination = backStack.last()
                backStack = backStack.dropLast(1)
                showDestination(destination)
            }
            currentDestination() != AndroidDestination.EXPLORE -> showDestination(AndroidDestination.EXPLORE)
            System.currentTimeMillis() - lastExploreBackMillis < 2_000L ->
                context.findActivity()?.finish()
            else -> {
                lastExploreBackMillis = System.currentTimeMillis()
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val enabledSources = setOf(
        WallpaperSource.SCAPES_API,
        WallpaperSource.PEXELS,
        WallpaperSource.UNSPLASH,
        WallpaperSource.PIXABAY
    )

    LaunchedEffect(Unit) {
        if (state.themePreference == ThemePreference.SYSTEM) {
            viewModel.setThemePreference(initialThemePreference)
        }
    }
    LaunchedEffect(isDarkMode) { onResolvedThemeChange(isDarkMode) }
    LaunchedEffect(settingsViewModel) { settingsViewModel.load() }
    LaunchedEffect(Unit) { viewModel.showHome() }

    var previousActionStates = remember { wallpaperActionStates }

    // Toast handler for download, save, and apply progress/results
    LaunchedEffect(wallpaperActionStates) {
        wallpaperActionStates.forEach { (id, state) ->
            val prevState = previousActionStates[id]
            val message = state.message
            if (message != null && message.isNotBlank() && message != prevState?.message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
        previousActionStates = wallpaperActionStates
    }

    BackHandler { handleBack() }

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
        val saveWallpaperWithToast: (WallpaperUi) -> Unit = { wallpaper ->
            Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
            searchViewModel.saveWallpaper(wallpaper)
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
                    pushCurrentFor(AndroidDestination.SEARCH)
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
                    navigateTo(AndroidDestination.EXPLORE)
                },
                onCategorySelected = { cat ->
                    viewModel.selectCategory(cat)
                    searchViewModel.search(cat.name, viewModel.uiState.value.selectedSource)
                },
                onCollectionsSelected = {
                    navigateTo(AndroidDestination.COLLECTIONS)
                },
                onSearch = {
                    pushCurrentFor(AndroidDestination.SEARCH)
                    val q = viewModel.showResults()
                    if (q.isNotBlank()) searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                },
                onLoadMore = searchViewModel::loadMore,
                onOpenWallpaper = { wallpaper -> fullscreenWallpaper = wallpaper },
                onSaveWallpaper = saveWallpaperWithToast,
                onApplyWallpaper = { },
                onBack = {
                    handleBack()
                },
            )
        } else {
            AndroidScreenWrapper(
                colors = colors,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleThemeLambda,
                showSearchIcon = showSearchIcon,
                onSearchClick = {
                    pushCurrentFor(AndroidDestination.SEARCH)
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
                            modifier = Modifier.clickable { navigateTo(AndroidDestination.EXPLORE) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ResolutionGlyph(color = if (homeActive) colors.amber else colors.secondaryText)
                            Text("Explore", fontSize = 11.sp, color = if (homeActive) colors.text else colors.secondaryText)
                        }

                        val collectionsActive = currentAndroidTab == AndroidNavigationTab.COLLECTIONS
                        Column(
                            modifier = Modifier.clickable {
                                navigateTo(AndroidDestination.COLLECTIONS)
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
                                navigateTo(AndroidDestination.SETTINGS)
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
                                pushCurrentFor(AndroidDestination.SEARCH)
                                viewModel.applyRecommendation(rec.queryValue)
                                val q = viewModel.showResults(rec.queryValue)
                                searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onDismissRecommendations = viewModel::dismissRecommendations,
                            onSourceSelected = viewModel::selectSource,
                            enabledSources = enabledSources,
                            onFeedSelected = {
                                navigateTo(AndroidDestination.EXPLORE)
                            },
                            onCategorySelected = { cat ->
                                pushCurrentFor(AndroidDestination.SEARCH)
                                viewModel.selectCategory(cat)
                                searchViewModel.search(cat.name, viewModel.uiState.value.selectedSource)
                            },
                            onCollectionsSelected = {},
                            onSearch = {
                                pushCurrentFor(AndroidDestination.SEARCH)
                                val q = viewModel.showResults()
                                if (q.isNotBlank()) searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onOpenWallpaper = { wallpaper -> fullscreenWallpaper = wallpaper },
                            onSaveWallpaper = saveWallpaperWithToast,
                            onApplyWallpaper = { },
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
                                pushCurrentFor(AndroidDestination.SEARCH)
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
                                pushCurrentFor(AndroidDestination.SEARCH)
                                viewModel.selectCategory(cat)
                                searchViewModel.search(cat.name, viewModel.uiState.value.selectedSource)
                            },
                            onCollectionsSelected = {
                                navigateTo(AndroidDestination.COLLECTIONS)
                            },
                            onOpenMenu = {},
                            onSearch = {
                                pushCurrentFor(AndroidDestination.SEARCH)
                                val q = viewModel.showResults()
                                if (q.isNotBlank()) searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onQuickSearch = { qq ->
                                pushCurrentFor(AndroidDestination.SEARCH)
                                val q = viewModel.showResults(qq)
                                searchViewModel.search(q, viewModel.uiState.value.selectedSource)
                            },
                            onOpenWallpaper = { wallpaper -> fullscreenWallpaper = wallpaper },
                            onSaveWallpaper = saveWallpaperWithToast,
                            onApplyWallpaper = { },
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
                onSave = { saveWallpaperWithToast(wallpaper) },
                onApply = {
                    fullscreenWallpaper = wallpaper
                    selectedWallpaper = null
                },
                onTagClick = { tag ->
                    selectedWallpaper = null
                    pushCurrentFor(AndroidDestination.SEARCH)
                    val q = viewModel.showResults(tag)
                    searchViewModel.search(q, state.selectedSource)
                }
            )
        }

        fullscreenWallpaper?.let { wallpaper ->
            FullscreenWallpaperPreview(
                wallpaper = wallpaper,
                colors = colors,
                onBack = { fullscreenWallpaper = null },
                onSave = {
                    if (currentAndroidTab == AndroidNavigationTab.COLLECTIONS) {
                        downloadedWallpaperStore.deleteById(wallpaper.wallpaper.id)
                        wallpaper.wallpaper.localPath?.let { fileSystemProvider.deleteFile(it) }
                        searchViewModel.loadCollections()
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                        fullscreenWallpaper = null
                    } else {
                        saveWallpaperWithToast(wallpaper)
                    }
                },
                onApply = { target, offset, scale ->
                    scope.launch {
                        Toast.makeText(context, "Applying wallpaper...", Toast.LENGTH_SHORT).show()
                        
                        try {
                            wallpaperApplier.applyWithPosition(wallpaper, target, offset, scale)
                            Toast.makeText(context, "Wallpaper applied!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to apply wallpaper", Toast.LENGTH_SHORT).show()
                        }
                        
                        searchViewModel.saveWallpaper(wallpaper)

                        fullscreenWallpaper = null
                        selectedWallpaper = null
                        homeViewModel.load(viewModel.uiState.value.selectedSource)
                    }
                },
                saveLabel = if (currentAndroidTab == AndroidNavigationTab.COLLECTIONS) "Delete" else "Save",
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

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
