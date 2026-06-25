package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.provider.GamePathProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun `condition node target pointing to ignore node is rewritten`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("condition-1", "condition", expression = "x == 1", trueTargetId = "ignore-2", falseTargetId = "msg-b"),
            node("ignore-2", "ignore", data = JsonArray()),
            node("msg-a", "message", text = "A", characterId = 1),
            node("msg-b", "message", text = "B", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "condition-1"),
            edge("ignore-2", "msg-a")
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

        val condition = graph.getNode("condition-1") as Node.Condition
        assertEquals("msg-a", condition.trueTargetId)
        assertEquals("msg-b", condition.falseTargetId)
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

    private fun node(
        id: String,
        type: String,
        text: String? = null,
        characterId: Int? = null,
        expression: String? = null,
        trueTargetId: String? = null,
        falseTargetId: String? = null,
        data: JsonElement? = null
    ): NodeDto {
        val dataObject = data as? JsonObject ?: JsonObject().apply {
            text?.let { addProperty("text", it) }
            characterId?.let { addProperty("characterId", it) }
            expression?.let { addProperty("expression", it) }
            trueTargetId?.let { addProperty("trueTargetId", it) }
            falseTargetId?.let { addProperty("falseTargetId", it) }
        }
        return NodeDto(id = id, type = type, data = dataObject)
    }

    private fun edge(source: String, target: String, type: String? = "futuristic"): EdgeDto {
        val json = buildString {
            append("{\"source\":\"$source\",\"target\":\"$target\"")
            if (type != null) append(",\"type\":\"$type\"")
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
