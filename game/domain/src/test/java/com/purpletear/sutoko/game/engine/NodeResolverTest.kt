package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeResolverTest {

    private val resolver = NodeResolver()

    @Test
    fun `handler returns explicit next node - should use it`() {
        val graph = createGraph()
        val currentNode = createMessageNode("msg1")

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = currentNode,
            handlerResult = "explicit_next"
        )

        assertEquals(
            NodeResolver.ResolutionResult.NextNode("explicit_next"),
            result
        )
    }

    @Test
    fun `chapter change node - should complete chapter`() {
        val graph = createGraph()
        val chapterChangeNode = Node.ChapterChange(
            id = "end",
            position = Node.Position(0f, 0f),
            chapterCode = "2A"
        )

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = chapterChangeNode,
            handlerResult = null
        )

        assertTrue(result is NodeResolver.ResolutionResult.ChapterComplete)
    }

    @Test
    fun `no edges - should complete chapter`() {
        val graph = createGraph(emptyList()) // No edges
        val currentNode = createMessageNode("msg1")

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = currentNode,
            handlerResult = null
        )

        assertTrue(result is NodeResolver.ResolutionResult.ChapterComplete)
    }

    @Test
    fun `has edges - should navigate to first target`() {
        val edges = listOf(
            Edge(source = "msg1", target = "msg2", type = EdgeType.NORMAL)
        )
        val graph = createGraph(edges)
        val currentNode = createMessageNode("msg1")

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = currentNode,
            handlerResult = null
        )

        assertEquals(
            NodeResolver.ResolutionResult.NextNode("msg2"),
            result
        )
    }

    private fun createGraph(edges: List<Edge> = emptyList()): ChapterGraph {
        return ChapterGraph(
            chapterCode = "1A",
            title = "Test Chapter",
            nodes = emptyMap(),
            edges = edges,
            startNodeId = "start"
        )
    }

    private fun createMessageNode(id: String): Node.Message {
        return Node.Message(
            id = id,
            position = Node.Position(0f, 0f),
            text = "Test message",
            characterId = 1
        )
    }
}
