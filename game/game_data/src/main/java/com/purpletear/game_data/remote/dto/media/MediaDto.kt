package com.purpletear.game_data.remote.dto.media

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.core.domain.model.Media

/**
 * Base Data Transfer Object for Media.
 */
@Keep
open class MediaDto(
    @SerializedName("id") open val id: Long,
    @SerializedName("type") open val type: String,
)

/**
 * Extension function to convert MediaDto to Media domain model.
 * This is a placeholder as Media is a sealed class and can't be instantiated directly.
 */
fun MediaDto.toDomain(): Media {
    // This should be overridden by subclasses
    throw UnsupportedOperationException("Cannot convert MediaDto directly to Media. Use a specific subclass.")
}
