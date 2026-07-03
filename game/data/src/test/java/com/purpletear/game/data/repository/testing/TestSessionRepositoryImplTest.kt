package com.purpletear.game.data.repository.testing

import com.purpletear.game.data.remote.testing.TestSessionApi
import com.purpletear.game.data.remote.testing.dto.JoinTestSessionRequest
import com.purpletear.game.data.remote.testing.dto.JoinTestSessionResponse
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.repository.testing.DeviceIdProvider
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class TestSessionRepositoryImplTest {

    @Test
    fun `join sends device id from provider`() = runBlocking {
        val api = FakeTestSessionApi()
        val userRepository = FakeUserRepository()
        val deviceIdProvider = FakeDeviceIdProvider("device-123")
        val repository = TestSessionRepositoryImpl(api, userRepository, deviceIdProvider)

        val result = repository.join("story-1", "Pixel - Android 15")

        assertTrue(result.isSuccess)
        assertEquals("session-1", result.getOrThrow().sessionId)
        assertEquals("story-1", api.capturedRequest?.storyId)
        assertEquals("device-123", api.capturedRequest?.deviceId)
    }

    @Test
    fun `join fails when device id is blank`() = runBlocking {
        val api = FakeTestSessionApi()
        val userRepository = FakeUserRepository()
        val deviceIdProvider = FakeDeviceIdProvider("   ")
        val repository = TestSessionRepositoryImpl(api, userRepository, deviceIdProvider)

        val result = repository.join("story-1", "Pixel - Android 15")

        assertTrue(result.isFailure)
    }

    @Test
    fun `join fails when device id provider throws`() = runBlocking {
        val api = FakeTestSessionApi()
        val userRepository = FakeUserRepository()
        val deviceIdProvider = FakeDeviceIdProvider(error = RuntimeException("provider failed"))
        val repository = TestSessionRepositoryImpl(api, userRepository, deviceIdProvider)

        val result = repository.join("story-1", "Pixel - Android 15")

        assertTrue(result.isFailure)
    }

    private class FakeTestSessionApi : TestSessionApi {
        var capturedRequest: JoinTestSessionRequest? = null

        override suspend fun join(
            authorization: String,
            request: JoinTestSessionRequest
        ): Response<JoinTestSessionResponse> {
            capturedRequest = request
            return Response.success(
                JoinTestSessionResponse(
                    sessionId = "session-1",
                    chapterSeeds = emptyMap()
                )
            )
        }

        override suspend fun registerInventory(
            authorization: String,
            sessionId: String,
            request: com.purpletear.game.data.remote.testing.dto.RegisterInventoryRequest
        ): Response<com.purpletear.game.data.remote.testing.dto.RegisterInventoryResponse> {
            throw UnsupportedOperationException()
        }
    }

    private class FakeUserRepository : UserRepository {
        override fun observeUser() = flowOf(User("user-1", "token-1"))
        override fun observeIsConnected() = flowOf(true)
        override fun isConnected() = Result.success(true)
        override suspend fun connect(id: String, token: String) = Result.success(Unit)
        override suspend fun disconnect() = Result.success(Unit)
    }

    private class FakeDeviceIdProvider(
        private val deviceId: String = "",
        private val error: Throwable? = null
    ) : DeviceIdProvider {
        override suspend fun get(): String {
            error?.let { throw it }
            return deviceId
        }
    }
}
