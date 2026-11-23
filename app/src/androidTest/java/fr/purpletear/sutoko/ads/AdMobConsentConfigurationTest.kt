package fr.purpletear.sutoko.ads

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.purpletear.sutoko.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdMobConsentConfigurationTest {
    @Test
    fun consentUsesPublisherAppInsteadOfGoogleSampleApp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val applicationInfo = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA
        )
        val appId = applicationInfo.metaData.getString("com.google.android.gms.ads.APPLICATION_ID")
        assertTrue("A publisher app ID is required for UMP", appId.orEmpty().matches(
            Regex("ca-app-pub-\\d{16}~\\d{10}")
        ))
        assertFalse("Google's sample app serves Publisher Test Ads consent, not Sutoko's message",
            appId.orEmpty().contains("3940256099942544"))
    }

    @Test
    fun debugBuildKeepsInterstitialTestInventory() {
        assertTrue(BuildConfig.DEBUG)
        assertEquals("ca-app-pub-3940256099942544/1033173712",
            BuildConfig.ADMOB_INTERSTITIAL_CHAPTER_UNIT_ID)
    }

    @Test
    fun debugBuildKeepsRewardedTestInventory() {
        assertTrue(BuildConfig.DEBUG)
        assertEquals("ca-app-pub-3940256099942544/5224354917",
            BuildConfig.ADMOB_REWARDED_CHAPTER_UNIT_ID)
    }

}
