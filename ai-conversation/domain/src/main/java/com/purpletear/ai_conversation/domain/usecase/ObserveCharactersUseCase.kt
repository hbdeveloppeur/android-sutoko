package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveCharactersUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    operator fun invoke(): StateFlow<List<AiCharacter>> {
        return characterRepository.accountCharacters
    }
}