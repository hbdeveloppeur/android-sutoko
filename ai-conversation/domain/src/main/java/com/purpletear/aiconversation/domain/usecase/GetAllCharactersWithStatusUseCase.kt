package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AiCharacterWithStatus
import com.purpletear.aiconversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCharactersWithStatusUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(
        userId: String,
    ): Flow<Result<List<AiCharacterWithStatus>>> {
        return characterRepository.getAccessibleCharactersWithStatus(
            userId = userId,
        )
    }
}