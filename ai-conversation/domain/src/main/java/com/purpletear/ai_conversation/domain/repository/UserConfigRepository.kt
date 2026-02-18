package com.purpletear.ai_conversation.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserConfigRepository {
    suspend fun updateDeviceToken(
        userId: String,
        userToken: String,
    ): Flow<Result<Unit>>
}