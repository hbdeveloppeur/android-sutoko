package com.purpletear.sutoko.game.model.chapter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CinematicExtractorTest {

    private fun edge(source: String, target: String) = Edge(source = source, target = target)

    private fun graphOf(
        nodes: List<Node>,
        edges: List<Edge>,
        start: String = "start"
    ) = ChapterGraph(
        chapterCode = "CH1",
        chapterNumber = 1,
        title = "test",
        nodes = nodes.associateBy { it.id },
        edges = edges,
        startNodeId = start
    )

    private fun cinematicGraph(): ChapterGraph {
        val start = Node.Code("start", "[intro=start]")
        val scene = Node.Scene("scene", sceneId = 1)
        val sound = Node.Sound("sound", soundUrl = "bg.mp3", loop = true)
        val sentence = Node.IntroSentence(
            id = "line",
            text = "Hello",
            alignment = IntroAlignment.CENTER,
            delayMs = 100,
            durationMs = 500
        )
        val end = Node.Code("end", "[intro=end]")
        val after = Node.Message("after", "back to sms", characterId = 0)
        return graphOf(
            nodes = listOf(start, scene, sound, sentence, end, after),
            edges = listOf(
                edge("start", "scene"),
                edge("scene", "sound"),
                edge("sound", "line"),
                edge("line", "end"),
                edge("end", "after")
            )
        )
    }

    @Test
    fun `extracts body in order and excludes both markers`() {
        val result = extractCinematicBody(cinematicGraph(), "start", "end")

        assertTrue(result.isSuccess)
        assertEquals(listOf("scene", "sound", "line"), result.getOrThrow().map { it.id })
    }

    @Test
    fun `empty body is a valid no-op cinematic`() {
        val graph = graphOf(
            nodes = listOf(Node.Code("start", "[intro=start]"), Node.Code("end", "[intro=end]")),
            edges = listOf(edge("start", "end"))
        )

        val result = extractCinematicBody(graph, "start", "end")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `rejects a branch inside the body`() {
        val graph = graphOf(
            nodes = listOf(
                Node.Code("start", "[intro=start]"),
                Node.Scene("scene", sceneId = 1),
                Node.Sound("a", "a.mp3"),
                Node.Sound("b", "b.mp3"),
                Node.Code("end", "[intro=end]")
            ),
            edges = listOf(
                edge("start", "scene"),
                edge("scene", "a"),
                edge("scene", "b"),
                edge("a", "end"),
                edge("b", "end")
            )
        )

        val error = extractCinematicBody(graph, "start", "end").exceptionOrNull()

        assertTrue(error is CinematicError.NonLinear)
        assertEquals("scene", (error as CinematicError.NonLinear).nodeId)
    }

    @Test
    fun `rejects a dead end inside the body`() {
        val graph = graphOf(
            nodes = listOf(
                Node.Code("start", "[intro=start]"),
                Node.Scene("scene", sceneId = 1),
                Node.Code("end", "[intro=end]")
            ),
            edges = listOf(edge("start", "scene"))
        )

        val error = extractCinematicBody(graph, "start", "end").exceptionOrNull()

        assertTrue(error is CinematicError.NonLinear)
        assertEquals("scene", (error as CinematicError.NonLinear).nodeId)
    }

    @Test
    fun `rejects a cycle inside the body`() {
        val graph = graphOf(
            nodes = listOf(
                Node.Code("start", "[intro=start]"),
                Node.Scene("a", sceneId = 1),
                Node.Scene("b", sceneId = 2),
                Node.Code("end", "[intro=end]")
            ),
            edges = listOf(
                edge("start", "a"),
                edge("a", "b"),
                edge("b", "a")
            )
        )

        val error = extractCinematicBody(graph, "start", "end").exceptionOrNull()

        assertTrue(error is CinematicError.Cycle)
    }

    @Test
    fun `rejects a reference to a missing node`() {
        val graph = graphOf(
            nodes = listOf(
                Node.Code("start", "[intro=start]"),
                Node.Scene("scene", sceneId = 1),
                Node.Code("end", "[intro=end]")
            ),
            edges = listOf(edge("start", "scene"), edge("scene", "ghost"))
        )

        val error = extractCinematicBody(graph, "start", "end").exceptionOrNull()

        assertTrue(error is CinematicError.MissingNode)
        assertEquals("ghost", (error as CinematicError.MissingNode).nodeId)
    }

    @Test
    fun `rejects an unsupported node inside the body`() {
        val graph = graphOf(
            nodes = listOf(
                Node.Code("start", "[intro=start]"),
                Node.Message("sms", "hi", characterId = 0),
                Node.Code("end", "[intro=end]")
            ),
            edges = listOf(edge("start", "sms"), edge("sms", "end"))
        )

        val error = extractCinematicBody(graph, "start", "end").exceptionOrNull()

        assertTrue(error is CinematicError.UnsupportedNode)
        assertEquals("sms", (error as CinematicError.UnsupportedNode).nodeId)
    }
}
