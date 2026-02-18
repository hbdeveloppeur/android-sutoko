package com.purpletear.sutoko.core.domain.appaction

import androidx.annotation.Keep

/**
 * Thrown when an [ActionName] cannot be parsed from an input string.
 */
@Keep
class InvalidActionNameException(message: String) : RuntimeException(message)
