package com.purpletear.smartads

import android.content.res.AssetManager

object Data {

    // (DEFAULT : TRUE) Enables ads
    const val IS_ADS_ENABLED = true
    val SHOW_TEST_ADS = false

    const val FIREBASE_ADS_PREFERENCES_DIR_PATH : String = "ads-preferences"

    const val FIREBASE_ADS_PREFERENCE_SID: String = "sid"
    const val FIREBASE_ADS_PREFERENCE_M1: String = "m1"
    const val FIREBASE_ADS_PREFERENCE_M2: String = "m2"

    // (DEFAULT : TestingDevice.NONE) Enables test device XIAOMI MI 8 PRO @purpletear
    val testingDevice : TestingDevice = TestingDevice.NONE


    // (DEFAULT : fr.purpletear.ledernierjour.iap.2) contains the removeAdsSKU reference.
    const val removeAdsSKU = "fr_purpletear_sutoko_iap_removeads"

    // (DEFAULT : pub-8906119025049365) contains the admob publish id
    const val adMobPublisherId = "pub-8906119025049365"


    // (DEFAULT : https://purpletear.net/friendzone/confidentialite) contains the privacy policy URL
    const val privacyPolicyUrl = "https://purpletear.net/friendzone/confidentialite"

    // (DEFAULT : TRUE)
    const val isGCSEnabled = true

    // (DEFAULT : FALSE) test Google Sdk Consent
    const val isGoogleConsentSdkTester = false

    // (DEFAULT : true) test Google Sdk Consent
    const val isGoogleConsentSdkTesterInEU = true

    // (DEFAULT : FALSE) Forces to reset the consent
    const val forceResetConsent = false

    // (DEFAULT : 60) Number of seconds the user will wait if the ads couldn't load
    const val adsWaitingSeconds = 60


    /**
     * Returns the testing device identifier given its type
     * @return String
     */
    val adUnitId = when (testingDevice) {
        Data.TestingDevice.NONE -> "ca-app-pub-8906119025049365/3120152823"
        else -> "ca-app-pub-3940256099942544/5224354917"
    }

    const val interstitialAdUnitId : String = "ca-app-pub-3940256099942544/1033173712"

    const val getLocalAdsPreferenceLocal : String = "ap.json"

    /**
     * Returns the content of an asset file given its path
     *
     * @param am   AssetManager
     * @param path String
     * @return String content
     * @throws IOException -
     */
    @Throws(IllegalStateException::class)
    fun getAssetContent(am: AssetManager, path: String): String {
        val `is` = am.open(path)
        val size = `is`.available()
        val buffer = ByteArray(size)
        val read = `is`.read(buffer)
        if (0 == read) {
            throw IllegalStateException("Error: $path seems empty or null")
        }
        `is`.close()
        return String(buffer)
    }

    // Determines if the current device is a testing device
    fun isTestingDevice() : Boolean = testingDevice != TestingDevice.NONE

    enum class TestingDevice(val identifier : String?) {
        GOOGLE_PIXEL_3XL("299421231BEB97DC027FC15CE16E4C3B"),
        EMULATOR(""),
        NONE(null)
    }

    enum class AdUnit(val identifier: String) {
        REWARDED_VIDEO("ca-app-pub-8906119025049365/4989960199")
    }

}
