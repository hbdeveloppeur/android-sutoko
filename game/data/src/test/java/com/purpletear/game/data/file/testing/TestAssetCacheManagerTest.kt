package com.purpletear.game.data.file.testing

import com.purpletear.game.data.provider.AndroidGamePathProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TestAssetCacheManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var pathProvider: AndroidGamePathProvider
    private lateinit var cacheManager: TestAssetCacheManager

    @Before
    fun setup() {
        pathProvider = FakeAndroidGamePathProvider(tempFolder.root)
        cacheManager = TestAssetCacheManager(pathProvider)
    }

    @Test
    fun `listAvailableAssets returns cached assets when original dir does not exist`() {
        val gameDir = pathProvider.getGameDirectory("game1", null)
        val cacheDir = File(gameDir, "test-assets")
        File(cacheDir, "cached.png").apply { parentFile?.mkdirs(); writeText("cached") }

        val assets = cacheManager.listAvailableAssets("game1")

        assertEquals(listOf("cached.png"), assets)
    }

    @Test
    fun `listAvailableAssets returns original assets when cache dir does not exist`() {
        val gameDir = pathProvider.getGameDirectory("game1", null)
        val originalDir = File(gameDir, "assets")
        File(originalDir, "original.png").apply { parentFile?.mkdirs(); writeText("original") }

        val assets = cacheManager.listAvailableAssets("game1")

        assertEquals(listOf("original.png"), assets)
    }

    @Test
    fun `listAvailableAssets merges cached and original assets`() {
        val gameDir = pathProvider.getGameDirectory("game1", null)
        val cacheDir = File(gameDir, "test-assets")
        val originalDir = File(gameDir, "assets")
        File(cacheDir, "cached.png").apply { parentFile?.mkdirs(); writeText("cached") }
        File(originalDir, "original.png").apply { parentFile?.mkdirs(); writeText("original") }

        val assets = cacheManager.listAvailableAssets("game1")

        assertEquals(listOf("cached.png", "original.png"), assets.sorted())
    }

    @Test
    fun `listAvailableAssets prefers cached asset over original with same relative path`() {
        val gameDir = pathProvider.getGameDirectory("game1", null)
        val cacheDir = File(gameDir, "test-assets")
        val originalDir = File(gameDir, "assets")
        File(cacheDir, "shared.png").apply { parentFile?.mkdirs(); writeText("cached") }
        File(originalDir, "shared.png").apply { parentFile?.mkdirs(); writeText("original") }

        val assets = cacheManager.listAvailableAssets("game1")

        assertEquals(listOf("shared.png"), assets)
    }

    @Test
    fun `listAvailableAssets includes nested files from both directories`() {
        val gameDir = pathProvider.getGameDirectory("game1", null)
        val cacheDir = File(gameDir, "test-assets")
        val originalDir = File(gameDir, "assets")
        File(cacheDir, "bg/cached.png").apply { parentFile?.mkdirs(); writeText("cached") }
        File(originalDir, "sounds/original.mp3").apply { parentFile?.mkdirs(); writeText("original") }

        val assets = cacheManager.listAvailableAssets("game1")

        assertEquals(
            listOf("bg/cached.png", "sounds/original.mp3"),
            assets.sorted()
        )
    }

    @Test
    fun `listAvailableAssets returns empty list when neither cache nor original exists`() {
        val assets = cacheManager.listAvailableAssets("game1")

        assertTrue(assets.isEmpty())
    }

    @Test
    fun `copyAssets copies files into cache and listCachedAssets returns them`() {
        val gameDir = pathProvider.getGameDirectory("game1", null)
        val extractedAssetsDir = File(gameDir, "extracted/assets")
        File(extractedAssetsDir, "image.png").apply { parentFile?.mkdirs(); writeText("image") }
        File(extractedAssetsDir, "audio/voice.mp3").apply { parentFile?.mkdirs(); writeText("audio") }

        val copied = cacheManager.copyAssets(extractedAssetsDir, "game1")

        assertEquals(listOf("audio/voice.mp3", "image.png"), copied.sorted())
        assertEquals(copied.sorted(), cacheManager.listCachedAssets("game1").sorted())
    }

    private class FakeAndroidGamePathProvider(private val rootDir: File) : AndroidGamePathProvider {
        override fun getStoriesDirectoryPath(): String = File(rootDir, "games").absolutePath
        override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String =
            File(getGamesDirectory(), legacyId?.toString() ?: storyId).absolutePath
        override fun getGamesDirectory(): File = File(rootDir, "games")
        override fun getGameDirectory(gameId: String, legacyId: Int?): File =
            File(getGamesDirectory(), legacyId?.toString() ?: gameId)
    }
}
