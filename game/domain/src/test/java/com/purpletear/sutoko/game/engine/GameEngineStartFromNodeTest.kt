package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineStartFromNodeTest {

    @Test
    fun `startFromNode - should execute target node`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "mid" to Node.Message(id = "mid", text = "Middle", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "mid")
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.startFromNode("mid")

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertEquals(1, textMessages.size)
        assertEquals("Middle", textMessages.first().text)
    }

    @Test
    fun `startFromNode - should clear previous messages`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "msg1" to Node.Message(id = "msg1", text = "First", characterId = 1),
                "msg2" to Node.Message(id = "msg2", text = "Second", characterId = 1)
            ),
            edges = emptyList(),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.startFromNode("msg1")

        assertEquals(1, engine.messages.value.filterIsInstance<GameMessageText>().size)

        engine.startFromNode("msg2")

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertEquals(1, textMessages.size)
        assertEquals("Second", textMessages.first().text)
    }

    @Test(expected = IllegalStateException::class)
    fun `startFromNode without initialize - should throw`() = runBlocking {
        val engine = createTestGameEngine()
        engine.startFromNode("start")
    }

    @Test(expected = IllegalStateException::class)
    fun `startFromNode with unknown node - should throw`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = startOnlyGraph()

        engine.initialize("game-1", graph)
        engine.startFromNode("missing")
    }
}
