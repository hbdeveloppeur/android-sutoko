package com.purpletear.ai_conversation.data.remote.dto

import androidx.annotation.Keep
import com.purpletear.ai_conversation.domain.enums.MediaType
import com.purpletear.ai_conversation.domain.model.Media

@Keep
data class UploadMediaResponseDto(
    val mediaId: Int,
    val url: String
)

fun UploadMediaResponseDto.toDomain(type: MediaType): Media {
    return Media(id = mediaId, url = url, typeCode = type.code)
}