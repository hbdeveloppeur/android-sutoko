package com.purpletear.sutoko.game.repository.testing

import com.purpletear.sutoko.game.model.testing.TestEvent
import kotlinx.coroutines.flow.Flow

/**
 * Data source for the test session SSE event stream.
 */
interface TestEventDataSource {

    /**
     * Opens an SSE stream for the given session.
     *
     * The returned [Flow] emits parsed [TestEvent]s and handles reconnection internally.
     * The flow completes only when [close] is called or a non-recoverable error occurs.
     *
     * @param sessionId Test session identifier.
     * @param inventoryToken Optional signed inventory token for delta packages.
     * @return Cold flow of test events.
     */
    fun events(sessionId: String, inventoryToken: String? = null): Flow<TestEvent>

    /**
     * Closes the active SSE connection and stops reconnection attempts.
     */
    fun close()
}
