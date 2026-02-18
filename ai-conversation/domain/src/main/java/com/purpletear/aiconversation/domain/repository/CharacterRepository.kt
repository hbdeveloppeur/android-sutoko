package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.model.AiCharacter
import com.purpletear.aiconversation.domain.model.AiCharacterStatus
import com.purpletear.aiconversation.domain.model.AiCharacterWithStatus
import com.purpletear.aiconversation.domain.model.AvatarBannerPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CharacterRepository {
    val accountCharacters: StateFlow<List<AiCharacter>>

    suspend fun loadCharacters(userId: String?, userToken: String?): Flow<Result<Unit>>

    suspend fun getRandomAvatarAndBannerPair(
        isFemale: Boolean
    ): Flow<Result<AvatarBannerPair>>

    suspend fun insertCharacter(
        userId: String,
        token: String,
        firstName: String,
        lastName: String,
        gender: String,
        description: String,
        avatarId: Int?,
        bannerId: Int?,
        styleId: Int
    ): Flow<Result<Unit>>

    suspend fun deleteCharacter(
        userId: String,
        token: String,
        aiCharacter: AiCharacter
    ): Flow<Result<Unit>>

    suspend fun getStatus(userId: String, characterId: Int): Flow<Result<AiCharacterStatus>>

    suspend fun getAccessibleCharactersWithStatus(userId: String): Flow<Result<List<AiCharacterWithStatus>>>

    suspend fun inviteCharacters(
        userId: String,
        conversationCharacterId: Int,
        characters: List<AiCharacter>
    ): Flow<Result<Unit>>
}