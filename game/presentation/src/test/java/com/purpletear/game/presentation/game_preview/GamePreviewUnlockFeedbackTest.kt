package com.purpletear.game.presentation.game_preview

import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewUnlockFeedbackTest {
    private val fixture = GamePreviewViewModelTestFixture()

    @Before
    fun setUp() = fixture.setUp()

    @After
    fun tearDown() = fixture.tearDown()

    @Test
    fun `purchase feedback survives without an event collector until acknowledged`() = runTest {
        fixture.gameRepository.setGame(
            TestFixtures.GAME_ID,
            TestFixtures.gameCatalog(price = 100, skus = listOf("sku-1")),
        )
        val viewModel = fixture.createViewModel(connectedUser = true)
        fixture.activateStateFlows(backgroundScope, viewModel)
        advanceUntilIdle()

        viewModel.onAction(GamePreviewAction.OnBuy)
        advanceUntilIdle()
        viewModel.onAction(GamePreviewAction.OnBuyConfirm)
        advanceUntilIdle()

        assertTrue(viewModel.unlockFeedbackPending.value)
        viewModel.onResume()
        assertTrue(viewModel.unlockFeedbackPending.value)
        viewModel.onUnlockFeedbackShown()
        assertFalse(viewModel.unlockFeedbackPending.value)
    }
}
