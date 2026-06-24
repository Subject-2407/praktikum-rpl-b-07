package com.scapes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.usecase.GetCategoriesUseCase
import com.scapes.domain.usecase.GetFeaturedWallpapersUseCase
import com.scapes.domain.usecase.GetTrendingCategoriesUseCase
import com.scapes.domain.usecase.SearchWallpapersUseCase
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
private const val FeaturedLandingSectionLimit = 10

/** Owns discovery feed state for the home screen. */
class HomeViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTrendingCategoriesUseCase: GetTrendingCategoriesUseCase,
    private val getFeaturedWallpapersUseCase: GetFeaturedWallpapersUseCase,
    private val searchWallpapersUseCase: SearchWallpapersUseCase,
    private val config: ScapesAppConfig,
) : ViewModel() {
    private val mutableFeedState =
        MutableStateFlow(
            LandingFeedState.loading(
                source = SourceOption.scapes().source,
                sectionTitles = listOf("Trending"),
            )
        )
    val feedState: StateFlow<LandingFeedState> = mutableFeedState.asStateFlow()

    private var loadGeneration = 0
    private var sectionJobs: List<Job> = emptyList()

    init {
        load(SourceOption.scapes())
    }

    fun load(sourceOption: SourceOption) {
        val generation = ++loadGeneration
        sectionJobs.forEach { it.cancel() }
        mutableFeedState.value =
            LandingFeedState.loading(
                source = sourceOption.source,
                sectionTitles = listOf("Trending"),
            )

        viewModelScope.launch {
            val isScapes = sourceOption.source == WallpaperSource.SCAPES_API
            val sectionMap = mutableMapOf<String, String>()

            if (isScapes) {
                when (val categoriesResult = getCategoriesUseCase()) {
                    is ScapesResult.Success -> {
                        categoriesResult.data.take(LandingSectionLimit).forEach { cat ->
                            sectionMap[cat.name] = cat.slug
                        }
                    }
                    else -> {}
                }
            } else {
                when (val trendingResult = getTrendingCategoriesUseCase(source = sourceOption.source, limit = LandingSectionLimit)) {
                    is ScapesResult.Success -> {
                        trendingResult.data.map { it.queryValue }.distinct().forEach { query ->
                            sectionMap[query] = query
                        }
                    }
                    else -> {}
                }
            }

            val featuredQuery = sectionMap.values.firstOrNull().orEmpty()
            val landingSections = listOf("Trending") + sectionMap.keys.toList()

            if (generation != loadGeneration) {
                return@launch
            }

            mutableFeedState.value =
                LandingFeedState.loading(
                    source = sourceOption.source,
                    sectionTitles = landingSections,
                )

            sectionJobs =
                landingSections.map { sectionTitle ->
                    val query = if (sectionTitle == "Trending") featuredQuery else sectionMap[sectionTitle] ?: ""
                    loadSection(
                        generation = generation,
                        sourceOption = sourceOption,
                        sectionTitle = sectionTitle,
                        sectionQuery = query,
                        isFeatured = sectionTitle == "Trending",
                    )
                }
        }
    }

    private fun loadSection(
        generation: Int,
        sourceOption: SourceOption,
        sectionTitle: String,
        sectionQuery: String,
        isFeatured: Boolean,
    ): Job =
        viewModelScope.launch {
            val useExternalFeaturedFeed =
                isFeatured && sourceOption.source != WallpaperSource.SCAPES_API
            if (sectionQuery.isBlank() && !useExternalFeaturedFeed) {
                mutableFeedState.update { state ->
                    state.updateSection(
                        sectionTitle,
                        LandingSectionState(
                            title = sectionTitle,
                            searchQuery = sectionQuery,
                            isFeatured = isFeatured,
                            isLoading = false,
                            message = "No wallpapers found.",
                        ),
                    )
                }
                return@launch
            }

            val result =
                if (useExternalFeaturedFeed) {
                    getFeaturedWallpapersUseCase(
                        page = 0,
                        source = sourceOption.source,
                        targetDevice = config.defaultTargetDevice,
                    )
                } else {
                    val isScapes = sourceOption.source == WallpaperSource.SCAPES_API
                    searchWallpapersUseCase(
                        query = if (isScapes) "" else sectionQuery,
                        page = 0,
                        source = sourceOption.source,
                        targetDevice = config.defaultTargetDevice,
                        categorySlug = if (isScapes && !isFeatured) sectionQuery else null,
                        limit = if (isScapes && isFeatured) 10 else null,
                    )
                }

            if (generation != loadGeneration) {
                return@launch
            }

            val sectionState =
                when (result) {
                    is ScapesResult.Error ->
                        LandingSectionState(
                            title = sectionTitle,
                            searchQuery = sectionQuery,
                            isFeatured = isFeatured,
                            isLoading = false,
                            message = result.message,
                        )

                    ScapesResult.Loading ->
                        LandingSectionState(
                            title = sectionTitle,
                            searchQuery = sectionQuery,
                            isFeatured = isFeatured,
                            isLoading = true,
                        )

                    is ScapesResult.Success ->
                        LandingSectionState(
                            title = sectionTitle,
                            searchQuery = sectionQuery,
                            isFeatured = isFeatured,
                            wallpapers =
                                result.data
                                    .take(
                                        if (isFeatured) {
                                            FeaturedLandingSectionLimit
                                        } else {
                                            LandingSectionLimit
                                        }
                                    )
                                    .mapIndexed { index, wallpaper ->
                                    wallpaper.toUi(index)
                                },
                            isLoading = false,
                            message = if (result.data.isEmpty()) "No wallpapers found." else null,
                        )
                }

            mutableFeedState.update { state -> state.updateSection(sectionTitle, sectionState) }
        }
}
