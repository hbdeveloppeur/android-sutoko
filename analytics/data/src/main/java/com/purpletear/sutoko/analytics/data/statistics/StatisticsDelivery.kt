package com.purpletear.sutoko.analytics.data.statistics

internal sealed interface UploadOutcome {
    data object Accepted : UploadOutcome
    data object Rejected : UploadOutcome
    data class Unavailable(val retryAfter: Long = 0) : UploadOutcome
}

internal object StatisticsDelivery {
    fun attempt(store: StatisticsStore, now: Long, jitter: Long = 0, upload: (List<ChapterReport>) -> UploadOutcome) {
        val batch=store.reserveBatch(now)
        if(batch.isEmpty() || !store.isEnabled()) return
        val outcome=try { upload(batch) } catch (_: Exception) { UploadOutcome.Unavailable() }
        when(outcome) {
            UploadOutcome.Accepted, UploadOutcome.Rejected -> store.acknowledge(batch)
            is UploadOutcome.Unavailable -> store.failed(now,outcome.retryAfter,jitter)
        }
    }
}
