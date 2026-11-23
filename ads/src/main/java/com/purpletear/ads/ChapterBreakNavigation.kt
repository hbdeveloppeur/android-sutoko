package com.purpletear.ads

/** One child launch per result; restored in-flight ads are bypassed, never replayed. */
internal class ChapterBreakNavigation(
    var childLaunched: Boolean = false,
    var consumedToken: String? = null,
) {
    var busy = false
        private set
    var candidate: String? = null
        private set

    fun childReturned(token: String?, eligible: Boolean) {
        childLaunched = false
        candidate = token?.takeIf { eligible && it != consumedToken }
    }

    fun begin(): Boolean {
        if (busy || childLaunched) return false
        busy = true
        consumedToken = candidate ?: consumedToken
        candidate = null
        return true
    }

    fun finish() { busy = false }
    fun launch(): Boolean {
        if (busy || childLaunched) return false
        childLaunched = true
        return true
    }
}
