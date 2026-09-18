package com.purpletear.game.data.local.parser

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterGraphParserBlankNodeTest {

    @Test
    fun `narration node with empty text is bypassed and its incoming edge is retargeted`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "Narration"),
            node("narration-2", "narration", text = ""),
            node("message-3", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "narration-2"),
            edge("narration-2", "message-3")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        assertNull("blank narration should be removed", graph.getNode("narration-2"))
        assertEquals("message-3", graph.getNextEdges("narration-1").first().target)
    }

    @Test
    fun `narration node with whitespace-only text is bypassed`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "   "),
            node("message-2", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "message-2")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        assertNull(graph.getNode("narration-1"))
        assertEquals("message-2", graph.getNextEdges("start-0").first().target)
    }

    @Test
    fun `narration node with non-object data is bypassed`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", data = JsonArray()),
            node("message-2", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "message-2")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        assertNull(graph.getNode("narration-1"))
        assertEquals("message-2", graph.getNextEdges("start-0").first().target)
    }

    @Test
    fun `blank narration feeding a choice fanout is spliced so choices stay reachable`() {
        val nodes = listOf(
            node("start-0", "start"),
            node(
                "moon-1",
                "message",
                text = "On a juste une vieille radio, mais on ne l'a jamais utilisée",
                characterId = 1
            ),
            node("narration-2", "narration", text = ""),
            node("choice-a", "message", text = "Merci Moon pour votre accueil", characterId = 0),
            node("choice-b", "message", text = "On mange bientôt?", characterId = 0),
            node("choice-c", "message", text = "Troisième option", characterId = 0)
        )
        val edges = listOf(
            edge("start-0", "moon-1"),
            edge("moon-1", "narration-2"),
            edge("narration-2", "choice-a"),
            edge("narration-2", "choice-b"),
            edge("narration-2", "choice-c")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "3")

        assertNull("blank narration should be removed", graph.getNode("narration-2"))
        assertEquals(
            "choices must be re-attached to the message feeding the blank narration",
            setOf("choice-a", "choice-b", "choice-c"),
            graph.getNextEdges("moon-1").map { it.target }.toSet()
        )
    }

    @Test
    fun `blank narration chain reaching a choice fanout is spliced transitively`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "Narration"),
            node("narration-2", "narration", text = ""),
            node("choice-a", "message", text = "A", characterId = 0),
            node("choice-b", "message", text = "B", characterId = 0)
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "narration-2"),
            edge("narration-2", "choice-a"),
            edge("narration-2", "choice-b")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "3")

        assertNull(graph.getNode("narration-2"))
        assertEquals(
            setOf("choice-a", "choice-b"),
            graph.getNextEdges("narration-1").map { it.target }.toSet()
        )
    }

    @Test
    fun `chained blank narration nodes are all bypassed transitively`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = ""),
            node("narration-2", "narration", text = ""),
            node("message-3", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "narration-2"),
            edge("narration-2", "message-3")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        assertNull(graph.getNode("narration-1"))
        assertNull(graph.getNode("narration-2"))
        val startEdges = graph.getNextEdges("start-0")
        assertEquals(1, startEdges.size)
        assertEquals("message-3", startEdges.first().target)
    }

    @Test
    fun `blank narration node with no outgoing edge drops incoming edges`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "Narration"),
            node("narration-2", "narration", text = "")
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "narration-2")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        assertNull(graph.getNode("narration-2"))
        assertTrue(graph.getNextEdges("narration-1").isEmpty())
    }

    @Test
    fun `message node with empty text and multiple outgoing edges is kept so choices stay reachable`() {
        val nodes = listOf(
            node("start-0", "start"),
            node(
                "message-1",
                "message",
                text = "Il suffit de regarder ta photo de profil",
                characterId = 1
            ),
            node("message-2", "message", text = "", characterId = 2),
            node("choice-a", "message", text = "Tant pis, j'aurai essaye", characterId = 2),
            node("choice-b", "message", text = "Tu me juges?", characterId = 2),
            node(
                "choice-c",
                "message",
                text = "Je ne comptais pas coucher avec toi ce soir",
                characterId = 2
            )
        )
        val edges = listOf(
            edge("start-0", "message-1"),
            edge("message-1", "message-2"),
            edge("message-2", "choice-a"),
            edge("message-2", "choice-b"),
            edge("message-2", "choice-c")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        val hub = graph.getNode("message-2") as Node.Message
        assertEquals("", hub.text)
        assertEquals("message-2", graph.getNextEdges("message-1").single().target)
        assertEquals(
            setOf("choice-a", "choice-b", "choice-c"),
            graph.getNextEdges("message-2").map { it.target }.toSet()
        )
    }

    @Test
    fun `message node with missing text and multiple outgoing edges is kept`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("message-1", "message", data = JsonObject(), characterId = 2),
            node("choice-a", "message", text = "A", characterId = 2),
            node("choice-b", "message", text = "B", characterId = 2)
        )
        val edges = listOf(
            edge("start-0", "message-1"),
            edge("message-1", "choice-a"),
            edge("message-1", "choice-b")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        val hub = graph.getNode("message-1") as Node.Message
        assertEquals("", hub.text)
        assertEquals(2, graph.getNextEdges("message-1").size)
    }

    @Test
    fun `message node with empty text is bypassed and its incoming edge is retargeted`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("narration-1", "narration", text = "Narration"),
            node("message-2", "message", text = "", characterId = 1),
            node("message-3", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "narration-1"),
            edge("narration-1", "message-2"),
            edge("message-2", "message-3")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        assertNull("blank message should be removed", graph.getNode("message-2"))
        assertEquals("message-3", graph.getNextEdges("narration-1").first().target)
    }

    @Test
    fun `message node with missing text is bypassed`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("message-1", "message", data = JsonObject()),
            node("message-2", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "message-1"),
            edge("message-1", "message-2")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        assertNull(graph.getNode("message-1"))
        assertEquals("message-2", graph.getNextEdges("start-0").first().target)
    }

    @Test
    fun `intro-sentence node with blank text is bypassed`() {
        val nodes = listOf(
            node("start-0", "start"),
            node("intro-1", "intro-sentence", text = "  "),
            node("message-2", "message", text = "Hello", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "intro-1"),
            edge("intro-1", "message-2")
        )

        val graph = parseGraph(nodes, edges, chapterCode = "1a")

        assertNull(graph.getNode("intro-1"))
        assertEquals("message-2", graph.getNextEdges("start-0").first().target)
    }
}
