package com.purpletear.sutoko.core.domain.date

/**
 * Thrown when formatting a news date fails unexpectedly.
 */
class RelativeDateFormatException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
