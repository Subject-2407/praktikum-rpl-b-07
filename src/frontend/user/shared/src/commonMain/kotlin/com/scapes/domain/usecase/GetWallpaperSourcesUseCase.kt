package com.scapes.domain.usecase

import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.model.WallpaperSourceInfo
import com.scapes.domain.repository.WallpaperRepository

/** Lists user-selectable wallpaper sources. */
class GetWallpaperSourcesUseCase(private val wallpaperRepository: WallpaperRepository? = null) {
    /** Returns every source currently supported by the user app. */
    operator fun invoke(): List<WallpaperSource> = WallpaperSource.entries

    /** Loads source metadata from Scapes API when repository wiring is available. */
    suspend fun load(): ScapesResult<List<WallpaperSourceInfo>> =
        wallpaperRepository?.getWallpaperSources()
            ?: ScapesResult.Success(
                WallpaperSource.entries.map { source ->
                    WallpaperSourceInfo(
                        source = source,
                        label = source.defaultLabel(),
                        baseUrl = "",
                        isDefault = source == WallpaperSource.SCAPES_API,
                        requiresPersonalApiKey = source != WallpaperSource.SCAPES_API,
                    )
                }
            )

    private fun WallpaperSource.defaultLabel(): String =
        when (this) {
            WallpaperSource.SCAPES_API -> "Scapes"
            WallpaperSource.PEXELS -> "Pexels"
            WallpaperSource.UNSPLASH -> "Unsplash"
            WallpaperSource.PIXABAY -> "Pixabay"
        }
}
