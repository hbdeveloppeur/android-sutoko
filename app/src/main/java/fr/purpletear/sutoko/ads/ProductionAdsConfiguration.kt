package fr.purpletear.sutoko.ads

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.purpletear.ads.AdsConfiguration
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.purpletear.sutoko.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductionAdsConfiguration @Inject constructor() : AdsConfiguration {
    private val remote = FirebaseRemoteConfig.getInstance()

    init {
        remote.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(300).build())
        remote.setDefaultsAsync(mapOf(
            "reader_ads_enabled" to BuildConfig.DEBUG,
            "reader_ads_sms_enabled" to true,
            "reader_ads_friendzoned_enabled" to true,
            "reader_ads_rollout_percent" to 10L,
        ))
        remote.fetchAndActivate()
        remote.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(update: ConfigUpdate) { remote.activate() }
            override fun onError(error: FirebaseRemoteConfigException) = Unit
        })
    }

    override val interstitialUnitId get() = BuildConfig.ADMOB_INTERSTITIAL_CHAPTER_UNIT_ID
    override val enabled get() = boolean("reader_ads_enabled", fallback = BuildConfig.DEBUG)
    override val rolloutPercent get() = if (BuildConfig.DEBUG) 100 else {
        val value = remote.getValue("reader_ads_rollout_percent")
        if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) 10 else runCatching { value.asLong() }.getOrDefault(0).coerceIn(0, 100).toInt()
    }
    override fun placementEnabled(placement: String): Boolean = when (placement) {
        "sms" -> boolean("reader_ads_sms_enabled")
        "friendzoned" -> boolean("reader_ads_friendzoned_enabled")
        else -> false
    }
    private fun boolean(key: String, fallback: Boolean = true): Boolean {
        val value = remote.getValue(key)
        return if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) fallback
            else runCatching { value.asBoolean() }.getOrDefault(false)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AdsConfigurationModule {
    @Binds abstract fun configuration(implementation: ProductionAdsConfiguration): AdsConfiguration
}
