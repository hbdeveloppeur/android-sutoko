package com.purpletear.ai_conversation.domain.repository

import com.purpletear.ai_conversation.domain.enums.MediaType
import com.purpletear.ai_conversation.domain.model.AvatarBannerPair
import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest
import com.purpletear.ai_conversation.domain.model.Media
import kotlinx.coroutines.flow.Flow
import java.io.File

interface MediaRepository {
    suspend fun uploadMedia(
        userId: String,
        userToken: String,
        file: File,
        type: MediaType
    ): Flow<Result<Media>>

    suspend fun getAvatarAndBanner(imageGenerationRequestSerialId: String): Flow<Result<AvatarBannerPair>>
    suspend fun getMediasFromImageRequest(imageGenerationRequest: ImageGenerationRequest): Flow<List<Media>>
    suspend fun persist(media: Media)
    suspend fun describeMedia(userId: String, mediaId: Int): Flow<Result<String>>
}