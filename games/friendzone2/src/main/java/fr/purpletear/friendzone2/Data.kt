package fr.purpletear.friendzone2

import android.content.res.AssetManager
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.util.*

object  Data {

    // (DEFAULT : FALSE) Enables the game debug mode. When starting a chapter, every message type are displayed
    const val gameDebugModeEnabled = false
    // (DEFAULT : FALSE) Permits the player to jump to the chapter he wants.
    const val fastButtonAlwaysDisplaying = false
    // (DEFAULT : FALSE) Test current chapter's phrases
    const val testCurrentChapterPhrases = false
    const val assetRootDir = "friendzone2assets"

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
        val `is` = am.open(assetRootDir + File.separator + path)
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
    fun getLinksPath(code: String, lang: String): String {
        val formatedCode = code.uppercase(
            Locale.getDefault())
        return "json/chapters/$formatedCode/$lang/links-$formatedCode.json"
    }


}