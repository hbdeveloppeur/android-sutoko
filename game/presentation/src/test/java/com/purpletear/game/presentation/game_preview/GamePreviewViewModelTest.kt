package com.purpletear.game.presentation.game_preview

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.FakeAppVersionProvider
import com.purpletear.game.presentation.game_preview.fakes.FakeChapterRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameInstallRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeLogger
import com.purpletear.game.presentation.game_preview.fakes.FakeMediaUrlResolver
import com.purpletear.game.presentation.game_preview.fakes.FakeMemoryRepository
import com.purpletear.game.presentation.game_preview.fakes.FakePurchaseRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeToastService
import com.purpletear.game.presentation.game_preview.fakes.FakeUserGameProgressRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeUserRepository
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.game_preview.handlers.GamePreviewPurchaseHandler
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewViewModelTest {

    private val gameRepository = FakeGameRepository()
    private val chapterRepository = FakeChapterRepository()
    private val gameInstallRepository = FakeGameInstallRepository()
    private val purchaseRepository = FakePurchaseRepository()
    private val mediaUrlResolver = FakeMediaUrlResolver()
    private val userRepository = FakeUserRepository()
    private val userGameProgressRepository = FakeUserGameProgressRepository()
    private val memoryRepository = FakeMemoryRepository()
    private val logger = FakeLogger()
    private val appVersionProvider = FakeAppVersionProvider(TestFixtures.APP_BUILD_NUMBER)
    private val toastService = FakeToastService()
    private val purchaseHandler = GamePreviewPurchaseHandler(purchaseRepository)

    private val getChaptersUseCase = GetChaptersUseCase(chapterRepository)
    private val saveUserNickNameUseCase = SaveUserNickNameUseCase(userGameProgressRepository)
    private val restartGameUseCase = RestartGameUseCase(userGameProgressRepository, memoryRepository)
    private val downloadGameUseCase = DownloadGameUseCase(gameRepository, gameInstallRepository, userRepository)

    @Before
    fun setUp() {
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(emptyList()))
    }

    private fun createViewModel(gameId: String = TestFixtures.GAME_ID): GamePreviewViewModel {
        return GamePreviewViewModel(
            savedStateHandle = SavedStateHandle(mapOf("gameId" to gameId)),
            gameRepository = gameRepository,
            chapterRepository = chapterRepository,
            gameInstallRepository = gameInstallRepository,
            gamePurchaseRepository = purchaseRepository,
            mediaUrlResolver = mediaUrlResolver,
            getChaptersUseCase = getChaptersUseCase,
            saveUserNickNameUseCase = saveUserNickNameUseCase,
            toastService = toastService,
            restartGameUseCase = restartGameUseCase,
            downloadGameUseCase = downloadGameUseCase,
            purchaseHandler = purchaseHandler,
            logger = logger,
            appVersionProvider = appVersionProvider,
        )
    }

    @Test
    fun `game emits Loading initially and Data when catalog emits`() = runTest {
        val viewModel = createViewModel()

        viewModel.game.test {
            assertEquals(GamePreviewUiState.Loading, awaitItem())

            gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
            assertTrue(awaitItem() is GamePreviewUiState.Data)
        }
    }

    @Test
    fun `game emits NotFound when catalog is null`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
        }
    }

    @Test
    fun `onAction OnBuy sets isPurchasing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(GamePreviewAction.OnBuy)
        advanceUntilIdle()

        assertTrue(viewModel.isPurchasing.value)
        assertFalse(viewModel.isPurchaseLoading.value)
    }

    @Test
    fun `onAction OnBuyConfirm emits ShowError when no SKU`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.ShowError(GameUiError.Purchase), awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `onAction OnDownload starts download and emits no error`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
            gameRepository.setDownloadLink(TestFixtures.GAME_ID, Result.success("https://example.com/download"))
            gameInstallRepository.setDownloadFlow(TestFixtures.GAME_ID, flowOf(0.5f, 1.0f))
            advanceUntilIdle()

            viewModel.onAction(GamePreviewAction.OnDownload)
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `onAction OnRestartConfirm restarts and emits no error`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnRestartConfirm)
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `start loads chapters and emits ShowError on failure`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            val error = RuntimeException("chapters failed")
            chapterRepository.setChapters(TestFixtures.GAME_ID, Result.failure(error))

            viewModel.start()
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.ShowError(GameUiError.Load), awaitItem())
        }
        assertTrue(logger.exceptions.any { it.throwable.message == "chapters failed" })
    }

    @Test
    fun `onAction OnTry emits PlayGame with isTrial and chapter code`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A"))
        // Keep currentChapter active so its StateFlow value is populated (as in the real screen).
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data) // game.value is now Data

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnTry)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
                event as GamePreviewEvent.PlayGame
                assertTrue(event.isTrial)
                assertEquals("1a", event.chapterCode)
            }
        }
    }

    @Test
    fun `onAction OnTry with nickname required keeps isTrial after confirm`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(
            TestFixtures.GAME_ID,
            TestFixtures.gameCatalog(
                price = 100,
                skus = listOf("sku-1"),
                userNickNameRequired = true,
            ),
        )
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A"))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.currentChapter.collect { } }
        advanceUntilIdle()

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnTry)
                advanceUntilIdle()
                val request = awaitItem()
                assertTrue(request is GamePreviewEvent.RequestNickName)
                request as GamePreviewEvent.RequestNickName
                assertTrue(request.isTrial)

                viewModel.onNickNameConfirmed("Alex", isTrial = request.isTrial)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
                event as GamePreviewEvent.PlayGame
                assertTrue(event.isTrial)
                assertEquals("1a", event.chapterCode)
            }
        }
    }

    @Test
    fun `onAction OnPlay emits PlayGame with isTrial false`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A"))

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnPlay)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
                assertFalse((event as GamePreviewEvent.PlayGame).isTrial)
            }
        }
    }

    @Test
    fun `global premium makes a paid game owned`() = runTest {
        purchaseRepository.setHasGlobalPremium(true)
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            val data = awaitItem()
            assertTrue(data is GamePreviewUiState.Data)
            assertTrue((data as GamePreviewUiState.Data).item.isPurchased)
        }
    }

    @Test
    fun `paid game without sku and without premium is not owned`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            val data = awaitItem()
            assertTrue(data is GamePreviewUiState.Data)
            assertFalse((data as GamePreviewUiState.Data).item.isPurchased)
        }
    }
}
