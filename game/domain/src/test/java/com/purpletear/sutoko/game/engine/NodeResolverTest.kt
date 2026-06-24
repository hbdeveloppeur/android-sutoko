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
            forceNodId = "explicit_next"
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
            chapterCode = "2A"
        )

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = chapterChangeNode,
            forceNodId = null
        )

        assertTrue(result is NodeResolver.ResolutionResult.NodeNextChapter)
    }

    @Test
    fun `no edges - should complete chapter`() {
        val graph = createGraph(emptyList()) // No edges
        val currentNode = createMessageNode("msg1")

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = currentNode,
            forceNodId = null
        )

        assertTrue(result is NodeResolver.ResolutionResult.NodeNextChapter)
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
            forceNodId = null
        )

        assertEquals(
            NodeResolver.ResolutionResult.NextNode("msg2"),
            result
        )
    }

    @Test
    fun `multiple outgoing edges to message nodes - should await choice`() {
        val edges = listOf(
            Edge(source = "msg1", target = "choiceA", type = EdgeType.NORMAL),
            Edge(source = "msg1", target = "choiceB", type = EdgeType.NORMAL)
        )
        val nodes = mapOf(
            "choiceA" to createMessageNode("choiceA", "Option A"),
            "choiceB" to createMessageNode("choiceB", "Option B")
        )
        val graph = createGraph(edges, nodes)
        val currentNode = createMessageNode("msg1")

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = currentNode,
            forceNodId = null
        )

        assertTrue(result is NodeResolver.ResolutionResult.AwaitChoice)
        val choices = (result as NodeResolver.ResolutionResult.AwaitChoice).choices
        assertEquals(2, choices.size)
        assertEquals("Option A", choices[0].text)
        assertEquals("choiceA", choices[0].nextNodeId)
        assertEquals("Option B", choices[1].text)
        assertEquals("choiceB", choices[1].nextNodeId)
    }

    @Test
    fun `single outgoing edge to message node - should navigate directly`() {
        val edges = listOf(
            Edge(source = "msg1", target = "msg2", type = EdgeType.NORMAL)
        )
        val nodes = mapOf(
            "msg2" to createMessageNode("msg2")
        )
        val graph = createGraph(edges, nodes)
        val currentNode = createMessageNode("msg1")

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = currentNode,
            forceNodId = null
        )

        assertEquals(
            NodeResolver.ResolutionResult.NextNode("msg2"),
            result
        )
    }

    @Test
    fun `mixed outgoing edges - only message targets are considered choices`() {
        val edges = listOf(
            Edge(source = "msg1", target = "choiceA", type = EdgeType.NORMAL),
            Edge(source = "msg1", target = "scene1", type = EdgeType.NORMAL),
            Edge(source = "msg1", target = "choiceB", type = EdgeType.NORMAL)
        )
        val nodes = mapOf(
            "choiceA" to createMessageNode("choiceA", "Option A"),
            "choiceB" to createMessageNode("choiceB", "Option B"),
            "scene1" to Node.Scene(id = "scene1", sceneId = 1)
        )
        val graph = createGraph(edges, nodes)
        val currentNode = createMessageNode("msg1")

        val result = resolver.resolveNextNode(
            graph = graph,
            currentNode = currentNode,
            forceNodId = null
        )

        assertTrue(result is NodeResolver.ResolutionResult.AwaitChoice)
        val choices = (result as NodeResolver.ResolutionResult.AwaitChoice).choices
        assertEquals(2, choices.size)
    }

    private fun createGraph(
        edges: List<Edge> = emptyList(),
        nodes: Map<String, Node> = emptyMap()
    ): ChapterGraph {
        return ChapterGraph(
            chapterCode = "1A",
            title = "Test Chapter",
            nodes = nodes,
            edges = edges,
            startNodeId = "start"
        )
    }

    private fun createMessageNode(id: String, text: String = "Test message"): Node.Message {
        return Node.Message(
            id = id,
            text = text,
            characterId = 1
        )
    }
}
