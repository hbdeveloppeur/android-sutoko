package com.purpletear.aiconversation.presentation.common.utils

import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.model.ImageGenerationRequest

fun getDirectoryImageRequest(document: Document?): ImageGenerationRequest? {
    if (document == null) return null
    return try {
        document.requests[document.cursor]
    } catch (e: Exception) {
        null
    }
}