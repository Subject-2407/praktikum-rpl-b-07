package com.scapes.platform

/** Platform directory chooser used by download-folder settings. */
expect class DirectoryPicker {
    /**
     * Opens a native directory picker and returns the selected absolute path, or null on cancel.
     */
    suspend fun chooseDirectory(initialPath: String? = null): String?
}
