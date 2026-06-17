package com.scapes.di

import com.scapes.data.remote.api.ExternalWallpaperApi
import com.scapes.data.repository.ExternalWallpaperRepository
import com.scapes.data.repository.PersistentSettingsRepository
import com.scapes.data.repository.SecureApiKeyRepository
import com.scapes.domain.repository.ApiKeyRepository
import com.scapes.domain.repository.SettingsRepository
import com.scapes.domain.repository.WallpaperRepository
import com.scapes.platform.FileSystemProvider
import com.scapes.platform.WallpaperApplier
import org.koin.core.module.Module
import org.koin.dsl.module

/** Data-layer dependency bindings. */
fun dataModule(): Module = module {
    single { createHttpClient() }
    single<SettingsRepository> { PersistentSettingsRepository(get(), get()) }
    single<ApiKeyRepository> { SecureApiKeyRepository(get()) }
    single { ExternalWallpaperApi(httpClient = get(), apiKeyRepository = get()) }
    single<WallpaperRepository> {
        ExternalWallpaperRepository(
            externalWallpaperApi = get(),
            storage = get(),
            settingsRepository = get(),
            fileSystemProvider = get<FileSystemProvider>(),
            wallpaperApplier = get<WallpaperApplier>(),
        )
    }
}
