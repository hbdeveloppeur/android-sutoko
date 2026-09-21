package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.engine.timing.TimingScheduler
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineRapidTapTest {
    @Test
    fun `taps during a transition must not advance a later message`() = runTest {
        var blockTransition = false
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory, timingScheduler = object : TimingScheduler {
            override suspend fun delay(millis: Long) {
                if (blockTransition) {
                    entered.complete(Unit)
                    release.await()
                }
            }
        })
        val nodes = listOf(
            Node.Start(id = "start"),
            Node.Message(id = "first", text = "First", characterId = 1),
            Node.Message(id = "second", text = "Second", characterId = 1),
            Node.Message(id = "third", text = "Third", characterId = 1),
        )
        engine.initialize("test", ChapterGraph(
            chapterCode = "1A", title = "Rapid taps", startNodeId = "start",
            nodes = nodes.associateBy { it.id },
            edges = nodes.zipWithNext { a, b -> Edge(a.id, b.id, EdgeType.NORMAL) },
        ))
        memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")
        engine.start()
        val originalGate = engine.state.value as GameEngineState.AwaitingTap
        blockTransition = true
        val firstTap = launch { engine.advanceOnTap(isUserInitiated = true) }
        entered.await()
        val burst = List(100) {
            launch(start = CoroutineStart.UNDISPATCHED) { engine.advanceOnTap(isUserInitiated = true) }
        }
        blockTransition = false
        release.complete(Unit)
        firstTap.join()
        burst.forEach { it.join() }
        assertEquals("second", (engine.state.value as GameEngineState.AwaitingTap).currentNodeId)
        engine.advanceOnTap(isUserInitiated = true, expectedGate = originalGate)
        assertEquals("second", (engine.state.value as GameEngineState.AwaitingTap).currentNodeId)
        engine.advanceOnTap(isUserInitiated = true)
        assertEquals("third", (engine.state.value as GameEngineState.AwaitingTap).currentNodeId)
    }
}
