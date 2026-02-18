package com.purpletear.aiconversation.presentation.model

import androidx.annotation.Keep
import com.purpletear.aiconversation.domain.model.ImageGenerationRequest

@Keep
data class DocumentView(
    val generationDocumentSerialId: String,
    val generationDateTimeStamp: Long = System.currentTimeMillis(),
    val urlHistory: MutableList<ImageGenerationRequest> = emptyList<ImageGenerationRequest>().toMutableList(),
    var currentIndex: Int = 0
)
