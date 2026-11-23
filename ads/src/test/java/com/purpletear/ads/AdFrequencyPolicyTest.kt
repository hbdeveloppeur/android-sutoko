package com.purpletear.ads

import org.junit.Assert.*
import org.junit.Test

class AdFrequencyPolicyTest {
    @Test fun twoUnrewardedViewsStillBlockThirdWithinTenMinutes() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 0))
        policy.reserve(AdFormat.REWARDED, 0)
        policy.confirmShown(0)
        policy.dismissed(1_000)
        policy.reconcile(1, 301_000)
        policy.reserve(AdFormat.REWARDED, 301_000)
        policy.confirmShown(301_000)
        policy.dismissed(302_000)
        policy.reconcile(1, 599_999)
        assertEquals("combined_cap", policy.rejection(AdFormat.REWARDED, 599_999, 300_000))
    }

    @Test fun exactRollingBoundaryExpiresFirstShow() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 0))
        policy.reserve(AdFormat.INTERSTITIAL, 0)
        policy.confirmShown(0)
        policy.dismissed(1000)
        policy.reconcile(1, 301_000)
        policy.reserve(AdFormat.INTERSTITIAL, 301_000)
        policy.confirmShown(301_000)
        policy.dismissed(302_000)
        policy.reconcile(1, 599_999)
        assertEquals("combined_cap", policy.rejection(AdFormat.INTERSTITIAL, 599_999, 300_000))
        policy.reconcile(1, 601_000)
        assertNull(policy.rejection(AdFormat.INTERSTITIAL, 601_000, 300_000))
    }

    @Test fun mixingFormatsDoesNotBypassCombinedCap() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 0))
        policy.reserve(AdFormat.INTERSTITIAL, 0)
        policy.confirmShown(0)
        policy.dismissed(1000)
        policy.reconcile(1, 181_000)
        policy.reserve(AdFormat.REWARDED, 181_000)
        policy.confirmShown(181_000)
        policy.reconcile(1, 400_000)
        assertEquals("combined_cap", policy.rejection(AdFormat.INTERSTITIAL, 400_000, 300_000))
    }

    @Test fun backgroundTimeDoesNotSubstituteForReading() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 0))
        policy.reserve(AdFormat.INTERSTITIAL, 0)
        policy.confirmShown(0)
        policy.dismissed(1000)
        policy.reconcile(1, 700_000)
        assertEquals("reading_gap", policy.rejection(AdFormat.INTERSTITIAL, 700_000, 5000))
        assertNull(policy.rejection(AdFormat.INTERSTITIAL, 700_000, READING_GAP_MS))
    }

    @Test fun definiteShowFailureReleasesReservation() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 0))
        policy.reserve(AdFormat.INTERSTITIAL, 0)
        policy.confirmShown(0)
        policy.releaseUnshown()
        assertNull(policy.rejection(AdFormat.INTERSTITIAL, 0, 0))
        assertTrue(policy.state.shows.isEmpty())
    }

    @Test fun restartPreservesBudgetAndAmbiguousReservation() {
        val first = AdFrequencyPolicy(AdFrequencyState(1, 0))
        first.reserve(AdFormat.INTERSTITIAL, 0)
        first.reconcile(1, 300_000)
        first.reserve(AdFormat.INTERSTITIAL, 300_000)
        val restored = AdFrequencyPolicy(first.state)
        restored.reconcile(1, 500_000)
        assertEquals("combined_cap", restored.rejection(AdFormat.INTERSTITIAL, 500_000, 300_000))
    }

    @Test fun rebootRebasesOnceAndNeverLocksOutIndefinitely() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 600_000, listOf(AdShow(AdFormat.INTERSTITIAL, 550_000))))
        policy.reconcile(2, 5000)
        assertEquals("same_format_gap", policy.rejection(AdFormat.INTERSTITIAL, 5000, 300_000))
        policy.reconcile(2, 605_000)
        assertNull(policy.rejection(AdFormat.INTERSTITIAL, 605_000, 300_000))
    }

    @Test fun monotonicClockRegressionFailsConservatively() {
        val policy = AdFrequencyPolicy(AdFrequencyState(-1, 600_000, listOf(AdShow(AdFormat.INTERSTITIAL, 550_000))))
        policy.reconcile(-1, 1000)
        assertEquals(1000L, policy.state.shows.single().elapsedMs)
    }

    @Test fun spacingRejectsUntilExactSameFormatBoundary() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 0))
        policy.reserve(AdFormat.INTERSTITIAL, 0)
        policy.confirmShown(0)
        policy.dismissed(0)
        policy.reconcile(1, 299_999)
        assertEquals("same_format_gap", policy.rejection(AdFormat.INTERSTITIAL, 299_999, 300_000))
        policy.reconcile(1, 300_000)
        assertNull(policy.rejection(AdFormat.INTERSTITIAL, 300_000, 300_000))
    }

    @Test fun actualShowTimeNotEarlierReservationControlsExpiry() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 0))
        policy.reserve(AdFormat.INTERSTITIAL, 0)
        policy.confirmShown(10_000)
        policy.dismissed(20_000)
        policy.reconcile(1, 600_000)
        assertEquals(1, policy.state.shows.size)
        policy.reconcile(1, 610_000)
        assertTrue(policy.state.shows.isEmpty())
    }

    @Test fun interruptedReservationIsRecoveredAtRestartNotForgotten() {
        val policy = AdFrequencyPolicy(AdFrequencyState(1, 0))
        policy.reserve(AdFormat.INTERSTITIAL, 0)
        policy.reconcile(1, 700_000)
        assertEquals(1, policy.state.shows.size)
        policy.recoverPending(700_000)
        assertEquals(700_000L, policy.state.shows.single().elapsedMs)
        assertFalse(policy.state.shows.single().pending)
    }
}
