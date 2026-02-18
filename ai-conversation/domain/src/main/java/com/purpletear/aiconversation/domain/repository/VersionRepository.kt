package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.model.VersionResponse
import kotlinx.coroutines.flow.Flow

interface VersionRepository {
    suspend fun getVersionInfo(
    ): Flow<Result<VersionResponse>>
}