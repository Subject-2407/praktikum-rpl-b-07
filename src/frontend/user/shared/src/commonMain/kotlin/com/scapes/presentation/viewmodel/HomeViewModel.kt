package com.scapes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.usecase.SearchWallpapersUseCase
import com.scapes.presentation.model.DefaultLandingSectionTitles
import com.scapes.presentation.model.LandingFeedState
import com.scapes.presentation.model.LandingSectionState
import com.scapes.presentation.model.ScapesAppConfig
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.ui.components.toUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val LandingSectionLimit = 6

/** Owns discovery feed state for the home screen. */
class HomeViewModel(
    private val searchWallpapersUseCase: SearchWallpapersUseCase,
    private val config: ScapesAppConfig,
) : ViewModel() {
    private val mutableFeedState =
        MutableStateFlow(LandingFeedState.loading(SourceOption.scapes().source))
    val feedState: StateFlow<LandingFeedState> = mutableFeedState.asStateFlow()

    private var loadGeneration = 0
    private var sectionJobs: List<Job> = emptyList()

    init {
        load(SourceOption.scapes())
    }

    fun load(sourceOption: SourceOption) {
        val generation = ++loadGeneration
        sectionJobs.forEach { it.cancel() }
        mutableFeedState.value = LandingFeedState.loading(sourceOption.source)

        sectionJobs =
            DefaultLandingSectionTitles.map { sectionTitle ->
                viewModelScope.launch {
                    val result =
                        searchWallpapersUseCase(
                            query = sectionTitle,
                            page = 0,
                            source = sourceOption.source,
                            targetDevice = config.defaultTargetDevice,
                        )

                    if (generation != loadGeneration) {
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
                                LandingSectionState(title = sectionTitle, isLoading = true)

                            is ScapesResult.Success ->
                                LandingSectionState(
                                    title = sectionTitle,
                                    wallpapers =
                                        result.data.take(LandingSectionLimit).mapIndexed {
                                            index,
                                            wallpaper ->
                                            wallpaper.toUi(index)
                                        },
                                    isLoading = false,
                                    message =
                                        if (result.data.isEmpty()) "No wallpapers found." else null,
                                )
                        }

                    mutableFeedState.update { state ->
                        state.updateSection(sectionTitle, sectionState)
                    }
                }
            }
    }
}
