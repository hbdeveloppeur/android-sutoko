package com.purpletear.ai_conversation.ui.common.utils

import com.purpletear.ai_conversation.domain.model.Document
import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest

fun getDirectoryImageRequest(document: Document?): ImageGenerationRequest? {
    if (document == null) return null
    return try {
        document.requests[document.cursor]
    } catch (e: Exception) {
        null
    }
}