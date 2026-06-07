package com.scapes.platform

import com.scapes.domain.model.ApplyTarget

/**
 * Android wallpaper applier placeholder.
 */
actual class WallpaperApplier {
    /**
     * Applies [imageBytes] to [target].
     */
    actual suspend fun apply(
        imageBytes: ByteArray,
        target: ApplyTarget,
    ): Result<Unit> =
        Result.failure(
            UnsupportedOperationException("Android wallpaper apply requires Context wiring."),
        )
}
