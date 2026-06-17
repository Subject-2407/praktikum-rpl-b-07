package com.scapes.di

import com.scapes.presentation.viewmodel.HomeViewModel
import com.scapes.presentation.viewmodel.ScapesViewModel
import com.scapes.presentation.viewmodel.SearchViewModel
import com.scapes.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/** Presentation-layer dependency bindings. */
fun presentationModule(): Module = module {
    single {
        ScapesViewModel(
            switchWallpaperSourceUseCase = get(),
            getCategoriesUseCase = get(),
            getSearchRecommendationsUseCase = get(),
        )
    }
    single {
        HomeViewModel(
            getTrendingCategoriesUseCase = get(),
            getFeaturedWallpapersUseCase = get(),
            searchWallpapersUseCase = get(),
            config = get(),
        )
    }
    single {
        SearchViewModel(
            getDownloadedWallpapersUseCase = get(),
            searchWallpapersUseCase = get(),
            saveWallpaperUseCase = get(),
            applyWallpaperUseCase = get(),
            logSearchEventUseCase = get(),
            config = get(),
        )
    }
    single {
        SettingsViewModel(
            getDownloadSettingsUseCase = get(),
            updateDownloadSettingsUseCase = get(),
            getApiKeyUseCase = get(),
            saveApiKeyUseCase = get(),
            removeApiKeyUseCase = get(),
        )
    }
}
