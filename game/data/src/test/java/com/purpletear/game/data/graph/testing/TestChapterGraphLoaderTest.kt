package com.purpletear.game.data.graph.testing

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.purpletear.game.data.file.testing.TestAssetCacheManager
import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.game.data.remote.testing.dto.AssetInventoryItemDto
import com.purpletear.game.data.remote.testing.dto.TestPackageManifestDto
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TestChapterGraphLoaderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val gson = Gson()

    @Test
    fun `loads chapter graph from manifest and compacts ignore nodes`() {
        val extractDir = tempFolder.newFolder("extracted")
        tempFolder.newFolder("games", "game1")

        val manifest = TestPackageManifestDto(
            seed = 1,
            chapterId = "chapter-1",
            storyId = "story-1",
            updatedAt = "2024-01-01T00:00:00Z",
            nodes = listOf(
                nodeDto("start-0", "start", label = "Start"),
                nodeDto("narration-1", "narration", text = "Once upon a time"),
                nodeDto("ignore-2", "ignore"),
                nodeDto("message-3", "message", text = "Hello", characterId = 1),
                nodeDto("end-4", "end")
            ),
            edges = listOf(
                edgeDto("start-0", "narration-1"),
                edgeDto("narration-1", "ignore-2"),
                edgeDto("ignore-2", "message-3"),
                edgeDto("message-3", "end-4")
            ),
            assetInventory = emptyList()
        )

        File(extractDir, "manifest.json").writeText(gson.toJson(manifest))

        val pathProvider = FakeAndroidGamePathProvider(tempFolder.root)
        val assetCacheManager = TestAssetCacheManager(pathProvider)
        val loader = TestChapterGraphLoader(assetCacheManager, pathProvider)

        val graph = loader.load(extractDir.absolutePath, gameId = "game1")

        assertEquals("chapter-1", graph.chapterCode)
        assertNotNull(graph.getNode("start-0"))
        assertNotNull(graph.getNode("message-3"))
        assertTrue("ignore node should be removed", graph.getNode("ignore-2") == null)

        val narrationEdges = graph.getNextEdges("narration-1")
        assertEquals(1, narrationEdges.size)
        assertEquals("message-3", narrationEdges.first().target)

        val messageNode = graph.getNode("message-3") as? Node.Message
        assertNotNull(messageNode)
        assertEquals("Hello", messageNode?.text)
        assertEquals(1, messageNode?.characterId)
    }

    @Test
    fun `loads graph when manifest is wrapped in a single-element array`() {
        val extractDir = tempFolder.newFolder("extracted-array")
        tempFolder.newFolder("games", "game1")

        val manifest = TestPackageManifestDto(
            seed = 2,
            chapterId = "chapter-array",
            storyId = "story-1",
            updatedAt = "2024-01-01T00:00:00Z",
            nodes = listOf(
                nodeDto("start-0", "start"),
                nodeDto("message-1", "message", text = "Hi", characterId = 1),
                nodeDto("end-2", "end")
            ),
            edges = listOf(
                edgeDto("start-0", "message-1"),
                edgeDto("message-1", "end-2")
            ),
            assetInventory = emptyList()
        )

        File(extractDir, "manifest.json").writeText(gson.toJson(listOf(manifest)))

        val pathProvider = FakeAndroidGamePathProvider(tempFolder.root)
        val assetCacheManager = TestAssetCacheManager(pathProvider)
        val loader = TestChapterGraphLoader(assetCacheManager, pathProvider)

        val graph = loader.load(extractDir.absolutePath, gameId = "game1")
        assertEquals("chapter-array", graph.chapterCode)
        assertNotNull(graph.getNode("message-1"))
    }

    @Test
    fun `resolves message-image asset paths from test cache as absolute path`() {
        val extractDir = tempFolder.newFolder("extracted")
        val rootDir = tempFolder.root
        val gameDir = File(rootDir, "games/game1").apply { mkdirs() }
        // copyAssets flattens files from the ZIP's assets/ directory into test-assets/.
        val testAssetsDir = File(gameDir, "test-assets").apply { mkdirs() }
        File(testAssetsDir, "cached-image.png").writeText("png")

        val manifest = TestPackageManifestDto(
            seed = 1,
            chapterId = "chapter-1",
            storyId = "story-1",
            updatedAt = "2024-01-01T00:00:00Z",
            nodes = listOf(
                nodeDto("start-0", "start"),
                nodeDto(
                    "image-1",
                    "message-image",
                    storagePath = "assets/cached-image.png"
                ),
                nodeDto("end-2", "end")
            ),
            edges = listOf(
                edgeDto("start-0", "image-1"),
                edgeDto("image-1", "end-2")
            ),
            assetInventory = listOf(AssetInventoryItemDto("assets/cached-image.png"))
        )

        File(extractDir, "manifest.json").writeText(gson.toJson(manifest))

        val pathProvider = FakeAndroidGamePathProvider(rootDir)
        val assetCacheManager = TestAssetCacheManager(pathProvider)
        val loader = TestChapterGraphLoader(assetCacheManager, pathProvider)

        val graph = loader.load(extractDir.absolutePath, gameId = "game1")
        val imageNode = graph.getNode("image-1") as? Node.MessageImage

        assertNotNull(imageNode)
        assertEquals(
            File(testAssetsDir, "cached-image.png").absolutePath,
            imageNode?.imageUrl
        )
    }

    @Test
    fun `falls back to installed story assets when test cache misses`() {
        val extractDir = tempFolder.newFolder("extracted")
        val rootDir = tempFolder.root
        val gameDir = File(rootDir, "games/game1").apply { mkdirs() }
        val originalAssetsDir = File(gameDir, "assets").apply { mkdirs() }
        File(originalAssetsDir, "delta-only.webp").writeText("webp")

        val manifest = TestPackageManifestDto(
            seed = 1,
            chapterId = "chapter-1",
            storyId = "story-1",
            updatedAt = "2024-01-01T00:00:00Z",
            nodes = listOf(
                nodeDto("start-0", "start"),
                nodeDto(
                    "image-1",
                    "message-image",
                    storagePath = "assets/delta-only.webp"
                ),
                nodeDto("end-2", "end")
            ),
            edges = listOf(
                edgeDto("start-0", "image-1"),
                edgeDto("image-1", "end-2")
            ),
            assetInventory = listOf(AssetInventoryItemDto("assets/delta-only.webp"))
        )

        File(extractDir, "manifest.json").writeText(gson.toJson(manifest))

        val pathProvider = FakeAndroidGamePathProvider(rootDir)
        val assetCacheManager = TestAssetCacheManager(pathProvider)
        val loader = TestChapterGraphLoader(assetCacheManager, pathProvider)

        val graph = loader.load(extractDir.absolutePath, gameId = "game1")
        val imageNode = graph.getNode("image-1") as? Node.MessageImage

        assertNotNull(imageNode)
        assertEquals(
            File(originalAssetsDir, "delta-only.webp").absolutePath,
            imageNode?.imageUrl
        )
    }

    private fun nodeDto(
        id: String,
        type: String,
        label: String? = null,
        text: String? = null,
        characterId: Int? = null,
        storagePath: String? = null
    ): com.purpletear.game.data.local.dto.NodeDto {
        val data = JsonObject().apply {
            label?.let { addProperty("label", it) }
            text?.let { addProperty("text", it) }
            characterId?.let { addProperty("characterId", it) }
            storagePath?.let { addProperty("storagePath", it) }
        }
        return com.purpletear.game.data.local.dto.NodeDto(
            id = id,
            type = type,
            data = data
        )
    }

    private fun edgeDto(source: String, target: String): com.purpletear.game.data.local.dto.EdgeDto {
        return com.purpletear.game.data.local.dto.EdgeDto(
            source = source,
            target = target
        )
    }

    private class FakeAndroidGamePathProvider(private val rootDir: File) : AndroidGamePathProvider {
        override fun getStoriesDirectoryPath(): String = File(rootDir, "games").absolutePath
        override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String =
            File(getGamesDirectory(), legacyId?.toString() ?: storyId).absolutePath
        override fun getGamesDirectory(): File = File(rootDir, "games").apply { mkdirs() }
        override fun getGameDirectory(gameId: String, legacyId: Int?): File =
            File(getGamesDirectory(), legacyId?.toString() ?: gameId).apply { mkdirs() }
    }
}
