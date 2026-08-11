package com.purpletear.game.data.repository

import com.purpletear.game.data.remote.dto.GameDto
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GameRepositoryImplOneUserGamesTest {

    @Test
    fun `getOneUserGames returns mapped domain catalogs on success`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun getOneUserGames(
                userId: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> {
                assertEquals("user-1", userId)
                assertEquals(1, page)
                assertEquals(20, limit)
                return Response.success(listOf(stubGameDto("game-1")))
            }
        }

        val repository = gameRepository(api)

        val result = repository.getOneUserGames(
            userId = "user-1",
            page = 1,
            limit = 20,
        )

        assertTrue("Expected success but got $result", result.isSuccess)
        val catalogs = result.getOrThrow()
        assertEquals(1, catalogs.size)
        assertEquals("game-1", catalogs.first().id)
        assertTrue(catalogs.first().isOnline)
    }

    @Test
    fun `getOneUserGames marks story offline when status is not online`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun getOneUserGames(
                userId: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> {
                return Response.success(
                    listOf(stubGameDto("game-draft").copy(status = "draft"))
                )
            }
        }

        val repository = gameRepository(api)

        val result = repository.getOneUserGames(
            userId = "user-1",
            page = 1,
            limit = 20,
        )

        assertTrue("Expected success but got $result", result.isSuccess)
        val catalogs = result.getOrThrow()
        assertEquals(1, catalogs.size)
        assertEquals(false, catalogs.first().isOnline)
    }

    @Test
    fun `getOneUserGames returns failure with HttpException on error response`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun getOneUserGames(
                userId: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.error(
                404,
                "Not found".toResponseBody(null)
            )
        }

        val repository = gameRepository(api)

        val result = repository.getOneUserGames(
            userId = "user-1",
            page = 1,
            limit = 20,
        )

        assertTrue("Expected failure but got $result", result.isFailure)
        assertTrue(
            "Expected HttpException but got ${result.exceptionOrNull()}",
            result.exceptionOrNull() is HttpException
        )
    }

    @Test
    fun `getOneUserGames returns empty list when body is null`() = runTest {
        val api = object : FakeGameApi() {
            override suspend fun getOneUserGames(
                userId: String,
                page: Int,
                limit: Int
            ): Response<List<GameDto>> = Response.success(null)
        }

        val repository = gameRepository(api)

        val result = repository.getOneUserGames(
            userId = "user-1",
            page = 1,
            limit = 20,
        )

        assertTrue(result.isSuccess)
        assertEquals(emptyList<GameDto>(), result.getOrThrow())
    }
}
