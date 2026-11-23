package com.purpletear.sutoko.analytics.data.statistics

internal object DeliveryPolicy {
    const val HOUR = 3600L
    const val QUEUE_TTL = 7 * 86400L
    const val RUN_TTL = 14 * 86400L
    const val MAX_QUEUE = 500
    const val MAX_BATCH = 100

    fun backoff(failures: Int, retryAfter: Long = 0, jitter: Long = 0): Long {
        val exponential = HOUR * (1L shl (failures - 1).coerceIn(0, 5))
        return maxOf(exponential, retryAfter.coerceIn(0, 86400)).coerceAtMost(86400) + jitter.coerceIn(0, 300)
    }
    fun isPermanentFailure(code: Int): Boolean = code in 400..499 && code != 408 && code != 429
}
