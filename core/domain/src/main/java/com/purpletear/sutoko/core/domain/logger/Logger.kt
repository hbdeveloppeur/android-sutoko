package com.purpletear.sutoko.core.domain.logger

/**
 * Domain-facing logger for remote diagnostics.
 *
 * Implementations are remote and fire-and-forget. Callers should not rely on
 * delivery guarantees; failures are silently dropped.
 */
interface Logger {
    fun warning(message: String, data: Map<String, String> = emptyMap())
    fun exception(
        throwable: Throwable,
        message: String? = null,
        data: Map<String, String> = emptyMap()
    )
}

/**
 * Lazy-evaluated [Logger.warning]. Use this when constructing the message is
 * non-trivial and logging might be a no-op.
 */
fun Logger.warning(
    data: Map<String, String> = emptyMap(),
    message: () -> String
) {
    warning(message(), data)
}

/**
 * Lazy-evaluated [Logger.exception]. Use this when constructing the message is
 * non-trivial and logging might be a no-op.
 */
fun Logger.exception(
    throwable: Throwable,
    data: Map<String, String> = emptyMap(),
    message: () -> String
) {
    exception(throwable, message(), data)
}
