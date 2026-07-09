package com.purpletear.game.data.repository.testing

import com.purpletear.game.data.remote.testing.TestSessionApi
import com.purpletear.game.data.remote.testing.dto.JoinTestSessionRequest
import com.purpletear.game.data.remote.testing.dto.RegisterInventoryRequest
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.testing.TestSession
import com.purpletear.sutoko.game.repository.testing.DeviceIdProvider
import com.purpletear.sutoko.game.repository.testing.TestSessionRepository
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSessionRepositoryImpl @Inject constructor(
    private val api: TestSessionApi,
    private val userRepository: UserRepository,
    private val deviceIdProvider: DeviceIdProvider,
) : TestSessionRepository {

    override suspend fun join(storyId: String, deviceInfo: String): Result<TestSession> {
        StoryTestingLogger.d("SESS") { "Requesting join — storyId=$storyId" }
        return try {
            val token = requireToken()
            val deviceId = deviceIdProvider.get()
            require(deviceId.isNotBlank()) { "Device id must not be blank" }
            val response = api.join(
                authorization = bearer(token),
                request = JoinTestSessionRequest(storyId, deviceInfo, deviceId)
            )
            val body = response.body()
                ?: throw TestSessionException(
                    "Join failed: HTTP ${response.code()} ${response.errorBody()?.string()}"
                )
            StoryTestingLogger.d("SESS") { "Join response — sessionId=${body.sessionId}" }
            Result.success(
                TestSession(
                    sessionId = body.sessionId,
                    chapterSeeds = body.chapterSeeds
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            StoryTestingLogger.e("SESS", e) { "Join request failed" }
            Result.failure(e)
        }
    }

    override suspend fun registerInventory(
        sessionId: String,
        assets: List<String>
    ): Result<String> {
        StoryTestingLogger.d("SYNC") { "Registering inventory — sessionId=$sessionId, assets=${assets.size}" }
        return try {
            val token = requireToken()
            val response = api.registerInventory(
                authorization = bearer(token),
                sessionId = sessionId,
                request = RegisterInventoryRequest(assets)
            )
            val inventoryToken = response.body()?.inventoryToken
                ?: throw TestSessionException(
                    "Inventory registration failed: HTTP ${response.code()} ${response.errorBody()?.string()}"
                )
            Result.success(inventoryToken)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            StoryTestingLogger.e("SYNC", e) { "Inventory registration failed" }
            Result.failure(e)
        }
    }

    private suspend fun requireToken(): String {
        return userRepository.observeUser().firstOrNull()?.token
            ?: throw TestSessionException("User not authenticated")
    }

    private fun bearer(token: String): String = "Bearer $token"
}

class TestSessionException(message: String) : Exception(message)
