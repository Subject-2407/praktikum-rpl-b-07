package com.scapes.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.usecase.ApplyWallpaperUseCase
import com.scapes.domain.usecase.GetDownloadedWallpapersUseCase
import com.scapes.domain.usecase.LogSearchEventUseCase
import com.scapes.domain.usecase.SaveWallpaperUseCase
import com.scapes.domain.usecase.SearchWallpapersUseCase
import com.scapes.presentation.model.ScapesAppConfig
import com.scapes.presentation.model.SourceOption
import com.scapes.presentation.model.WallpaperActionState
import com.scapes.presentation.model.WallpaperFeedState
import com.scapes.presentation.ui.components.WallpaperUi
import com.scapes.presentation.ui.components.toUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Owns search results, pagination, and wallpaper save/apply actions. */
class SearchViewModel(
    private val getDownloadedWallpapersUseCase: GetDownloadedWallpapersUseCase,
    private val searchWallpapersUseCase: SearchWallpapersUseCase,
    private val saveWallpaperUseCase: SaveWallpaperUseCase,
    private val applyWallpaperUseCase: ApplyWallpaperUseCase,
    private val logSearchEventUseCase: LogSearchEventUseCase,
    private val config: ScapesAppConfig,
) : ViewModel() {
    private val mutableFeedState = MutableStateFlow(WallpaperFeedState())
    val feedState: StateFlow<WallpaperFeedState> = mutableFeedState.asStateFlow()
    private val mutableCollectionsState =
        MutableStateFlow(
            WallpaperFeedState(query = "Collections", isInitialLoading = false, endReached = true)
        )
    val collectionsState: StateFlow<WallpaperFeedState> = mutableCollectionsState.asStateFlow()

    private val mutableActionStates =
        MutableStateFlow<Map<String, WallpaperActionState>>(emptyMap())
    val actionStates: StateFlow<Map<String, WallpaperActionState>> =
        mutableActionStates.asStateFlow()

    private var searchGeneration = 0

    fun search(query: String, sourceOption: SourceOption) {
        search(query = query, sourceOption = sourceOption, categorySlug = null)
    }

    fun search(query: String, sourceOption: SourceOption, categorySlug: String?) {
        val generation = ++searchGeneration
        mutableActionStates.value = emptyMap()
        mutableFeedState.value =
            WallpaperFeedState(
                query = query,
                source = sourceOption.source,
                categorySlug = categorySlug,
                isInitialLoading = true,
            )

        viewModelScope.launch {
            val result =
                searchWallpapersUseCase(
                    query = query,
                    page = 0,
                    source = sourceOption.source,
                    targetDevice = config.defaultTargetDevice,
                    categorySlug = categorySlug,
                )
            if (generation != searchGeneration) {
                return@launch
            }

            when (result) {
                is ScapesResult.Error ->
                    mutableFeedState.update { state ->
                        state.copy(
                            isInitialLoading = false,
                            message = result.message,
                            endReached = true,
                        )
                    }

                ScapesResult.Loading ->
                    mutableFeedState.update { state -> state.copy(isInitialLoading = true) }

                is ScapesResult.Success ->
                    mutableFeedState.update { state ->
                        val wallpapers =
                            result.data
                                .distinctBy { wallpaper -> wallpaper.id }
                                .mapIndexed { index, wallpaper -> wallpaper.toUi(index) }
                        state.copy(
                            wallpapers = wallpapers,
                            isInitialLoading = false,
                            nextPage = 1,
                            endReached = wallpapers.isEmpty(),
                            message = if (wallpapers.isEmpty()) "No wallpapers found." else null,
                        )
                    }
            }

            when (result) {
                is ScapesResult.Success ->
                    logSearchEvent(query, sourceOption, result.data.size, generation)

                is ScapesResult.Error -> logSearchEvent(query, sourceOption, null, generation)
                ScapesResult.Loading -> Unit
            }
        }
    }

    fun loadMore() {
        val feed = feedState.value
        if (
            feed.isInitialLoading || feed.isLoadingMore || feed.endReached || feed.query.isBlank()
        ) {
            return
        }

        val generation = searchGeneration
        mutableFeedState.update { state -> state.copy(isLoadingMore = true, message = null) }

        viewModelScope.launch {
            val result =
                searchWallpapersUseCase(
                    query = feed.query,
                    page = feed.nextPage,
                    source = feed.source,
                    targetDevice = config.defaultTargetDevice,
                    categorySlug = feed.categorySlug,
                )
            if (generation != searchGeneration) {
                return@launch
            }

            when (result) {
                is ScapesResult.Error ->
                    mutableFeedState.update { state ->
                        state.copy(isLoadingMore = false, message = result.message)
                    }

                ScapesResult.Loading ->
                    mutableFeedState.update { state -> state.copy(isLoadingMore = true) }

                is ScapesResult.Success ->
                    mutableFeedState.update { state ->
                        val existingCount = state.wallpapers.size
                        val existingIds = state.wallpapers.map { wallpaper -> wallpaper.wallpaper.id }.toSet()
                        val moreWallpapers =
                            result.data
                                .filterNot { wallpaper -> wallpaper.id in existingIds }
                                .distinctBy { wallpaper -> wallpaper.id }
                                .mapIndexed { index, wallpaper ->
                                    wallpaper.toUi(existingCount + index)
                                }
                        state.copy(
                            wallpapers = state.wallpapers + moreWallpapers,
                            isLoadingMore = false,
                            nextPage = state.nextPage + 1,
                            endReached = result.data.isEmpty() || moreWallpapers.isEmpty(),
                        )
                    }
            }
        }
    }

    fun saveWallpaper(wallpaperUi: WallpaperUi) {
        val wallpaper = wallpaperForAction(wallpaperUi)
        val generation = searchGeneration
        updateWallpaperAction(wallpaper.id) { copy(isSaving = true, message = null) }

        viewModelScope.launch {
            val result = saveWallpaperUseCase(wallpaper)
            if (generation != searchGeneration) {
                return@launch
            }

            when (result) {
                is ScapesResult.Error ->
                    updateWallpaperAction(wallpaper.id) {
                        copy(isSaving = false, message = result.message)
                    }

                ScapesResult.Loading ->
                    updateWallpaperAction(wallpaper.id) { copy(isSaving = true) }

                is ScapesResult.Success ->
                    updateWallpaperAction(wallpaper.id) {
                        copy(isSaving = false, localPath = result.data.localPath, message = "Saved")
                    }
            }

            if (result is ScapesResult.Success) {
                loadCollections()
            }
        }
    }

    fun applyWallpaper(wallpaperUi: WallpaperUi) {
        val wallpaper = wallpaperForAction(wallpaperUi)
        val generation = searchGeneration
        updateWallpaperAction(wallpaper.id) { copy(isApplying = true, message = null) }

        viewModelScope.launch {
            val result = applyWallpaperUseCase(wallpaper, config.defaultApplyTarget)
            if (generation != searchGeneration) {
                return@launch
            }

            when (result) {
                is ScapesResult.Error ->
                    updateWallpaperAction(wallpaper.id) {
                        copy(isApplying = false, message = result.message)
                    }

                ScapesResult.Loading ->
                    updateWallpaperAction(wallpaper.id) { copy(isApplying = true) }

                is ScapesResult.Success ->
                    updateWallpaperAction(wallpaper.id) {
                        copy(isApplying = false, message = "Applied")
                    }
            }
        }
    }

    fun loadCollections() {
        mutableCollectionsState.value =
            WallpaperFeedState(query = "Collections", isInitialLoading = true, endReached = true)

        viewModelScope.launch {
            when (val result = getDownloadedWallpapersUseCase()) {
                is ScapesResult.Error ->
                    mutableCollectionsState.value =
                        WallpaperFeedState(
                            query = "Collections",
                            isInitialLoading = false,
                            endReached = true,
                            message = result.message,
                        )

                ScapesResult.Loading ->
                    mutableCollectionsState.value =
                        WallpaperFeedState(
                            query = "Collections",
                            isInitialLoading = true,
                            endReached = true,
                        )

                is ScapesResult.Success ->
                    mutableCollectionsState.value =
                        WallpaperFeedState(
                            query = "Collections",
                            wallpapers = result.data.mapIndexed { index, wallpaper -> wallpaper.toUi(index) },
                            isInitialLoading = false,
                            endReached = true,
                            message =
                                if (result.data.isEmpty()) {
                                    "No downloaded wallpapers yet."
                                } else {
                                    null
                                },
                        )
            }
        }
    }

    private fun updateWallpaperAction(
        wallpaperId: String,
        transform: WallpaperActionState.() -> WallpaperActionState,
    ) {
        mutableActionStates.update { states ->
            states + (wallpaperId to (states[wallpaperId] ?: WallpaperActionState()).transform())
        }
    }

    private fun wallpaperForAction(wallpaperUi: WallpaperUi) =
        mutableActionStates.value[wallpaperUi.wallpaper.id]
            ?.localPath
            ?.takeIf { localPath -> localPath.isNotBlank() }
            ?.let { localPath -> wallpaperUi.wallpaper.copy(localPath = localPath) }
            ?: wallpaperUi.wallpaper

    private fun logSearchEvent(
        query: String,
        sourceOption: SourceOption,
        resultCount: Int?,
        generation: Int,
    ) {
        viewModelScope.launch {
            if (generation != searchGeneration) {
                return@launch
            }

            logSearchEventUseCase(
                query = query,
                source = sourceOption.source,
                resultCount = resultCount,
            )
        }
    }
}
