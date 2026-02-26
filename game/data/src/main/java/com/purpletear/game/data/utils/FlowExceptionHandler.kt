package com.purpletear.game.data.utils

import kotlinx.coroutines.CancellationException

/**
 * Checks if this throwable is a cancellation exception that should not be reported.
 *
 * Cancellation exceptions are expected when coroutines are cancelled (e.g., when using
 * .first() on a Flow which aborts the flow after receiving the first element).
 * These should not be reported as errors to crash reporting services.
 */
fun Throwable.isCancellation(): Boolean = this is CancellationException

/**
 * Executes the given block only if this throwable is NOT a cancellation exception.
 *
 * Usage:
 * ```
 * } catch (e: Exception) {
 *     e.ifNotCancellation { ntfy.exception(it) }
 *     emit(Result.failure(e))
 * }
 * ```
 */
inline fun Throwable.ifNotCancellation(block: (Throwable) -> Unit) {
    if (!isCancellation()) {
        block(this)
    }
}
