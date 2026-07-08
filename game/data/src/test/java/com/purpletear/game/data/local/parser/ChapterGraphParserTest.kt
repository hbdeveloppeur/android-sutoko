package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto
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
