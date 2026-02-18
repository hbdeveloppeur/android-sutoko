package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.AiCharacterStatus
import com.purpletear.ai_conversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCharacterStatusUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(
        userId: String,
        characterId: Int,
    ): Flow<Result<AiCharacterStatus>> {
        return characterRepository.getStatus(
            userId = userId,
            characterId = characterId,
        )
    }
}