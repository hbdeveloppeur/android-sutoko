package com.purpletear.ai_conversation.domain.repository

import com.purpletear.ai_conversation.domain.model.VersionResponse
import kotlinx.coroutines.flow.Flow

interface VersionRepository {
    suspend fun getVersionInfo(
    ): Flow<Result<VersionResponse>>
}