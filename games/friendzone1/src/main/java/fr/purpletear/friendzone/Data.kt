/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone

import android.content.res.AssetManager
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.util.*

object Data {
    // (DEFAULT : TRUE) Enables logs events
    const val logEventEnabled = true

    // (DEFAULT : TestingDevice.NONE) Enables test device XIAOMI MI 8 PRO @purpletear
    val testingDevice : TestingDevice = TestingDevice.NONE

    // (DEFAULT : FALSE) Enables the game debug mode. When starting a chapter, every message type are displayed
    const val gameDebugModeEnabled = false

    // (DEFAULT : FALSE) Test current chapter's phrases
    const val testCurrentChapterPhrases = false
    /**
     * Returns the testing device identifier given its type
     * @return String
     */
    val testingDeviceIdentifier = when (testingDevice) {
        TestingDevice.XIAOMI_MI_8_PRO -> "0413A648A3F5853423AB75327A732E18"
        TestingDevice.HUAWEI_P9_LITE -> "53A202BE6069FCAB16713F08554F32F3"
        TestingDevice.NONE -> ""
    }


    const val assetsDirectoryName : String = "friendzone1_assets"

    /**
     * Returns the content of an asset file given its path
     *
     * @param am   AssetManager
     * @param path String
     * @return String content
     * @throws IOException -
     */
    @Throws(IOException::class)
    fun getAssetContent(am: AssetManager, path: String): String {
        val `is` = am.open(assetsDirectoryName + File.separator + path)
        val size = `is`.available()
        val buffer = ByteArray(size)
        val read = `is`.read(buffer)
        if (0 == read) {
            throw IllegalStateException("Error: $path seems empty or null")
        }
        `is`.close()
        return String(buffer)
    }


    /**
     * Returns the Phrases path given it's chapter code and lang
     * @param code : String
     * @param lang : String
     */
    fun getPhrasesPath(code: String, lang: String): String = "json/chapters/${code.uppercase(
        Locale.getDefault())}/$lang/phrases-${code.uppercase()}.json"

    /**
     * Returns the Links path given it's chapter code and lang
     * @param code : String
     * @param lang : String
     */
    fun getLinksPath(code: String, lang: String): String = "json/chapters/${code.uppercase()}/$lang/links-${code.uppercase()}.json"

    // Determines if the current device is a testing device
    fun isTestingDevice() : Boolean = testingDevice != Data.TestingDevice.NONE

    enum class TestingDevice {
        XIAOMI_MI_8_PRO,
        HUAWEI_P9_LITE,
        NONE
    }
}
