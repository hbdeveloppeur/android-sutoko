package com.purpletear.game.data.repository.testing

import com.purpletear.game.data.remote.testing.TestSessionApi
import com.purpletear.game.data.remote.testing.dto.JoinTestSessionRequest
import com.purpletear.game.data.remote.testing.dto.RegisterInventoryRequest
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.testing.TestSession
import com.purpletear.sutoko.game.repository.testing.TestSessionRepository
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSessionRepositoryImpl @Inject constructor(
    private val api: TestSessionApi,
    private val userRepository: UserRepository,
) : TestSessionRepository {

    override suspend fun join(storyId: String, deviceInfo: String): Result<TestSession> {
        StoryTestingLogger.d("SESS") { "Requesting join — storyId=$storyId" }
        return runCatching {
            val token = requireToken()
            val response = api.join(
                authorization = bearer(token),
                request = JoinTestSessionRequest(storyId, deviceInfo)
            )
            val body = response.body()
                ?: throw TestSessionException(
                    "Join failed: HTTP ${response.code()} ${response.errorBody()?.string()}"
                )
            StoryTestingLogger.d("SESS") { "Join response — sessionId=${body.sessionId}" }
            TestSession(
                sessionId = body.sessionId,
                chapterSeeds = body.chapterSeeds
            )
        }.onFailure { error ->
            StoryTestingLogger.e("SESS", error) { "Join request failed" }
        }
    }

    override suspend fun registerInventory(
        sessionId: String,
        assets: List<String>
    ): Result<String> {
        StoryTestingLogger.d("SYNC") { "Registering inventory — sessionId=$sessionId, assets=${assets.size}" }
        return runCatching {
            val token = requireToken()
            val response = api.registerInventory(
                authorization = bearer(token),
                sessionId = sessionId,
                request = RegisterInventoryRequest(assets)
            )
            response.body()?.inventoryToken
                ?: throw TestSessionException(
                    "Inventory registration failed: HTTP ${response.code()} ${response.errorBody()?.string()}"
                )
        }.onFailure { error ->
            StoryTestingLogger.e("SYNC", error) { "Inventory registration failed" }
        }
    }

    private suspend fun requireToken(): String {
        return userRepository.observeUser().firstOrNull()?.token
            ?: throw TestSessionException("User not authenticated")
    }

    private fun bearer(token: String): String = "Bearer $token"
}

class TestSessionException(message: String) : Exception(message)
