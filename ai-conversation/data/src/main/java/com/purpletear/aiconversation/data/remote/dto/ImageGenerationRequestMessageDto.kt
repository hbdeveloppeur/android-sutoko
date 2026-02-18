package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep

@Keep
data class ImageGenerationRequestMessageDto(
    val imageGenerationSerialId: String,
    val generationDocumentSerialId: String,
    val generationDocumentTimestamp: Long,
    val status: String,

    val bannerId: Int?,
    val bannerUrl: String?,

    val avatarId: Int?,
    val avatarUrl: String?
)
