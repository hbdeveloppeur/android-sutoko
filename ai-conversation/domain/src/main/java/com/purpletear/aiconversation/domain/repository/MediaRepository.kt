package com.purpletear.aiconversation.domain.repository

import com.purpletear.aiconversation.domain.enums.MediaType
import com.purpletear.aiconversation.domain.model.AvatarBannerPair
import com.purpletear.aiconversation.domain.model.ImageGenerationRequest
import com.purpletear.aiconversation.domain.model.Media
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