package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.enums.Gender
import com.purpletear.ai_conversation.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(
        userId: String,
        token: String,
        firstName: String,
        lastName: String,
        gender: Gender,
        description: String,
        avatarId: Int?,
        bannerId: Int?,
        styleId: Int,
    ): Flow<Result<Unit>> {
        return characterRepository.insertCharacter(
            userId = userId,
            token = token,
            firstName = firstName,
            lastName = lastName,
            gender = gender.code,
            description = description,
            avatarId = avatarId,
            bannerId = bannerId,
            styleId = styleId,
        )
    }
}