package com.purpletear.game.data.local.parser

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChapterGraphParserConditionTest {

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

        val graph = parseGraph(nodes, edges)

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
    fun `memory-condition node without expectedValue falls back to memory initial value`() {
        val memoryData = JsonObject().apply {
            add("memory", JsonObject().apply {
                addProperty("name", "Relation Ime")
                addProperty("value", "0")
                addProperty("chapterId", "canvas_1")
            })
        }
        val nodes = listOf(
            node("start-0", "start"),
            node("memory-condition-1", "memory-condition-node", data = memoryData),
            node("msg-a", "message", text = "A", characterId = 1),
            node("msg-b", "message", text = "B", characterId = 1)
        )
        val edges = listOf(
            edge("start-0", "memory-condition-1"),
            edge("memory-condition-1", "msg-a", edgeType = "ConditionTrue"),
            edge("memory-condition-1", "msg-b", edgeType = "ConditionFalse")
        )

        val graph = parseGraph(nodes, edges)

        val condition = graph.getNode("memory-condition-1") as Node.Condition
        assertEquals("Relation Ime == 0", condition.expression)
        assertEquals(2, graph.getNextEdges("memory-condition-1").size)
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

        val graph = parseGraph(nodes, edges)

        assertNull(graph.getNode("ignore-2"))
        val conditionEdges = graph.getNextEdges("condition-1")
        assertEquals(2, conditionEdges.size)
        assertEquals(
            setOf("msg-a", "msg-b"),
            conditionEdges.map { it.target }.toSet()
        )
    }
}
