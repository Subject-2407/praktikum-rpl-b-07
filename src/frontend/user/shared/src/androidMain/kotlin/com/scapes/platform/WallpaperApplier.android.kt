package com.scapes.platform

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import androidx.compose.ui.geometry.Offset
import com.scapes.domain.model.ApplyTarget
import com.scapes.presentation.ui.components.WallpaperUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL
import androidx.core.graphics.createBitmap

/** Android native wallpaper applier using WallpaperManager. */
actual class WallpaperApplier : KoinComponent {
    private val context: Context by inject()
    private val wallpaperManager by lazy { WallpaperManager.getInstance(context) }

    /** Applies [imageBytes] to [target]. (used by Desktop/Legacy). */
    actual suspend fun apply(imageBytes: ByteArray, target: ApplyTarget): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: throw IllegalArgumentException("Failed to decode image bytes.")

                val flag = when (target) {
                    ApplyTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                    ApplyTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                    ApplyTarget.BOTH_SCREENS -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    else -> WallpaperManager.FLAG_SYSTEM
                }

                wallpaperManager.setBitmap(bitmap, null, true, flag)
                Unit
            }
        }

    suspend fun applyWithPosition(
        wallpaper: WallpaperUi,
        target: ApplyTarget,
        offset: Offset,
        scale: Float
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Download image
            val originalBitmap = URL(wallpaper.wallpaper.fullImageUrl).openStream().use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
                    ?: throw IllegalArgumentException("Failed to decode wallpaper from URL.")
            }

            var resultBitmap: Bitmap? = null
            try {
                // Resolve target screen dimensions in pixels
                val screenWidth = context.resources.displayMetrics.widthPixels
                val screenHeight = context.resources.displayMetrics.heightPixels

                val cropScale = maxOf(
                    screenWidth.toFloat() / originalBitmap.width,
                    screenHeight.toFloat() / originalBitmap.height
                )

                val finalScale = cropScale * scale
                val scaledWidth = (originalBitmap.width * finalScale).toInt()
                val scaledHeight = (originalBitmap.height * finalScale).toInt()

                // Create Bitmap
                resultBitmap = createBitmap(screenWidth, screenHeight)
                val canvas = Canvas(resultBitmap)

                val left = (screenWidth - scaledWidth) / 2f + offset.x
                val top = (screenHeight - scaledHeight) / 2f + offset.y

                val destRect = Rect(
                    left.toInt(),
                    top.toInt(),
                    (left + scaledWidth).toInt(),
                    (top + scaledHeight).toInt()
                )

                canvas.drawBitmap(originalBitmap, null, destRect, null)

                // Apply to system via WallpaperManager
                val flag = when (target) {
                    ApplyTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                    ApplyTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                    ApplyTarget.BOTH_SCREENS -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    else -> WallpaperManager.FLAG_SYSTEM
                }

                wallpaperManager.setBitmap(resultBitmap, null, true, flag)
            } finally {
                // Cleanup
                originalBitmap.recycle()
                resultBitmap?.recycle()
            }
            Unit
        }
    }
}
