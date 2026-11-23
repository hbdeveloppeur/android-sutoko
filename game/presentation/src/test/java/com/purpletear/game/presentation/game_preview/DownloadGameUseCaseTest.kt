package com.purpletear.game.presentation.game_preview

import com.purpletear.game.presentation.game_preview.fakes.FakeGameInstallRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeGameRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeUserRepository
import com.purpletear.game.presentation.game_preview.fakes.TestFixtures
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.game.usecase.DownloadGameUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadGameUseCaseTest {
    private val games = FakeGameRepository().apply {
        setGame(TestFixtures.GAME_ID, TestFixtures.gameCatalog())
        setDownloadLink(TestFixtures.GAME_ID, Result.success("https://example.invalid/story.zip"))
    }
    private val installs = FakeGameInstallRepository().apply {
        setDownloadFlow(TestFixtures.GAME_ID, flowOf(1f))
    }
    private val users = FakeUserRepository()
    private val download = DownloadGameUseCase(games, installs, users)

    @Test
    fun `tester uses the ordinary download identity`() = runTest {
        val user = User("8be954c7a18f4e7cba9c", "local-test-token")
        users.setUser(user)
        assertEquals(listOf(1f), download(TestFixtures.GAME_ID).toList())
        assertEquals(listOf(Triple(TestFixtures.GAME_ID, user.id, user.token)), games.downloadLinkRequests)
        assertEquals(1, installs.downloadCalls)
    }

    @Test
    fun `anonymous player can still download a free story`() = runTest {
        assertEquals(listOf(1f), download(TestFixtures.GAME_ID).toList())
        assertEquals(listOf(Triple(TestFixtures.GAME_ID, null, null)), games.downloadLinkRequests)
        assertEquals(1, installs.downloadCalls)
    }
}
