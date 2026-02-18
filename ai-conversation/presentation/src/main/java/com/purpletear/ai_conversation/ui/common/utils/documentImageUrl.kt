package com.purpletear.ai_conversation.ui.common.utils

import com.purpletear.ai_conversation.domain.enums.ProcessStatus
import com.purpletear.ai_conversation.domain.model.Document
import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest

fun getDocumentLastImageUrl(document: Document): String? {
    val request =
        document.requests.lastOrNull { it.status == ProcessStatus.COMPLETED.code }
    return getRequestImageUrl(request)
}

fun getDocumentImageUrl(document: Document): String? {
    return try {
        val request = document.requests[document.cursor]
        request.url?.let { url ->
            return getRemoteAssetsUrl(url)
        }
        return null
    } catch (e: Exception) {
        null
    }
}

fun getRequestImageUrl(request: ImageGenerationRequest?): String? {
    return try {
        request?.url?.let { url ->
            return getRemoteAssetsUrl(url)
        }
        return null
    } catch (e: Exception) {
        null
    }
}