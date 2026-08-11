package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ChapterGraphParserMediaNodeTest {

    private val gson = Gson()

    @Test
    fun `manga-page node parses asset image path messages and timing`() {
        val message = JsonObject().apply {
            addProperty("sentence", "Au revoir [prenom], sache que tu ne seras jamais seul")
            addProperty("size", 28)
            addProperty("x", 74.3)
            addProperty("y", 30.4)
            addProperty("w", 22)
        }
        val data = JsonObject().apply {
            addProperty("assetId", 3887)
            addProperty("storagePath", "fc81f14a-484b-43d3-82fd-405774d9f1e3.webp")
            addProperty("name", "manga_page_bestfrien")
            add("messages", JsonArray().apply { add(message) })
            addProperty("delay", 0)
            addProperty("duration", 0)
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("fxba4BVO3ul-1A-311", "manga-page", data = data)
        )
        val edges = listOf(edge("start-0", "fxba4BVO3ul-1A-311"))

        val graph = parseGraph(nodes, edges)

        val page = graph.getNode("fxba4BVO3ul-1A-311") as? Node.MangaPage
        assertNotNull(page)
        page!!
        assertEquals(
            "/tmp/games/game1/assets/fc81f14a-484b-43d3-82fd-405774d9f1e3.webp",
            page.imageUrl
        )
        assertEquals(3887, page.assetId)
        assertEquals(1, page.messages.size)
        val parsed = page.messages.first()
        // [prenom] is kept raw here; substitution happens in the handler.
        assertEquals("Au revoir [prenom], sache que tu ne seras jamais seul", parsed.text)
        assertEquals(28f, parsed.size, 0.001f)
        assertEquals(74.3f, parsed.x, 0.001f)
        assertEquals(30.4f, parsed.y, 0.001f)
        assertEquals(22f, parsed.w, 0.001f)
    }

    @Test
    fun `manga-page applies legacy defaults and drops blank messages`() {
        val blank = JsonObject().apply {
            addProperty("sentence", "   ")
            addProperty("size", 40)
        }
        val good = JsonObject().apply { addProperty("sentence", "Hi") }
        val data = JsonObject().apply {
            addProperty("storagePath", "page.webp")
            add("messages", JsonArray().apply { add(blank); add(good) })
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("manga-1", "manga-page", data = data)
        )
        val edges = listOf(edge("start-0", "manga-1"))

        val graph = parseGraph(nodes, edges)

        val page = graph.getNode("manga-1") as Node.MangaPage
        assertEquals(1, page.messages.size)
        val parsed = page.messages.first()
        assertEquals("Hi", parsed.text)
        assertEquals(30f, parsed.size, 0.001f) // default size
        assertEquals(1f, parsed.x, 0.001f)     // default x
        assertEquals(1f, parsed.y, 0.001f)     // default y
        assertEquals(10f, parsed.w, 0.001f)    // default w
    }

    @Test
    fun `manga-page without assetFileName fails fast`() {
        val data = JsonObject().apply {
            add("messages", JsonArray().apply {
                add(JsonObject().apply { addProperty("sentence", "Hi") })
            })
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("manga-1", "manga-page", data = data)
        )
        val edges = listOf(edge("start-0", "manga-1"))

        assertThrows(IllegalArgumentException::class.java) {
            parseGraph(nodes, edges)
        }
    }

    @Test
    fun `fake-notification node is parsed with resolved image path and timings`() {
        val data = gson.fromJson(
            """{
              "title":"Kelly J.",
              "imageName":"kelly.jpeg",
              "subtitle":"Tu me manques ❤️",
              "actionText":"Votre petite amie",
              "storagePath": "./kelly.jpeg",
              "characterId":124,
              "delay":1250,
              "duration":6000,
              "isAutoTiming":false,
              "isHesitating":false
            }""",
            JsonObject::class.java
        )
        val nodes = listOf(
            node("start-0", "start"),
            node("WMPmUkArG6d-1A-193", "fake-notification", data = data)
        )
        val edges = listOf(edge("start-0", "WMPmUkArG6d-1A-193"))

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        val node = graph.getNode("WMPmUkArG6d-1A-193") as Node.FakeNotification
        assertEquals("Kelly J.", node.title)
        assertEquals("Tu me manques ❤️", node.subtitle)
        assertEquals("Votre petite amie", node.actionText)
        assertEquals("/tmp/games/game1/assets/kelly.jpeg", node.imageUrl)
        assertEquals(124, node.characterId)
        assertEquals(1250, node.delayMs)
        assertEquals(6000, node.durationMs)
        assertEquals(false, node.isAutoTiming)
        assertEquals(false, node.isHesitating)
    }
}
