package com.purpletear.sutoko.game.usecase.testing

import com.purpletear.sutoko.game.model.testing.TestSession
import com.purpletear.sutoko.game.repository.testing.TestSessionRepository
import javax.inject.Inject

class JoinTestSessionUseCase @Inject constructor(
    private val repository: TestSessionRepository,
) {
    suspend operator fun invoke(storyId: String, deviceInfo: String): Result<TestSession> {
        return repository.join(storyId, deviceInfo)
    }
}
