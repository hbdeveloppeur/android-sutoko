package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.VersionResponse
import com.purpletear.aiconversation.domain.repository.VersionRepository
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