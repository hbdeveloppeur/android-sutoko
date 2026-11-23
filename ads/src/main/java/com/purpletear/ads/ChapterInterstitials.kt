package com.purpletear.ads

import androidx.annotation.MainThread
import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@MainThread
@Singleton
class ChapterInterstitials internal constructor(
    private val configuration: AdsConfiguration,
    private val gateway: InterstitialGateway,
    private val eligibility: AdEligibility,
    private val store: AdFrequencyStore,
    private val analytics: AnalyticsTracker,
    private val scope: CoroutineScope,
    private val io: CoroutineDispatcher,
) {
    @Inject internal constructor(configuration: AdsConfiguration, gateway: InterstitialGateway,
        eligibility: AdEligibility, store: AdFrequencyStore, analytics: AnalyticsTracker) :
        this(configuration, gateway, eligibility, store, analytics,
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate), Dispatchers.IO)
    private val initialization = CompletableDeferred<Unit>()
    private val persistenceMutex = Mutex()
    private var policy: AdFrequencyPolicy? = null
    private var storageHealthy = false
    private var bucket = 100
    private var gameId: String? = null
    private var ownershipJob: kotlinx.coroutines.Job? = null
    private var verifiedUntil = 0L
    private val readers = mutableSetOf<Int>()
    private var readingStartedAt = 0L
    private var accumulatedReading = 0L
    private var readingAtDismissal = 0L
    private var actions = 0
    private var busy = false

    init {
        scope.launch {
            try {
                val loaded = withContext(io) { store.load() }
                policy = AdFrequencyPolicy(loaded.first)
                bucket = loaded.second
                analytics.setUserProperty("reader_ads_bucket", bucket.toString())
                storageHealthy = persist()
                preload()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                analytics.logEvent("reader_ad_storage_failed", mapOf("error_type" to error.javaClass.simpleName))
            } finally {
                initialization.complete(Unit)
            }
        }
    }

    fun prepare(key: String) {
        if (gameId == key) { preload(); return }
        gameId = key
        verifiedUntil = 0
        ownershipJob?.cancel()
        ownershipJob = scope.launch {
            initialization.await()
            eligibility.observe(key).collectLatest { potentiallyEligible ->
                verifiedUntil = 0
                if (potentiallyEligible) {
                    do {
                        val placement = if (key.startsWith("legacy:")) "friendzoned" else "sms"
                        val allowed = storageHealthy && enabled(placement) && eligibility.mayShow(key)
                        verifiedUntil = if (allowed) store.now() + 5 * 60_000 else 0
                        preload()
                        delay(4 * 60_000L)
                        // No repeated remote ownership checks while the reader is outside gameplay.
                        while (readers.isEmpty()) delay(60_000)
                    } while (true)
                }
            }
        }
    }

    fun readerResumed(id: Int) {
        if (readers.isEmpty()) readingStartedAt = store.now()
        readers.add(id)
        preload()
    }

    fun readerPaused(id: Int) {
        if (!readers.remove(id)) return
        if (readers.isEmpty()) accumulatedReading += (store.now() - readingStartedAt).coerceAtLeast(0)
    }

    fun userAction(id: Int) {
        if (id in readers && !busy) actions = (actions + 1).coerceAtMost(2)
    }

    fun consentUpdated() {
        gateway.invalidateConsent()
        preload()
    }

    private fun readingMillis(): Long = accumulatedReading +
        if (readers.isNotEmpty()) (store.now() - readingStartedAt).coerceAtLeast(0) else 0

    private fun enabled(placement: String): Boolean = configuration.enabled &&
        bucket < configuration.rolloutPercent.coerceIn(0, 100) && configuration.placementEnabled(placement)

    private fun preload() {
        val placement = if (gameId?.startsWith("legacy:") == true) "friendzoned" else "sms"
        if (storageHealthy && enabled(placement) && verifiedUntil > store.now()) gateway.preload()
    }

    fun atChapterBreak(activity: Activity, key: String, placement: String, onShown: () -> Unit,
                       onFinished: () -> Unit) {
        val params = mapOf("game_id" to key, "placement" to placement, "format" to "interstitial",
            "cohort" to if (enabled(placement)) "treatment" else "control",
            "rollout_percent" to configuration.rolloutPercent.coerceIn(0, 100))
        analytics.logEvent("reader_ad_opportunity", params)
        fun bypass(reason: String) {
            analytics.logEvent("reader_ad_bypass", params + ("reason" to reason))
            onFinished()
        }
        if (busy) { bypass("fullscreen_busy"); return }
        val frequency = policy
        val now = store.now()
        val reason = when {
            !storageHealthy || frequency == null -> "storage_unavailable"
            !enabled(placement) -> "placement_disabled"
            gameId != key || verifiedUntil <= now -> "ownership_unverified"
            readingMillis() < READING_GAP_MS -> "reading_warmup"
            actions < 2 -> "user_actions"
            activity.isFinishing || activity.isDestroyed ||
                (activity as? LifecycleOwner)?.lifecycle?.currentState != Lifecycle.State.RESUMED -> "not_resumed"
            !gateway.ready -> "inventory_unavailable"
            else -> {
                frequency.reconcile(frequency.state.boot, now)
                frequency.rejection(AdFormat.INTERSTITIAL, now, readingMillis() - readingAtDismissal)
            }
        }
        if (reason != null || frequency == null) { bypass(reason ?: "storage_unavailable"); preload(); return }
        busy = true
        frequency.reserve(AdFormat.INTERSTITIAL, now)
        scope.launch {
            if (!persist()) {
                busy = false
                bypass("storage_unavailable")
                return@launch
            }
            // The reservation is durable. Recheck after IO: privacy, ownership and host may have changed.
            if (!enabled(placement) || gameId != key || verifiedUntil <= store.now() ||
                activity.isFinishing || activity.isDestroyed ||
                (activity as? LifecycleOwner)?.lifecycle?.currentState != Lifecycle.State.RESUMED) {
                frequency.releaseUnshown()
                persist()
                busy = false
                bypass("eligibility_changed")
                return@launch
            }
            var finished = false
            var everShown = false
            val startedAt = store.now()
            gateway.show(activity, key, placement, {
                if (!everShown) {
                    everShown = true
                    frequency.confirmShown(store.now())
                    scope.launch { persist() }
                    actions = 0
                    analytics.logEvent("reader_ad_shown", params)
                    onShown()
                }
            }) { shown ->
                if (!finished) {
                    finished = true
                    val wasShown = shown || everShown
                    if (wasShown) {
                        frequency.dismissed(store.now())
                        readingAtDismissal = readingMillis()
                        actions = 0
                    } else frequency.releaseUnshown()
                    scope.launch {
                        persist()
                        busy = false
                        analytics.logEvent("reader_ad_continue", params + mapOf(
                            "shown" to wasShown, "duration_ms" to (store.now() - startedAt)))
                        onFinished()
                    }
                }
            }
        }
    }

    private suspend fun persist(): Boolean = persistenceMutex.withLock {
        val state = policy?.state ?: return@withLock false
        val saved = try {
            withContext(io) { store.save(state) }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) { false }
        if (!saved) {
            storageHealthy = false
            analytics.logEvent("reader_ad_storage_failed")
        }
        saved
    }
}
