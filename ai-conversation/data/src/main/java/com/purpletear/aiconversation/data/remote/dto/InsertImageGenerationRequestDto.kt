package com.purpletear.aiconversation.data.remote.dto

import androidx.annotation.Keep

@Keep
data class InsertImageGenerationRequestDto(
    val imageGenerationId: Int,
    val generationDocumentSerialId: String,
    val generationDocumentTimestamp: Long
)
