package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.IntroAlignment
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterGraphParserTest {

    private val gson = Gson()
    private val pathProvider = FakeGamePathProvider()

    @Test
    fun `ignore node is removed and its incoming edge is retargeted to its outgoing target`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "Narration"),
            node("ignore-2", "ignore", data = JsonArray()),
            node("message-3", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "ignore-2"),
            edge("ignore-2", "message-3")
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        assertNull("ignore node should be removed", graph.getNode("ignore-2"))
        assertNotNull(graph.getNode("message-3"))

        val narrationEdges = graph.getNextEdges("narration-1")
        assertEquals(1, narrationEdges.size)
        assertEquals("message-3", narrationEdges.first().target)

        assertTrue(graph.getNextEdges("ignore-2").isEmpty())
    }

    @Test
    fun `multiple incoming edges to an ignore node are all retargeted`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("msg-a", "message", text = "A", characterId = 1),
            node("msg-b", "message", text = "B", characterId = 1),
            node("ignore-1", "ignore", data = JsonArray()),
            node("msg-c", "message", text = "C", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "msg-a"),
            edge("start-0", "msg-b"),
            edge("msg-a", "ignore-1"),
            edge("msg-b", "ignore-1"),
            edge("ignore-1", "msg-c")
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        assertNull(graph.getNode("ignore-1"))
        assertEquals(
            setOf("msg-c"),
            graph.getNextEdges("msg-a").map { it.target }.toSet()
        )
        assertEquals(
            setOf("msg-c"),
            graph.getNextEdges("msg-b").map { it.target }.toSet()
        )
    }

    @Test
    fun `ignore node with no outgoing edge drops incoming edges`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "Narration"),
            node("ignore-2", "ignore", data = JsonArray())
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "ignore-2")
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        assertNull(graph.getNode("ignore-2"))
        assertTrue(graph.getNextEdges("narration-1").isEmpty())
    }

    @Test
    fun `ignore node with multiple outgoing edges drops incoming edges`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "Narration"),
            node("ignore-2", "ignore", data = JsonArray()),
            node("msg-a", "message", text = "A", characterId = 1),
            node("msg-b", "message", text = "B", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "ignore-2"),
            edge("ignore-2", "msg-a"),
            edge("ignore-2", "msg-b")
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        assertNull(graph.getNode("ignore-2"))
        assertTrue(graph.getNextEdges("narration-1").isEmpty())
    }

    @Test
    fun `condition node branches are preserved as conditional edges`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("condition-1", "condition", expression = "x == 1"),
            node("msg-a", "message", text = "A", characterId = 1),
            node("msg-b", "message", text = "B", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "condition-1"),
            edge("condition-1", "msg-a", edgeType = "ConditionTrue"),
            edge("condition-1", "msg-b", edgeType = "ConditionFalse")
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        val conditionEdges = graph.getNextEdges("condition-1")
        assertEquals(2, conditionEdges.size)
        assertEquals(
            mapOf(true to "msg-a", false to "msg-b"),
            conditionEdges.associate {
                (it.data?.edgeType == "ConditionTrue") to it.target
            }
        )
    }

    @Test
    fun `conditional edge pointing to ignore node is retargeted`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("condition-1", "condition", expression = "x == 1"),
            node("ignore-2", "ignore", data = JsonArray()),
            node("msg-a", "message", text = "A", characterId = 1),
            node("msg-b", "message", text = "B", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "condition-1"),
            edge("condition-1", "ignore-2", edgeType = "ConditionTrue"),
            edge("ignore-2", "msg-a"),
            edge("condition-1", "msg-b", edgeType = "ConditionFalse")
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        assertNull(graph.getNode("ignore-2"))
        val conditionEdges = graph.getNextEdges("condition-1")
        assertEquals(2, conditionEdges.size)
        assertEquals(
            setOf("msg-a", "msg-b"),
            conditionEdges.map { it.target }.toSet()
        )
    }

    @Test
    fun `graph without ignore nodes is unchanged`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("msg-1", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "msg-1")
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        assertEquals(2, graph.nodes.size)
        assertEquals(1, graph.edges.size)
        assertEquals("msg-1", graph.edges.first().target)
    }

    @Test
    fun `ignore node bypass works when edge type is null`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "Narration"),
            node("ignore-2", "ignore", data = JsonArray()),
            node("message-3", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "narration-1", type = null),
            edge("narration-1", "ignore-2", type = null),
            edge("ignore-2", "message-3", type = null)
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        assertNull(graph.getNode("ignore-2"))
        assertEquals("message-3", graph.getNextEdges("narration-1").first().target)
    }

    @Test
    fun `code node parses to Node Code and recognises intro markers`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("code-start", "code", text = "[intro=start]"),
            node("msg-1", "message", text = "Hello", characterId = 1),
            node("code-end", "code", text = "[intro=end]")
        )
        val edges = listOf(
            edge("start-0", "code-start"),
            edge("code-start", "msg-1"),
            edge("msg-1", "code-end")
        )

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        val start = graph.getNode("code-start") as? Node.Code
        assertNotNull(start)
        assertTrue(start!!.isIntroStart)
        assertEquals("[intro=start]", start.sentence)

        val end = graph.getNode("code-end") as? Node.Code
        assertNotNull(end)
        assertTrue(end!!.isIntroEnd)
    }

    @Test
    fun `intro-sentence node parses text alignment delay and duration`() {
        val data = JsonObject().apply {
            addProperty("text", "Once upon a time")
            addProperty("alignment", "top")
            addProperty("delay", 200)
            addProperty("duration", 1500)
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("line-1", "intro-sentence", data = data)
        )
        val edges = listOf(edge("start-0", "line-1"))

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        val sentence = graph.getNode("line-1") as? Node.IntroSentence
        assertNotNull(sentence)
        assertEquals("Once upon a time", sentence!!.text)
        assertEquals(IntroAlignment.TOP, sentence.alignment)
        assertEquals(200, sentence.delayMs)
        assertEquals(1500, sentence.durationMs)
    }

    @Test
    fun `intro-sentence defaults alignment to center and timings to zero`() {
        val data = JsonObject().apply { addProperty("text", "Hi") }
        val nodes = listOf(
            node("start-0", "start"),
            node("line-1", "intro-sentence", data = data)
        )
        val edges = listOf(edge("start-0", "line-1"))

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        val sentence = graph.getNode("line-1") as Node.IntroSentence
        assertEquals(IntroAlignment.CENTER, sentence.alignment)
        assertEquals(0, sentence.delayMs)
        assertEquals(0, sentence.durationMs)
    }

    @Test
    fun `intro-sentence with unknown alignment fails fast`() {
        val data = JsonObject().apply {
            addProperty("text", "Hi")
            addProperty("alignment", "diagonal")
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("line-1", "intro-sentence", data = data)
        )
        val edges = listOf(edge("start-0", "line-1"))

        assertThrows(IllegalArgumentException::class.java) {
            ChapterGraphParser.parse(
                chapterCode = "2a",
                metadata = ChapterMetadataDto(title = "Chapter 2A"),
                nodeDtos = nodes,
                edgeDtos = edges,
                gameId = "game1",
                legacyId = null,
                pathProvider = pathProvider
            )
        }
    }

    @Test
    fun `message-theme node parses background and foreground colors`() {
        val data = JsonObject().apply {
            addProperty("backgroundColor", "#FF2200")
            addProperty("foregroundColor", "#00FF00")
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("theme-1", "message-theme", data = data)
        )
        val edges = listOf(edge("start-0", "theme-1"))

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        val theme = graph.getNode("theme-1") as? Node.MessageTheme
        assertNotNull(theme)
        assertEquals("#FF2200", theme!!.backgroundColor)
        assertEquals("#00FF00", theme.foregroundColor)
    }

    @Test
    fun `message-theme node normalizes blank colors to null`() {
        val data = JsonObject().apply {
            addProperty("backgroundColor", "   ")
            addProperty("foregroundColor", "")
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("theme-1", "message-theme", data = data)
        )
        val edges = listOf(edge("start-0", "theme-1"))

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

        val theme = graph.getNode("theme-1") as? Node.MessageTheme
        assertNotNull(theme)
        assertNull(theme!!.backgroundColor)
        assertNull(theme.foregroundColor)
    }

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
            addProperty("assetFileName", "fc81f14a-484b-43d3-82fd-405774d9f1e3.webp")
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

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

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
            addProperty("assetFileName", "page.webp")
            add("messages", JsonArray().apply { add(blank); add(good) })
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("manga-1", "manga-page", data = data)
        )
        val edges = listOf(edge("start-0", "manga-1"))

        val graph = ChapterGraphParser.parse(
            chapterCode = "2a",
            metadata = ChapterMetadataDto(title = "Chapter 2A"),
            nodeDtos = nodes,
            edgeDtos = edges,
            gameId = "game1",
            legacyId = null,
            pathProvider = pathProvider
        )

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
            ChapterGraphParser.parse(
                chapterCode = "2a",
                metadata = ChapterMetadataDto(title = "Chapter 2A"),
                nodeDtos = nodes,
                edgeDtos = edges,
                gameId = "game1",
                legacyId = null,
                pathProvider = pathProvider
            )
        }
    }

    private fun node(
        id: String,
        type: String,
        text: String? = null,
        characterId: Int? = null,
        expression: String? = null,
        data: JsonElement? = null
    ): NodeDto {
        val dataObject = data as? JsonObject ?: JsonObject().apply {
            text?.let { addProperty("text", it) }
            characterId?.let { addProperty("characterId", it) }
            expression?.let { addProperty("expression", it) }
        }
        return NodeDto(id = id, type = type, data = dataObject)
    }

    private fun edge(
        source: String,
        target: String,
        type: String? = null,
        edgeType: String? = null
    ): EdgeDto {
        val json = buildString {
            append("{\"source\":\"$source\",\"target\":\"$target\"")
            if (type != null) append(",\"type\":\"$type\"")
            if (edgeType != null) append(",\"data\":{\"edgeType\":\"$edgeType\"}")
            append("}")
        }
        return gson.fromJson(json, EdgeDto::class.java)
    }

    private class FakeGamePathProvider : GamePathProvider {
        override fun getStoriesDirectoryPath(): String = "/tmp/games"
        override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String =
            "/tmp/games/${legacyId ?: storyId}"
    }
}
