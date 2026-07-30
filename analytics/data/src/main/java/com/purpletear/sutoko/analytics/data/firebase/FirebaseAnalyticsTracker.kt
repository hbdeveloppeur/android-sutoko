package com.purpletear.sutoko.analytics.data.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    private val analytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun logEvent(name: String, params: Map<String, Any?>) {
        analytics.logEvent(name, params.toBundle())
    }

    override fun setUserProperty(name: String, value: String?) {
        analytics.setUserProperty(name, value)
    }

    private fun Map<String, Any?>.toBundle(): Bundle? {
        if (isEmpty()) return null
        val bundle = Bundle()
        for ((key, value) in this) {
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        return bundle
    }
}
