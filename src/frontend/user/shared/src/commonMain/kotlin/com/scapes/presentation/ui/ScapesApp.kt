package com.scapes.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.scapes.data.remote.api.ExternalWallpaperApi
import com.scapes.data.repository.ExternalWallpaperRepository
import com.scapes.data.repository.SecureApiKeyRepository
import com.scapes.di.createHttpClient
import com.scapes.domain.model.ApiKey
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.ApiKeyRepository
import com.scapes.domain.repository.WallpaperRepository
import com.scapes.platform.EncryptedStorage
import com.scapes.presentation.ui.theme.ScapesThemeColors
import com.scapes.presentation.ui.theme.ScapesTypography
import com.scapes.presentation.ui.theme.ThemePreference
import com.scapes.presentation.ui.theme.scapesThemeColors
import com.scapes.shared.generated.resources.Res
import com.scapes.shared.generated.resources.scapes_dark
import com.scapes.shared.generated.resources.scapes_light
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private val Scrim = Color.Black.copy(alpha = 0.32f)

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
    val externalWallpaperApi =
        remember {
            ExternalWallpaperApi(
                httpClient = createHttpClient(),
                apiKeyRepository = apiKeyRepository,
            )
        }
    val wallpaperRepository: WallpaperRepository =
        remember(externalWallpaperApi) {
            ExternalWallpaperRepository(
                externalWallpaperApi = externalWallpaperApi,
            )
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

        coroutineScope.launch {
            when (
                val result =
                    wallpaperRepository.searchWallpapers(
                        query = normalizedQuery,
                        page = 0,
                        source = sourceOption.source,
                    )
            ) {
                is ScapesResult.Error ->
                    searchFeedState =
                        searchFeedState.copy(
                            isInitialLoading = false,
                            message = result.message,
                            endReached = true,
                        )

                ScapesResult.Loading ->
                    searchFeedState = searchFeedState.copy(isInitialLoading = true)

                is ScapesResult.Success -> {
                    val wallpapers = result.data.mapIndexed { index, wallpaper -> wallpaper.toUi(index) }
                    searchFeedState =
                        searchFeedState.copy(
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

        coroutineScope.launch {
            when (
                val result =
                    wallpaperRepository.searchWallpapers(
                        query = currentState.query,
                        page = currentState.nextPage,
                        source = currentState.source,
                    )
            ) {
                is ScapesResult.Error ->
                    searchFeedState =
                        searchFeedState.copy(
                            isLoadingMore = false,
                            message = result.message,
                        )

                ScapesResult.Loading ->
                    searchFeedState = searchFeedState.copy(isLoadingMore = true)

                is ScapesResult.Success -> {
                    val existingCount = currentState.wallpapers.size
                    val moreWallpapers =
                        result.data.mapIndexed { index, wallpaper ->
                            wallpaper.toUi(existingCount + index)
                        }
                    searchFeedState =
                        searchFeedState.copy(
                            wallpapers = currentState.wallpapers + moreWallpapers,
                            isLoadingMore = false,
                            nextPage = currentState.nextPage + 1,
                            endReached = result.data.isEmpty(),
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

@Composable
private fun HomeScreen(
    query: String,
    selectedSource: SourceOption,
    landingFeedState: LandingFeedState,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onQueryChange: (String) -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
    onQuickSearch: (String) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.base,
                            colors.support.copy(alpha = if (isDarkMode) 0.2f else 0.12f),
                            colors.base,
                        ),
                    ),
                ),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            HomeAppBar(
                colors = colors,
                isDarkMode = isDarkMode,
                onOpenMenu = onOpenMenu,
                onSearch = onSearch,
            )
        }
        item {
            SearchStudio(
                query = query,
                selectedSource = selectedSource,
                colors = colors,
                onQueryChange = onQueryChange,
                onSourceSelected = onSourceSelected,
                onSearch = onSearch,
            )
        }
        item {
            CategoryCarouselFeed(
                feedState = landingFeedState,
                colors = colors,
                onQuickSearch = onQuickSearch,
            )
        }
    }
}

@Composable
private fun SearchResultsScreen(
    query: String,
    selectedSource: SourceOption,
    feedState: WallpaperFeedState,
    colors: ScapesThemeColors,
    onQueryChange: (String) -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.base, colors.elevated.copy(alpha = 0.86f), colors.base),
                ),
            ),
    ) {
        SearchResultBar(
            query = query,
            colors = colors,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onBack = onBack,
        )
        SearchStudioSourceDropdown(
            selectedSource = selectedSource,
            colors = colors,
            onSourceSelected = onSourceSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        ResultHeader(query = query, selectedSource = selectedSource, colors = colors)
        MasonryWallpaperGrid(
            wallpapers = feedState.wallpapers,
            isInitialLoading = feedState.isInitialLoading,
            isLoadingMore = feedState.isLoadingMore,
            endReached = feedState.endReached,
            message = feedState.message,
            colors = colors,
            onLoadMore = onLoadMore,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HomeAppBar(
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .background(colors.base.copy(alpha = 0.96f))
                .padding(WindowInsets.statusBars.asPaddingValues())
                .height(60.dp)
                .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconShell(onClick = onOpenMenu, colors = colors) { MenuGlyph(colors.text) }
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(if (isDarkMode) Res.drawable.scapes_dark else Res.drawable.scapes_light),
            contentDescription = "Scapes",
            modifier = Modifier.height(36.dp),
        )
        Spacer(Modifier.weight(1f))
        IconShell(onClick = onSearch, colors = colors) { SearchGlyph(colors.text) }
    }
}

@Composable
private fun SearchResultBar(
    query: String,
    colors: ScapesThemeColors,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .background(colors.base.copy(alpha = 0.96f))
                .padding(WindowInsets.statusBars.asPaddingValues())
                .height(60.dp)
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconShell(onClick = onBack, colors = colors) { BackGlyph(colors.text) }
        SearchInput(
            query = query,
            onQueryChange = onQueryChange,
            colors = colors,
            onSearch = onSearch,
            modifier = Modifier.weight(1f),
        )
        IconShell(onClick = onSearch, colors = colors) { SearchGlyph(colors.text) }
    }
}

@Composable
private fun SearchStudio(
    query: String,
    selectedSource: SourceOption,
    colors: ScapesThemeColors,
    onQueryChange: (String) -> Unit,
    onSourceSelected: (SourceOption) -> Unit,
    onSearch: () -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.surface,
                            colors.support.copy(alpha = 0.22f),
                            colors.amber.copy(alpha = 0.18f),
                        ),
                    ),
                )
                .border(1.dp, colors.support.copy(alpha = 0.34f), RoundedCornerShape(8.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Set the mood before your phone wakes.",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
        )
        SearchInput(
            query = query,
            onQueryChange = onQueryChange,
            colors = colors,
            onSearch = onSearch,
            modifier = Modifier.fillMaxWidth(),
        )
        SearchStudioSourceDropdown(
            selectedSource = selectedSource,
            colors = colors,
            onSourceSelected = onSourceSelected,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Search wallpaper",
                color = colors.base,
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(colors.text)
                        .clickable(onClick = onSearch)
                        .padding(horizontal = 16.dp, vertical = 11.dp),
            )
        }
    }
}

