package fr.purpletear.sutoko.helpers

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import fr.purpletear.sutoko.BuildConfig

/**
 * Displays the GDPR consent form (Google UMP) at the first app start when required.
 * The consent status is persisted by the UMP SDK: the form is only shown again
 * if consent becomes required once more.
 */
object GdprConsentHelper {

    fun requestConsentIfRequired(activity: Activity) {
        val params = ConsentRequestParameters.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                            .build()
                    )
                }
            }
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let {
                        android.util.Log.w("GdprConsentHelper", "Consent form error: ${it.message}")
                    }
                }
            },
            { requestError ->
                android.util.Log.w("GdprConsentHelper", "Consent info update failed: ${requestError.message}")
            }
        )
    }
}
