package com.purpletear.game.presentation.game_chapters

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.purpletear.game.presentation.game_preview.fakes.FakeChapterRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeFriendzonedProgressRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeLogger
import com.purpletear.game.presentation.game_preview.fakes.FakeMediaUrlResolver
import com.purpletear.game.presentation.game_preview.fakes.FakeMemoryRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeToastService
import com.purpletear.game.presentation.game_preview.fakes.FakeUserGameProgressRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeUserRoleRepository
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import com.purpletear.sutoko.game.repository.GameProgressTransaction
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.PrepareGameLaunchUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import com.purpletear.sutoko.game.usecase.SelectChapterUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChaptersNicknameNavigationTest {
    private val gameRepository = FakeGameRepository()
    private val chapterRepository = FakeChapterRepository()
    private val progressRepository = FakeUserGameProgressRepository()
    private val friendzonedRepository = FakeFriendzonedProgressRepository()
    private val memoryRepository = FakeMemoryRepository()
    private val toastService = FakeToastService()
    private val chapter = Chapter(id = "chapter-3", number = 3, code = "3A", available = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(userNickNameRequired = true))
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(listOf(chapter)))
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.createViewModel(): ChaptersViewModel {
        val viewModel = ChaptersViewModel(
            savedStateHandle = SavedStateHandle(mapOf("gameId" to TestFixtures.GAME_ID)),
            getChaptersUseCase = GetChaptersUseCase(chapterRepository),
            gameRepository = gameRepository,
            mediaUrlResolver = FakeMediaUrlResolver(),
            chapterRepository = chapterRepository,
            selectChapterUseCase = SelectChapterUseCase(
                progressRepository, GameProgressTransaction { it() },
            ),
            prepareGameLaunchUseCase = PrepareGameLaunchUseCase(progressRepository, friendzonedRepository),
            saveUserNickNameUseCase = SaveUserNickNameUseCase(progressRepository),
            userRoleRepository = FakeUserRoleRepository(),
            toastService = toastService,
            logger = FakeLogger(),
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `first launch at a later chapter asks for a name before changing progress`() = runTest {
        val decisions = mapOf("trusted_friend" to MemoryEntry("true", 1))
        memoryRepository.save(TestFixtures.GAME_ID, decisions)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onChapterSelected(chapter)
            advanceUntilIdle()
            expectNoEvents()
            assertEquals(chapter, viewModel.nickNameChapter.value)
            assertEquals(0, progressRepository.saveCalls)
            assertEquals(decisions, memoryRepository.load(TestFixtures.GAME_ID, 3))

            viewModel.onNickNameConfirmed(null)
            assertEquals(ChaptersEvent.OpenChapter("3a"), awaitItem())
            assertNull(viewModel.nickNameChapter.value)
            val progress = progressRepository.get(TestFixtures.GAME_ID)
            assertEquals(SaveUserNickNameUseCase.DEFAULT_HERO_NAME, progress.heroName)
            assertEquals("3a", progress.normalizedChapterCode)
        }
    }

    @Test
    fun `failed nickname save keeps the selected chapter and allows retry`() = runTest {
        val viewModel = createViewModel()
        viewModel.events.test {
            viewModel.onChapterSelected(chapter)
            advanceUntilIdle()
            progressRepository.saveError = IllegalStateException("write failed")
            viewModel.onNickNameConfirmed("Alice")
            advanceUntilIdle()
            expectNoEvents()
            assertEquals(chapter, viewModel.nickNameChapter.value)
            assertFalse(viewModel.isSavingNickName.value)
            assertEquals(listOf(GameUiError.NickName.stringRes), toastService.shownMessages)

            progressRepository.saveError = null
            viewModel.onNickNameConfirmed("Alice")
            assertEquals(ChaptersEvent.OpenChapter("3a"), awaitItem())
            assertEquals("Alice", progressRepository.get(TestFixtures.GAME_ID).heroName)
        }
    }

    @Test
    fun `dismissing the nickname leaves progress unchanged and permits another chapter`() = runTest {
        val viewModel = createViewModel()
        viewModel.events.test {
            viewModel.onChapterSelected(chapter)
            advanceUntilIdle()
            viewModel.onNickNameDismissed()
            viewModel.onNickNameConfirmed("Stale")
            assertNull(viewModel.nickNameChapter.value)
            assertEquals(0, progressRepository.saveCalls)

            val next = chapter.copy(number = 4, code = "4A")
            viewModel.onChapterSelected(next)
            advanceUntilIdle()
            assertEquals(next, viewModel.nickNameChapter.value)
            viewModel.onNickNameConfirmed("Alice")
            assertEquals(ChaptersEvent.OpenChapter("4a"), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `nickname read failure blocks launch without changing progress`() = runTest {
        val viewModel = createViewModel()
        progressRepository.getError = IllegalStateException("read failed")
        viewModel.events.test {
            viewModel.onChapterSelected(chapter)
            advanceUntilIdle()
            expectNoEvents()
            assertNull(viewModel.nickNameChapter.value)
            assertEquals(0, progressRepository.saveCalls)
            assertEquals(listOf(GameUiError.NickName.stringRes), toastService.shownMessages)

            progressRepository.getError = null
            viewModel.onChapterSelected(chapter)
            advanceUntilIdle()
            assertEquals(chapter, viewModel.nickNameChapter.value)
        }
    }

    @Test
    fun `repeated confirmations preserve the chapter and submit one save`() = runTest {
        val viewModel = createViewModel()
        val saveGate = CompletableDeferred<Unit>()
        progressRepository.beforeSave = { saveGate.await() }
        viewModel.events.test {
            viewModel.onChapterSelected(chapter)
            advanceUntilIdle()
            viewModel.onNickNameConfirmed("Alice")
            viewModel.onNickNameConfirmed("Eve")
            viewModel.onChapterSelected(chapter.copy(number = 4, code = "4A"))
            viewModel.onNickNameDismissed()
            advanceUntilIdle()
            assertTrue(viewModel.isSavingNickName.value)
            assertEquals(1, progressRepository.saveCalls)
            assertEquals(chapter, viewModel.nickNameChapter.value)

            saveGate.complete(Unit)
            assertEquals(ChaptersEvent.OpenChapter("3a"), awaitItem())
            viewModel.onChapterSelected(chapter)
            advanceUntilIdle()
            expectNoEvents()
            assertEquals("Alice", progressRepository.get(TestFixtures.GAME_ID).heroName)
        }
    }

    @Test
    fun `saved name bypasses the dialog and a legacy mirror failure blocks launch`() = runTest {
        gameRepository.setGame(
            TestFixtures.GAME_ID,
            TestFixtures.gameCatalog(userNickNameRequired = true, legacyId = 162),
        )
        progressRepository.save(UserGameProgress(gameId = TestFixtures.GAME_ID, heroName = "Alice"))
        friendzonedRepository.saveNameError = IllegalStateException("legacy save failed")
        val viewModel = createViewModel()
        viewModel.events.test {
            viewModel.onChapterSelected(chapter)
            advanceUntilIdle()
            expectNoEvents()
            assertNull(viewModel.nickNameChapter.value)
            assertEquals(listOf(GameUiError.NickName.stringRes), toastService.shownMessages)

            friendzonedRepository.saveNameError = null
            viewModel.onChapterSelected(chapter)
            assertEquals(ChaptersEvent.OpenChapter("3a"), awaitItem())
            assertEquals("Alice", friendzonedRepository.firstNames[162])
        }
    }
}
