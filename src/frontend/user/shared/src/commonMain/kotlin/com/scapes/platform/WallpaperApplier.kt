package com.scapes.platform

import com.scapes.domain.model.ApplyTarget

/** Platform wallpaper application boundary. */
expect class WallpaperApplier {
    /** Applies [imageBytes] to [target]. */
    suspend fun apply(imageBytes: ByteArray, target: ApplyTarget): Result<Unit>
}
