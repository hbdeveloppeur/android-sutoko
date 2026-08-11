package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTextProcessingTest {

    @Test
    fun `info node text containing prenom - should be replaced with heroName`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory)
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "info1" to Node.Info(id = "info1", text = "[prenom] entend quelque chose")
            ),
            edges = listOf(
                Edge(source = "start", target = "info1", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        memory.set("heroName", "Alex")
        engine.start()

        val infoMessages = engine.messages.value.filterIsInstance<GameMessageInfo>()
        assertTrue(infoMessages.any { it.text == "Alex entend quelque chose" })
    }

    @Test
    fun `message node with empty text - should not add GameMessageText`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "emptyMsg" to Node.Message(id = "emptyMsg", text = "", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "emptyMsg", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.start()

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(textMessages.isEmpty())
    }

    @Test
    fun `message node with blank text after substitution - should not add GameMessageText`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory)
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "blankMsg" to Node.Message(id = "blankMsg", text = "[prenom]", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "blankMsg", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        memory.set("heroName", "")
        engine.start()

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(textMessages.isEmpty())
    }
}
