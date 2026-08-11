package com.purpletear.game.data.local.parser

import com.google.gson.JsonArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterGraphParserIgnoreNodeTest {

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

        val graph = parseGraph(nodes, edges)

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

        val graph = parseGraph(nodes, edges)

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

        val graph = parseGraph(nodes, edges)

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

        val graph = parseGraph(nodes, edges)

        assertNull(graph.getNode("ignore-2"))
        assertTrue(graph.getNextEdges("narration-1").isEmpty())
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

        val graph = parseGraph(nodes, edges)

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

        val graph = parseGraph(nodes, edges)

        assertNull(graph.getNode("ignore-2"))
        assertEquals("message-3", graph.getNextEdges("narration-1").first().target)
    }
}
