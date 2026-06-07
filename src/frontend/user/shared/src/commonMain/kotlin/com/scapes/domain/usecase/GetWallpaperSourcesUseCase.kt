package com.scapes.domain.usecase

import com.scapes.domain.model.WallpaperSource

/**
 * Lists user-selectable wallpaper sources.
 */
class GetWallpaperSourcesUseCase {
    /**
     * Returns every source currently supported by the user app.
     */
    operator fun invoke(): List<WallpaperSource> = WallpaperSource.entries
}
