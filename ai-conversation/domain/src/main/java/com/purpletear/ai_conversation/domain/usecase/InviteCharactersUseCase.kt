package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.domain.repository.CharacterRepository
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