package com.scapes.di

import com.scapes.domain.usecase.GetWallpaperSourcesUseCase
import com.scapes.domain.usecase.SearchWallpapersUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Domain-layer dependency bindings.
 */
fun domainModule(): Module =
    module {
        factory { GetWallpaperSourcesUseCase() }
        factory { SearchWallpapersUseCase(get(), get()) }
    }
