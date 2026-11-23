package com.purpletear.ads

internal const val AD_WINDOW_MS = 10 * 60_000L
internal const val SAME_FORMAT_GAP_MS = 5 * 60_000L
internal const val READING_GAP_MS = 3 * 60_000L

enum class AdFormat { INTERSTITIAL, REWARDED }
internal data class AdShow(val format: AdFormat, val elapsedMs: Long, val pending: Boolean = false)
internal data class AdFrequencyState(
    val boot: Int,
    val observedAt: Long,
    val shows: List<AdShow> = emptyList(),
    val dismissedAt: Long? = null,
)

/** Pure, bounded policy. Reservations count until a definite failure proves nothing was shown. */
internal class AdFrequencyPolicy(initial: AdFrequencyState) {
    var state = initial
        private set

    fun reconcile(boot: Int, now: Long, forceRebase: Boolean = false) {
        state = if (forceRebase || boot != state.boot || now < state.observedAt) {
            // A reboot/unknown clock cannot erase the budget. Rebase once, then expire normally.
            AdFrequencyState(boot, now, state.shows.takeLast(2).map { it.copy(elapsedMs = now, pending = false) },
                state.dismissedAt?.let { now })
        } else {
            state.copy(observedAt = now,
                shows = state.shows.filter { it.pending || now - it.elapsedMs < AD_WINDOW_MS },
                dismissedAt = state.dismissedAt)
        }
    }

    fun rejection(format: AdFormat, now: Long, readingSinceDismissal: Long): String? = when {
        state.shows.size >= 2 -> "combined_cap"
        state.shows.count { it.format == format } >= 2 -> "format_cap"
        state.shows.lastOrNull { it.format == format }?.let { now - it.elapsedMs < SAME_FORMAT_GAP_MS } == true -> "same_format_gap"
        state.dismissedAt?.let { now - it < READING_GAP_MS } == true -> "fullscreen_gap"
        state.dismissedAt != null && readingSinceDismissal < READING_GAP_MS -> "reading_gap"
        else -> null
    }

    fun reserve(format: AdFormat, now: Long) {
        check(state.shows.size < 2)
        state = state.copy(observedAt = now, shows = state.shows + AdShow(format, now, pending = true))
    }

    fun confirmShown(now: Long) {
        val last = state.shows.lastOrNull() ?: return
        if (!last.pending) return
        state = state.copy(observedAt = now,
            shows = state.shows.dropLast(1) + last.copy(elapsedMs = now, pending = false))
    }

    fun recoverPending(now: Long) {
        state = state.copy(observedAt = maxOf(state.observedAt, now),
            shows = state.shows.map { if (it.pending) it.copy(elapsedMs = now, pending = false) else it })
    }

    fun releaseUnshown() {
        state = state.copy(shows = state.shows.dropLast(1))
    }

    fun dismissed(now: Long) {
        confirmShown(now)
        state = state.copy(observedAt = now, dismissedAt = now)
    }
}
