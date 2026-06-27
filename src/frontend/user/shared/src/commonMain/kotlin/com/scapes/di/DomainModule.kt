package com.scapes.di

import com.scapes.domain.usecase.ApplyWallpaperUseCase
import com.scapes.domain.usecase.GetApiKeyUseCase
import com.scapes.domain.usecase.GetCategoriesUseCase
import com.scapes.domain.usecase.GetDownloadedWallpapersUseCase
import com.scapes.domain.usecase.GetDownloadSettingsUseCase
import com.scapes.domain.usecase.GetFeaturedWallpapersUseCase
import com.scapes.domain.usecase.GetSearchRecommendationsUseCase
import com.scapes.domain.usecase.GetTrendingCategoriesUseCase
import com.scapes.domain.usecase.GetWallpaperSourcesUseCase
import com.scapes.domain.usecase.LogSearchEventUseCase
import com.scapes.domain.usecase.RemoveApiKeyUseCase
import com.scapes.domain.usecase.SaveApiKeyUseCase
import com.scapes.domain.usecase.SaveWallpaperUseCase
import com.scapes.domain.usecase.DeleteWallpaperUseCase
import com.scapes.domain.usecase.SearchWallpapersUseCase
import com.scapes.domain.usecase.SwitchWallpaperSourceUseCase
import com.scapes.domain.usecase.UpdateDownloadSettingsUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/** Domain-layer dependency bindings. */
fun domainModule(): Module = module {
    factory { ApplyWallpaperUseCase(get()) }
    factory { GetApiKeyUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { GetDownloadedWallpapersUseCase(get()) }
    factory { GetDownloadSettingsUseCase(get()) }
    factory { GetFeaturedWallpapersUseCase(get()) }
    factory { GetSearchRecommendationsUseCase(get()) }
    factory { GetTrendingCategoriesUseCase(get()) }
    factory { GetWallpaperSourcesUseCase(get()) }
    factory { LogSearchEventUseCase(get()) }
    factory { RemoveApiKeyUseCase(get(), get()) }
    factory { SaveApiKeyUseCase(get(), get()) }
    factory { SaveWallpaperUseCase(get()) }
    factory { DeleteWallpaperUseCase(get()) }
    factory { SearchWallpapersUseCase(get(), get()) }
    factory { SwitchWallpaperSourceUseCase(get()) }
    factory { UpdateDownloadSettingsUseCase(get()) }
}
