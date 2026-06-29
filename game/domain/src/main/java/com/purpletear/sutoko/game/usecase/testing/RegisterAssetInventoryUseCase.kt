package com.purpletear.sutoko.game.usecase.testing

import com.purpletear.sutoko.game.repository.testing.TestSessionRepository
import javax.inject.Inject

class RegisterAssetInventoryUseCase @Inject constructor(
    private val repository: TestSessionRepository,
) {
    suspend operator fun invoke(sessionId: String, assets: List<String>): Result<String> {
        return repository.registerInventory(sessionId, assets)
    }
}
