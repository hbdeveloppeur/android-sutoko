package com.purpletear.sutoko.game.usecase.testing

import com.purpletear.sutoko.game.model.testing.TestEvent
import com.purpletear.sutoko.game.repository.testing.TestEventDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTestEventsUseCase @Inject constructor(
    private val dataSource: TestEventDataSource,
) {
    operator fun invoke(sessionId: String, inventoryToken: String? = null): Flow<TestEvent> {
        return dataSource.events(sessionId, inventoryToken)
    }

    fun stop() {
        dataSource.close()
    }
}
