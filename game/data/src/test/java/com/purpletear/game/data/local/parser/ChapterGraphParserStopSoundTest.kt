package com.purpletear.game.data.local.parser

import com.google.gson.JsonObject
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChapterGraphParserStopSoundTest {

    private fun parseStopSound(data: JsonObject): Node.StopSound? =
        parseGraph(
            nodes = listOf(
                node(id = "start", type = "start"),
                node(id = "stop1", type = "stop-sound", data = data)
            ),
            edges = emptyList()
        ).getNode("stop1") as? Node.StopSound

    @Test
    fun `stop-sound node maps targetNodeId`() {
        val data = JsonObject().apply { addProperty("targetNodeId", "sound-1786241134869") }
        assertEquals("sound-1786241134869", parseStopSound(data)?.targetNodeId)
    }

    @Test
    fun `stop-sound node without targetNodeId is dropped`() {
        assertNull(parseStopSound(JsonObject()))
    }

    @Test
    fun `stop-sound node with blank targetNodeId is dropped`() {
        val data = JsonObject().apply { addProperty("targetNodeId", "  ") }
        assertNull(parseStopSound(data))
    }
}
