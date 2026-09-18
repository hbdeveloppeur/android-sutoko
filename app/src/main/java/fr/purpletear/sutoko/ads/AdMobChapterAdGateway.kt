package fr.purpletear.sutoko.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.UserMessagingPlatform
import com.purpletear.game.presentation.game_play.ads.ChapterAdAvailability
import com.purpletear.game.presentation.game_play.ads.ChapterAdGateway
import com.purpletear.game.presentation.game_play.ads.ChapterAdResult
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.purpletear.sutoko.BuildConfig
import fr.purpletear.sutoko.helpers.GdprConsentHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Called on the main thread, as required by the Mobile Ads SDK. */
@Singleton
class AdMobChapterAdGateway @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analytics: AnalyticsTracker,
) : ChapterAdGateway {
    private val mutableAvailability = MutableStateFlow(ChapterAdAvailability.UNAVAILABLE)
    override val availability = mutableAvailability.asStateFlow()
    private var cachedAd: RewardedAd? = null
    private var loadedAt = 0L
    private var initializing = false
    private var initialized = false
    private var loading = false
    private var showing = false
    private var pendingPreload = false
    private var consentGeneration = 0

    init {
        GdprConsentHelper.onConsentUpdated = {
            consentGeneration++
            val shouldPreload = pendingPreload || cachedAd != null || loading
            cachedAd = null
            mutableAvailability.value = ChapterAdAvailability.UNAVAILABLE
            if (shouldPreload) preload()
        }
    }

    override fun preload() {
        if (!canRequestAds()) {
            pendingPreload = true
            return
        }
        pendingPreload = false
        if (showing) return
        if (cachedAd != null && SystemClock.elapsedRealtime() - loadedAt < AD_MAX_AGE_MS) return
        cachedAd = null
        mutableAvailability.value = ChapterAdAvailability.LOADING
        if (loading || initializing) return
        if (!initialized) {
            initializing = true
            try {
                MobileAds.initialize(context) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        initializing = false
                        initialized = true
                        preload()
                    }
                }
            } catch (error: RuntimeException) {
                initializing = false
                mutableAvailability.value = ChapterAdAvailability.UNAVAILABLE
                analytics.logEvent("chapter_ad_init_failed", mapOf("error_type" to error.javaClass.simpleName))
            }
            return
        }
        loading = true
        analytics.logEvent("chapter_ad_request")
        val requestConsentGeneration = consentGeneration
        try {
            RewardedAd.load(context, BuildConfig.ADMOB_REWARDED_CHAPTER_UNIT_ID, AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        loading = false
                        if (requestConsentGeneration != consentGeneration) {
                            preload()
                            return
                        }
                        if (!canRequestAds()) {
                            mutableAvailability.value = ChapterAdAvailability.UNAVAILABLE
                            return
                        }
                        cachedAd = ad
                        loadedAt = SystemClock.elapsedRealtime()
                        mutableAvailability.value = ChapterAdAvailability.READY
                        ad.setOnPaidEventListener { value ->
                            analytics.logEvent("chapter_ad_revenue", mapOf(
                                "value" to value.valueMicros / 1_000_000.0,
                                "currency" to value.currencyCode,
                                "precision" to value.precisionType,
                                "ad_platform" to "AdMob",
                                "ad_format" to "rewarded",
                                "ad_source" to ad.responseInfo?.loadedAdapterResponseInfo?.adSourceName,
                            ))
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        loading = false
                        mutableAvailability.value = ChapterAdAvailability.UNAVAILABLE
                        analytics.logEvent("chapter_ad_load_failed", mapOf("error_code" to error.code))
                        if (requestConsentGeneration != consentGeneration) preload()
                    }
                })
        } catch (error: RuntimeException) {
            loading = false
            mutableAvailability.value = ChapterAdAvailability.UNAVAILABLE
            analytics.logEvent("chapter_ad_load_failed", mapOf("error_type" to error.javaClass.simpleName))
        }
    }

    override fun show(activity: Activity, onResult: (ChapterAdResult) -> Unit) {
        val ad = cachedAd
        if (showing || activity.isFinishing || activity.isDestroyed || !canRequestAds() || ad == null ||
            SystemClock.elapsedRealtime() - loadedAt >= AD_MAX_AGE_MS) {
            onResult(ChapterAdResult.UNAVAILABLE)
            return
        }
        cachedAd = null
        mutableAvailability.value = ChapterAdAvailability.SHOWING
        showing = true
        val completion = RewardedChapterAdCompletion { result ->
            showing = false
            mutableAvailability.value = ChapterAdAvailability.UNAVAILABLE
            ad.fullScreenContentCallback = null
            onResult(result)
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                analytics.logEvent("chapter_ad_impression")
            }

            override fun onAdDismissedFullScreenContent() {
                analytics.logEvent("chapter_ad_dismissed")
                completion.onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                analytics.logEvent("chapter_ad_show_failed", mapOf("error_code" to error.code))
                completion.onUnavailable()
            }
        }
        try {
            ad.show(activity) {
                if (completion.onReward()) analytics.logEvent("chapter_ad_reward")
            }
        } catch (error: RuntimeException) {
            analytics.logEvent("chapter_ad_show_failed", mapOf("error_type" to error.javaClass.simpleName))
            completion.onUnavailable()
        }
    }

    private fun canRequestAds() = UserMessagingPlatform.getConsentInformation(context).canRequestAds()

    private companion object {
        const val AD_MAX_AGE_MS = 60 * 60 * 1000L
    }
}
