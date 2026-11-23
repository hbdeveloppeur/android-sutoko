package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.*
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import com.purpletear.sutoko.game.engine.message.GameMessageText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GameEngineCheckpointRegressionTest {
    @Test fun reopeningAfterChoiceKeepsDecisionAndReadingPosition() = runTest {
        val store=CheckpointTestStore();val memory=GameMemory(store.memories,store.progress)
        val graph=checkpointChoiceGraph();val first=createTestGameEngine(memory)
        first.initialize("story",graph);first.start();first.submitChoice("stay")
        val savedMessageCount=first.messages.value.size
        val reopenedMemory=GameMemory(store.memories,store.progress)
        val reopened=createTestGameEngine(reopenedMemory)
        reopened.initialize("story",graph);reopened.start()
        assertEquals("true",reopenedMemory.get("IStayed"))
        assertEquals("after",(reopened.state.value as GameEngineState.AwaitingTap).currentNodeId)
        assertEquals(savedMessageCount,reopened.messages.value.size)
        reopened.advanceOnTap()
        assertTrue(reopened.state.value is GameEngineState.ChapterFinished)
    }

    @Test fun pendingChoicesResumeAndRejectStaleInputWithoutDuplicatingMessages() = runTest {
        val store=CheckpointTestStore();val graph=checkpointChoiceGraph()
        val first=createTestGameEngine(GameMemory(store.memories,store.progress))
        first.initialize("story",graph);first.start()
        val resumed=createTestGameEngine(GameMemory(store.memories,store.progress))
        resumed.initialize("story",graph);resumed.start()
        assertEquals(listOf("stay","leave"),(resumed.state.value as GameEngineState.AwaitingInput).choices.map { it.nextNodeId })
        resumed.submitChoice("not-a-choice");assertTrue(resumed.state.value is GameEngineState.AwaitingInput)
        resumed.submitChoice("stay");resumed.submitChoice("stay")
        assertEquals(1,resumed.messages.value.filterIsInstance<GameMessageText>().count { it.text=="Stay" })
    }

    @Test fun interruptedChoiceExecutionResumesSelectedBranchOnlyOnce() = runTest {
        val store=CheckpointTestStore();val graph=checkpointChoiceGraph()
        val first=createTestGameEngine(GameMemory(store.memories,store.progress))
        first.initialize("story",graph);first.start();store.cancelAtGate=GameCheckpoint.Gate.TAP
        try {first.submitChoice("stay");fail("Expected cancellation")}
        catch (_:CancellationException){}
        assertEquals(GameCheckpoint.Gate.NODE,store.saved.checkpoint?.gate)
        store.cancelAtGate=null
        val memory=GameMemory(store.memories,store.progress);val reopened=createTestGameEngine(memory)
        reopened.initialize("story",graph);reopened.start()
        assertEquals("true",memory.get("IStayed"))
        assertEquals(1,reopened.messages.value.filterIsInstance<GameMessageText>().count { it.text=="Stay" })
        assertEquals("after",(reopened.state.value as GameEngineState.AwaitingTap).currentNodeId)
    }

    @Test fun changedGraphInvalidatesTheCheckpointInsteadOfJumpingToStaleContent() = runTest {
        val store=CheckpointTestStore();val graph=checkpointChoiceGraph()
        val first=createTestGameEngine(GameMemory(store.memories,store.progress))
        first.initialize("story",graph);first.start();first.submitChoice("stay")
        val changed=graph.copy(title="Updated export")
        val memory=GameMemory(store.memories,store.progress);val resumed=createTestGameEngine(memory)
        resumed.initialize("story",changed);resumed.start()
        assertTrue(resumed.state.value is GameEngineState.AwaitingInput)
        assertNull(memory.get("IStayed"))
    }

    @Test fun trialOrDebugSessionCannotReadDeleteOrOverwriteTheRealCheckpoint() = runTest {
        val store=CheckpointTestStore();val graph=checkpointChoiceGraph()
        val first=createTestGameEngine(GameMemory(store.memories,store.progress))
        first.initialize("story",graph);first.start();first.submitChoice("stay")
        val saved=store.saved
        val trialMemory=GameMemory(store.memories,store.progress);val trial=createTestGameEngine(trialMemory)
        trial.initialize("story",graph,checkpointingEnabled=false);trial.start();trial.submitChoice("leave")
        assertNull(trialMemory.get("IStayed"));assertEquals(saved,store.saved)
        val reopenedMemory=GameMemory(store.memories,store.progress);val reopened=createTestGameEngine(reopenedMemory)
        reopened.initialize("story",graph);reopened.start();assertEquals("true",reopenedMemory.get("IStayed"))
    }

    @Test fun chapterTransitionClearsOldGateWithoutLosingEarlierChoices() = runTest {
        val store=CheckpointTestStore();val base=checkpointChoiceGraph()
        val graph=base.copy(nodes=base.nodes+("end" to Node.ChapterChange("end","9B")))
        val memory=GameMemory(store.memories,store.progress);val first=createTestGameEngine(memory)
        first.initialize("story",graph);first.start();first.submitChoice("stay");first.advanceOnTap()
        assertEquals("9B",store.saved.currentChapterCode);assertNull(store.saved.checkpoint)
        val next=base.copy(chapterCode="9B",chapterNumber=9)
        val reopenedMemory=GameMemory(store.memories,store.progress);val reopened=createTestGameEngine(reopenedMemory)
        reopened.initialize("story",next);assertEquals("true",reopenedMemory.get("IStayed"))
    }

    @Test fun mangaGateResumesWithoutAddingThePageTwice() = runTest {
        val store=CheckpointTestStore()
        val graph=linearCheckpointGraph(Node.MangaPage("page","/page.webp",messages=listOf(Node.MangaPage.MangaMessage("A page",24f,10f,10f,40f))),Node.Message("after","After page",2))
        val first=createTestGameEngine(GameMemory(store.memories,store.progress))
        first.initialize("story",graph);first.start()
        val reopened=createTestGameEngine(GameMemory(store.memories,store.progress))
        reopened.initialize("story",graph);reopened.start()
        assertTrue(reopened.state.value is GameEngineState.AwaitingMangaDismissal)
        assertEquals(1,reopened.messages.value.size)
        reopened.resumeFromMangaPage();assertEquals("after",(reopened.state.value as GameEngineState.AwaitingTap).currentNodeId)
    }

    @Test fun visualNovelGateRestoresItsOverlayAndCanBeDismissed() = runTest {
        val store=CheckpointTestStore()
        val overlay=Node.VisualNovel("overlay","Title",listOf(Node.VisualNovel.Layer("/layer.webp")),
            listOf(Node.VisualNovel.Dialog("Dialog")),emptyList(),Node.VisualNovel.Theme())
        val graph=linearCheckpointGraph(overlay,Node.Message("after","After overlay",2))
        val first=createTestGameEngine(GameMemory(store.memories,store.progress))
        first.initialize("story",graph);first.start()
        val reopened=createTestGameEngine(GameMemory(store.memories,store.progress))
        reopened.initialize("story",graph);reopened.start()
        assertTrue(reopened.state.value is GameEngineState.AwaitingVisualNovelDismissal)
        assertTrue(reopened.effects.take(1).toList().single() is HandlerEffect.ShowVisualNovel)
        reopened.resumeFromVisualNovel()
        assertEquals("after",(reopened.state.value as GameEngineState.AwaitingTap).currentNodeId)
    }

    @Test fun sceneAndLoopingMusicAreRestoredButOneShotSoundsAreNotReplayed() = runTest {
        val store=CheckpointTestStore()
        val graph=linearCheckpointGraph(Node.Scene("scene",12),Node.Sound("music","/music.mp3",true),
            Node.Sound("effect","/effect.mp3",false),Node.Message("after","Hello",2))
        val first=createTestGameEngine(GameMemory(store.memories,store.progress))
        first.initialize("story",graph);first.start()
        repeat(8){if(first.state.value is GameEngineState.AwaitingTap)first.advanceOnTap()}
        // Finish can be restored too; background context remains in its durable gate.
        val reopened=createTestGameEngine(GameMemory(store.memories,store.progress))
        reopened.initialize("story",graph);reopened.start()
        val effects=reopened.effects.take(2).toList()
        assertTrue(effects.contains(HandlerEffect.ChangeScene(12)))
        assertEquals(listOf("music"),effects.filterIsInstance<HandlerEffect.PlaySound>().map { it.nodeId })
    }

    @Test fun cinematicBoundaryResumesAsACinematicRatherThanSmsDialogue() = runTest {
        val store=CheckpointTestStore()
        val graph=linearCheckpointGraph(Node.Code("intro","[intro=start]"),
            Node.IntroSentence("sentence","Introduction",IntroAlignment.CENTER),
            Node.Code("outro","[intro=end]"),Node.Message("after","Hello",2))
        val first=createTestGameEngine(GameMemory(store.memories,store.progress));first.initialize("story",graph);first.start()
        val reopened=createTestGameEngine(GameMemory(store.memories,store.progress));reopened.initialize("story",graph);reopened.start()
        assertEquals(HandlerEffect.EnterCinematic("intro","outro"),reopened.effects.take(1).toList().single())
        assertTrue(reopened.messages.value.isEmpty())
    }
}
