package com.scapes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scapes.domain.usecase.SwitchWallpaperSourceUseCase
import com.scapes.presentation.model.ScapesDestination
import com.scapes.presentation.model.ScapesUiState
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.ui.theme.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MaxSearchLength = 100
private const val DefaultSearchQuery = "Ancient Ruin"

/** Owns app-level shell state: navigation, source selection, query input, and theme. */
class ScapesViewModel(private val switchWallpaperSourceUseCase: SwitchWallpaperSourceUseCase) :
    ViewModel() {
    private val mutableUiState = MutableStateFlow(ScapesUiState())
    val uiState: StateFlow<ScapesUiState> = mutableUiState.asStateFlow()

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
        mutableUiState.update { state -> state.copy(query = query.take(MaxSearchLength)) }
    }

    fun openMenu() {
        mutableUiState.update { state -> state.copy(drawerOpen = true) }
    }

    fun closeDrawer() {
        mutableUiState.update { state -> state.copy(drawerOpen = false) }
    }

    fun showHome() {
        mutableUiState.update { it.copy(drawerOpen = false, destination = ScapesDestination.HOME) }
    }

    fun openSettings() {
        mutableUiState.update {
            it.copy(drawerOpen = false, destination = ScapesDestination.SETTINGS)
        }
    }

    fun onBack() {
        mutableUiState.update { state ->
            when {
                state.drawerOpen -> state.copy(drawerOpen = false)
                state.showSettings || state.showResults ->
                    state.copy(destination = ScapesDestination.HOME)
                else -> state
            }
        }
    }

    fun selectSource(sourceOption: SourceOption) {
        mutableUiState.update { state -> state.copy(selectedSource = sourceOption) }
        viewModelScope.launch { switchWallpaperSourceUseCase(sourceOption.source) }
    }

    fun showResults(rawQuery: String = uiState.value.query): String {
        val normalizedQuery = normalizedQuery(rawQuery)
        mutableUiState.update { state ->
            state.copy(query = normalizedQuery, destination = ScapesDestination.SEARCH_RESULTS)
        }
        return normalizedQuery
    }

    private fun normalizedQuery(rawQuery: String): String =
        rawQuery.trim().ifBlank { DefaultSearchQuery }.take(MaxSearchLength)
}
