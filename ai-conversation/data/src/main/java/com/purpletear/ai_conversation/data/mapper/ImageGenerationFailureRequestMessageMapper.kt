package com.purpletear.ai_conversation.data.mapper

import com.purpletear.ai_conversation.data.exception.MalformedResponseException
import com.purpletear.ai_conversation.data.remote.dto.ImageGenerationFailureRequestMessageDto

object ImageGenerationFailureRequestMessageMapper

/**
 * @throws MalformedResponseException
 */
fun ImageGenerationFailureRequestMessageMapper.fromMessage(data: Map<String, String>): ImageGenerationFailureRequestMessageDto {
    return ImageGenerationFailureRequestMessageDto(
        imageGenerationSerialId = data["imageGenerationSerialId"]
            ?: throw MalformedResponseException("ImageGenerationRequestMessageDto imageGenerationSerialId is null"),
        generationDocumentSerialId = data["generatedDocumentId"]
            ?: throw MalformedResponseException("ImageGenerationRequestMessageDto generatedDocumentId is null"),
        status = data["status"]
            ?: throw MalformedResponseException("ImageGenerationRequestMessageDto status is null"),
    )
}