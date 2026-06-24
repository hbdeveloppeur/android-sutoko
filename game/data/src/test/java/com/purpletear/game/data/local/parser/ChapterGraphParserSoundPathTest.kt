package com.purpletear.game.data.local.parser

import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ChapterGraphParserSoundPathTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun parseVocalNode(
        assetName: String,
        createFiles: List<String> = emptyList()
    ): Node.MessageVocal {
        val gameId = "0AZY0NtFQKu"
        val storyDir = temporaryFolder.newFolder("games", gameId)
        val assetsDir = File(storyDir, "assets").also { it.mkdirs() }
        val legacySoundsDir = File(storyDir, "medias/sounds").also { it.mkdirs() }

        createFiles.forEach { relativePath ->
            val file = File(storyDir, relativePath)
            file.parentFile?.mkdirs()
            file.createNewFile()
        }

        val pathProvider = object : GamePathProvider {
            override fun getStoriesDirectoryPath(): String =
                temporaryFolder.root.resolve("games").absolutePath

            override fun getStoryDirectoryPath(storyId: String): String =
                getStoriesDirectoryPath() + File.separator + storyId
        }

        val data = JsonObject().apply {
            addProperty("assetName", assetName)
            addProperty("characterId", 83)
        }

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = listOf(NodeDto(id = "vocal-1", type = "message-vocal", data = data)),
            edgeDtos = emptyList(),
            gameId = gameId,
            pathProvider = pathProvider
        )

        return graph.getNode("vocal-1") as Node.MessageVocal
    }

    @Test
    fun `resolveSoundPath returns assets path when file exists in assets`() {
        val node = parseVocalNode(
            assetName = "snow_walking.mp3",
            createFiles = listOf("assets/snow_walking.mp3")
        )

        assertEquals("snow_walking.mp3", File(node.audioUrl).name)
        assertEquals("assets", File(node.audioUrl).parentFile?.name)
    }

    @Test
    fun `resolveSoundPath falls back to legacy medias-sounds when assets file is missing`() {
        val node = parseVocalNode(
            assetName = "snow_walking.mp3",
            createFiles = listOf("medias/sounds/snow_walking.mp3")
        )

        assertEquals("snow_walking.mp3", File(node.audioUrl).name)
        assertEquals("sounds", File(node.audioUrl).parentFile?.name)
        assertEquals("medias", File(node.audioUrl).parentFile?.parentFile?.name)
    }

    @Test
    fun `resolveSoundPath prefers assets over legacy when both exist`() {
        val node = parseVocalNode(
            assetName = "snow_walking.mp3",
            createFiles = listOf(
                "assets/snow_walking.mp3",
                "medias/sounds/snow_walking.mp3"
            )
        )

        assertEquals("assets", File(node.audioUrl).parentFile?.name)
    }

    @Test
    fun `resolveSoundPath preserves subdirectories in assetName`() {
        val node = parseVocalNode(
            assetName = "vocals/snow_walking.mp3",
            createFiles = listOf("assets/vocals/snow_walking.mp3")
        )

        assertEquals("snow_walking.mp3", File(node.audioUrl).name)
        assertEquals("vocals", File(node.audioUrl).parentFile?.name)
    }

    @Test
    fun `resolveSoundPath tries extensions when assetName has no extension`() {
        val node = parseVocalNode(
            assetName = "snow_walking",
            createFiles = listOf("assets/snow_walking.ogg")
        )

        assertEquals("snow_walking.ogg", File(node.audioUrl).name)
    }

    @Test
    fun `resolveSoundPath defaults to primary path when no file exists`() {
        val node = parseVocalNode(assetName = "snow_walking.mp3")

        assertEquals("assets", File(node.audioUrl).parentFile?.name)
        assertEquals("snow_walking.mp3", File(node.audioUrl).name)
    }
}
