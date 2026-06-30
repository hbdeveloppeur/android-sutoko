package com.purpletear.game.data.file.testing

import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestAssetCacheManager @Inject constructor(
    private val pathProvider: AndroidGamePathProvider,
) {

    /**
     * Returns the directory where test assets for [gameId] are cached.
     */
    fun getCacheDirectory(gameId: String): File {
        return File(pathProvider.getGameDirectory(gameId, legacyId = null), TEST_ASSETS_DIR)
    }

    /**
     * Copies assets from the extracted package into the test asset cache.
     *
     * Assets are keyed by [uniqueFileName]; the path inside the ZIP's assets/ folder is used
     * as the unique file name.
     *
     * @param extractedAssetsDir The assets/ directory extracted from the ZIP.
     * @param gameId Game identifier.
     * @return List of cached uniqueFileNames.
     */
    fun copyAssets(extractedAssetsDir: File, gameId: String): List<String> {
        val cacheDir = getCacheDirectory(gameId)
        cacheDir.mkdirs()

        if (!extractedAssetsDir.exists()) {
            StoryTestingLogger.d("ASST") { "No assets to copy — $extractedAssetsDir does not exist" }
            return emptyList()
        }

        val cached = mutableListOf<String>()
        extractedAssetsDir.walkTopDown()
            .filter { it.isFile }
            .forEach { sourceFile ->
                val relativePath = sourceFile.relativeTo(extractedAssetsDir).path
                val destFile = File(cacheDir, relativePath)
                destFile.parentFile?.mkdirs()
                sourceFile.copyTo(destFile, overwrite = true)
                cached.add(relativePath)
            }

        StoryTestingLogger.d("ASST") { "Copied ${cached.size} assets to test cache for $gameId" }
        return cached
    }

    /**
     * Lists all currently cached uniqueFileNames for [gameId].
     */
    fun listCachedAssets(gameId: String): List<String> {
        val cacheDir = getCacheDirectory(gameId)
        return listRelativeFiles(cacheDir)
    }

    /**
     * Lists all assets available for testing for [gameId].
     *
     * This is the union of the test asset cache and the installed story assets.
     * The cache takes precedence, so a test-specific override shadows an official
     * asset with the same relative path.
     */
    fun listAvailableAssets(gameId: String): List<String> {
        val cached = listCachedAssets(gameId).toSet()
        val original = listOriginalAssets(gameId)
        return cached.toList() + original.filter { it !in cached }
    }

    private fun listOriginalAssets(gameId: String): List<String> {
        val originalAssetsDir = File(
            pathProvider.getGameDirectory(gameId, legacyId = null),
            ORIGINAL_ASSETS_DIR
        )
        return listRelativeFiles(originalAssetsDir)
    }

    private fun listRelativeFiles(root: File): List<String> {
        if (!root.exists()) {
            return emptyList()
        }

        return root.walkTopDown()
            .filter { it.isFile }
            .map { it.relativeTo(root).path }
            .toList()
    }

    private companion object {
        const val TEST_ASSETS_DIR = "test-assets"
        const val ORIGINAL_ASSETS_DIR = "assets"
    }
}
