package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserGameProgress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewChapterRestartTest {
    private val fixture = GamePreviewViewModelTestFixture()
    private val gameId = TestFixtures.GAME_ID
    private val checkpoint = GameCheckpoint("3B", "graph", "node", GameCheckpoint.Gate.TAP, emptyMap(), emptyList())
    private val saved = UserGameProgress(gameId, "3B", "3b", "Alex", checkpoint)

    @Before fun setUp() = fixture.setUp()
    @After fun tearDown() = fixture.tearDown()

    private suspend fun TestScope.preview(legacyId: Int = 160, chapter: Chapter? = Chapter(story = gameId, code = "3B", number = 3, available = true)): GamePreviewViewModel {
        fixture.gameRepository.setGame(gameId, TestFixtures.gameCatalog(legacyId = legacyId))
        fixture.chapterRepository.setCurrentChapter(gameId, chapter)
        fixture.userGameProgressRepository.save(saved)
        val viewModel = fixture.createViewModel()
        fixture.activateStateFlows(backgroundScope, viewModel)
        backgroundScope.launch { viewModel.currentChapter.collect { } }
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun restartRequestOnlyShowsConfirmationThenConfirmKeepsTheChapterAndName() = runTest {
        val viewModel = preview()
        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartChapter)
            advanceUntilIdle()
            assertEquals(GamePreviewEvent.ShowRestartChapterDialog, awaitItem())
            assertEquals(saved, fixture.userGameProgressRepository.get(gameId))
            viewModel.onAction(GamePreviewAction.OnRestartChapterConfirm)
            advanceUntilIdle()
            expectNoEvents()
        }
        assertEquals(saved.copy(checkpoint = null), fixture.userGameProgressRepository.get(gameId))
        assertTrue(fixture.toastService.shownMessages.contains(R.string.game_presentation_chapter_restart_success))
        assertFalse(viewModel.isRestarting.value)
    }

    @Test
    fun duplicateRestartAndPlayAreIgnoredWhileSaving() = runTest {
        val viewModel = preview()
        val gate = CompletableDeferred<Unit>()
        fixture.userGameProgressRepository.beforeSave = { gate.await() }
        val callsBefore = fixture.userGameProgressRepository.saveCalls
        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartChapterConfirm)
            advanceUntilIdle()
            assertTrue(viewModel.isRestarting.value)
            viewModel.onAction(GamePreviewAction.OnRestartChapterConfirm)
            viewModel.onAction(GamePreviewAction.OnRestartConfirm)
            viewModel.onAction(GamePreviewAction.OnPlay)
            advanceUntilIdle()
            assertEquals(callsBefore + 1, fixture.userGameProgressRepository.saveCalls)
            expectNoEvents()
            gate.complete(Unit)
            advanceUntilIdle()
            assertFalse(viewModel.isRestarting.value)
        }
    }

    @Test
    fun failureShowsChapterErrorAndReleasesBusyState() = runTest {
        val viewModel = preview()
        fixture.userGameProgressRepository.saveError = IllegalStateException("disk error")
        viewModel.onAction(GamePreviewAction.OnRestartChapterConfirm)
        advanceUntilIdle()
        assertEquals(saved, fixture.userGameProgressRepository.get(gameId))
        assertTrue(fixture.toastService.shownMessages.contains(R.string.game_presentation_chapter_restart_error))
        assertFalse(viewModel.isRestarting.value)
    }

    @Test
    fun friendzonedProgrammaticChapterRestartIsIgnored() = runTest {
        val viewModel = preview(legacyId = 162)
        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartChapter)
            viewModel.onAction(GamePreviewAction.OnRestartChapterConfirm)
            advanceUntilIdle()
            expectNoEvents()
        }
        assertEquals(saved, fixture.userGameProgressRepository.get(gameId))
        assertTrue(fixture.friendzonedProgressRepository.resetLegacyIds.isEmpty())
    }

    @Test
    fun unreleasedChapterCannotShowOrConfirmAChapterRestart() = runTest {
        val viewModel = preview(chapter = Chapter(story = gameId, code = "3B", number = 3, available = false))
        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartChapter)
            viewModel.onAction(GamePreviewAction.OnRestartChapterConfirm)
            advanceUntilIdle()
            expectNoEvents()
        }
        assertEquals(saved, fixture.userGameProgressRepository.get(gameId))
    }

    @Test
    fun missingChapterCannotShowOrConfirmAChapterRestart() = runTest {
        val viewModel = preview(chapter = null)
        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartChapter)
            viewModel.onAction(GamePreviewAction.OnRestartChapterConfirm)
            advanceUntilIdle()
            expectNoEvents()
        }
        assertEquals(saved, fixture.userGameProgressRepository.get(gameId))
    }

    @Test
    fun optionsRestartReportsSuccessWithoutDuplicateToastOrDialog() = runTest {
        val viewModel = preview()
        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartFromOptions(GamePreviewRestartTarget.CHAPTER))
            advanceUntilIdle()
            expectNoEvents()
        }
        assertEquals(GamePreviewRestartResult.Success(GamePreviewRestartTarget.CHAPTER), viewModel.restartResult.value)
        assertEquals(saved.copy(checkpoint = null), fixture.userGameProgressRepository.get(gameId))
        assertTrue(fixture.toastService.shownMessages.isEmpty())
        viewModel.clearRestartResult()
        assertNull(viewModel.restartResult.value)
    }

    @Test
    fun optionsFailureStaysRetryableAndRetryReplacesTheError() = runTest {
        val viewModel = preview()
        fixture.userGameProgressRepository.saveError = IllegalStateException("disk error")
        viewModel.onAction(GamePreviewAction.OnRestartFromOptions(GamePreviewRestartTarget.CHAPTER))
        advanceUntilIdle()
        assertEquals(GamePreviewRestartResult.Failure(GamePreviewRestartTarget.CHAPTER), viewModel.restartResult.value)
        assertFalse(viewModel.isRestarting.value)
        assertTrue(fixture.toastService.shownMessages.isEmpty())
        fixture.userGameProgressRepository.saveError = null
        viewModel.onAction(GamePreviewAction.OnRestartFromOptions(GamePreviewRestartTarget.CHAPTER))
        advanceUntilIdle()
        assertEquals(GamePreviewRestartResult.Success(GamePreviewRestartTarget.CHAPTER), viewModel.restartResult.value)
    }

    @Test
    fun optionsFullRestartUsesTheExistingResetWithoutDuplicateToast() = runTest {
        val viewModel = preview()
        viewModel.onAction(GamePreviewAction.OnRestartFromOptions(GamePreviewRestartTarget.GAME))
        advanceUntilIdle()
        assertEquals(GamePreviewRestartResult.Success(GamePreviewRestartTarget.GAME), viewModel.restartResult.value)
        assertEquals(UserGameProgress(gameId = gameId), fixture.userGameProgressRepository.get(gameId))
        assertTrue(fixture.toastService.shownMessages.isEmpty())
    }

    @Test
    fun newOptionsActionsCannotResetFriendzoned() = runTest {
        val viewModel = preview(legacyId = 162)
        viewModel.onAction(GamePreviewAction.OnRestartFromOptions(GamePreviewRestartTarget.GAME))
        viewModel.onAction(GamePreviewAction.OnRestartFromOptions(GamePreviewRestartTarget.CHAPTER))
        advanceUntilIdle()
        assertEquals(saved, fixture.userGameProgressRepository.get(gameId))
        assertTrue(fixture.friendzonedProgressRepository.resetLegacyIds.isEmpty())
        assertNull(viewModel.restartResult.value)
    }

}
