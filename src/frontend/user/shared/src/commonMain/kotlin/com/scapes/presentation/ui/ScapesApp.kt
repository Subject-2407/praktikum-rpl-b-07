package com.scapes.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.scapes.data.remote.api.ExternalWallpaperApi
import com.scapes.data.repository.ExternalWallpaperRepository
import com.scapes.data.repository.SecureApiKeyRepository
import com.scapes.di.createHttpClient
import com.scapes.domain.model.ApiKey
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.ApiKeyRepository
import com.scapes.domain.repository.WallpaperRepository
import com.scapes.platform.EncryptedStorage
import com.scapes.presentation.model.ApiKeyFormState
import com.scapes.presentation.model.LandingFeedState
import com.scapes.presentation.model.LandingSectionState
import com.scapes.presentation.model.SettingsUiState
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperFeedState
import com.scapes.presentation.ui.components.ScapesDrawer
import com.scapes.presentation.ui.components.toUi
import com.scapes.presentation.ui.screens.HomeScreen
import com.scapes.presentation.ui.screens.SearchResultsScreen
import com.scapes.presentation.ui.screens.SettingsScreen
import com.scapes.presentation.ui.theme.ScapesTypography
import com.scapes.presentation.ui.theme.ThemePreference
import com.scapes.presentation.ui.theme.scapesThemeColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MaxSearchLength = 100
private const val MaxApiKeyLength = 256
private const val LandingSectionLimit = 6

private val settingsSources = listOf(
    SourceOption.pexels(),
    SourceOption.unsplash(),
    SourceOption.pixabay(),
)

private val homeSections = listOf(
    "Quiet Forests",
    "Amber Evenings",
    "Urban Lights",
    "Stone and Ruins",
    "Soft Horizons",
    "Road Motion",
    "Minimal Calm",
)

