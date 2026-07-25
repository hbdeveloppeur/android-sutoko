package com.purpletear.game.presentation.game_chapters

import androidx.lifecycle.SavedStateHandle
import com.purpletear.game.presentation.game_preview.fakes.FakeChapterRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeLogger
import com.purpletear.game.presentation.game_preview.fakes.FakeMediaUrlResolver
import com.purpletear.game.presentation.game_preview.fakes.FakeMemoryRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeToastService
import com.purpletear.game.presentation.game_preview.fakes.FakeUserGameProgressRepository
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.SelectChapterUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChaptersViewModelTest {

    private val gameRepository = FakeGameRepository()
    private val chapterRepository = FakeChapterRepository()
    private val progressRepository = FakeUserGameProgressRepository()
    private val memoryRepository = FakeMemoryRepository()
    private val mediaUrlResolver = FakeMediaUrlResolver()
    private val toastService = FakeToastService()
    private val logger = FakeLogger()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(gameId: String = TestFixtures.GAME_ID): ChaptersViewModel {
        return ChaptersViewModel(
            savedStateHandle = SavedStateHandle(mapOf("gameId" to gameId)),
            getChaptersUseCase = GetChaptersUseCase(chapterRepository),
            gameRepository = gameRepository,
            mediaUrlResolver = mediaUrlResolver,
            chapterRepository = chapterRepository,
            selectChapterUseCase = SelectChapterUseCase(progressRepository, memoryRepository),
            toastService = toastService,
            logger = logger,
        )
    }

    @Test
    fun `chapters are sorted by number and media urls are resolved`() = runTest {
        chapterRepository.setChapters(
            TestFixtures.GAME_ID,
            Result.success(
                listOf(
                    Chapter(id = "chapter-2", number = 2, title = "Second"),
                    Chapter(id = "chapter-1", number = 1, title = "First"),
                )
            )
        )
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ChaptersUiState.Data
        assertEquals(listOf(1, 2), state.chapters.map { it.number })
        assertEquals("https://example.com/background/game-1", state.backgroundUrl)
    }

    @Test
    fun `current chapter code is exposed normalized`() = runTest {
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(emptyList()))
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(id = "chapter-1", number = 1, code = "CH1"),
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ChaptersUiState.Data
        assertEquals("ch1", state.currentChapterCode)
    }

    @Test
    fun `chapters load failure maps to Error state`() = runTest {
        chapterRepository.setChapters(
            TestFixtures.GAME_ID,
            Result.failure(IllegalStateException("boom"))
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ChaptersUiState.Error)
    }

    @Test
    fun `empty chapter list maps to Data with no chapters`() = runTest {
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(emptyList()))
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ChaptersUiState.Data
        assertTrue(state.chapters.isEmpty())
    }

    @Test
    fun `refresh re-fetches chapters and resets isRefreshing`() = runTest {
        chapterRepository.setChapters(
            TestFixtures.GAME_ID,
            Result.success(listOf(Chapter(id = "chapter-1", number = 1, title = "First"))),
        )

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        assertEquals(1, chapterRepository.getChaptersCalls)
        assertFalse(viewModel.isRefreshing.value)

        chapterRepository.setChapters(
            TestFixtures.GAME_ID,
            Result.success(
                listOf(
                    Chapter(id = "chapter-1", number = 1, title = "First"),
                    Chapter(id = "chapter-2", number = 2, title = "Second"),
                )
            ),
        )
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, chapterRepository.getChaptersCalls)
        assertFalse(viewModel.isRefreshing.value)
        val state = viewModel.uiState.value as ChaptersUiState.Data
        assertEquals(listOf(1, 2), state.chapters.map { it.number })
    }

    @Test
    fun `refresh while already refreshing is ignored`() = runTest {
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(emptyList()))

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        assertEquals(1, chapterRepository.getChaptersCalls)

        val gate = CompletableDeferred<Unit>()
        chapterRepository.getChaptersGate = gate
        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(viewModel.isRefreshing.value)

        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(2, chapterRepository.getChaptersCalls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `selecting a locked chapter does nothing`() = runTest {
        val locked = Chapter(
            id = "chapter-2",
            number = 2,
            code = "CH2",
            releaseDate = Long.MAX_VALUE,
        )
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(listOf(locked)))

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }
        val events = mutableListOf<ChaptersEvent>()
        backgroundScope.launch { viewModel.events.collect { events.add(it) } }
        advanceUntilIdle()

        viewModel.onChapterSelected(locked)
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        assertTrue(toastService.shownMessages.isEmpty())
    }
}
