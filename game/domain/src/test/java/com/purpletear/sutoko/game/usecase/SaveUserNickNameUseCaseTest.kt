package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveUserNickNameUseCaseTest {

    private class FakeUserGameProgressRepository : UserGameProgressRepository {
        private val storage = mutableMapOf<String, UserGameProgress>()

        override fun observe(gameId: String): Flow<UserGameProgress> {
            return flowOf(storage[gameId] ?: UserGameProgress(gameId = gameId))
        }

        override suspend fun get(gameId: String): UserGameProgress {
            return storage[gameId] ?: UserGameProgress(gameId = gameId)
        }

        override suspend fun save(progress: UserGameProgress) {
            storage[progress.gameId] = progress
        }

        override suspend fun delete(gameId: String) {
            storage.remove(gameId)
        }
    }

    @Test
    fun `sanitizer accepts valid names`() {
        assertEquals("Léa", UserNickNameSanitizer.sanitize("Léa"))
        assertEquals("Pierre-Louis", UserNickNameSanitizer.sanitize("Pierre-Louis"))
        assertEquals("Marie Anne", UserNickNameSanitizer.sanitize("Marie  Anne"))
    }

    @Test
    fun `sanitizer removes invalid characters`() {
        assertEquals("Nick", UserNickNameSanitizer.sanitize("N_i_ck"))
        assertEquals("Hello", UserNickNameSanitizer.sanitize("He😀llo"))
        assertEquals("Jhn", UserNickNameSanitizer.sanitize("J0hn"))
    }

    @Test
    fun `sanitizer rejects blank names`() {
        assertEquals(null, UserNickNameSanitizer.sanitize(""))
        assertEquals(null, UserNickNameSanitizer.sanitize("   "))
    }

    @Test
    fun `sanitizer rejects names shorter than 3 characters`() {
        assertEquals(null, UserNickNameSanitizer.sanitize("Ab"))
    }

    @Test
    fun `sanitizer rejects names longer than 15 characters`() {
        assertEquals(null, UserNickNameSanitizer.sanitize("Averylongnameindeed"))
    }

    @Test
    fun `use case saves sanitized hero name`() = runTest {
        val repository = FakeUserGameProgressRepository()
        val useCase = SaveUserNickNameUseCase(repository)

        val result = useCase("game-1", "  Pierre-Louis  ")

        assertTrue(result.isSuccess)
        assertEquals("Pierre-Louis", repository.get("game-1").heroName)
    }

    @Test
    fun `use case returns failure for invalid name`() = runTest {
        val repository = FakeUserGameProgressRepository()
        val useCase = SaveUserNickNameUseCase(repository)

        val result = useCase("game-1", "AB")

        assertTrue(result.isFailure)
    }

    @Test
    fun `use case saves default hero name when nickname is null`() = runTest {
        val repository = FakeUserGameProgressRepository()
        val useCase = SaveUserNickNameUseCase(repository)

        val result = useCase("game-1", null)

        assertTrue(result.isSuccess)
        assertEquals(SaveUserNickNameUseCase.DEFAULT_HERO_NAME, repository.get("game-1").heroName)
    }

    @Test
    fun `use case rethrows CancellationException instead of returning failure`() = runTest {
        val repository = object : UserGameProgressRepository {
            override fun observe(gameId: String): Flow<UserGameProgress> =
                flowOf(UserGameProgress(gameId = gameId))

            override suspend fun get(gameId: String): UserGameProgress =
                throw CancellationException("cancelled")

            override suspend fun save(progress: UserGameProgress) = Unit

            override suspend fun delete(gameId: String) = Unit
        }
        val useCase = SaveUserNickNameUseCase(repository)

        var thrown: Throwable? = null
        try {
            useCase("game-1", "Valid")
        } catch (t: Throwable) {
            thrown = t
        }

        assertTrue(
            "CancellationException must escape, not be boxed into Result.failure",
            thrown is CancellationException,
        )
    }
}
