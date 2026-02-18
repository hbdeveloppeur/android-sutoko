package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveCharactersUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    operator fun invoke(): StateFlow<List<AiCharacter>> {
        return characterRepository.accountCharacters
    }
}