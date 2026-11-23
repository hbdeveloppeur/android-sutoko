package com.purpletear.ads

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdFrequencyPersistenceTest {
    private fun isolatedContext(): Context {
        val suffix = "_ads_test_${System.nanoTime()}"
        return object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences(name + suffix, mode)
        }
    }

    @Test fun durableReservationAndCohortSurviveStoreRecreation() {
        val context = isolatedContext()
        val first = AdFrequencyStore(context)
        val loaded = first.load()
        val now = first.now()
        val policy = AdFrequencyPolicy(loaded.first)
        policy.reconcile(first.boot(), now)
        policy.reserve(AdFormat.INTERSTITIAL, now)
        assertTrue(first.save(policy.state))
        val second = AdFrequencyStore(context).load()
        assertEquals(loaded.second, second.second)
        assertEquals(AdFormat.INTERSTITIAL, second.first.shows.single().format)
        assertFalse(second.first.shows.single().pending)
        assertTrue(second.first.shows.single().elapsedMs >= now)
        assertEquals("same_format_gap", AdFrequencyPolicy(second.first).rejection(AdFormat.INTERSTITIAL, first.now(), 300_000))
    }

    @Test fun corruptLedgerBlocksConservativelyInsteadOfResettingBudget() {
        val context = isolatedContext()
        context.getSharedPreferences("reader_ads", 0).edit().putString("frequency", "not-json").commit()
        val store = AdFrequencyStore(context)
        val state = store.load().first
        assertEquals(2, state.shows.size)
        assertEquals("combined_cap", AdFrequencyPolicy(state).rejection(AdFormat.INTERSTITIAL, store.now(), 300_000))
    }

    @Test fun unknownBootRebasesSavedShowsWithBoundedExpiry() {
        val store = AdFrequencyStore(isolatedContext())
        assertTrue(store.save(AdFrequencyState(store.boot() - 1, 10, listOf(AdShow(AdFormat.INTERSTITIAL, 5)))))
        val loaded = store.load().first
        assertEquals(store.boot(), loaded.boot)
        val policy = AdFrequencyPolicy(loaded)
        policy.reconcile(loaded.boot, loaded.observedAt + AD_WINDOW_MS)
        assertTrue(policy.state.shows.isEmpty())
    }

    @Test fun existingRewardCooldownIsMigratedWithoutGrantingNewBudget() {
        val context = isolatedContext()
        context.getSharedPreferences("chapter_ads", 0).edit()
            .putLong("last_reward_at", System.currentTimeMillis()).commit()
        val state = AdFrequencyStore(context).load().first
        assertEquals(AdFormat.REWARDED, state.shows.single().format)
    }
}
