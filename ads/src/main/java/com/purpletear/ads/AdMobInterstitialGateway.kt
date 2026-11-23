package com.purpletear.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.UserMessagingPlatform
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobInterstitialGateway @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configuration: AdsConfiguration,
    private val analytics: AnalyticsTracker,
) : InterstitialGateway {
    private val handler = Handler(Looper.getMainLooper())
    private val initializationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ad: InterstitialAd? = null
    private var loadedAt = 0L
    private var initialized = false
    private var initializing = false
    private var loading = false
    private var generation = 0
    private var nextRequestAt = 0L
    private var showing = false

    override val ready: Boolean get() = ad != null && !showing && canRequest() &&
        SystemClock.elapsedRealtime() - loadedAt < 55 * 60_000L

    override fun invalidateConsent() {
        generation++
        ad = null
        loading = false
        nextRequestAt = 0L
    }

    override fun preload() {
        if (!configuration.enabled || !canRequest() || showing || ready || loading || initializing) return
        if (SystemClock.elapsedRealtime() < nextRequestAt) return
        if (!initialized) {
            initializing = true
            initializationScope.launch {
                try {
                    MobileAds.initialize(context) {
                        handler.post { initialized = true; initializing = false; preload() }
                    }
                } catch (error: RuntimeException) {
                    handler.post {
                        initializing = false
                        nextRequestAt = SystemClock.elapsedRealtime() + 60_000
                        analytics.logEvent("reader_ad_init_failed", mapOf("error_type" to error.javaClass.simpleName))
                    }
                }
            }
            return
        }
        ad = null
        loading = true
        val requestGeneration = generation
        nextRequestAt = SystemClock.elapsedRealtime() + 60_000
        analytics.logEvent("reader_ad_request", mapOf("format" to "interstitial"))
        try {
            InterstitialAd.load(context, configuration.interstitialUnitId, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(loaded: InterstitialAd) {
                        if (requestGeneration != generation) return
                        loading = false
                        if (canRequest() && configuration.enabled) {
                            ad = loaded
                            loadedAt = SystemClock.elapsedRealtime()
                            analytics.logEvent("reader_ad_loaded", mapOf("format" to "interstitial"))
                        }
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        if (requestGeneration != generation) return
                        loading = false
                        analytics.logEvent("reader_ad_load_failed", mapOf("format" to "interstitial", "error_code" to error.code))
                    }
                })
        } catch (error: RuntimeException) {
            loading = false
            analytics.logEvent("reader_ad_load_failed", mapOf("error_type" to error.javaClass.simpleName))
        }
    }

    override fun show(activity: Activity, gameId: String, placement: String, onShown: () -> Unit,
                      onFinished: (Boolean) -> Unit) {
        val loaded = ad
        if (!ready || loaded == null || activity.isFinishing || activity.isDestroyed || !configuration.enabled) {
            onFinished(false)
            return
        }
        ad = null
        showing = true
        var completed = false
        var shown = false
        val params = mapOf("game_id" to gameId, "placement" to placement, "format" to "interstitial")
        fun finish(wasShown: Boolean) {
            if (completed) return
            completed = true
            showing = false
            loaded.fullScreenContentCallback = null
            onFinished(wasShown)
        }
        loaded.setOnPaidEventListener { value ->
            analytics.logEvent("reader_ad_revenue", params + mapOf(
                "value" to value.valueMicros / 1_000_000.0, "currency" to value.currencyCode,
                "precision" to value.precisionType, "ad_platform" to "AdMob",
                "ad_source" to loaded.responseInfo?.loadedAdapterResponseInfo?.adSourceName))
        }
        loaded.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                if (shown || completed) return
                shown = true
                onShown()
            }
            override fun onAdImpression() { analytics.logEvent("reader_ad_impression", params) }
            override fun onAdDismissedFullScreenContent() {
                analytics.logEvent("reader_ad_dismissed", params)
                finish(true)
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                analytics.logEvent("reader_ad_show_failed", params + ("error_code" to error.code))
                finish(shown)
            }
        }
        try { loaded.show(activity) } catch (error: RuntimeException) {
            analytics.logEvent("reader_ad_show_failed", params + ("error_type" to error.javaClass.simpleName))
            finish(shown)
        }
    }

    private fun canRequest() = UserMessagingPlatform.getConsentInformation(context).canRequestAds()
}
