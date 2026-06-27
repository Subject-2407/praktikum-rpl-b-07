package com.scapes.platform

import com.scapes.domain.model.ApplyTarget
import com.sun.jna.Native
import com.sun.jna.WString
import com.sun.jna.win32.StdCallLibrary
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SPI_SET_DESK_WALLPAPER = 20
private const val SPIF_UPDATE_INI_FILE = 0x01
private const val SPIF_SEND_CHANGE = 0x02

private interface DesktopUser32 : StdCallLibrary {
    fun SystemParametersInfoW(uiAction: Int, uiParam: Int, pvParam: WString, fWinIni: Int): Boolean
}

/** Desktop wallpaper applier backed by the Windows SystemParametersInfo API. */
actual class WallpaperApplier {
    private val user32: DesktopUser32 = Native.load("user32", DesktopUser32::class.java)

    /** Applies [imageBytes] to [target]. */
    actual suspend fun apply(imageBytes: ByteArray, target: ApplyTarget): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(target == ApplyTarget.DESKTOP) {
                    "Desktop supports ApplyTarget.DESKTOP only."
                }
                require(imageBytes.isNotEmpty()) { "Wallpaper image is empty." }

                val wallpaperPath = writeWallpaperImage(imageBytes)
                val applied =
                    user32.SystemParametersInfoW(
                        SPI_SET_DESK_WALLPAPER,
                        0,
                        WString(wallpaperPath.toString()),
                        SPIF_UPDATE_INI_FILE or SPIF_SEND_CHANGE,
                    )

                check(applied) { "Windows rejected the wallpaper update request." }
            }
        }

    private fun writeWallpaperImage(imageBytes: ByteArray): Path {
        val image =
            ImageIO.read(ByteArrayInputStream(imageBytes))
                ?: error("Unsupported image format for desktop wallpaper.")
        val directory = appDataDirectory("runtime")
        Files.createDirectories(directory)

        val target = directory.resolve("current-wallpaper.bmp")
        check(ImageIO.write(image, "bmp", target.toFile())) {
            "Desktop wallpaper image could not be encoded."
        }
        return target
    }
}

internal fun appDataDirectory(child: String): Path {
    val base =
        System.getenv("APPDATA")?.takeIf { it.isNotBlank() }?.let(Path::of)
            ?: Path.of(System.getProperty("user.home"), "AppData", "Roaming")

    return base.resolve("Scapes").resolve(child)
}
