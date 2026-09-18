package com.purpletear.game.presentation.game_preview

import com.purpletear.game.presentation.game_preview.events.GamePreviewEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewEventDeliveryTest {
    private val fixture = GamePreviewViewModelTestFixture()

    @Before
    fun setUp() = fixture.setUp()

    @After
    fun tearDown() = fixture.tearDown()

    @Test
    fun `events remain ordered and are not dropped while the collector is busy`() = runTest {
        val viewModel = fixture.createViewModel()
        val releaseCollector = CompletableDeferred<Unit>()
        val received = mutableListOf<GamePreviewEvent>()
        backgroundScope.launch {
            viewModel.events.collect {
                received += it
                releaseCollector.await()
            }
        }
        runCurrent()

        viewModel.onAction(GamePreviewAction.OnRestart)
        runCurrent()
        viewModel.onAction(GamePreviewAction.OnUpdateApp)
        viewModel.onAction(GamePreviewAction.OnRestart)
        runCurrent()
        assertEquals(listOf(GamePreviewEvent.ShowRestartDialog), received)

        releaseCollector.complete(Unit)
        runCurrent()
        assertEquals(
            listOf(
                GamePreviewEvent.ShowRestartDialog,
                GamePreviewEvent.OpenAppStore,
                GamePreviewEvent.ShowRestartDialog,
            ),
            received,
        )
    }
}
