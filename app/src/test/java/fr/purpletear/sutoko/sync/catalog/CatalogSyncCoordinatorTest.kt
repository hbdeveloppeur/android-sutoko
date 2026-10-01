package fr.purpletear.sutoko.sync.catalog

import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogSyncCoordinatorTest {
    private val repository = mockk<GameRepository>()
    private val installs = mockk<GameInstallRepository>(relaxed = true)
    private val coordinator = CatalogSyncCoordinator(repository, installs)

    @Test
    fun `failed sync finishes loading and can be retried`() = runTest {
        coEvery { repository.syncOfficialGames(any()) } returns Result.failure(IOException("offline"))
        assertEquals(CatalogSyncStatus.Loading, coordinator.status.value)
        coordinator.sync()
        assertEquals(CatalogSyncStatus.Failed, coordinator.status.value)

        coEvery { repository.syncOfficialGames(any()) } returns Result.success(Unit)
        coEvery { repository.observeOfficialGames() } returns flowOf(emptyList())
        coordinator.sync()
        assertEquals(CatalogSyncStatus.Ready, coordinator.status.value)
        coVerify(exactly = 1) { installs.ensureBuiltInGamesInstalled(emptyList()) }
    }

    @Test
    fun `concurrent refreshes do not duplicate network requests`() = runTest {
        val result = CompletableDeferred<Result<Unit>>()
        coEvery { repository.syncOfficialGames(any()) } coAnswers { result.await() }
        coEvery { repository.observeOfficialGames() } returns flowOf(emptyList())
        val first = launch { coordinator.sync() }
        runCurrent()
        coordinator.sync()
        result.complete(Result.success(Unit))
        first.join()
        coVerify(exactly = 1) { repository.syncOfficialGames(any()) }
        assertEquals(CatalogSyncStatus.Ready, coordinator.status.value)
    }

    @Test
    fun `cancellation releases the sync lock and does not leave a permanent loading state`() = runTest {
        coEvery { repository.syncOfficialGames(any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
        val first = launch { coordinator.sync() }
        runCurrent()
        first.cancel()
        first.join()
        assertEquals(CatalogSyncStatus.Failed, coordinator.status.value)
        coEvery { repository.syncOfficialGames(any()) } returns Result.failure(IOException("offline"))
        coordinator.sync()
        coVerify(exactly = 2) { repository.syncOfficialGames(any()) }
    }
}
