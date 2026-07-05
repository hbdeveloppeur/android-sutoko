package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeData
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConditionNodeHandlerTest {

    private val handler = ConditionNodeHandler()
    private val memory = createFakeGameMemory()

    @Test
    fun `condition true - should follow ConditionTrue edge`() {
        memory.set("x", "1")
        val graph = graphWithConditionEdges()

        val script = handler.buildScript(graph.getNode("condition-1")!!, memory, graph)

        assertTrue(script.commands.isEmpty())
        assertEquals("msg-true", script.nextNodeId)
    }

    @Test
    fun `condition false - should follow ConditionFalse edge`() {
        memory.set("x", "2")
        val graph = graphWithConditionEdges()

        val script = handler.buildScript(graph.getNode("condition-1")!!, memory, graph)

        assertTrue(script.commands.isEmpty())
        assertEquals("msg-false", script.nextNodeId)
    }

    @Test
    fun `missing matching branch edge - should return empty script`() {
        memory.set("x", "1")
        val graph = ChapterGraph(
            chapterCode = "test",
            title = "Test",
            nodes = mapOf(
                "condition-1" to Node.Condition(id = "condition-1", expression = "x == 1"),
                "msg-false" to Node.Message(id = "msg-false", text = "False", characterId = 1)
            ),
            edges = listOf(
                Edge(
                    source = "condition-1",
                    target = "msg-false",
                    type = EdgeType.CONDITIONAL,
                    data = EdgeData(edgeType = "ConditionFalse")
                )
            ),
            startNodeId = "condition-1"
        )

        val script = handler.buildScript(graph.getNode("condition-1")!!, memory, graph)

        assertTrue(script.commands.isEmpty())
        assertNull(script.nextNodeId)
    }

    @Test
    fun `wrong node type - should return empty script`() {
        val graph = ChapterGraph(
            chapterCode = "test",
            title = "Test",
            nodes = mapOf("msg-1" to Node.Message(id = "msg-1", text = "hello", characterId = 1)),
            edges = emptyList(),
            startNodeId = "msg-1"
        )

        val script = handler.buildScript(graph.getNode("msg-1")!!, memory, graph)

        assertTrue(script.commands.isEmpty())
        assertNull(script.nextNodeId)
    }

    private fun graphWithConditionEdges(): ChapterGraph {
        return ChapterGraph(
            chapterCode = "test",
            title = "Test",
            nodes = mapOf(
                "condition-1" to Node.Condition(id = "condition-1", expression = "x == 1"),
                "msg-true" to Node.Message(id = "msg-true", text = "True", characterId = 1),
                "msg-false" to Node.Message(id = "msg-false", text = "False", characterId = 1)
            ),
            edges = listOf(
                Edge(
                    source = "condition-1",
                    target = "msg-true",
                    type = EdgeType.CONDITIONAL,
                    data = EdgeData(edgeType = "ConditionTrue")
                ),
                Edge(
                    source = "condition-1",
                    target = "msg-false",
                    type = EdgeType.CONDITIONAL,
                    data = EdgeData(edgeType = "ConditionFalse")
                )
            ),
            startNodeId = "condition-1"
        )
    }
}
