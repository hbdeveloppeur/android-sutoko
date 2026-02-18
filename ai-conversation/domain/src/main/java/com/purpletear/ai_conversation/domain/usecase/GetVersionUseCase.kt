package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.model.VersionResponse
import com.purpletear.ai_conversation.domain.repository.VersionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVersionUseCase @Inject constructor(
    private val versionRepository: VersionRepository
) {
    suspend operator fun invoke(
    ): Flow<Result<VersionResponse>> {
        return versionRepository.getVersionInfo()
    }
}