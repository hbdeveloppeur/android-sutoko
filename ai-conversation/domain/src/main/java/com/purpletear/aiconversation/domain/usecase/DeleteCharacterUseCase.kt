package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(
        userId: String,
        token: String,
        aiCharacter: AiCharacter,
    ): Flow<Result<Unit>> {
        return characterRepository.deleteCharacter(
            userId = userId,
            token = token,
            aiCharacter = aiCharacter
        )
    }
}