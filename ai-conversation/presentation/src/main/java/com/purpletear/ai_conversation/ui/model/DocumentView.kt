package com.purpletear.ai_conversation.ui.model

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest

@Keep
data class DocumentView(
    val generationDocumentSerialId: String,
    val generationDateTimeStamp: Long = System.currentTimeMillis(),
    val urlHistory: MutableList<ImageGenerationRequest> = emptyList<ImageGenerationRequest>().toMutableList(),
    var currentIndex: Int = 0
)
