package com.purpletear.ai_conversation.data.messaging

import android.util.Log
import com.purpletear.ai_conversation.data.enum.Action
import com.purpletear.ai_conversation.data.exception.MalformedResponseException
import com.purpletear.ai_conversation.data.exception.UnknownActionException
import com.purpletear.ai_conversation.data.mapper.ActionMapper
import com.purpletear.ai_conversation.data.mapper.ImageGenerationFailureRequestMessageMapper
import com.purpletear.ai_conversation.data.mapper.ImageGenerationRequestMessageMapper
import com.purpletear.ai_conversation.data.mapper.fromMessage
import com.purpletear.ai_conversation.domain.enums.MediaType
import com.purpletear.ai_conversation.domain.messaging.ImageGenerationRequestMessageHandler
import com.purpletear.ai_conversation.domain.model.Media
import com.purpletear.ai_conversation.domain.repository.ImageGenerationRepository
import com.purpletear.ai_conversation.domain.repository.MediaRepository

class ImageGenerationRequestMessageHandlerImpl(
    private val repository: ImageGenerationRepository,
    private val mediaRepository: MediaRepository,
) : ImageGenerationRequestMessageHandler {

    /**
     * @throws UnknownActionException
     * @throws MalformedResponseException
     */
    override suspend fun handleMessage(data: Map<String, String>) {
        if (!data.containsKey("action")) {
            return
        }
        when (val action = ActionMapper.map(data["action"]!!)) {
            Action.GenerationSuccess -> ::onGenerationSuccess.invoke(data)
            Action.GenerationFailure -> ::onGenerationFailure.invoke(data)
            else -> {}
        }
    }

    private fun onGenerationFailure(data: Map<String, String>) {

        try {
            val dto = ImageGenerationFailureRequestMessageMapper.fromMessage(data = data)

            repository.onGenerationError(
                imageGenerationSerialId = dto.imageGenerationSerialId
            )
        } catch (e: MalformedResponseException) {
            Log.e("ImageGenerationRequestMessageHandlerImpl", e.message.toString())
        } catch (e: Exception) {
            Log.e("ImageGenerationRequestMessageHandlerImpl", e.message.toString())
        }
    }

    /**
     * @throws MalformedResponseException
     */
    private suspend fun onGenerationSuccess(data: Map<String, String>) {
        try {
            val dto = ImageGenerationRequestMessageMapper.fromMessage(data = data)

            val avatar = if (dto.avatarId != null && dto.avatarUrl != null) {
                val avatar = Media(
                    id = dto.avatarId,
                    url = dto.avatarUrl,
                    typeCode = MediaType.Avatar.code,
                    imageGenerationRequestSerialId = dto.imageGenerationSerialId
                )
                mediaRepository.persist(avatar)
                avatar
            } else {
                null
            }

            val banner = Media(
                id = dto.bannerId!!,
                url = dto.bannerUrl!!,
                typeCode = MediaType.Banner.code,
                imageGenerationRequestSerialId = dto.imageGenerationSerialId
            )
            mediaRepository.persist(banner)

            repository.onGenerationSuccess(
                avatar = avatar,
                banner = banner,
                imageGenerationSerialId = dto.imageGenerationSerialId
            )
        } catch (e: MalformedResponseException) {
            Log.e("ImageGenerationRequestMessageHandlerImpl", e.message.toString())
        } catch (e: Exception) {
            Log.e("ImageGenerationRequestMessageHandlerImpl", e.message.toString())
        }
    }
}