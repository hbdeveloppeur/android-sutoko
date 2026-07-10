package com.purpletear.game.presentation.game_play

import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CinematicResumeDecisionTest {

    private fun graphWith(vararg nodeIds: String): ChapterGraph {
        val nodes = nodeIds.map { Node.Message(it, text = "t", characterId = 0) }
        return ChapterGraph(
            chapterCode = "CH1",
            chapterNumber = 1,
            title = "test",
            nodes = nodes.associateBy { it.id },
            edges = emptyList(),
            startNodeId = nodes.first().id
        )
    }

    @Test
    fun `pending graph reuses the resume node when it still exists`() {
        val newGraph = graphWith("start", "resume-1")

        val action = decideCinematicResume(resumeNodeId = "resume-1", pendingGraph = newGraph)

        assertTrue(action is CinematicResumeAction.ApplyPendingGraph)
        action as CinematicResumeAction.ApplyPendingGraph
        assertSame(newGraph, action.graph)
        assertEquals("resume-1", action.safeResumeNodeId)
    }

    @Test
    fun `pending graph falls back to chapter start when the resume node vanished`() {
        val newGraph = graphWith("start") // does not contain "resume-1"

        val action = decideCinematicResume(resumeNodeId = "resume-1", pendingGraph = newGraph)

        assertTrue(action is CinematicResumeAction.ApplyPendingGraph)
        assertNull((action as CinematicResumeAction.ApplyPendingGraph).safeResumeNodeId)
    }

    @Test
    fun `no pending graph resumes the old graph at the resume node`() {
        val action = decideCinematicResume(resumeNodeId = "resume-1", pendingGraph = null)

        assertTrue(action is CinematicResumeAction.ResumeOldGraph)
        assertEquals("resume-1", (action as CinematicResumeAction.ResumeOldGraph).nodeId)
    }

    @Test
    fun `no pending graph and no resume node does nothing`() {
        assertTrue(decideCinematicResume(resumeNodeId = null, pendingGraph = null) is CinematicResumeAction.None)
    }
}
