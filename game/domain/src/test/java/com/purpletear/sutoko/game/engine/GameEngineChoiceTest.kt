package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineChoiceTest {

    @Test
    fun `start node with multiple message targets - should emit ShowChoices and await input`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = choiceGraph()

        engine.initialize("game-1", graph)
        engine.start()

        assertTrue(engine.state.value is GameEngineState.AwaitingInput)

        val showChoices = engine.effects.first { it is HandlerEffect.ShowChoices } as HandlerEffect.ShowChoices
        assertEquals(2, showChoices.choices.size)
        assertEquals("Option A", showChoices.choices[0].text)
        assertEquals("choiceA", showChoices.choices[0].nextNodeId)
    }

    @Test
    fun `submit valid choice - should resume at selected node`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = choiceGraph()

        engine.initialize("game-1", graph)
        engine.start()

        assertTrue(engine.state.value is GameEngineState.AwaitingInput)

        engine.submitChoice("choiceB")

        assertTrue(engine.state.value !is GameEngineState.AwaitingInput)
        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(textMessages.any { it.text == "Option B" })
    }

    @Test
    fun `submit choice while not awaiting input - should be ignored`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = startOnlyGraph()

        engine.initialize("game-1", graph)
        engine.start()

        engine.submitChoice("invalid")

        assertTrue(engine.state.value !is GameEngineState.AwaitingInput)
        assertTrue(engine.messages.value.isEmpty())
    }

    @Test
    fun `submit invalid choice - should be ignored and still accept a valid choice`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = choiceGraph()

        engine.initialize("game-1", graph)
        engine.start()

        engine.submitChoice("not-a-choice")

        // Stale input is dropped: the engine must still be waiting with its choices intact.
        assertTrue(engine.state.value is GameEngineState.AwaitingInput)

        engine.submitChoice("choiceA")
        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(textMessages.any { it.text == "Option A" })
    }

    @Test
    fun `resubmit stale choice after engine moved to next hub - should be ignored`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "choiceA" to Node.Message(id = "choiceA", text = "Option A", characterId = 1),
                "choiceB" to Node.Message(id = "choiceB", text = "Option B", characterId = 1),
                "hub2" to Node.Message(id = "hub2", text = "After A", characterId = 2),
                "choiceC" to Node.Message(id = "choiceC", text = "Option C", characterId = 1),
                "choiceD" to Node.Message(id = "choiceD", text = "Option D", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "choiceA", type = EdgeType.NORMAL),
                Edge(source = "start", target = "choiceB", type = EdgeType.NORMAL),
                Edge(source = "choiceA", target = "hub2", type = EdgeType.NORMAL),
                Edge(source = "hub2", target = "choiceC", type = EdgeType.NORMAL),
                Edge(source = "hub2", target = "choiceD", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.start()
        assertTrue(engine.state.value is GameEngineState.AwaitingInput)

        engine.submitChoice("choiceA")
        assertTrue(engine.state.value is GameEngineState.AwaitingTap)

        engine.advanceOnTap()
        assertTrue(engine.state.value is GameEngineState.AwaitingInput)

        // Regression: a stale duplicate from the previous hub must not crash the engine.
        engine.submitChoice("choiceA")
        assertTrue(engine.state.value is GameEngineState.AwaitingInput)

        engine.submitChoice("choiceC")
        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(textMessages.any { it.text == "Option C" })
    }

    @Test
    fun `choice labels containing prenom - should be replaced with heroName`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory)
        val graph = choiceGraph(choiceAText = "Salut [prenom]", choiceBText = "[prenom] ?")

        engine.initialize("game-1", graph)
        memory.set("heroName", "Alex")
        engine.start()

        val showChoices = engine.effects.first { it is HandlerEffect.ShowChoices } as HandlerEffect.ShowChoices
        assertEquals("Salut Alex", showChoices.choices[0].text)
        assertEquals("Alex ?", showChoices.choices[1].text)
    }
}