@Composable
fun ScapesApp(
    initialThemePreference: ThemePreference = ThemePreference.SYSTEM,
    onThemePreferenceChange: (ThemePreference) -> Unit = {},
    onResolvedThemeChange: (Boolean) -> Unit = {},
) {
    var drawerOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf(SourceOption.pexels()) }
    var showResults by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var themePreference by remember(initialThemePreference) { mutableStateOf(initialThemePreference) }
    var landingFeedState by remember { mutableStateOf(LandingFeedState.loading(selectedSource.source)) }
    var searchFeedState by remember { mutableStateOf(WallpaperFeedState()) }
    var settingsState by remember { mutableStateOf(SettingsUiState.fromSources()) }
    val coroutineScope = rememberCoroutineScope()
    val apiKeyRepository: ApiKeyRepository =
        remember {
            SecureApiKeyRepository(EncryptedStorage())
        }
    val kmpHttpClient = remember {
        createHttpClient()
    }
    val externalWallpaperApi = remember(kmpHttpClient) {
        ExternalWallpaperApi(
            httpClient = kmpHttpClient,
            apiKeyRepository = apiKeyRepository,
        )
    }
    val wallpaperRepository: WallpaperRepository = remember(externalWallpaperApi) {
        ExternalWallpaperRepository(externalWallpaperApi = externalWallpaperApi)
    }
    DisposableEffect(kmpHttpClient) {
        onDispose {
            try {
                kmpHttpClient.close()
            } catch (e: Exception) {
            }
        }
    }
    val systemIsDark = isSystemInDarkTheme()
    val isDarkMode =
        when (themePreference) {
            ThemePreference.SYSTEM -> systemIsDark
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
        }
    val colors = scapesThemeColors(isDarkMode)

    LaunchedEffect(isDarkMode) {
        onResolvedThemeChange(isDarkMode)
    }

    fun loadLandingWallpapers(sourceOption: SourceOption = selectedSource) {
        landingFeedState = LandingFeedState.loading(sourceOption.source)

        if (sourceOption.source == WallpaperSource.SCAPES_API) {
            landingFeedState =
                LandingFeedState.message(
                    source = sourceOption.source,
                    message = "Scapes API is coming soon. Pick Pexels, Unsplash, or Pixabay.",
                )
            return
        }

        homeSections.forEach { sectionTitle ->
            coroutineScope.launch {
                val result =
                    wallpaperRepository.searchWallpapers(
                        query = sectionTitle,
                        page = 0,
                        source = sourceOption.source,
                    )

                if (landingFeedState.source != sourceOption.source) {
                    return@launch
                }

                val sectionState =
                    when (result) {
                        is ScapesResult.Error ->
                            LandingSectionState(
                                title = sectionTitle,
                                isLoading = false,
                                message = result.message,
                            )

                        ScapesResult.Loading ->
                            LandingSectionState(
                                title = sectionTitle,
                                isLoading = true,
                            )

                        is ScapesResult.Success ->
                            LandingSectionState(
                                title = sectionTitle,
                                wallpapers =
                                    result.data
                                        .take(LandingSectionLimit)
                                        .mapIndexed { index, wallpaper -> wallpaper.toUi(index) },
                                isLoading = false,
                                message = if (result.data.isEmpty()) "No wallpapers found." else null,
                            )
                    }
                landingFeedState = landingFeedState.updateSection(sectionTitle, sectionState)
            }
        }
    }

    fun loadSettingsState() {
        settingsState = settingsState.copy(isLoading = true, message = null)
        coroutineScope.launch {
            var message: String? = null
            val loadedForms =
                settingsSources.map { sourceOption ->
                    when (val result = apiKeyRepository.getApiKey(sourceOption.source)) {
                        is ScapesResult.Error -> {
                            message = result.message
                            ApiKeyFormState(sourceOption = sourceOption)
                        }

                        ScapesResult.Loading ->
                            ApiKeyFormState(sourceOption = sourceOption, isLoading = true)

                        is ScapesResult.Success ->
                            ApiKeyFormState(
                                sourceOption = sourceOption,
                                maskedKey = result.data?.value?.let(::maskedApiKey),
                            )
                    }
                }
            settingsState =
                SettingsUiState(
                    forms = loadedForms,
                    isLoading = false,
                    message = message,
                )
        }
    }

    fun updateApiKeyInput(
        source: WallpaperSource,
        input: String,
    ) {
        settingsState =
            settingsState.updateForm(source) {
                copy(input = input.trim().take(MaxApiKeyLength), message = null)
            }
    }

    fun saveApiKey(source: WallpaperSource) {
        val rawKey = settingsState.form(source)?.input.orEmpty().trim()
        settingsState =
            settingsState.updateForm(source) {
                copy(isSaving = true, message = null)
            }

        coroutineScope.launch {
            when (val validation = externalWallpaperApi.validateApiKey(source, rawKey)) {
                is ScapesResult.Error ->
                    settingsState =
                        settingsState.updateForm(source) {
                            copy(isSaving = false, message = validation.message)
                        }

                ScapesResult.Loading ->
                    settingsState =
                        settingsState.updateForm(source) {
                            copy(isSaving = true)
                        }

                is ScapesResult.Success ->
                    when (
                        val saveResult =
                            apiKeyRepository.saveApiKey(
                                ApiKey(
                                    source = source,
                                    value = rawKey,
                                ),
                            )
                    ) {
                        is ScapesResult.Error ->
                            settingsState =
                                settingsState.updateForm(source) {
                                    copy(isSaving = false, message = saveResult.message)
                                }

                        ScapesResult.Loading ->
                            settingsState =
                                settingsState.updateForm(source) {
                                    copy(isSaving = true)
                                }

                        is ScapesResult.Success -> {
                            externalWallpaperApi.invalidateSource(source)
                            settingsState =
                                settingsState.updateForm(source) {
                                    copy(
                                        input = "",
                                        maskedKey = maskedApiKey(rawKey),
                                        isSaving = false,
                                        message = "Saved",
                                    )
                                }
                            if (selectedSource.source == source) {
                                loadLandingWallpapers(selectedSource)
                            }
                        }
                    }
            }
        }
    }

    fun removeApiKey(source: WallpaperSource) {
        settingsState =
            settingsState.updateForm(source) {
                copy(isRemoving = true, message = null)
            }

        coroutineScope.launch {
            when (val result = apiKeyRepository.removeApiKey(source)) {
                is ScapesResult.Error ->
                    settingsState =
                        settingsState.updateForm(source) {
                            copy(isRemoving = false, message = result.message)
                        }

                ScapesResult.Loading ->
                    settingsState =
                        settingsState.updateForm(source) {
                            copy(isRemoving = true)
                        }

                is ScapesResult.Success -> {
                    externalWallpaperApi.invalidateSource(source)
                    settingsState =
                        settingsState.updateForm(source) {
                            copy(
                                input = "",
                                maskedKey = null,
                                isRemoving = false,
                                message = "Removed",
                            )
                        }
                    if (selectedSource.source == source) {
                        loadLandingWallpapers(selectedSource)
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedSource.source) {
        loadLandingWallpapers(selectedSource)
    }

    PlatformBackHandler(enabled = drawerOpen || showSettings || showResults) {
        when {
            drawerOpen -> drawerOpen = false
            showSettings -> showSettings = false
            showResults -> showResults = false
        }
    }

    fun searchWallpapers(
        sourceOption: SourceOption = selectedSource,
        rawQuery: String = query,
    ) {
        val normalizedQuery = rawQuery.trim().ifBlank { "Ancient Ruin" }.take(MaxSearchLength)
        query = normalizedQuery
        showResults = true
        showSettings = false

        if (sourceOption.source == WallpaperSource.SCAPES_API) {
            searchFeedState =
                WallpaperFeedState(
                    query = normalizedQuery,
                    source = sourceOption.source,
                    message = "Scapes API is coming soon. Try Pexels, Unsplash, or Pixabay for now.",
                    endReached = true,
                )
            return
        }

        searchFeedState =
            WallpaperFeedState(
                query = normalizedQuery,
                source = sourceOption.source,
                isInitialLoading = true,
            )

        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val result = wallpaperRepository.searchWallpapers(
                query = normalizedQuery,
                page = 0,
                source = sourceOption.source,
            )

            withContext(kotlinx.coroutines.Dispatchers.Main) {
                when (result) {
                    is ScapesResult.Error ->
                        searchFeedState = searchFeedState.copy(
                            isInitialLoading = false,
                            message = result.message,
                            endReached = true,
                        )

                    ScapesResult.Loading ->
                        searchFeedState = searchFeedState.copy(isInitialLoading = true)

                    is ScapesResult.Success -> {
                        val wallpapers =
                            result.data.mapIndexed { index, wallpaper -> wallpaper.toUi(index) }
                        searchFeedState = searchFeedState.copy(
                            wallpapers = wallpapers,
                            isInitialLoading = false,
                            nextPage = 1,
                            endReached = result.data.isEmpty(),
                            message = if (wallpapers.isEmpty()) "No portrait wallpapers found." else null,
                        )
                    }
                }
            }
        }
    }

    fun loadMoreWallpapers() {
        val currentState = searchFeedState
        if (
            !showResults ||
            currentState.source == WallpaperSource.SCAPES_API ||
            currentState.isInitialLoading ||
            currentState.isLoadingMore ||
            currentState.endReached ||
            currentState.query.isBlank()
        ) {
            return
        }

        searchFeedState = currentState.copy(isLoadingMore = true, message = null)

        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val result = wallpaperRepository.searchWallpapers(
                query = currentState.query,
                page = currentState.nextPage,
                source = currentState.source,
            )

            withContext(kotlinx.coroutines.Dispatchers.Main) {
                when (result) {
                    is ScapesResult.Error ->
                        searchFeedState = searchFeedState.copy(
                            isLoadingMore = false,
                            message = result.message,
                        )

                    ScapesResult.Loading ->
                        searchFeedState = searchFeedState.copy(isLoadingMore = true)

                    is ScapesResult.Success -> {
                        val existingCount = currentState.wallpapers.size
                        val moreWallpapers = result.data.mapIndexed { index, wallpaper ->
                            wallpaper.toUi(existingCount + index)
                        }
                        searchFeedState = searchFeedState.copy(
                            wallpapers = currentState.wallpapers + moreWallpapers,
                            isLoadingMore = false,
                            nextPage = currentState.nextPage + 1,
                            endReached = result.data.isEmpty(),
                        )
                    }
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
                showSettings ->
                    SettingsScreen(
                        state = settingsState,
                        colors = colors,
                        onInputChange = ::updateApiKeyInput,
                        onSave = ::saveApiKey,
                        onRemove = ::removeApiKey,
                        onBack = { showSettings = false },
                    )

                showResults ->
                    SearchResultsScreen(
                        query = query,
                        selectedSource = selectedSource,
                        feedState = searchFeedState,
                        colors = colors,
                        onQueryChange = { query = it.take(MaxSearchLength) },
                        onSourceSelected = {
                            selectedSource = it
                            searchWallpapers(it)
                        },
                        onSearch = { searchWallpapers() },
                        onLoadMore = { loadMoreWallpapers() },
                        onBack = {
                            showResults = false
                        },
                    )

                else ->
                    HomeScreen(
                        query = query,
                        selectedSource = selectedSource,
                        landingFeedState = landingFeedState,
                        colors = colors,
                        isDarkMode = isDarkMode,
                        onQueryChange = { query = it.take(MaxSearchLength) },
                        onSourceSelected = { selectedSource = it },
                        onOpenMenu = { drawerOpen = true },
                        onSearch = { searchWallpapers() },
                        onQuickSearch = {
                            searchWallpapers(rawQuery = it)
                        },
                    )
            }

            ScapesDrawer(
                isOpen = drawerOpen,
                colors = colors,
                isDarkMode = isDarkMode,
                onThemeToggle = {
                    themePreference =
                        if (isDarkMode) {
                            ThemePreference.LIGHT
                        } else {
                            ThemePreference.DARK
                        }
                    onThemePreferenceChange(themePreference)
                },
                onHome = {
                    drawerOpen = false
                    showResults = false
                    showSettings = false
                },
                onSettings = {
                    drawerOpen = false
                    showSettings = true
                    loadSettingsState()
                },
                onClose = { drawerOpen = false },
            )
        }
    }
}

private fun maskedApiKey(rawKey: String): String {
    val key = rawKey.trim()
    return if (key.length > 4) {
        "***${key.takeLast(4)}"
    } else {
        "****"
    }
}
