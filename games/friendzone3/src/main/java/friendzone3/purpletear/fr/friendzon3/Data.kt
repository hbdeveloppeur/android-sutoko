package friendzone3.purpletear.fr.friendzon3

import android.content.Context
import android.content.res.AssetManager
import com.example.sharedelements.OnlineAssetsManager
import purpletear.fr.purpleteartools.GlobalData
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.util.*

object Data {

    const val assetsDirectoryName : String = "friendzone3_assets"
    // DEFAULT FALSE
    const val debugMode : Boolean = false


    fun selectSound(context: Context, name : String) : String {
        return OnlineAssetsManager.getSoundFilePath(context, GlobalData.Game.FRIENDZONE3.id.toString(), name)
    }

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
    fun getPhrasesPath(code: String, lang: String): String = "json/chapters/${code.lowercase(
        Locale.getDefault())}/$lang/phrases-${code.lowercase(
        Locale.getDefault())}.json"

    /**
     * Returns the Links path given it's chapter code and lang
     * @param code : String
     * @param lang : String
     */
    fun getLinksPath(code: String, lang: String): String = "json/chapters/${code.lowercase(
        Locale.getDefault())}/$lang/links-${code.lowercase(
        Locale.getDefault())}.json"

    /**
     * Returns the Links path given it's chapter code and lang
     * @param code : String
     * @param lang : String
     */
    fun getCharactersPath(code: String, lang: String): String = "json/chapters/${code.lowercase(
        Locale.getDefault())}/$lang/private_characters-${code.lowercase(
        Locale.getDefault())}.json"


}