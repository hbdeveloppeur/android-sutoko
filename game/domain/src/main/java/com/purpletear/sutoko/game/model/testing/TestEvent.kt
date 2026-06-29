package com.purpletear.sutoko.game.model.testing

import androidx.annotation.Keep

/**
 * Events received from the test session SSE channel.
 */
@Keep
sealed class TestEvent {

    /**
     * Initial (or re-sync) event. Contains the current server seeds.
     */
    @Keep
    data class Connected(
        val sessionId: String,
        val chapterSeeds: Map<String, Int>,
    ) : TestEvent()

    /**
     * Another phone joined the session. Can be ignored.
     */
    @Keep
    data class PhoneConnected(
        val phoneId: String,
        val deviceInfo: String?,
    ) : TestEvent()

    /**
     * A phone left the session. Not emitted in v1.
     */
    @Keep
    data class PhoneDisconnected(
        val phoneId: String,
    ) : TestEvent()

    /**
     * A chapter seed was updated on the server. The phone should download the package if it
     * concerns the current chapter and the seed is newer than the local seed.
     */
    @Keep
    data class SeedUpdated(
        val chapterId: String,
        val seed: Int,
        val packageUrl: String,
        val changedAssets: List<String>,
    ) : TestEvent()

    /**
     * The author asked to play from a specific node. The phone must switch chapter if needed,
     * sync to the requested seed, then start playing from [nodeId].
     */
    @Keep
    data class PlayFromNode(
        val chapterId: String,
        val nodeId: String,
        val seedAtRequest: Int,
    ) : TestEvent()

    /**
     * Server-reported error.
     */
    @Keep
    data class Error(
        val code: String,
        val message: String,
    ) : TestEvent()
}
