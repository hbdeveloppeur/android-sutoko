package com.purpletear.game.presentation.game_play

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameEngineViewModelStartNodeTest {

    private fun graphWith(vararg nodeIds: String): ChapterGraph = ChapterGraph(
        chapterCode = "chapter-1",
        chapterNumber = 1,
        title = "Chapter 1",
        nodes = nodeIds.associateWith { Node.Start(it) },
        edges = emptyList(),
        startNodeId = nodeIds.firstOrNull().orEmpty(),
    )

    @Test
    fun `resolveStartNodeId - null request returns null`() {
        val graph = graphWith("start")

        assertNull(resolveStartNodeId(graph, requestedNodeId = null))
    }

    @Test
    fun `resolveStartNodeId - existing node returns the requested id`() {
        val graph = graphWith("start", "mid", "end")

        assertEquals("mid", resolveStartNodeId(graph, requestedNodeId = "mid"))
    }

    @Test
    fun `resolveStartNodeId - unknown node returns null so caller falls back to start`() {
        val graph = graphWith("start")

        assertNull(resolveStartNodeId(graph, requestedNodeId = "missing"))
    }
}
