package com.purpletear.sutoko.game.model.testing

import androidx.annotation.Keep

/**
 * Result of joining a backend real-time test session.
 *
 * @property sessionId Backend session identifier; used for SSE and inventory endpoints.
 * @property chapterSeeds Map of chapter UUID -> latest seed known by the server.
 */
@Keep
data class TestSession(
    val sessionId: String,
    val chapterSeeds: Map<String, Int>,
)
