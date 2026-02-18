package com.purpletear.sutoko.data.user

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.purpletear.sutoko.user.port.UserValuesUpdater

/**
 * Implementation of UserValuesUpdater that writes reminder preference to Firebase Analytics user properties.
 */
class UserReminderSetter(
    private val context: Context,
) : UserValuesUpdater {

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    override fun setValue(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
