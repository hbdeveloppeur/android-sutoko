package com.purpletear.ai_conversation.domain.model

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.ProcessStatus
import java.util.UUID

@Keep
data class ImageGenerationRequest(
    val serial: String = ImageGenerationRequest.generateRandomSerial(),
    var prompt: String = "",
    val modelName: String = "",
    val documentSerialId: String = "",
    var status: String = ProcessStatus.INITIAL.code,
    val url: String? = null,
    val avatarUrl: String? = null,
    val timeStamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateRandomSerial(): String = UUID.randomUUID().toString()
    }
}