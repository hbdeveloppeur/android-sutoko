package com.purpletear.aiconversation.domain.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class Document(
    val serial: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val requests: List<ImageGenerationRequest> = emptyList(),
    val cursor: Int = 0,
)

fun Document.hasNext(): Boolean {
    return cursor < requests.size - 1
}

fun Document.hasPrevious(): Boolean {
    return cursor > 0
}
