package com.purpletear.game.presentation.game_preview

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import com.purpletear.game.presentation.game_preview.fakes.FakeAppVersionProvider
import com.purpletear.game.presentation.game_preview.fakes.FakeChapterRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeFavoriteGamesRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeFriendzonedProgressRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameInstallRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeBuyStoryWithCoinsUseCase
import com.purpletear.game.presentation.game_preview.fakes.FakeIsStoryGrantedUseCase
import com.purpletear.game.presentation.game_preview.fakes.FakeLogger
import com.purpletear.game.presentation.game_preview.fakes.FakeMediaUrlResolver
import com.purpletear.game.presentation.game_preview.fakes.FakeMemoryRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeObserveCoinPurchasedSkusUseCase
import com.purpletear.game.presentation.game_preview.fakes.FakePurchaseRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeToastService
import com.purpletear.game.presentation.game_preview.fakes.FakeUserGameProgressRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeUserRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeUserRoleRepository
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.game_preview.handlers.GamePreviewPurchaseHandler
import com.purpletear.game.presentation.model.GameUiError
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserRole
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class GamePreviewViewModelTest {

    private val gameRepository = FakeGameRepository()
    private val chapterRepository = FakeChapterRepository()
    private val gameInstallRepository = FakeGameInstallRepository()
    private val favoriteGamesRepository = FakeFavoriteGamesRepository()
    private val purchaseRepository = FakePurchaseRepository()
    private val mediaUrlResolver = FakeMediaUrlResolver()
    private val userRepository = FakeUserRepository()
    private val userRoleRepository = FakeUserRoleRepository()
    private val userGameProgressRepository = FakeUserGameProgressRepository()
    private val memoryRepository = FakeMemoryRepository()
    private val friendzonedProgressRepository = FakeFriendzonedProgressRepository()
    private val logger = FakeLogger()
    private val appVersionProvider = FakeAppVersionProvider(TestFixtures.APP_BUILD_NUMBER)
    private val toastService = FakeToastService()
    private val buyStoryWithCoinsUseCase = FakeBuyStoryWithCoinsUseCase()
    private val purchaseHandler = GamePreviewPurchaseHandler(buyStoryWithCoinsUseCase)
    private val observeCoinPurchasedSkusUseCase = FakeObserveCoinPurchasedSkusUseCase()
    private val isStoryGrantedUseCase = FakeIsStoryGrantedUseCase()

    private val getChaptersUseCase = GetChaptersUseCase(chapterRepository)
    private val saveUserNickNameUseCase = SaveUserNickNameUseCase(userGameProgressRepository)
    private val restartGameUseCase =
        RestartGameUseCase(userGameProgressRepository, memoryRepository, friendzonedProgressRepository)
    private val downloadGameUseCase = DownloadGameUseCase(gameRepository, gameInstallRepository, userRepository)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // viewModelScope falls back to EmptyCoroutineContext when Dispatchers.Main is unset
        // (see lifecycle createViewModelScope), which would run VM coroutines on real threads
        // and make advanceUntilIdle()-based assertions race.
        Dispatchers.setMain(testDispatcher)
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun activateStateFlows(
        scope: CoroutineScope,
        viewModel: GamePreviewViewModel,
    ) {
        scope.launch { viewModel.isUserConnected.collect { } }
        scope.launch { viewModel.game.collect { } }
    }

    private fun createViewModel(
        gameId: String = TestFixtures.GAME_ID,
        connectedUser: Boolean = false,
    ): GamePreviewViewModel {
        if (connectedUser) {
            userRepository.setUser(User(id = "user-1", token = "token-1"))
        }
        return GamePreviewViewModel(
            savedStateHandle = SavedStateHandle(mapOf("gameId" to gameId)),
            gameRepository = gameRepository,
            favoriteGamesRepository = favoriteGamesRepository,
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
            userRepository = userRepository,
            userRoleRepository = userRoleRepository,
            observeCoinPurchasedSkusUseCase = observeCoinPurchasedSkusUseCase,
            isStoryGrantedUseCase = isStoryGrantedUseCase,
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
    fun `options entry point is visible only for the tester uid`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.isOptionsVisible.collect { } }
        advanceUntilIdle()
        assertFalse(viewModel.isOptionsVisible.value)

        userRepository.setUser(User(id = "user-1", token = "token-1"))
        advanceUntilIdle()
        assertFalse(viewModel.isOptionsVisible.value)

        userRepository.setUser(User(id = "8be954c7a18f4e7cba9c", token = "token-2"))
        advanceUntilIdle()
        assertTrue(viewModel.isOptionsVisible.value)
    }

    @Test
    fun `administrator role is exposed as isAdmin`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.isAdmin.collect { } }
        advanceUntilIdle()
        assertFalse(viewModel.isAdmin.value)

        userRoleRepository.set(UserRole.ADMINISTRATOR)
        advanceUntilIdle()
        assertTrue(viewModel.isAdmin.value)
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
    fun `game emits NotFound when catalog is null`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
        }
        assertTrue(logger.warnings.isEmpty())
    }

    @Test
    fun `game emits NotFound after start logs warning`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
        }
        assertTrue(logger.warnings.any { it.message.contains("not found locally") })
    }

    @Test
    fun `NotFound then recovery success self-heals to Data`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        gameRepository.getGameCatalogResult = Result.success(TestFixtures.gameCatalog())
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            // NotFound may be conflated away by the StateFlow; the healed state must be Data.
            assertTrue(expectMostRecentItem() is GamePreviewUiState.Data)
        }
        assertEquals(1, gameRepository.getGameCatalogCalls)
    }

    @Test
    fun `NotFound recovery failure keeps NotFound and attempts repository once`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        gameRepository.getGameCatalogResult = Result.failure(RuntimeException("network"))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
            expectNoEvents()
        }
        assertEquals(1, gameRepository.getGameCatalogCalls)
        assertTrue(logger.warnings.any { it.message.contains("remote recovery failed") })
    }

    @Test
    fun `refresh on NotFound triggers one more recovery attempt`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        gameRepository.getGameCatalogResult = Result.failure(RuntimeException("network"))
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()
        assertEquals(1, gameRepository.getGameCatalogCalls)

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, gameRepository.getGameCatalogCalls)
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `start refreshes catalog from remote once data is shown`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertTrue(expectMostRecentItem() is GamePreviewUiState.Data)
        }
        assertEquals(1, gameRepository.refreshGameCatalogCalls)
    }

    @Test
    fun `start does not refresh catalog remotely while catalog is missing`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, null)
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertEquals(GamePreviewUiState.NotFound, awaitItem())
        }
        assertEquals(0, gameRepository.refreshGameCatalogCalls)
    }

    @Test
    fun `remote catalog refresh failure keeps cached data`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        gameRepository.refreshGameCatalogResult = Result.failure(RuntimeException("network"))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            viewModel.start()
            advanceUntilIdle()
            assertTrue(expectMostRecentItem() is GamePreviewUiState.Data)
        }
        assertEquals(1, gameRepository.refreshGameCatalogCalls)
    }

    @Test
    fun `refresh on Data refreshes catalog from remote once more`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()
        assertEquals(1, gameRepository.refreshGameCatalogCalls)

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, gameRepository.refreshGameCatalogCalls)
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `onAction OnBuy when not connected emits OpenAccountConnection`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.OpenAccountConnection, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `onAction OnBuy when connected sets isPurchasing`() = runTest {
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.onAction(GamePreviewAction.OnBuy)
        advanceUntilIdle()

        assertTrue(viewModel.isPurchasing.value)
        assertFalse(viewModel.isPurchaseLoading.value)
    }

    @Test
    fun `onAction OnBuyConfirm emits ShowError when no SKU`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

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
    fun `start loads empty chapters and logs warning`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(emptyList()))

            viewModel.start()
            advanceUntilIdle()

            expectNoEvents()
        }
        assertTrue(logger.warnings.any { it.message.contains("empty chapter list") })
    }

    @Test
    fun `refresh reloads chapters and resets isRefreshing`() = runTest {
        val viewModel = createViewModel()

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(0, gameRepository.syncOfficialGamesCalls)
        assertEquals(1, chapterRepository.getChaptersCalls)
        assertFalse(viewModel.isRefreshing.value)
        assertTrue(toastService.shownMessages.isEmpty())
    }

    @Test
    fun `refresh shows toast and resets isRefreshing on failure`() = runTest {
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.failure(RuntimeException("chapters failed")))
        val viewModel = createViewModel()

        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(toastService.shownMessages.contains(R.string.game_presentation_error_load_game))
        assertFalse(viewModel.isRefreshing.value)
        assertTrue(logger.warnings.any { it.message.contains("refresh failed") })
    }

    @Test
    fun `refresh while already refreshing is ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        chapterRepository.getChaptersGate = gate
        val viewModel = createViewModel()

        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(viewModel.isRefreshing.value)

        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(1, chapterRepository.getChaptersCalls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.isRefreshing.value)
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
        backgroundScope.launch { viewModel.currentChapter.collect { } }

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)
            advanceUntilIdle()

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
    fun `onAction OnPlay with unavailable chapter shows toast and does not navigate`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 2, code = "1B", releaseDate = System.currentTimeMillis() / 1000 + 86_400),
        )
        backgroundScope.launch { viewModel.currentChapter.collect { } }
        val events = mutableListOf<GamePreviewEvent>()
        backgroundScope.launch { viewModel.events.collect { events.add(it) } }
        advanceUntilIdle()

        viewModel.onAction(GamePreviewAction.OnPlay)
        advanceUntilIdle()

        assertTrue(toastService.shownMessages.contains(R.string.game_presentation_game_preview_next_chapter))
        assertTrue(events.none { it is GamePreviewEvent.PlayGame })
    }

    @Test
    fun `onAction OnPlay with unavailable chapter as admin still navigates`() = runTest {
        val viewModel = createViewModel()
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        chapterRepository.setCurrentChapter(
            TestFixtures.GAME_ID,
            Chapter(number = 2, code = "1B", releaseDate = System.currentTimeMillis() / 1000 + 86_400),
        )
        userRoleRepository.set(UserRole.ADMINISTRATOR)
        backgroundScope.launch { viewModel.currentChapter.collect { } }
        backgroundScope.launch { viewModel.isAdmin.collect { } }

        viewModel.game.test {
            skipItems(1) // Loading
            assertTrue(awaitItem() is GamePreviewUiState.Data)
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.onAction(GamePreviewAction.OnPlay)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GamePreviewEvent.PlayGame)
            }
        }
        assertTrue(toastService.shownMessages.isEmpty())
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

    @Test
    fun `coin purchased sku makes a paid game owned`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        observeCoinPurchasedSkusUseCase.setSkus(setOf("sku-1"))
        val viewModel = createViewModel()

        viewModel.game.test {
            skipItems(1) // Loading
            val data = awaitItem()
            assertTrue(data is GamePreviewUiState.Data)
            assertTrue((data as GamePreviewUiState.Data).item.isPurchased)
        }
    }

    @Test
    fun `coin grant check runs once when connected and story unbought`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()

        assertEquals(1, isStoryGrantedUseCase.calls)
    }

    @Test
    fun `coin grant check is deferred until the user connects`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        val viewModel = createViewModel()
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()
        assertEquals(0, isStoryGrantedUseCase.calls)

        userRepository.setUser(User(id = "user-1", token = "token-1"))
        advanceUntilIdle()
        assertEquals(1, isStoryGrantedUseCase.calls)
    }

    @Test
    fun `coin grant check retries transient failures until a definitive answer`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        isStoryGrantedUseCase.enqueueResults(
            listOf(
                Result.failure(RuntimeException("network")),
                Result.failure(RuntimeException("network")),
                Result.success(true),
            )
        )
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()

        assertEquals(3, isStoryGrantedUseCase.calls)
    }

    @Test
    fun `coin grant check gives up after bounded retries and refresh grants a fresh round`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        isStoryGrantedUseCase.enqueueResults(
            listOf(
                Result.failure(RuntimeException("network")),
                Result.failure(RuntimeException("network")),
                Result.failure(RuntimeException("network")),
            )
        )
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)

        viewModel.start()
        advanceUntilIdle()
        assertEquals(3, isStoryGrantedUseCase.calls)

        viewModel.refresh()
        advanceUntilIdle()
        assertEquals(4, isStoryGrantedUseCase.calls)
    }

    @Test
    fun `successful coin purchase emits PurchaseSuccess`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.success(com.purpletear.sutoko.shop.domain.repository.model.Balance(coins = 900, diamonds = 0)))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.PurchaseSuccess, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `coin purchase already owned emits ShowAlreadyBoughtAlert`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.failure(com.purpletear.sutoko.shop.domain.error.BuyStoryError.AlreadyOwned()))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.ShowAlreadyBoughtAlert, awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `coin purchase not purchasable emits ShowError`() = runTest {
        gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")))
        buyStoryWithCoinsUseCase.setResult("sku-1", Result.failure(com.purpletear.sutoko.shop.domain.error.BuyStoryError.NotPurchasable()))
        val viewModel = createViewModel(connectedUser = true)
        activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(GamePreviewAction.OnBuy)
            viewModel.onAction(GamePreviewAction.OnBuyConfirm)
            advanceUntilIdle()

            assertEquals(GamePreviewEvent.ShowError(GameUiError.Purchase), awaitItem())
        }
        assertFalse(viewModel.isPurchasing.value)
    }

    @Test
    fun `releasedChaptersCount is null when no chapters are stored`() = runTest {
        val viewModel = createViewModel()

        viewModel.releasedChaptersCount.test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `releasedChaptersCount counts only available chapters`() = runTest {
        val viewModel = createViewModel()
        val now = System.currentTimeMillis() / 1000

        viewModel.releasedChaptersCount.test {
            assertEquals(null, awaitItem())

            chapterRepository.setChapters(
                TestFixtures.GAME_ID,
                Result.success(
                    listOf(
                        Chapter(id = "1", number = 1, releaseDate = now - 200),
                        Chapter(id = "2", number = 2, releaseDate = now - 100),
                        Chapter(id = "3", number = 3, releaseDate = now + 100_000),
                    )
                )
            )

            assertEquals(2, awaitItem())
        }
    }
}
