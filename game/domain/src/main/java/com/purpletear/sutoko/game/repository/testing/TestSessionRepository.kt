package com.purpletear.sutoko.game.repository.testing

import com.purpletear.sutoko.game.model.testing.TestSession

/**
 * Repository for test session lifecycle endpoints.
 */
interface TestSessionRepository {

    /**
     * Joins a test session for the given story.
     *
     * @param storyId Story identifier.
     * @param deviceInfo Human-readable device name for diagnostics.
     * @return Result containing the [TestSession] or a failure.
     */
    suspend fun join(storyId: String, deviceInfo: String): Result<TestSession>

    /**
     * Registers the phone's local asset inventory to enable delta packages.
     *
     * @param sessionId Test session identifier.
     * @param assets List of uniqueFileNames the phone already owns.
     * @return Result containing the signed inventory token or a failure.
     */
    suspend fun registerInventory(sessionId: String, assets: List<String>): Result<String>
}
