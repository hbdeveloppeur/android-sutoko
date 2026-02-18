package com.purpletear.news_data.remote.dto.media

import com.purpletear.sutoko.core.domain.model.Media

/**
 * Base Data Transfer Object for Media.
 */
open class MediaDto(
    open val id: Long,
    open val type: String,
)

/**
 * Extension function to convert MediaDto to Media domain model.
 * This is a placeholder as Media is a sealed class and can't be instantiated directly.
 */
fun MediaDto.toDomain(): Media {
    // This should be overridden by subclasses
    throw UnsupportedOperationException("Cannot convert MediaDto directly to Media. Use a specific subclass.")
}