@Composable
private fun SearchStudioSourceDropdown(
    selectedSource: SourceOption,
    colors: ScapesThemeColors,
    onSourceSelected: (SourceOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.base.copy(alpha = 0.72f))
                    .border(1.dp, colors.support.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier =
                    Modifier.size(9.dp)
                        .clip(CircleShape)
                        .background(sourceColor(selectedSource.source, colors)),
            )
            Text(
                text = selectedSource.label,
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ChevronDownGlyph(colors.secondaryText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier =
                Modifier.background(colors.surface),
        ) {
            SourceOption.defaults().forEach { source ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(9.dp)
                                        .clip(CircleShape)
                                        .background(sourceColor(source.source, colors)),
                            )
                            Text(source.label, color = colors.text)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSourceSelected(source)
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    colors: ScapesThemeColors,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle =
            MaterialTheme.typography.bodyMedium.copy(
                color = colors.text,
                fontSize = 16.sp,
            ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.base.copy(alpha = 0.72f))
                        .border(1.dp, colors.support.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isBlank()) {
                    Text("Search mobile wallpapers", color = colors.secondaryText)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun CategoryCarouselFeed(
    feedState: LandingFeedState,
    colors: ScapesThemeColors,
    onQuickSearch: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        feedState.sections.forEach { section ->
            CategoryCarouselSection(
                section = section,
                colors = colors,
                onQuickSearch = onQuickSearch,
            )
        }
    }
}

@Composable
private fun CategoryCarouselSection(
    section: LandingSectionState,
    colors: ScapesThemeColors,
    onQuickSearch: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.text,
        )
        if (section.wallpapers.isEmpty()) {
            SearchStatusPanel(
                message =
                    when {
                        section.isLoading -> "Fetching ${section.title}..."
                        section.message != null -> section.message
                        else -> "No wallpapers found."
                    },
                colors = colors,
                loading = section.isLoading,
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                section.wallpapers.forEachIndexed { index, wallpaper ->
                    CarouselWallpaperCard(
                        wallpaper = wallpaper,
                        colors = colors,
                        height = if (index % 3 == 1) 228.dp else 194.dp,
                        onClick = { onQuickSearch(wallpaper.title) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarouselWallpaperCard(
    wallpaper: WallpaperUi,
    colors: ScapesThemeColors,
    height: Dp,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.width(156.dp).height(height).combinedClickable(onClick = onClick, onLongClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            WallpaperVisual(wallpaper = wallpaper, colors = colors, modifier = Modifier.fillMaxSize())
            Text(
                text = wallpaper.title,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                        .padding(8.dp),
            )
        }
    }
}

@Composable
private fun ResultHeader(
    query: String,
    selectedSource: SourceOption,
    colors: ScapesThemeColors,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = if (query.isBlank()) "Fresh mobile picks" else query,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${selectedSource.label} source, portrait results",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
        )
    }
}

@Composable
private fun MasonryWallpaperGrid(
    wallpapers: List<WallpaperUi>,
    isInitialLoading: Boolean,
    isLoadingMore: Boolean,
    endReached: Boolean,
    message: String?,
    colors: ScapesThemeColors,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val left = wallpapers.filterIndexed { index, _ -> index % 2 == 0 }
    val right = wallpapers.filterIndexed { index, _ -> index % 2 == 1 }
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 22.dp),
    ) {
        if (wallpapers.isEmpty()) {
            item {
                SearchStatusPanel(
                    message =
                        when {
                            isInitialLoading -> "Fetching portrait wallpapers..."
                            message != null -> message
                            else -> "No portrait wallpapers found."
                        },
                    colors = colors,
                    loading = isInitialLoading,
                )
            }
        } else {
            item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    left.forEach { wallpaper -> MasonryWallpaperCard(wallpaper, colors) }
                }
                Column(
                    modifier = Modifier.weight(1f).padding(top = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    right.forEach { wallpaper -> MasonryWallpaperCard(wallpaper, colors) }
                }
            }
        }
        }

        if (wallpapers.isNotEmpty() && isLoadingMore) {
            item {
                SearchStatusPanel(
                    message = "Loading more portrait wallpapers...",
                    colors = colors,
                    loading = true,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        if (wallpapers.isNotEmpty() && message != null && !isLoadingMore) {
            item {
                SearchStatusPanel(
                    message = message,
                    colors = colors,
                    loading = false,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        if (wallpapers.isNotEmpty() && !isInitialLoading && !isLoadingMore && !endReached) {
            item {
                LaunchedEffect(wallpapers.size) {
                    onLoadMore()
                }
                Spacer(Modifier.height(1.dp))
            }
        }
    }
}

@Composable
private fun SearchStatusPanel(
    message: String,
    colors: ScapesThemeColors,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.84f))
                .border(1.dp, colors.support.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (loading) {
            LoadingGlyph(colors.text)
        }
        Text(
            text = message,
            color = colors.text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MasonryWallpaperCard(
    wallpaper: WallpaperUi,
    colors: ScapesThemeColors,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by
        animateFloatAsState(
            targetValue = if (pressed) 0.98f else 1f,
            animationSpec = tween(durationMillis = 150, easing = EaseOutCubic),
        )

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(90)
            pressed = false
        }
    }

    Card(
        modifier =
            Modifier.fillMaxWidth()
                .height(wallpaper.height)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .combinedClickable(onClick = { pressed = true }, onLongClick = { pressed = true }),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            WallpaperVisual(wallpaper = wallpaper, colors = colors, modifier = Modifier.fillMaxSize())
            Box(
                modifier =
                    Modifier.align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.base.copy(alpha = 0.72f))
                        .border(1.dp, colors.support.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text(
                    wallpaper.resolution,
                    color = colors.text,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Column(
                modifier =
                    Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.56f)),
                            ),
                        )
                        .padding(10.dp),
            ) {
                Text(
                    wallpaper.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    wallpaper.author,
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    colors: ScapesThemeColors,
    onInputChange: (WallpaperSource, String) -> Unit,
    onSave: (WallpaperSource) -> Unit,
    onRemove: (WallpaperSource) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(colors.base, colors.elevated.copy(alpha = 0.84f), colors.base),
                    ),
                ),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(colors.base.copy(alpha = 0.96f))
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .height(60.dp)
                        .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconShell(onClick = onBack, colors = colors) { BackGlyph(colors.text) }
                Text("Settings", color = colors.text, style = MaterialTheme.typography.titleLarge)
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "API Keys",
                    color = colors.text,
                    style = MaterialTheme.typography.headlineMedium,
                )
                state.message?.let { message ->
                    SearchStatusPanel(
                        message = message,
                        colors = colors,
                        loading = state.isLoading,
                    )
                }
                state.forms.forEach { form ->
                    ApiKeyCard(
                        form = form,
                        colors = colors,
                        onInputChange = onInputChange,
                        onSave = onSave,
                        onRemove = onRemove,
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeyCard(
    form: ApiKeyFormState,
    colors: ScapesThemeColors,
    onInputChange: (WallpaperSource, String) -> Unit,
    onSave: (WallpaperSource) -> Unit,
    onRemove: (WallpaperSource) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .border(1.dp, colors.support.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier =
                        Modifier.size(9.dp)
                            .clip(CircleShape)
                            .background(colors.text),
                )
                Text(
                    form.sourceOption.label,
                    color = colors.text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    form.maskedKey ?: "Not set",
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            ApiKeyInput(
                value = form.input,
                onValueChange = { onInputChange(form.sourceOption.source, it) },
                colors = colors,
                enabled = !form.isSaving && !form.isRemoving,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsActionButton(
                    label = if (form.isSaving) "Saving" else "Save",
                    colors = colors,
                    enabled = form.input.isNotBlank() && !form.isSaving && !form.isRemoving,
                    onClick = { onSave(form.sourceOption.source) },
                )
                SettingsActionButton(
                    label = if (form.isRemoving) "Removing" else "Remove",
                    colors = colors,
                    enabled = form.maskedKey != null && !form.isSaving && !form.isRemoving,
                    outlined = true,
                    onClick = { onRemove(form.sourceOption.source) },
                )
            }

            form.message?.let { message ->
                Text(
                    message,
                    color = colors.secondaryText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun ApiKeyInput(
    value: String,
    onValueChange: (String) -> Unit,
    colors: ScapesThemeColors,
    enabled: Boolean,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        textStyle =
            MaterialTheme.typography.bodyMedium.copy(
                color = colors.text,
                fontSize = 16.sp,
            ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.base.copy(alpha = 0.72f))
                        .border(1.dp, colors.support.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isBlank()) {
                    Text("Enter API key", color = colors.secondaryText)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun SettingsActionButton(
    label: String,
    colors: ScapesThemeColors,
    enabled: Boolean,
    outlined: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val background =
        when {
            outlined -> Color.Transparent
            enabled -> colors.text
            else -> colors.secondaryText.copy(alpha = 0.28f)
        }
    val foreground =
        when {
            outlined -> colors.text
            enabled -> colors.base
            else -> colors.secondaryText
        }
    val clickableModifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier =
            Modifier.height(40.dp)
                .clip(shape)
                .background(background)
                .border(1.dp, colors.text.copy(alpha = if (enabled) 0.75f else 0.24f), shape)
                .then(clickableModifier)
                .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = foreground,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ScapesDrawer(
    isOpen: Boolean,
    colors: ScapesThemeColors,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit,
) {
    if (isOpen) {
        Box(
            modifier = Modifier.fillMaxSize().background(Scrim).clickable { onClose() },
        )
    }
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(animationSpec = tween(340, easing = EaseOutCubic)) { -it } + fadeIn(),
        exit = slideOutHorizontally(animationSpec = tween(240, easing = EaseInCubic)) { -it } + fadeOut(),
    ) {
        Surface(
            modifier = Modifier.width(296.dp).fillMaxHeight(),
            color = colors.elevated,
            shadowElevation = 12.dp,
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(WindowInsets.statusBars.asPaddingValues())
                            .height(58.dp)
                            .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    IconShell(onClick = onClose, colors = colors) { CloseGlyph(colors.text) }
                    Text("Menu", style = MaterialTheme.typography.titleMedium, color = colors.text)
                    Spacer(Modifier.weight(1f))
                    IconShell(onClick = onThemeToggle, colors = colors) {
                        if (isDarkMode) {
                            SunGlyph(colors.text)
                        } else {
                            MoonGlyph(colors.text, colors.elevated)
                        }
                    }
                }
                DrawerItem("Home", colors, onHome)
                DrawerItem("Settings", colors, onSettings)
            }
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    colors: ScapesThemeColors,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(55.dp)
                .border(width = 0.5.dp, color = colors.support.copy(alpha = 0.32f))
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(label, color = colors.text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun WallpaperVisual(
    wallpaper: WallpaperUi,
    colors: ScapesThemeColors,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        WallpaperArt(wallpaper = wallpaper, colors = colors, modifier = Modifier.fillMaxSize())
        if (wallpaper.imageUrl != null) {
            AsyncImage(
                model = wallpaper.imageUrl,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun WallpaperArt(
    wallpaper: WallpaperUi,
    colors: ScapesThemeColors,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.background(wallpaper.colors.first())) {
        drawRect(brush = Brush.verticalGradient(wallpaper.colors), size = size)
        drawPath(
            path =
                Path().apply {
                    moveTo(size.width * 0.54f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.44f)
                    close()
                },
            color = colors.amber.copy(alpha = 0.18f),
        )
        val sunRadius = size.minDimension * 0.16f
        drawCircle(
            color = colors.amber.copy(alpha = 0.28f),
            radius = sunRadius,
            center = Offset(size.width * 0.72f, size.height * 0.18f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.14f),
            radius = sunRadius * 1.7f,
            center = Offset(size.width * 0.72f, size.height * 0.18f),
            style = Stroke(width = 3.dp.toPx()),
        )
        val rear =
            Path().apply {
                moveTo(0f, size.height * wallpaper.rearHorizon)
                lineTo(size.width * 0.28f, size.height * (wallpaper.rearHorizon - 0.18f))
                lineTo(size.width * 0.58f, size.height * (wallpaper.rearHorizon - 0.04f))
                lineTo(size.width, size.height * (wallpaper.rearHorizon - 0.22f))
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
        drawPath(rear, Color.White.copy(alpha = 0.16f))
        val front =
            Path().apply {
                moveTo(0f, size.height * wallpaper.frontHorizon)
                lineTo(size.width * 0.2f, size.height * (wallpaper.frontHorizon - 0.12f))
                lineTo(size.width * 0.47f, size.height * (wallpaper.frontHorizon - 0.02f))
                lineTo(size.width * 0.75f, size.height * (wallpaper.frontHorizon - 0.19f))
                lineTo(size.width, size.height * (wallpaper.frontHorizon - 0.08f))
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
        drawPath(front, colors.text.copy(alpha = 0.22f))
        drawLine(
            color = Color.White.copy(alpha = 0.22f),
            start = Offset(size.width * 0.12f, size.height * 0.08f),
            end = Offset(size.width * 0.48f, size.height * 0.08f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun IconShell(
    onClick: () -> Unit,
    colors: ScapesThemeColors,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier.size(40.dp)
                .clip(CircleShape)
                .background(colors.surface.copy(alpha = 0.34f))
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun MenuGlyph(color: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        val stroke = 2.dp.toPx()
        drawLine(color, Offset(3.dp.toPx(), 6.dp.toPx()), Offset(size.width - 3.dp.toPx(), 6.dp.toPx()), stroke, StrokeCap.Round)
        drawLine(color, Offset(3.dp.toPx(), size.height / 2), Offset(size.width - 3.dp.toPx(), size.height / 2), stroke, StrokeCap.Round)
        drawLine(color, Offset(3.dp.toPx(), size.height - 6.dp.toPx()), Offset(size.width - 8.dp.toPx(), size.height - 6.dp.toPx()), stroke, StrokeCap.Round)
    }
}

@Composable
private fun SearchGlyph(color: Color, modifier: Modifier = Modifier.size(23.dp)) {
    Canvas(modifier) {
        drawCircle(
            color = color,
            radius = size.minDimension * 0.28f,
            center = Offset(size.width * 0.42f, size.height * 0.4f),
            style = Stroke(width = 2.2.dp.toPx()),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.62f, size.height * 0.62f),
            end = Offset(size.width * 0.86f, size.height * 0.86f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun LoadingGlyph(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier) {
        drawArc(
            color = color.copy(alpha = 0.3f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun ChevronDownGlyph(color: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier) {
        val stroke = 2.dp.toPx()
        drawLine(
            color,
            Offset(size.width * 0.24f, size.height * 0.38f),
            Offset(size.width * 0.5f, size.height * 0.64f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(size.width * 0.5f, size.height * 0.64f),
            Offset(size.width * 0.76f, size.height * 0.38f),
            stroke,
            StrokeCap.Round,
        )
    }
}

@Composable
private fun BackGlyph(color: Color, modifier: Modifier = Modifier.size(23.dp)) {
    Canvas(modifier) {
        val stroke = 2.3.dp.toPx()
        drawLine(color, Offset(size.width * 0.72f, size.height * 0.16f), Offset(size.width * 0.28f, size.height / 2), stroke, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.28f, size.height / 2), Offset(size.width * 0.72f, size.height * 0.84f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun CloseGlyph(color: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        drawLine(color, Offset(4.dp.toPx(), 4.dp.toPx()), Offset(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()), 2.3.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(size.width - 4.dp.toPx(), 4.dp.toPx()), Offset(4.dp.toPx(), size.height - 4.dp.toPx()), 2.3.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun MoonGlyph(
    color: Color,
    cutoutColor: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier) {
        drawCircle(color.copy(alpha = 0.18f), radius = size.minDimension * 0.48f, center = Offset(size.width / 2, size.height / 2))
        drawCircle(color, radius = size.minDimension * 0.32f, center = Offset(size.width * 0.48f, size.height * 0.45f))
        drawCircle(cutoutColor, radius = size.minDimension * 0.28f, center = Offset(size.width * 0.6f, size.height * 0.32f))
    }
}

@Composable
private fun SunGlyph(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier) {
        drawCircle(color, radius = size.minDimension * 0.22f, center = Offset(size.width / 2, size.height / 2))
        val stroke = 2.dp.toPx()
        listOf(
            Offset(size.width / 2, 1.dp.toPx()) to Offset(size.width / 2, 6.dp.toPx()),
            Offset(size.width / 2, size.height - 1.dp.toPx()) to Offset(size.width / 2, size.height - 6.dp.toPx()),
            Offset(1.dp.toPx(), size.height / 2) to Offset(6.dp.toPx(), size.height / 2),
            Offset(size.width - 1.dp.toPx(), size.height / 2) to Offset(size.width - 6.dp.toPx(), size.height / 2),
        ).forEach { (start, end) ->
            drawLine(color, start, end, stroke, StrokeCap.Round)
        }
    }
}

private data class SourceOption(
    val source: WallpaperSource,
    val label: String,
) {
    companion object {
        fun scapes() = SourceOption(WallpaperSource.SCAPES_API, "Scapes")

        fun pexels() = SourceOption(WallpaperSource.PEXELS, "Pexels")

        fun unsplash() = SourceOption(WallpaperSource.UNSPLASH, "Unsplash")

        fun pixabay() = SourceOption(WallpaperSource.PIXABAY, "Pixabay")

        fun defaults(): List<SourceOption> =
            listOf(
                scapes(),
                pexels(),
                unsplash(),
                pixabay(),
            )
    }
}

private fun sourceColor(
    source: WallpaperSource,
    colors: ScapesThemeColors,
): Color =
    when (source) {
        WallpaperSource.SCAPES_API -> colors.text
        WallpaperSource.PEXELS -> colors.text
        WallpaperSource.UNSPLASH -> colors.text
        WallpaperSource.PIXABAY -> colors.text
    }

private data class WallpaperUi(
    val title: String,
    val author: String,
    val height: Dp,
    val resolution: String,
    val colors: List<Color>,
    val rearHorizon: Float,
    val frontHorizon: Float,
    val imageUrl: String? = null,
)

private const val MaxSearchLength = 100
private const val MaxApiKeyLength = 256
private const val LandingSectionLimit = 6

private data class WallpaperFeedState(
    val query: String = "",
    val source: WallpaperSource = WallpaperSource.PEXELS,
    val wallpapers: List<WallpaperUi> = emptyList(),
    val nextPage: Int = 0,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = true,
    val message: String? = null,
)

private data class LandingSectionState(
    val title: String,
    val wallpapers: List<WallpaperUi> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
)

private data class LandingFeedState(
    val source: WallpaperSource,
    val sections: List<LandingSectionState>,
) {
    fun updateSection(
        title: String,
        sectionState: LandingSectionState,
    ): LandingFeedState =
        copy(
            sections =
                sections.map { section ->
                    if (section.title == title) {
                        sectionState
                    } else {
                        section
                    }
                },
        )

    companion object {
        fun loading(source: WallpaperSource): LandingFeedState =
            LandingFeedState(
                source = source,
                sections = homeSections.map { LandingSectionState(title = it, isLoading = true) },
            )

        fun message(
            source: WallpaperSource,
            message: String,
        ): LandingFeedState =
            LandingFeedState(
                source = source,
                sections = homeSections.map { LandingSectionState(title = it, message = message) },
            )
    }
}

private data class ApiKeyFormState(
    val sourceOption: SourceOption,
    val input: String = "",
    val maskedKey: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isRemoving: Boolean = false,
    val message: String? = null,
)

private data class SettingsUiState(
    val forms: List<ApiKeyFormState>,
    val isLoading: Boolean = false,
    val message: String? = null,
) {
    fun form(source: WallpaperSource): ApiKeyFormState? =
        forms.firstOrNull { it.sourceOption.source == source }

    fun updateForm(
        source: WallpaperSource,
        transform: ApiKeyFormState.() -> ApiKeyFormState,
    ): SettingsUiState =
        copy(
            forms =
                forms.map { form ->
                    if (form.sourceOption.source == source) {
                        form.transform()
                    } else {
                        form
                    }
                },
        )

    companion object {
        fun fromSources(): SettingsUiState =
            SettingsUiState(forms = settingsSources.map { ApiKeyFormState(sourceOption = it) })
    }
}

private fun Wallpaper.toUi(index: Int): WallpaperUi {
    val palette = source.toWallpaperPalette(index)
    val displayTitle = title.replaceFirstChar { char -> char.uppercase() }

    return WallpaperUi(
        title = displayTitle,
        author = authorName ?: source.name.lowercase().replaceFirstChar { char -> char.uppercase() },
        height = listOf(248.dp, 292.dp, 226.dp, 318.dp, 270.dp)[index % 5],
        resolution =
            if (width > 0 && height > 0) {
                "${width}x${height}"
            } else {
                "Portrait"
            },
        colors = palette,
        rearHorizon = 0.54f + (index % 4) * 0.04f,
        frontHorizon = 0.74f + (index % 3) * 0.03f,
        imageUrl = previewUrl,
    )
}

private fun WallpaperSource.toWallpaperPalette(index: Int): List<Color> =
    when (this) {
        WallpaperSource.PEXELS ->
            listOf(Color(0xFF0D6271), Color(0xFFF9C52E), Color(0xFF137586))

        WallpaperSource.UNSPLASH ->
            listOf(Color(0xFF70C3C6), Color(0xFFF8F8EF), Color(0xFF0F0F0F))

        WallpaperSource.PIXABAY ->
            listOf(Color(0xFF1A6D75), Color(0xFF70C3C6), Color(0xFF0D6271))

        WallpaperSource.SCAPES_API ->
            listOf(Color(0xFF0F0F0F), Color(0xFFF8F8EF), Color(0xFF202828))
    }

private fun maskedApiKey(rawKey: String): String {
    val key = rawKey.trim()
    return if (key.length > 4) {
        "***${key.takeLast(4)}"
    } else {
        "****"
    }
}

private val settingsSources =
    listOf(
        SourceOption.pexels(),
        SourceOption.unsplash(),
        SourceOption.pixabay(),
    )

private val homeSections =
    listOf(
        "Quiet Forests",
        "Amber Evenings",
        "Urban Lights",
        "Stone and Ruins",
        "Soft Horizons",
        "Road Motion",
        "Minimal Calm",
    )
