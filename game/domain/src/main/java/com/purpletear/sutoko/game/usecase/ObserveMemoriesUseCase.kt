package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    operator fun invoke(gameId: String): Flow<Map<String, String>> {
        return memoryRepository.observe(gameId)
    }
}
