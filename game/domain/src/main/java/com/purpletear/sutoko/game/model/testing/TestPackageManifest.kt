package com.purpletear.sutoko.game.model.testing

import androidx.annotation.Keep

/**
 * Information extracted from a downloaded test package manifest.
 *
 * @property seed Monotonic seed of this package.
 * @property chapterId Backend chapter UUID.
 * @property storyId Story identifier.
 * @property updatedAt Package generation timestamp (ISO-8601 string).
 * @property assetInventory Complete list of asset uniqueFileNames for this chapter.
 * @property extractedDirectory Local directory where the ZIP was extracted.
 */
@Keep
data class TestPackageManifest(
    val seed: Int,
    val chapterId: String,
    val storyId: String,
    val updatedAt: String,
    val assetInventory: List<String>,
    val extractedDirectory: String,
)
