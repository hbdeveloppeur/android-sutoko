package com.purpletear.game.presentation.game_preview

import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.game.GameDownloadState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewInstallActionsTest {
    private val fixture = GamePreviewViewModelTestFixture()

    @Before fun setUp() = fixture.setUp()
    @After fun tearDown() = fixture.tearDown()

    @Test
    fun `delete cannot overtake an update waiting for its download link`() = runTest {
        val gate = CompletableDeferred<Unit>()
        fixture.gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(version = 2))
        fixture.gameInstallRepository.setInstall(TestFixtures.GAME_ID, TestFixtures.gameInstall(localVersion = 1))
        fixture.gameRepository.setDownloadLink(TestFixtures.GAME_ID, Result.success("https://example.com/game.zip"))
        fixture.gameRepository.downloadLinkGate = gate
        val vm = fixture.createViewModel()
        fixture.activateStateFlows(backgroundScope, vm)
        runCurrent()

        vm.onAction(GamePreviewAction.OnUpdateGame)
        runCurrent()
        assertEquals(GameDownloadState.Preparing, (vm.game.value as GamePreviewUiState.Data).item.downloadState)
        vm.onAction(GamePreviewAction.OnDelete)
        runCurrent()
        assertTrue(fixture.gameInstallRepository.deletions.isEmpty())

        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, fixture.gameInstallRepository.downloadCalls)
    }

    @Test
    fun `deletion uses the same legacy directory identifier as installation`() = runTest {
        fixture.gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(legacyId = 42))
        val vm = fixture.createViewModel()
        fixture.activateStateFlows(backgroundScope, vm)
        runCurrent()
        vm.onAction(GamePreviewAction.OnDelete)
        advanceUntilIdle()
        assertEquals(listOf(TestFixtures.GAME_ID to 42), fixture.gameInstallRepository.deletions)
    }

    @Test
    fun `trial downloads the current archive when its installed version is obsolete`() = runTest {
        fixture.gameRepository.setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog(version = 2, price = 100))
        fixture.gameInstallRepository.setInstall(TestFixtures.GAME_ID, TestFixtures.gameInstall(localVersion = 1))
        fixture.gameRepository.setDownloadLink(TestFixtures.GAME_ID, Result.success("https://example.com/game.zip"))
        fixture.chapterRepository.setCurrentChapter(TestFixtures.GAME_ID, Chapter(number = 1, code = "1A", available = true))
        val vm = fixture.createViewModel()
        fixture.activateStateFlows(backgroundScope, vm)
        backgroundScope.launch { vm.currentChapter.collect {} }
        runCurrent()
        vm.onAction(GamePreviewAction.OnTry)
        advanceUntilIdle()
        assertEquals(1, fixture.gameInstallRepository.downloadCalls)
    }
}
