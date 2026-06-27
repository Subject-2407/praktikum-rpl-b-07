package com.scapes.platform

import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JFileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

/** Desktop directory picker backed by Swing's native file chooser integration. */
actual class DirectoryPicker {
    actual suspend fun chooseDirectory(initialPath: String?): String? =
        withContext(Dispatchers.Swing) {
            val chooser =
                JFileChooser(initialPath?.takeIf(::isExistingDirectory)).apply {
                    dialogTitle = "Choose Scapes download folder"
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    isAcceptAllFileFilterUsed = false
                }

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile.absolutePath
            } else {
                null
            }
        }

    private fun isExistingDirectory(path: String): Boolean =
        runCatching { Files.isDirectory(Path.of(path)) }.getOrDefault(false)
}
