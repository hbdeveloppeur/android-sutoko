package com.purpletear.ads

import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

class AdSdkTestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "Isolated Google test ads" })
    }
}

@RunWith(AndroidJUnit4::class)
class GoogleInterstitialSdkTest {
    @Test fun realAdapterShowsAndDismissesGoogleTestInventoryInIsolatedPackage() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        // This package has separate UMP preferences and no publisher inventory. Never alter Sutoko consent.
        assertEquals("com.purpletear.ads.test", context.packageName)
        val info = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        assertEquals("ca-app-pub-3940256099942544~3347511713",
            info.metaData.getString("com.google.android.gms.ads.APPLICATION_ID"))
        val device = UiDevice.getInstance(instrumentation)
        val consent = UserMessagingPlatform.getConsentInformation(context)
        val consentDone = CountDownLatch(1)
        val error = AtomicReference<String?>()
        val phase = AtomicReference("requesting_info")
        val shown = CountDownLatch(1)
        val done = CountDownLatch(1)
        val completions = AtomicInteger()
        val displayed = AtomicBoolean(false)
        val config = object : AdsConfiguration {
            override val interstitialUnitId = "ca-app-pub-3940256099942544/1033173712"
            override val enabled = true
            override val rolloutPercent = 100
            override fun placementEnabled(placement: String) = true
        }
        val analytics = object : AnalyticsTracker {
            override fun logEvent(name: String, params: Map<String, Any?>) = Unit
            override fun setUserProperty(name: String, value: String?) = Unit
        }
        val gateway = AdMobInterstitialGateway(context, config, analytics)
        ActivityScenario.launch(AdSdkTestActivity::class.java).use { scenario ->
            scenario.onActivity { host ->
                consent.requestConsentInfoUpdate(host, ConsentRequestParameters.Builder().build(), {
                    phase.set("form_required_status_${consent.consentStatus}")
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(host) { formError ->
                        error.set(formError?.message)
                        consentDone.countDown()
                    }
                }, { formError -> error.set(formError.message); consentDone.countDown() })
            }
            val consentStarted = android.os.SystemClock.elapsedRealtime()
            val consentDeadline = consentStarted + 60_000
            while (consentDone.count > 0 && android.os.SystemClock.elapsedRealtime() < consentDeadline) {
                // Decline personalization only in Google's fictitious test publisher message.
                val declineNames = Pattern.compile("Do not consent|Ne pas consentir|Refuser|Reject all|Tout refuser|Refuser tout", Pattern.CASE_INSENSITIVE)
                val decline = device.findObject(By.desc(declineNames)) ?: device.findObject(By.text(declineNames))
                if (decline != null) decline.click() else {
                    val manageNames = Pattern.compile("Manage options|Manage preferences|Gérer les options|Gérer les préférences", Pattern.CASE_INSENSITIVE)
                    (device.findObject(By.desc(manageNames)) ?: device.findObject(By.text(manageNames)))?.click()
                }
                consentDone.await(250, TimeUnit.MILLISECONDS)
            }
            assertTrue("Google sample consent did not finish: ${phase.get()}", consentDone.count == 0L)
            assertNull(error.get())
            assertTrue("UMP must permit the test request after resolving sample consent", consent.canRequestAds())
            scenario.onActivity { gateway.preload() }
            val ready = AtomicBoolean(false)
            val loadDeadline = android.os.SystemClock.elapsedRealtime() + 60_000
            while (!ready.get() && android.os.SystemClock.elapsedRealtime() < loadDeadline) {
                instrumentation.runOnMainSync { ready.set(gateway.ready) }
                android.os.SystemClock.sleep(200)
            }
            assertTrue("Google test interstitial did not load", ready.get())
            scenario.onActivity { host ->
                gateway.show(host, "isolated_test", "test", { shown.countDown() }) {
                    displayed.set(it)
                    completions.incrementAndGet()
                    done.countDown()
                }
            }
            assertTrue("Show callback missing", shown.await(15, TimeUnit.SECONDS))
            android.os.SystemClock.sleep(12_000)
            val closeNames = Pattern.compile("Close ad|Close|Fermer|Fermer l.annonce|Interstitial close button|Skip ad|Passer l.annonce", Pattern.CASE_INSENSITIVE)
            val dismissDeadline = android.os.SystemClock.elapsedRealtime() + 60_000
            while (done.count > 0 && android.os.SystemClock.elapsedRealtime() < dismissDeadline) {
                val close = device.findObject(By.desc(closeNames)) ?: device.findObject(By.text(closeNames))
                if (close != null) close.click() else device.pressBack()
                done.await(1, TimeUnit.SECONDS)
            }
            if (done.count > 0) device.dumpWindowHierarchy(java.io.File(context.getExternalFilesDir(null), "ad-ui.xml"))
            assertTrue("Dismiss callback missing", done.count == 0L)
            assertTrue(displayed.get())
            assertEquals(1, completions.get())
        }
    }
}
