package com.purpletear.game.presentation.game_preview

import app.cash.turbine.test
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewUserActionsTest {

    private val fixture = GamePreviewViewModelTestFixture()
    private val gameRepository get() = fixture.gameRepository
    private val chapterRepository get() = fixture.chapterRepository
    private val friendzonedProgressRepository get() = fixture.friendzonedProgressRepository

    @Before
    fun setUp() = fixture.setUp()

    @After
    fun tearDown() = fixture.tearDown()

    private fun createViewModel(
        gameId: String = TestFixtures.GAME_ID,
        connectedUser: Boolean = false,
    ) = fixture.createViewModel(gameId, connectedUser)

    private fun activateStateFlows(
        scope: CoroutineScope,
        viewModel: GamePreviewViewModel,
    ) = fixture.activateStateFlows(scope, viewModel)

    @Test
    fun `onAction OnToggleMenuSound toggles persisted muted state`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.isMenuSoundMuted.collect { } }
        advanceUntilIdle()
        assertFalse(viewModel.isMenuSoundMuted.value)

        viewModel.onAction(GamePreviewAction.OnToggleMenuSound)
        advanceUntilIdle()
        assertTrue(viewModel.isMenuSoundMuted.value)

        viewModel.onAction(GamePreviewAction.OnToggleMenuSound)
        advanceUntilIdle()
        assertFalse(viewModel.isMenuSoundMuted.value)
    }

    @Test
    fun `onAction OnToggleFavorite toggles isFavorite in game state`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        assertFalse((viewModel.game.value as GamePreviewUiState.Data).item.isFavorite)

        viewModel.onAction(GamePreviewAction.OnToggleFavorite)
        advanceUntilIdle()
        assertTrue((viewModel.game.value as GamePreviewUiState.Data).item.isFavorite)

        viewModel.onAction(GamePreviewAction.OnToggleFavorite)
        advanceUntilIdle()
        assertFalse((viewModel.game.value as GamePreviewUiState.Data).item.isFavorite)
    }

    @Test
    fun `onAction OnRestartConfirm restarts and emits no error`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartConfirm)
            advanceUntilIdle()

            expectNoEvents()
        }
        // Default fixture legacyId (42) is not Friendzoned: no symbols reset.
        assertTrue(friendzonedProgressRepository.resetLegacyIds.isEmpty())
    }

    @Test
    fun `onAction OnRestartConfirm re-reads the current chapter`() = runTest {
        val viewModel = createViewModel()
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 3, code = "3A"))
        backgroundScope.launch { viewModel.currentChapter.collect { } }
        advanceUntilIdle()
        val callsBefore = chapterRepository.observeCurrentChapterCalls

        viewModel.onAction(GamePreviewAction.OnRestartConfirm)
        advanceUntilIdle()

        // A wiped progress must be re-read immediately: the preview would
        // otherwise keep showing the pre-restart chapter until ON_RESUME.
        assertTrue(chapterRepository.observeCurrentChapterCalls > callsBefore)
    }

    @Test
    fun `onAction OnRestartConfirm with friendzoned game resets its own progress store`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(legacyId = 162))
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartConfirm)
            advanceUntilIdle()

            expectNoEvents()
        }
        assertEquals(listOf(162), friendzonedProgressRepository.resetLegacyIds)
    }
}
