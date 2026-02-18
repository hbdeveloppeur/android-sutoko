package com.purpletear.ai_conversation.data.mapper

import com.purpletear.ai_conversation.data.exception.MalformedResponseException
import com.purpletear.ai_conversation.data.remote.dto.ImageGenerationRequestMessageDto

object ImageGenerationRequestMessageMapper

/**
 * @throws MalformedResponseException
 */
fun ImageGenerationRequestMessageMapper.fromMessage(data: Map<String, String>): ImageGenerationRequestMessageDto {
    return ImageGenerationRequestMessageDto(
        imageGenerationSerialId = data["imageGenerationSerialId"]
            ?: throw MalformedResponseException("ImageGenerationRequestMessageDto imageGenerationSerialId is null"),
        generationDocumentSerialId = data["generatedDocumentId"]
            ?: throw MalformedResponseException("ImageGenerationRequestMessageDto generatedDocumentId is null"),
        status = data["status"]
            ?: throw MalformedResponseException("ImageGenerationRequestMessageDto status is null"),
        generationDocumentTimestamp = System.currentTimeMillis(),

        bannerId = data["bannerId"]?.toInt(),
        bannerUrl = data["bannerUrl"],
        avatarId = if (data["avatarId"].isNullOrBlank()) null else data["avatarId"]!!.toInt(),
        avatarUrl = data["avatarUrl"]
    )
}