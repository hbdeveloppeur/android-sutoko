package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.AiCharacterWithStatus
import com.purpletear.ai_conversation.domain.repository.CharacterRepository
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