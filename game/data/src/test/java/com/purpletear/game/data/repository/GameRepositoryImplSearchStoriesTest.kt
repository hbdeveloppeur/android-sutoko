package com.purpletear.game.data.repository

import com.purpletear.game.data.remote.dto.GameDto
import com.purpletear.game.data.remote.dto.GameMetadataDto
import com.purpletear.game.data.remote.dto.toDomain
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GameRepositoryImplSearchStoriesTest {

    @Test
    fun `searchStories returns mapped domain catalogs on success`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.success(
                listOf(
                    stubGameDto("game-1").copy(
                        metadata = GameMetadataDto(
                            title = "Search Result",
                            description = null,
                            lang = "fr-FR",
                            catchingPhrase = null
                        )
                    )
                )
            )
        }

        val repository = gameRepository(api)

        val result = repository.searchStories(
            query = "search",
            languageTag = "fr-FR"
        )

        assertTrue("Expected success but got $result", result.isSuccess)
        val catalogs = result.getOrThrow()
        assertEquals(1, catalogs.size)
        assertEquals("game-1", catalogs.first().id)
        assertEquals("Search Result", catalogs.first().metadata.title)
        assertEquals("Author", catalogs.first().author?.displayName)
    }

    @Test
    fun `searchStories returns failure with HttpException on error response`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.error(
                400,
                "Bad Request".toResponseBody(null)
            )
        }

        val repository = gameRepository(api)

        val result = repository.searchStories(
            query = "a",
            languageTag = "fr-FR"
        )

        assertTrue("Expected failure but got $result", result.isFailure)
        assertTrue(
            "Expected HttpException but got ${result.exceptionOrNull()}",
            result.exceptionOrNull() is HttpException
        )
    }

    @Test
    fun `searchStories upserts results to dao on success`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.success(listOf(stubGameDto("game-1")))
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.searchStories(query = "search", languageTag = "fr-FR")

        assertTrue(result.isSuccess)
        assertEquals(1, recordingDao.upsertAllCalls.size)
        assertEquals("game-1", recordingDao.upsertAllCalls.first().first().id)
    }

    @Test
    fun `searchStories upserts nothing on failure`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.error(
                500,
                "Server error".toResponseBody(null)
            )
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.searchStories(query = "search", languageTag = "fr-FR")

        assertTrue(result.isFailure)
        assertTrue(recordingDao.upsertAllCalls.isEmpty())
    }

    @Test
    fun `searchStories returns empty list when body is null`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.success(null)
        }

        val repository = gameRepository(api)

        val result = repository.searchStories(
            query = "query",
            languageTag = "fr-FR"
        )

        assertTrue(result.isSuccess)
        assertEquals(emptyList<GameDto>(), result.getOrThrow())
    }

    @Test
    fun `searchStories preserves officialOrder and isOfficial of existing official story`() = runTest {
        val recordingDao = RecordingGameDao()
        recordingDao.storedGames = mapOf(
            "game-1" to stubGameDto("game-1").toDomain()
                .copy(isOfficial = true, officialOrder = 5)
        )
        val api = object : FakeGameApi() {
            override suspend fun searchStories(
                query: String,
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.success(listOf(stubGameDto("game-1")))
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.searchStories(query = "search", languageTag = "fr-FR")

        assertTrue(result.isSuccess)
        val upserted = recordingDao.upsertAllCalls.first().first()
        assertEquals("game-1", upserted.id)
        assertTrue(upserted.isOfficial)
        assertEquals(5, upserted.officialOrder)
    }
}
