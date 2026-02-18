package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadCharactersUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(
        userId: String?,
        userToken: String?
    ): Flow<Result<Unit>> = characterRepository.loadCharacters(
        userId,
        userToken,
    )
}