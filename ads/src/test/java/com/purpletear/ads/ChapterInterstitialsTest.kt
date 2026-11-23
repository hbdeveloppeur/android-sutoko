package com.purpletear.ads

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChapterInterstitialsTest {
    private class Fixture(val scope: TestScope) {
        var now = 0L
        var percent = 100
        var enabled = true
        var ready = true
        var persisted = true
        var done: ((Boolean) -> Unit)? = null
        var onShown: (() -> Unit)? = null
        var continuations = 0
        var shows = 0
        val ownership = MutableStateFlow(true)
        val config = mockk<AdsConfiguration>()
        val gateway = mockk<InterstitialGateway>(relaxed = true)
        val eligibility = mockk<AdEligibility>()
        val store = mockk<AdFrequencyStore>()
        val analytics = mockk<AnalyticsTracker>(relaxed = true)
        val activity = mockk<Activity>(moreInterfaces = arrayOf(LifecycleOwner::class))
        val lifecycle = mockk<Lifecycle>()
        val coordinator: ChapterInterstitials
        init {
            every { config.enabled } answers { enabled }
            every { config.rolloutPercent } answers { percent }
            every { config.placementEnabled(any()) } returns true
            every { gateway.ready } answers { ready }
            every { store.now() } answers { now }
            every { store.load() } returns (AdFrequencyState(1, 0) to 0)
            every { store.save(any()) } answers { persisted }
            every { eligibility.observe(any()) } returns ownership
            coEvery { eligibility.mayShow(any()) } returns true
            every { activity.isFinishing } returns false
            every { activity.isDestroyed } returns false
            every { (activity as LifecycleOwner).lifecycle } returns lifecycle
            every { lifecycle.currentState } returns Lifecycle.State.RESUMED
            every { gateway.show(any(), any(), any(), any(), any()) } answers {
                shows++
                onShown = arg(3)
                done = arg(4)
            }
            coordinator = ChapterInterstitials(config, gateway, eligibility, store, analytics,
                scope.backgroundScope, StandardTestDispatcher(scope.testScheduler))
            coordinator.prepare("story")
            scope.runCurrent()
            coordinator.readerResumed(1)
        }
        fun warmed() {
            now = 181_000
            coordinator.userAction(1)
            coordinator.userAction(1)
        }
        fun boundary(key: String = "story") {
            coordinator.atChapterBreak(activity, key, "sms", {}, { continuations++ })
            scope.runCurrent()
        }
        fun dismiss(shown: Boolean = true) {
            if (shown) onShown?.invoke()
            done?.invoke(shown)
            scope.runCurrent()
        }
    }

    @Test fun warmupAndTwoActionsAreRequired() = runTest {
        val f = Fixture(this)
        f.boundary()
        assertEquals(0, f.shows)
        f.now = 181_000
        f.boundary()
        assertEquals(0, f.shows)
        f.coordinator.userAction(1)
        f.coordinator.userAction(1)
        f.boundary()
        assertEquals(1, f.shows)
    }

    @Test fun missingInventoryContinuesWithoutReservingBudget() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.ready = false
        f.boundary()
        assertEquals(1, f.continuations)
        assertEquals(0, f.shows)
        f.ready = true
        f.boundary()
        assertEquals(1, f.shows)
    }

    @Test fun doubleCallbacksNavigateOnceAndOtherConsumersCannotShowConcurrently() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.boundary()
        f.boundary()
        assertEquals(1, f.shows)
        f.dismiss()
        f.dismiss()
        assertEquals(2, f.continuations) // busy consumer bypass + the original dismissal
    }

    @Test fun privacyChangeDuringReservationPreventsShow() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.coordinator.atChapterBreak(f.activity, "story", "sms", {}, { f.continuations++ })
        f.enabled = false
        runCurrent()
        assertEquals(0, f.shows)
        assertEquals(1, f.continuations)
    }

    @Test fun ownershipRevocationSkipsAds() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.ownership.value = false
        runCurrent()
        f.boundary()
        assertEquals(0, f.shows)
        assertEquals(1, f.continuations)
    }

    @Test fun destroyedOrPausedActivityCannotShow() = runTest {
        val f = Fixture(this)
        f.warmed()
        every { f.lifecycle.currentState } returns Lifecycle.State.STARTED
        f.boundary()
        every { f.lifecycle.currentState } returns Lifecycle.State.RESUMED
        every { f.activity.isDestroyed } returns true
        f.boundary()
        assertEquals(0, f.shows)
        assertEquals(2, f.continuations)
    }

    @Test fun failedDurableWriteBypassesWithoutShowing() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.persisted = false
        f.boundary()
        assertEquals(0, f.shows)
        assertEquals(1, f.continuations)
    }

    @Test fun differentGameCannotReuseStaleOwnership() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.boundary("another_game")
        assertEquals(0, f.shows)
        assertEquals(1, f.continuations)
    }

    @Test fun definiteShowFailureAllowsRetryAtAnotherBoundary() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.boundary()
        f.dismiss(false)
        f.boundary()
        assertEquals(2, f.shows)
        assertEquals(1, f.continuations)
    }

    @Test fun rolloutZeroIsAnEmergencyKillSwitchWithoutBlockingReading() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.percent = 0
        f.boundary()
        assertEquals(0, f.shows)
        assertEquals(1, f.continuations)
    }

    @Test fun backgroundTimeDoesNotWarmUpReader() = runTest {
        val f = Fixture(this)
        f.coordinator.userAction(1)
        f.coordinator.userAction(1)
        f.coordinator.readerPaused(1)
        f.now = 181_000
        f.coordinator.readerResumed(1)
        f.boundary()
        assertEquals(0, f.shows)
    }

    @Test fun ownershipExpiryBypassesWithoutWaitingOnNetwork() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.now = 301_000
        f.boundary()
        assertEquals(0, f.shows)
        assertEquals(1, f.continuations)
    }

    @Test fun aReportedShowCannotBeErasedByAnInconsistentFailureCallback() = runTest {
        val f = Fixture(this)
        f.warmed()
        f.boundary()
        f.onShown!!.invoke()
        f.done!!.invoke(false)
        runCurrent()
        f.coordinator.userAction(1)
        f.coordinator.userAction(1)
        f.boundary()
        assertEquals(1, f.shows)
        assertEquals(2, f.continuations)
    }
}
