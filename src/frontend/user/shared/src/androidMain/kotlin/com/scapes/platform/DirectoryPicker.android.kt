package com.scapes.platform

/** Android directory picking is wired from the Android app shell later. */
actual class DirectoryPicker {
    actual suspend fun chooseDirectory(initialPath: String?): String? = null
}
