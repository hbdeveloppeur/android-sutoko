package com.purpletear.game.data.repository

import com.purpletear.game.data.remote.dto.GameDto
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GameRepositoryImplUserGamesTest {

    @Test
    fun `syncUserGames fetches page 1 and replaces user games`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getUserGames(
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> {
                assertEquals("fr-FR", languageCode)
                assertEquals(1, page)
                assertEquals(20, limit)
                return Response.success(listOf(stubGameDto("game-1")))
            }
        }
        val repository = gameRepository(api, recordingDao)

        val result = repository.syncUserGames("fr-FR")

        assertTrue(result.isSuccess)
        assertEquals(1, recordingDao.replaceAllUserGamesCalls.size)
        assertEquals(1, recordingDao.replaceAllUserGamesCalls.first().size)
        assertEquals("game-1", recordingDao.replaceAllUserGamesCalls.first().first().id)
        assertTrue(recordingDao.upsertAllCalls.isEmpty())
    }

    @Test
    fun `loadMoreUserGames fetches next page and upserts`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            var callCount = 0
            override suspend fun getUserGames(
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> {
                callCount++
                return when (page) {
                    1 -> Response.success(List(20) { index -> stubGameDto("game-$index") })
                    2 -> Response.success(listOf(stubGameDto("game-next")))
                    else -> Response.success(emptyList())
                }
            }
        }
        val repository = gameRepository(api, recordingDao)
        repository.syncUserGames("fr-FR")

        val result = repository.loadMoreUserGames("fr-FR")

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
        assertEquals(1, recordingDao.upsertAllCalls.size)
        assertEquals(1, recordingDao.upsertAllCalls.first().size)
        assertEquals("game-next", recordingDao.upsertAllCalls.first().first().id)
    }

    @Test
    fun `loadMoreUserGames returns true when page is full`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getUserGames(
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> {
                return Response.success(List(20) { index -> stubGameDto("game-page$page-$index") })
            }
        }
        val repository = gameRepository(api, recordingDao)
        repository.syncUserGames("fr-FR")

        val result = repository.loadMoreUserGames("fr-FR")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    @Test
    fun `loadMoreUserGames returns failure on error response`() = runTest {
        val recordingDao = RecordingGameDao()
        val api = object : FakeGameApi() {
            override suspend fun getUserGames(
                languageCode: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = if (page == 1) {
                Response.success(emptyList())
            } else {
                Response.error(500, "Server error".toResponseBody(null))
            }
        }
        val repository = gameRepository(api, recordingDao)
        repository.syncUserGames("fr-FR")

        val result = repository.loadMoreUserGames("fr-FR")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }
}
