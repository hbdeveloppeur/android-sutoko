package com.purpletear.game.data.local.parser

import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ChapterGraphParserAudioDialogueTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun parseAudioDialogueNode(data: JsonObject?): Node.MessageAudioDialogue {
        val gameId = "0AZY0NtFQKu"
        val storyDir = temporaryFolder.newFolder("games", gameId)
        File(storyDir, "assets").mkdirs()
        File(storyDir, "medias/sounds").mkdirs()

        val pathProvider = FakeGamePathProvider(temporaryFolder.root.resolve("games").absolutePath)

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = listOf(NodeDto(id = "dialogue-1", type = "message-audio-dialogue", data = data)),
            edgeDtos = emptyList(),
            gameId = gameId,
            legacyId = null,
            pathProvider = pathProvider
        )

        return graph.getNode("dialogue-1") as Node.MessageAudioDialogue
    }

    private fun validData(): JsonObject = JsonObject().apply {
        addProperty("storagePath", "assets/dialogue.mp3")
        addProperty("characterId", 83)
        addProperty("text", "Je t'attendais.")
    }

    @Test
    fun `message-audio-dialogue maps storagePath characterId and text`() {
        val node = parseAudioDialogueNode(validData())

        assertEquals("dialogue-1", node.id)
        assertEquals(83, node.characterId)
        assertEquals("Je t'attendais.", node.text)
        assertEquals("dialogue.mp3", File(node.audioUrl).name)
        assertEquals("assets", File(node.audioUrl).parentFile?.name)
    }

    @Test
    fun `message-audio-dialogue fails when storagePath is missing`() {
        val data = validData().apply { remove("storagePath") }

        assertThrows(IllegalArgumentException::class.java) {
            parseAudioDialogueNode(data)
        }
    }

    @Test
    fun `message-audio-dialogue fails when characterId is missing`() {
        val data = validData().apply { remove("characterId") }

        assertThrows(IllegalArgumentException::class.java) {
            parseAudioDialogueNode(data)
        }
    }

    @Test
    fun `message-audio-dialogue fails when text is missing`() {
        val data = validData().apply { remove("text") }

        assertThrows(IllegalArgumentException::class.java) {
            parseAudioDialogueNode(data)
        }
    }
}
