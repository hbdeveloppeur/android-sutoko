package com.purpletear.game.presentation.game_preview

import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.sutoko.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewEntitlementIdentityTest {
    private val fixture = GamePreviewViewModelTestFixture()
    @Before fun setUp() = fixture.setUp()
    @After fun tearDown() = fixture.tearDown()

    @Test
    fun `switching connected accounts checks ownership again`() = runTest {
        fixture.gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-a")))
        val vm = fixture.createViewModel(connectedUser = true)
        fixture.activateStateFlows(backgroundScope, vm)
        vm.start()
        advanceUntilIdle()
        assertEquals(1, fixture.entitlementRepository.refreshGrantCalls)

        fixture.entitlementRepository.refreshGrantResult = Result.success(true)
        fixture.userRepository.setUser(User("user-2", "token-2"))
        advanceUntilIdle()
        assertEquals(2, fixture.entitlementRepository.refreshGrantCalls)
        assertTrue((vm.game.value as GamePreviewUiState.Data).item.isPurchased)
    }

    @Test
    fun `changed catalog SKU receives a new ownership check`() = runTest {
        fixture.gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-a")))
        val vm = fixture.createViewModel(connectedUser = true)
        fixture.activateStateFlows(backgroundScope, vm)
        vm.start()
        advanceUntilIdle()
        assertEquals(1, fixture.entitlementRepository.refreshGrantCalls)

        fixture.gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(price = 100, skus = listOf("sku-b")))
        advanceUntilIdle()
        assertEquals(2, fixture.entitlementRepository.refreshGrantCalls)
        fixture.gameInstallRepository.setDownloadProgress(TestFixtures.GAME_ID, 0.3f)
        advanceUntilIdle()
        assertEquals(2, fixture.entitlementRepository.refreshGrantCalls)
    }
}
