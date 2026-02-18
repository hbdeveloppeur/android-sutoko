package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InviteCharactersUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(
        userId: String,
        conversationCharacterId: Int,
        characters: List<AiCharacter>
    ): Flow<Result<Unit>> {
        return characterRepository.inviteCharacters(
            userId = userId,
            conversationCharacterId = conversationCharacterId,
            characters = characters
        )
    }
}