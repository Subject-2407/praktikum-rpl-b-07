package com.scapes.platform

import com.scapes.domain.model.ApplyTarget

/** Desktop wallpaper applier placeholder. */
actual class WallpaperApplier {
    /** Applies [imageBytes] to [target]. */
    actual suspend fun apply(imageBytes: ByteArray, target: ApplyTarget): Result<Unit> =
        Result.failure(
            UnsupportedOperationException("Desktop wallpaper apply requires JNA User32 wiring.")
        )
}
