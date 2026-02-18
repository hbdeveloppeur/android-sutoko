package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep

@Keep
data class ImageGenerationFailureRequestMessageDto(
    val imageGenerationSerialId: String,
    val generationDocumentSerialId: String,
    val status: String,
)
