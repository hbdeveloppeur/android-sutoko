package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineChoiceArrivalTest {
    @Test
    fun `one manual advance carries choices and tap intent together`() = runBlocking {
        val graph = messageChoiceGraph()
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory)
        engine.initialize("game", graph)
        memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")
        engine.start()
        assertTrue(engine.state.value is GameEngineState.AwaitingTap)

        engine.advanceOnTap(isUserInitiated = true)

        val state = engine.state.value as GameEngineState.AwaitingInput
        assertTrue(state.isUserInitiated)
        assertEquals(listOf("Option A", "Option B"), state.choices.map { it.text })
    }
    @Test
    fun `automatic advance carries choices without manual intent`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory)
        engine.initialize("game", messageChoiceGraph())
        memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")
        engine.start()

        engine.advanceOnTap()

        val state = engine.state.value as GameEngineState.AwaitingInput
        assertEquals(2, state.choices.size)
        assertFalse(state.isUserInitiated)
    }

    @Test
    fun `manual intent ends at the next message gate`() = runBlocking {
        val base = messageChoiceGraph()
        val graph = base.copy(
            nodes = base.nodes + ("earlier" to Node.Message(id = "earlier", text = "Earlier", characterId = 2)),
            edges = listOf(
                Edge(source = "start", target = "earlier", type = EdgeType.NORMAL),
                Edge(source = "earlier", target = "message", type = EdgeType.NORMAL)
            ) + base.edges.filter { it.source != "start" }
        )
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory)
        engine.initialize("game", graph)
        memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")
        engine.start()

        engine.advanceOnTap(isUserInitiated = true)
        assertEquals("message", (engine.state.value as GameEngineState.AwaitingTap).currentNodeId)
        engine.advanceOnTap()

        assertFalse((engine.state.value as GameEngineState.AwaitingInput).isUserInitiated)
    }

    private fun messageChoiceGraph(): ChapterGraph {
        val base = choiceGraph()
        return base.copy(
            nodes = base.nodes + ("message" to Node.Message(id = "message", text = "Hello", characterId = 2)),
            edges = listOf(Edge(source = "start", target = "message", type = EdgeType.NORMAL)) +
                base.edges.map { it.copy(source = "message") }
        )
    }

}
