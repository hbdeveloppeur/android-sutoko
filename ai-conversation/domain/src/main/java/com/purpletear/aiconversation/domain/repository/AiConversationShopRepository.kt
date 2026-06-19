package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.model.AiMessagePack
import com.purpletear.aiconversation.domain.model.AiTokensState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AiConversationShopRepository {
    suspend fun tryMessagePack(userId: String, userToken: String): Result<Unit>
    suspend fun buyMessagePack(
        userId: String,
        userToken: String,
        packId: String,
        orderId: String,
        purchaseToken: String,
        productId: String,
    ): Result<AiTokensState>

    suspend fun isTrialAvailable(): Boolean
    fun saveUserMessageCount(count: Int): Unit
    fun observeAiTokenState(): StateFlow<AiTokensState>
    fun getAiTokenState(userId: String): Flow<Result<AiTokensState>>
    suspend fun getAiMessagesPacks(): Result<List<AiMessagePack>>
}