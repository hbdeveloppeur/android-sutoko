package com.purpletear.ai_conversation.domain.usecase

import com.purpletear.ai_conversation.domain.enums.MediaType
import com.purpletear.ai_conversation.domain.model.Media
import com.purpletear.ai_conversation.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class UploadMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        userId: String, userToken: String, file: File, mediaType: MediaType
    ): Flow<Result<Media>> {
        return mediaRepository.uploadMedia(
            userId = userId,
            userToken = userToken,
            file = file,
            type = mediaType
        )
    }
}