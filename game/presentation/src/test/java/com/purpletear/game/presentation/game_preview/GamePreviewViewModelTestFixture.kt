package com.purpletear.game.presentation.game_preview

import androidx.lifecycle.SavedStateHandle
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import com.purpletear.game.presentation.game_preview.fakes.FakeChapterRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeFavoriteGamesRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeFriendzonedProgressRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameInstallRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGamePreviewSoundRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeBuyStoryWithCoinsUseCase
import com.purpletear.game.presentation.game_preview.fakes.FakeEntitlementRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeLogger
import com.purpletear.game.presentation.game_preview.fakes.FakeMediaUrlResolver
import com.purpletear.game.presentation.game_preview.fakes.FakeMemoryRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeToastService
import com.purpletear.game.presentation.game_preview.fakes.FakeUserGameProgressRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeUserRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeUserRoleRepository
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.game.presentation.game_preview.handlers.GamePreviewPurchaseHandler
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.RestartGameUseCase
import com.purpletear.sutoko.game.usecase.SaveUserNickNameUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Composed fixture for the GamePreviewViewModel test classes.
 * Owns the fakes, use cases and dispatcher lifecycle; each test class
 * instance gets a fresh fixture, so no state is shared between tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewViewModelTestFixture {

    val gameRepository = FakeGameRepository()
    val chapterRepository = FakeChapterRepository()
    val gameInstallRepository = FakeGameInstallRepository()
    val favoriteGamesRepository = FakeFavoriteGamesRepository()
    val mediaUrlResolver = FakeMediaUrlResolver()
    val userRepository = FakeUserRepository()
    val userRoleRepository = FakeUserRoleRepository()
    val soundRepository = FakeGamePreviewSoundRepository()
    val userGameProgressRepository = FakeUserGameProgressRepository()
    val memoryRepository = FakeMemoryRepository()
    val friendzonedProgressRepository = FakeFriendzonedProgressRepository()
    val logger = FakeLogger()
    private val analyticsTracker = FakeAnalyticsTracker()
    val toastService = FakeToastService()
    val buyStoryWithCoinsUseCase = FakeBuyStoryWithCoinsUseCase()
    val purchaseHandler = GamePreviewPurchaseHandler(buyStoryWithCoinsUseCase)
    val entitlementRepository = FakeEntitlementRepository()

    val getChaptersUseCase = GetChaptersUseCase(chapterRepository)
    val saveUserNickNameUseCase = SaveUserNickNameUseCase(userGameProgressRepository)
    val restartGameUseCase =
        RestartGameUseCase(userGameProgressRepository, memoryRepository, friendzonedProgressRepository)
    val downloadGameUseCase = DownloadGameUseCase(gameRepository, gameInstallRepository, userRepository)

    private val testDispatcher = StandardTestDispatcher()

    fun setUp() {
        // viewModelScope falls back to EmptyCoroutineContext when Dispatchers.Main is unset
        // (see lifecycle createViewModelScope), which would run VM coroutines on real threads
        // and make advanceUntilIdle()-based assertions race.
        Dispatchers.setMain(testDispatcher)
        chapterRepository.setChapters(TestFixtures.GAME_ID, Result.success(emptyList()))
    }

    fun tearDown() {
        Dispatchers.resetMain()
    }

    fun activateStateFlows(
        scope: CoroutineScope,
        viewModel: GamePreviewViewModel,
    ) {
        scope.launch { viewModel.isUserConnected.collect { } }
        scope.launch { viewModel.game.collect { } }
    }

    fun createViewModel(
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
            friendzonedProgressRepository = friendzonedProgressRepository,
            gameInstallRepository = gameInstallRepository,
            mediaUrlResolver = mediaUrlResolver,
            getChaptersUseCase = getChaptersUseCase,
            saveUserNickNameUseCase = saveUserNickNameUseCase,
            toastService = toastService,
            restartGameUseCase = restartGameUseCase,
            downloadGameUseCase = downloadGameUseCase,
            purchaseHandler = purchaseHandler,
            userRepository = userRepository,
            userRoleRepository = userRoleRepository,
            soundRepository = soundRepository,
            entitlementRepository = entitlementRepository,
            analyticsTracker = analyticsTracker,
            logger = logger,
        )
    }

    private class FakeAnalyticsTracker : AnalyticsTracker {
        val events = mutableListOf<String>()
        override fun logEvent(name: String, params: Map<String, Any?>) {
            events += name
        }
        override fun setUserProperty(name: String, value: String?) = Unit
    }
}
