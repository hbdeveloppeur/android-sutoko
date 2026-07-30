package com.purpletear.sutoko.core.domain.analytics

/**
 * Domain-facing analytics tracker for product/monetization events.
 *
 * Contract:
 * - Event names: snake_case, max 40 chars (Firebase limit).
 * - Params: only String/Int/Long/Double/Boolean values are forwarded;
 *   other types are dropped by the implementation.
 *
 * Implementations are fire-and-forget. Callers must not rely on delivery
 * guarantees; failures are silently dropped.
 */
interface AnalyticsTracker {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun setUserProperty(name: String, value: String?)
}
