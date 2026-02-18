package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DescribeMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        userId: String,
        mediaId: Int
    ): Flow<Result<String>> {
        return mediaRepository.describeMedia(
            userId = userId,
            mediaId = mediaId,
        )
    }
}