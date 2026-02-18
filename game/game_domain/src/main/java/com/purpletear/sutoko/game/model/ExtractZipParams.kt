package com.purpletear.sutoko.game.model

import androidx.annotation.Keep
import java.io.File

/**
 * Parameters for extracting a zip file.
 *
 * @property zipFile The zip file to extract.
 * @property destinationPath The directory path where the zip file will be extracted.
 * @property password Optional password for encrypted zip files.
 * @property deleteArchiveAfterExtraction Whether to delete the zip file after successful extraction.
 */
@Keep
data class ExtractZipParams(
    val zipFile: File,
    val destinationPath: String,
    val password: String? = null,
    val deleteArchiveAfterExtraction: Boolean = false
)
