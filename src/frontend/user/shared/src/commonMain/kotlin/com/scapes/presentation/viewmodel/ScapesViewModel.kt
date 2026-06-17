package com.scapes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.usecase.GetCategoriesUseCase
import com.scapes.domain.usecase.GetSearchRecommendationsUseCase
import com.scapes.domain.usecase.SwitchWallpaperSourceUseCase
import com.scapes.presentation.model.ScapesDestination
import com.scapes.presentation.model.ScapesUiState
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.ui.theme.ThemePreference
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MaxSearchLength = 100
private const val RecommendationDebounceMillis = 700L

/** Owns app-level shell state: navigation, source selection, query input, and theme. */
class ScapesViewModel(
    private val switchWallpaperSourceUseCase: SwitchWallpaperSourceUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSearchRecommendationsUseCase: GetSearchRecommendationsUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ScapesUiState())
    val uiState: StateFlow<ScapesUiState> = mutableUiState.asStateFlow()
    private var recommendationJob: Job? = null

    init {
        loadCategories()
    }

    fun setThemePreference(themePreference: ThemePreference) {
        mutableUiState.update { state -> state.copy(themePreference = themePreference) }
    }

    fun toggleTheme(isDarkMode: Boolean): ThemePreference {
        val nextPreference =
            if (isDarkMode) {
                ThemePreference.LIGHT
            } else {
                ThemePreference.DARK
            }
        setThemePreference(nextPreference)
        return nextPreference
    }

    fun onQueryChange(query: String) {
        val boundedQuery = query.take(MaxSearchLength)
        mutableUiState.update { state -> state.copy(query = boundedQuery) }
        refreshRecommendations(boundedQuery, mutableUiState.value.selectedSource.source)
    }

    fun openMenu() {
        mutableUiState.update { state -> state.copy(drawerOpen = true) }
    }

    fun closeDrawer() {
        mutableUiState.update { state -> state.copy(drawerOpen = false) }
    }

    fun showHome() {
        dismissRecommendations()
        mutableUiState.update {
            it.copy(
                drawerOpen = false,
                destination = ScapesDestination.HOME,
                activeCategorySlug = null,
            )
        }
    }

    fun openSettings() {
        dismissRecommendations()
        mutableUiState.update {
            it.copy(drawerOpen = false, destination = ScapesDestination.SETTINGS)
        }
    }

    fun showCollections() {
        dismissRecommendations()
        mutableUiState.update {
            it.copy(
                drawerOpen = false,
                destination = ScapesDestination.COLLECTIONS,
                activeCategorySlug = null,
            )
        }
    }

    fun onBack() {
        dismissRecommendations()
        mutableUiState.update { state ->
            when {
                state.drawerOpen -> state.copy(drawerOpen = false)
                state.showSettings || state.showResults || state.showCollections ->
                    state.copy(destination = ScapesDestination.HOME)
                else -> state
            }
        }
    }

    fun selectSource(sourceOption: SourceOption) {
        dismissRecommendations()
        mutableUiState.update { state -> state.copy(selectedSource = sourceOption) }
        viewModelScope.launch { switchWallpaperSourceUseCase(sourceOption.source) }
    }

    fun showResults(rawQuery: String = uiState.value.query, categorySlug: String? = null): String {
        val normalizedQuery = normalizedQuery(rawQuery)
        dismissRecommendations()
        if (normalizedQuery.isBlank()) {
            mutableUiState.update { state -> state.copy(query = "") }
            return ""
        }

        mutableUiState.update { state ->
            state.copy(
                query = normalizedQuery,
                destination = ScapesDestination.SEARCH_RESULTS,
                activeCategorySlug = categorySlug,
            )
        }
        return normalizedQuery
    }

    fun selectCategory(category: WallpaperCategory) {
        showResults(rawQuery = category.name, categorySlug = category.slug)
    }

    fun applyRecommendation(query: String) {
        val normalizedQuery = query.take(MaxSearchLength)
        dismissRecommendations()
        mutableUiState.update { state -> state.copy(query = normalizedQuery) }
    }

    fun dismissRecommendations() {
        recommendationJob?.cancel()
        mutableUiState.update {
            it.copy(searchRecommendations = emptyList(), isLoadingRecommendations = false)
        }
    }

    private fun normalizedQuery(rawQuery: String): String =
        rawQuery.trim().take(MaxSearchLength)

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is ScapesResult.Success ->
                    mutableUiState.update { state -> state.copy(categories = result.data) }

                else -> Unit
            }
        }
    }

    private fun refreshRecommendations(query: String, source: WallpaperSource) {
        recommendationJob?.cancel()

        if (!shouldRequestRecommendations(query)) {
            mutableUiState.update {
                it.copy(searchRecommendations = emptyList(), isLoadingRecommendations = false)
            }
            return
        }

        mutableUiState.update {
            it.copy(searchRecommendations = emptyList(), isLoadingRecommendations = false)
        }

        recommendationJob =
            viewModelScope.launch {
                delay(RecommendationDebounceMillis)

                when (
                    val result =
                        getSearchRecommendationsUseCase(
                            query = query,
                            source = source,
                        )
                ) {
                    is ScapesResult.Success ->
                        mutableUiState.update {
                            if (
                                it.query == query &&
                                    it.selectedSource.source == source &&
                                    it.searchRecommendations.isEmpty()
                            ) {
                                it.copy(
                                    searchRecommendations = result.data,
                                    isLoadingRecommendations = false,
                                )
                            } else {
                                it
                            }
                        }

                    else ->
                        mutableUiState.update {
                            it.copy(
                                searchRecommendations = emptyList(),
                                isLoadingRecommendations = false,
                            )
                        }
                }
            }
    }

    private fun shouldRequestRecommendations(query: String): Boolean {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return false
        }

        if (trimmedQuery.startsWith("#")) {
            return trimmedQuery.removePrefix("#").isNotBlank()
        }

        return trimmedQuery.length >= 2
    }
}
