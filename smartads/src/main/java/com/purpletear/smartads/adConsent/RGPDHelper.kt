package com.purpletear.smartads.adConsent


import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import purpletear.fr.purpleteartools.Std

class RGPDHelper {
    private lateinit var consentForm: ConsentForm
    private lateinit var consentInformation: ConsentInformation

    fun checkRgpdAndRun(activity: Activity, f: () -> Unit) {
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            //.addTestDeviceHashedId("52694C02D0512B25EDE6D8C27E4682A2")
            .build()


        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(activity, params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    loadForm(activity, f)
                } else {
                    Handler(Looper.getMainLooper()).post(f)
                }
            },
            {
                Handler(Looper.getMainLooper()).post(f)
            })
    }


    private fun loadForm(activity: Activity, f: () -> Unit) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentForm ->
                this.consentForm = consentForm
                Std.debug("Consent form loaded", consentInformation.consentStatus.toString())
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(
                        activity
                    ) {
                        Handler(Looper.getMainLooper()).post(f)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post(f)
                }
            }
        ) {
            Handler(Looper.getMainLooper()).post(f)
        }
    }
}
