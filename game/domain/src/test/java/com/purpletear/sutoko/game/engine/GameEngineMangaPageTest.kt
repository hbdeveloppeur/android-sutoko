package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.message.GameMessageMangaPage
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineMangaPageTest {

    @Test
    fun `manga node parks engine and successor is not emitted until resume`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = singleSuccessorGraph()

        engine.initialize("game-1", graph)
        engine.start()

        assertTrue(engine.state.value is GameEngineState.AwaitingMangaDismissal)
        assertTrue(engine.messages.value.any { it is GameMessageMangaPage })
        assertFalse(
            "successor must not appear while the manga page is gating",
            engine.messages.value.filterIsInstance<GameMessageText>().any { it.text == "After manga" }
        )
    }

    @Test
    fun `resumeFromMangaPage continues to the successor`() = runBlocking {
        val engine = createTestGameEngine()
        engine.initialize("game-1", singleSuccessorGraph())
        engine.start()
        assertTrue(engine.state.value is GameEngineState.AwaitingMangaDismissal)

        engine.resumeFromMangaPage()

        assertTrue(engine.state.value !is GameEngineState.AwaitingMangaDismissal)
        assertTrue(
            engine.messages.value.filterIsInstance<GameMessageText>().any { it.text == "After manga" }
        )
    }

    @Test
    fun `resumeFromMangaPage keeps the manga message in history`() = runBlocking {
        val engine = createTestGameEngine()
        engine.initialize("game-1", singleSuccessorGraph())
        engine.start()
        engine.resumeFromMangaPage()

        assertTrue(
            "manga bubble must remain in the conversation after resume",
            engine.messages.value.any { it is GameMessageMangaPage }
        )
    }

    @Test
    fun `manga node with no successor finishes the chapter on resume`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "manga" to mangaNode("manga"),
            ),
            edges = listOf(Edge(source = "start", target = "manga", type = EdgeType.NORMAL)),
            startNodeId = "start",
        )

        engine.initialize("game-1", graph)
        engine.start()
        assertTrue(engine.state.value is GameEngineState.AwaitingMangaDismissal)

        engine.resumeFromMangaPage()

        assertTrue(engine.state.value is GameEngineState.ChapterFinished)
    }

    @Test
    fun `manga node followed by a choice awaits input after resume`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "manga" to mangaNode("manga"),
                "choiceA" to Node.Message(id = "choiceA", text = "Option A", characterId = 1),
                "choiceB" to Node.Message(id = "choiceB", text = "Option B", characterId = 1),
            ),
            edges = listOf(
                Edge(source = "start", target = "manga", type = EdgeType.NORMAL),
                Edge(source = "manga", target = "choiceA", type = EdgeType.NORMAL),
                Edge(source = "manga", target = "choiceB", type = EdgeType.NORMAL),
            ),
            startNodeId = "start",
        )

        engine.initialize("game-1", graph)
        engine.start()
        assertTrue(engine.state.value is GameEngineState.AwaitingMangaDismissal)

        engine.resumeFromMangaPage()

        assertTrue(engine.state.value is GameEngineState.AwaitingInput)
    }

    @Test
    fun `resumeFromMangaPage when not parked is a no-op`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = startOnlyGraph()

        engine.initialize("game-1", graph)
        engine.start()
        val stateBefore = engine.state.value
        val messagesBefore = engine.messages.value

        engine.resumeFromMangaPage()

        assertEquals(stateBefore::class, engine.state.value::class)
        assertEquals(messagesBefore, engine.messages.value)
    }

    private fun singleSuccessorGraph(): ChapterGraph = ChapterGraph(
        chapterCode = "1A",
        title = "Test",
        nodes = mapOf(
            "start" to Node.Start(id = "start"),
            "manga" to mangaNode("manga"),
            "after" to Node.Message(id = "after", text = "After manga", characterId = 1),
        ),
        edges = listOf(
            Edge(source = "start", target = "manga", type = EdgeType.NORMAL),
            Edge(source = "manga", target = "after", type = EdgeType.NORMAL),
        ),
        startNodeId = "start",
    )

    private fun mangaNode(id: String): Node.MangaPage = Node.MangaPage(
        id = id,
        imageUrl = "/tmp/games/game1/assets/page.webp",
        messages = listOf(
            Node.MangaPage.MangaMessage(text = "Hello", size = 28f, x = 50f, y = 50f, w = 40f),
        ),
    )
}
