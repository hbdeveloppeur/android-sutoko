package com.purpletear.game.data.local.parser

import com.google.gson.JsonObject
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Test

class ChapterGraphParserSoundDelayTest {

    private fun parseSound(data: JsonObject): Node.Sound =
        parseGraph(nodes = listOf(node(id = "s1", type = "sound", data = data)), edges = emptyList())
            .getNode("s1") as Node.Sound

    @Test
    fun `sound node maps optional delay to delayMs`() {
        val data = JsonObject().apply {
            addProperty("storagePath", "assets/s.mp3")
            addProperty("delay", 1500)
        }
        assertEquals(1500L, parseSound(data).delayMs)
    }

    @Test
    fun `sound node delay defaults to zero when absent`() {
        val data = JsonObject().apply { addProperty("storagePath", "assets/s.mp3") }
        assertEquals(0L, parseSound(data).delayMs)
    }

    @Test
    fun `sound node negative delay is clamped to zero`() {
        val data = JsonObject().apply {
            addProperty("storagePath", "assets/s.mp3")
            addProperty("delay", -10)
        }
        assertEquals(0L, parseSound(data).delayMs)
    }
}
