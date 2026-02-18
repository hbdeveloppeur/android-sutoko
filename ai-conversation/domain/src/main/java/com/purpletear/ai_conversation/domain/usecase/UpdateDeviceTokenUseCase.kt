package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.repository.UserConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateDeviceTokenUseCase @Inject constructor(
    private val userConfigRepository: UserConfigRepository
) {
    suspend operator fun invoke(
        userId: String,
        userToken: String
    ): Flow<Result<Unit>> {
        return userConfigRepository.updateDeviceToken(
            userId = userId,
            userToken = userToken,
        )
    }
}