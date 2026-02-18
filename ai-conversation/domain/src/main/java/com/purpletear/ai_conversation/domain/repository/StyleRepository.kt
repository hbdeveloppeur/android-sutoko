package com.purpletear.ai_conversation.domain.repository

import com.purpletear.ai_conversation.domain.model.Style
import kotlinx.coroutines.flow.Flow


interface StyleRepository {
    suspend fun getAll(): Flow<Result<List<Style>>>
}