package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.model.Style
import kotlinx.coroutines.flow.Flow


interface StyleRepository {
    suspend fun getAll(): Flow<Result<List<Style>>>
}