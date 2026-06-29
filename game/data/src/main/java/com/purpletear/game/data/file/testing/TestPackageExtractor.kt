package com.purpletear.game.data.file.testing

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestPackageExtractor @Inject constructor() {

    /**
     * Extracts a ZIP file to the given destination directory.
     *
     * @param zipFile Source ZIP file.
     * @param destinationDir Directory to extract into; created if needed.
     * @return The destination directory.
     * @throws IllegalStateException if a ZIP entry attempts path traversal.
     */
    fun extract(zipFile: File, destinationDir: File): File {
        destinationDir.mkdirs()
        StoryTestingLogger.d("PKG") { "Extracting ${zipFile.length()} bytes to $destinationDir" }

        var entryCount = 0
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                val outputFile = File(destinationDir, entry.name)
                val canonicalDest = destinationDir.canonicalPath
                val canonicalOutput = outputFile.canonicalPath
                require(canonicalOutput.startsWith(canonicalDest)) {
                    "Path traversal attempt in ZIP: ${entry.name}"
                }

                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    outputFile.parentFile?.mkdirs()
                    FileOutputStream(outputFile).use { output ->
                        zipIn.copyTo(output)
                    }
                }
                entryCount++
                entry = zipIn.nextEntry
            }
        }

        StoryTestingLogger.d("PKG") { "Extracted $entryCount entries to $destinationDir" }
        return destinationDir
    }
}
