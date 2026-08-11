package com.purpletear.game.data.repository

import com.purpletear.game.data.remote.dto.GameDto
import com.purpletear.game.data.remote.dto.toDomain
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GameRepositoryImplCatalogTest {

    @Test
    fun `getGameCatalog returns local catalog without api call`() = runTest {
        val recordingDao = RecordingGameDao()
        recordingDao.game = stubGameDto("game-1").toDomain()
        val api = object : FakeGameApi() {
            // getStory not overridden: any call fails the test via NotImplementedError
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.getGameCatalog("game-1", "fr-FR")

        assertTrue("Expected success but got $result", result.isSuccess)
        assertEquals("game-1", result.getOrThrow()?.id)
        assertTrue(recordingDao.upsertAllCalls.isEmpty())
    }

    @Test
    fun `getGameCatalog fetches remotely and persists on local miss`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> {
                assertEquals("game-1", gameId)
                assertEquals("fr-FR", languageCode)
                return Response.success(stubGameDto("game-1"))
            }
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.getGameCatalog("game-1", "fr-FR")

        assertTrue("Expected success but got $result", result.isSuccess)
        assertEquals("game-1", result.getOrThrow()?.id)
        assertEquals(1, recordingDao.upsertAllCalls.size)
        assertEquals("game-1", recordingDao.upsertAllCalls.first().first().id)
    }

    @Test
    fun `getGameCatalog returns success null on 404`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> =
                Response.error(404, "story_not_found".toResponseBody(null))
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.getGameCatalog("game-1", "fr-FR")

        assertTrue("Expected success but got $result", result.isSuccess)
        assertEquals(null, result.getOrThrow())
        assertTrue(recordingDao.upsertAllCalls.isEmpty())
    }

    @Test
    fun `getGameCatalog returns failure with HttpException on 500`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> =
                Response.error(500, "Server error".toResponseBody(null))
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.getGameCatalog("game-1", "fr-FR")

        assertTrue("Expected failure but got $result", result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
        assertTrue(recordingDao.upsertAllCalls.isEmpty())
    }

    @Test
    fun `getGameCatalog rethrows CancellationException`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> =
                throw CancellationException("cancelled")
        }
        val repository = gameRepository(api)

        try {
            repository.getGameCatalog("game-1", "fr-FR")
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            // expected
        }
    }

    @Test
    fun `refreshGameCatalog fetches remotely even when local catalog exists`() = runTest {
        val recordingDao = RecordingGameDao()
        recordingDao.game = stubGameDto("game-1").toDomain().copy(version = 14)
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> {
                assertEquals("game-1", gameId)
                assertEquals("fr-FR", languageCode)
                return Response.success(stubGameDto("game-1").copy(version = 15))
            }
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.refreshGameCatalog("game-1", "fr-FR")

        assertTrue("Expected success but got $result", result.isSuccess)
        assertEquals(15, result.getOrThrow()?.version)
        assertEquals(1, recordingDao.upsertAllCalls.size)
        assertEquals(15, recordingDao.upsertAllCalls.first().first().version)
    }

    @Test
    fun `refreshGameCatalog returns success null on 404 without persisting`() = runTest {
        val recordingDao = RecordingGameDao()
        recordingDao.game = stubGameDto("game-1").toDomain()
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> =
                Response.error(404, "story_not_found".toResponseBody(null))
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.refreshGameCatalog("game-1", "fr-FR")

        assertTrue("Expected success but got $result", result.isSuccess)
        assertEquals(null, result.getOrThrow())
        assertTrue(recordingDao.upsertAllCalls.isEmpty())
    }

    @Test
    fun `refreshGameCatalog returns failure with HttpException on 500 without persisting`() = runTest {
        val recordingDao = RecordingGameDao()
        recordingDao.game = stubGameDto("game-1").toDomain()
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> =
                Response.error(500, "Server error".toResponseBody(null))
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.refreshGameCatalog("game-1", "fr-FR")

        assertTrue("Expected failure but got $result", result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
        assertTrue(recordingDao.upsertAllCalls.isEmpty())
    }

    @Test
    fun `refreshGameCatalog rethrows CancellationException`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> =
                throw CancellationException("cancelled")
        }
        val repository = gameRepository(api)

        try {
            repository.refreshGameCatalog("game-1", "fr-FR")
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            // expected
        }
    }

    @Test
    fun `refreshGameCatalog preserves officialOrder and isOfficial of existing official story`() = runTest {
        val recordingDao = RecordingGameDao()
        recordingDao.storedGames = mapOf(
            "game-1" to stubGameDto("game-1").toDomain()
                .copy(isOfficial = true, officialOrder = 5)
        )
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> =
                Response.success(stubGameDto("game-1").copy(version = 15))
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.refreshGameCatalog("game-1", "fr-FR")

        assertTrue(result.isSuccess)
        val upserted = recordingDao.upsertAllCalls.first().first()
        assertEquals(15, upserted.version)
        assertTrue(upserted.isOfficial)
        assertEquals(5, upserted.officialOrder)
    }

    @Test
    fun `refreshGameCatalog upserts unknown story with default order`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getStory(
                gameId: String,
                languageCode: String,
                authorization: String?,
            ): Response<GameDto> = Response.success(stubGameDto("game-new"))
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.refreshGameCatalog("game-new", "fr-FR")

        assertTrue(result.isSuccess)
        val upserted = recordingDao.upsertAllCalls.first().first()
        assertEquals("game-new", upserted.id)
        assertFalse(upserted.isOfficial)
        assertEquals(0, upserted.officialOrder)
    }
}
