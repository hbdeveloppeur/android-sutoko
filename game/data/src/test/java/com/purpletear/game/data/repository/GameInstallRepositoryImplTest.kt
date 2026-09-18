package com.purpletear.game.data.repository

import com.purpletear.game.data.file.GameFileManager
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.entity.GameInstallEntity
import com.purpletear.sutoko.game.model.game.GameDownloadRequest
import com.purpletear.sutoko.game.model.game.GameDownloadState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class GameInstallRepositoryImplTest {
    @Test
    fun `preparation starts before resolving the link and completion retains the installed version`() = runBlocking {
        val repository = GameInstallRepositoryImpl(FakeDao(), FakeFiles())
        repository.download("game") {
            assertEquals(GameDownloadState.Preparing, repository.observeDownloadState("game").first())
            GameDownloadRequest("url", "2")
        }.toList()

        assertEquals(GameDownloadState.Completed(2), repository.observeDownloadState("game").first())
        assertNull(repository.observeDownloadProgress("game").first())
    }

    @Test
    fun `unknown download and installation remain distinct while repeated transfer progress is deduplicated`() = runBlocking {
        val files = FakeFiles()
        val repository = GameInstallRepositoryImpl(FakeDao(), files)
        files.reportStates = { report ->
            report(GameDownloadState.Downloading(null))
            assertEquals(GameDownloadState.Downloading(null), repository.observeDownloadState("game").first())
            repeat(3) { report(GameDownloadState.Downloading(0.99f)) }
            report(GameDownloadState.Installing)
            assertEquals(GameDownloadState.Installing, repository.observeDownloadState("game").first())
        }

        assertEquals(listOf(0f, 0.99f, 1f), repository.download().toList())
    }

    @Test
    fun `link failure retains failure without changing an existing install`() = runBlocking {
        val dao = FakeDao().apply { record = GameInstallEntity("game", 1) }
        val repository = GameInstallRepositoryImpl(dao, FakeFiles())
        assertTrue(runCatching {
            repository.download("game") { error("Offline") }.toList()
        }.isFailure)

        assertEquals(GameDownloadState.Failed, repository.observeDownloadState("game").first())
        assertEquals(1, dao.record?.localVersion)
    }

    @Test
    fun `cancelling link resolution reports cancellation and allows retry`() = runBlocking {
        withTimeout(5_000) {
            val repository = GameInstallRepositoryImpl(FakeDao(), FakeFiles())
            val preparing = CompletableDeferred<Unit>()
            val job = launch {
                repository.download("game") {
                    preparing.complete(Unit)
                    awaitCancellation()
                }.toList()
            }
            preparing.await()
            repository.cancelDownload("game")
            job.join()

            assertEquals(GameDownloadState.Cancelled, repository.observeDownloadState("game").first())
            assertEquals(1f, repository.download().toList().last())
        }
    }

    @Test
    fun `deleting an installed game clears the retained completion version`() = runBlocking {
        val repository = GameInstallRepositoryImpl(FakeDao(), FakeFiles())
        repository.download().toList()
        assertTrue(repository.deleteGame("game").isSuccess)
        assertNull(repository.observeDownloadState("game").first())
    }

    @Test
    fun `external install replacement or removal invalidates completion for active and new observers`() = runBlocking {
        withTimeout(5_000) {
            for (replacement in listOf(GameInstallEntity("game", 3), null)) {
                val dao = FakeDao()
                val repository = GameInstallRepositoryImpl(dao, FakeFiles())
                repository.download().toList()
                val observed = MutableStateFlow<GameDownloadState?>(null)
                val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                    repository.observeDownloadState("game").collect { observed.value = it }
                }
                try {
                    observed.first { it == GameDownloadState.Completed(2) }
                    dao.record = replacement
                    observed.first { it == null }
                    assertNull(repository.observeDownloadState("game").first())
                } finally {
                    collector.cancelAndJoin()
                }
            }
        }
    }

    @Test
    fun `cancelling deletion completes database cleanup before releasing the directory`() = runBlocking {
        withTimeout(5_000) {
            val dao = FakeDao().apply { record = GameInstallEntity("game", 1) }
            val files = FakeFiles()
            val repository = GameInstallRepositoryImpl(dao, files)
            val filesDeleted = CompletableDeferred<Unit>()
            val finishDeletion = CompletableDeferred<Unit>()
            files.delete = { filesDeleted.complete(Unit); finishDeletion.await() }
            val job = launch { repository.deleteGame("game", 42) }
            filesDeleted.await()
            job.cancel()
            assertTrue(runCatching { repository.download().toList() }.isFailure)
            finishDeletion.complete(Unit)
            job.join()
            assertNull(dao.record)
            assertEquals(1f, repository.download().toList().last())
        }
    }

    @Test
    fun `delete during download returns failure without deleting files`() = runBlocking {
        withTimeout(5_000) {
            val files = FakeFiles()
            val repository = GameInstallRepositoryImpl(FakeDao(), files)
            val downloading = CompletableDeferred<Unit>()
            files.download = { downloading.complete(Unit); awaitCancellation() }
            val job = launch { repository.download().toList() }
            downloading.await()
            assertTrue(repository.deleteGame("game", 42).isFailure)
            assertEquals(0, files.deleteCalls)
            job.cancelAndJoin()
            assertTrue(repository.deleteGame("game", 42).isSuccess)
            assertEquals(1, files.deleteCalls)
        }
    }

    @Test
    fun `download cannot start while deletion owns the game directory`() = runBlocking {
        withTimeout(5_000) {
            val files = FakeFiles()
            val repository = GameInstallRepositoryImpl(FakeDao(), files)
            val deleting = CompletableDeferred<Unit>()
            val finishDeletion = CompletableDeferred<Unit>()
            files.delete = { deleting.complete(Unit); finishDeletion.await() }
            val job = launch { assertTrue(repository.deleteGame("game", 42).isSuccess) }
            deleting.await()
            assertTrue(runCatching { repository.download().toList() }.isFailure)
            finishDeletion.complete(Unit)
            job.join()
            assertEquals(1f, repository.download().toList().last())
        }
    }

    @Test
    fun `read failure releases download and allows retry`() = runBlocking {
        val dao = FakeDao()
        val repository = GameInstallRepositoryImpl(dao, FakeFiles())
        dao.read = { error("Database unavailable") }
        assertTrue(runCatching { repository.download().toList() }.isFailure)
        assertNull(repository.observeDownloadProgress("game").first())
        dao.read = { dao.record }
        assertEquals(1f, repository.download().toList().last())
    }

    @Test
    fun `cancellation while reading releases download and allows retry`() = runBlocking {
        withTimeout(5_000) {
            val dao = FakeDao()
            val repository = GameInstallRepositoryImpl(dao, FakeFiles())
            val reading = CompletableDeferred<Unit>()
            dao.read = { reading.complete(Unit); awaitCancellation() }
            val job = launch { repository.download().toList() }
            reading.await()
            job.cancelAndJoin()
            assertNull(repository.observeDownloadProgress("game").first())
            dao.read = { dao.record }
            assertEquals(1f, repository.download().toList().last())
        }
    }

    @Test
    fun `upsert failure preserves existing install and allows retry`() = runBlocking {
        val dao = FakeDao()
        val previous = GameInstallEntity("game", 1)
        dao.record = previous
        dao.failNextUpsert = true
        val repository = GameInstallRepositoryImpl(dao, FakeFiles())
        assertTrue(runCatching { repository.download().toList() }.isFailure)
        assertEquals(previous, dao.record)
        assertNull(repository.observeDownloadProgress("game").first())
        assertEquals(1f, repository.download().toList().last())
    }

    @Test
    fun `cancel keeps download reserved until rollback finishes`() = runBlocking {
        withTimeout(5_000) {
            val dao = FakeDao()
            val files = FakeFiles()
            val repository = GameInstallRepositoryImpl(dao, files)
            val downloading = CompletableDeferred<Unit>()
            val rollingBack = CompletableDeferred<Unit>()
            val releaseRollback = CompletableDeferred<Unit>()
            files.download = { downloading.complete(Unit); awaitCancellation() }
            dao.beforeDelete = { rollingBack.complete(Unit); releaseRollback.await() }
            val job = launch { repository.download().toList() }
            downloading.await()
            repository.cancelDownload("game")
            rollingBack.await()
            assertNotNull(repository.observeDownloadProgress("game").first())
            assertTrue(runCatching { repository.download().toList() }.isFailure)
            releaseRollback.complete(Unit)
            job.join()
            assertNull(dao.record)
            assertNull(repository.observeDownloadProgress("game").first())
            files.download = { "path" }
            assertEquals(1f, repository.download().toList().last())
        }
    }

    private fun GameInstallRepositoryImpl.download() = download("game", "url", "2", null)

    private class FakeDao : GameInstallationDao {
        private val records = MutableStateFlow<GameInstallEntity?>(null)
        var record: GameInstallEntity?
            get() = records.value
            set(value) { records.value = value }
        var read: suspend () -> GameInstallEntity? = { record }
        var beforeDelete: suspend () -> Unit = {}
        var failNextUpsert = false
        override fun observeByGameId(gameId: String): Flow<GameInstallEntity?> = flow {
            emit(read())
            emitAll(records)
        }
        override fun observeAll() = flowOf(listOfNotNull(record))
        override suspend fun upsert(entity: GameInstallEntity) {
            if (failNextUpsert) {
                failNextUpsert = false
                error("Write failed")
            }
            record = entity
        }
        override suspend fun deleteByGameId(gameId: String) {
            beforeDelete()
            record = null
        }
        override suspend fun markDownloaded(gameId: String, version: String) {
            record = GameInstallEntity(gameId, version.toInt())
        }
    }

    private class FakeFiles : GameFileManager {
        var download: suspend () -> String = { "path" }
        var reportStates: suspend (suspend (GameDownloadState) -> Unit) -> Unit = {}
        var delete: suspend () -> Unit = {}
        var deleteCalls = 0
        override suspend fun downloadAndExtract(
            gameId: String,
            downloadUrl: String,
            onState: suspend (GameDownloadState) -> Unit,
            legacyId: Int?,
        ): String {
            reportStates(onState)
            return download()
        }
        override suspend fun deleteGame(gameId: String, legacyId: Int?) {
            deleteCalls++
            delete()
        }
        override fun getInstallPath(gameId: String, legacyId: Int?) = "path"
    }
}
