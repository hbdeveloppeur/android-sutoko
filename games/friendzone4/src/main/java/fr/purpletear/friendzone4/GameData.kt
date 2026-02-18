package fr.purpletear.friendzone4

import android.content.Context
import android.content.res.AssetManager
import purpletear.fr.purpleteartools.GameLanguage
import java.io.IOException
import java.util.*

object GameData {
    const val assetRootDir = "friendzone4_assets"

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
        //
        val `is` = am.open("${assetRootDir}/$path")

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
    fun getPhrasesPath(code: String, lang: String): String = "json/chapters/${code.uppercase()}/$lang/phrases-${code.uppercase()}.json"

    /**
     * Returns the Links path given it's chapter code and lang
     * @param code : String
     * @param lang : String
     */
    fun getLinksPath(code: String, lang: String): String = "json/chapters/${code.uppercase()}/$lang/links-${code.uppercase()}.json"

    fun updateNames(context: Context, sentence:  String) : String {
        when (GameLanguage.determinCode()) {
            GameLanguage.Companion.Code.DE -> {
                var res = sentence.replace("\\bEva Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_eva_belle))
                res = res.replace("\\bEva\\b".toRegex(), context.getString(R.string.fz4_personnage_eva))
                res = res.replace("\\bEVA\\b".toRegex(), context.getString(R.string.fz4_personnage_eva).uppercase())
                res = res.replace("\\bEvas\\b".toRegex(), context.getString(R.string.fz4_personnage_eva) + "s")
                res = res.replace("\\bZoé Topaze\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe_topaze))
                res = res.replace("\\bZoe Topaze\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe_topaze))
                res = res.replace("\\bZoe\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe))
                res = res.replace("\\bZoé\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe))
                res = res.replace("\\bZoes\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe) + "s")
                res = res.replace("\\bZoés\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe) + "s")
                res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_christophe_belle))
                res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.fz4_personnage_christophe))
                res = res.replace("\\bChristophes\\b".toRegex(), context.getString(R.string.fz4_personnage_christophe) + "s")
                res = res.replace("\\bSylvie Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_sylvie_belle))
                res = res.replace("\\bSylvie\\b".toRegex(), context.getString(R.string.fz4_personnage_sylvie))
                res = res.replace("\\bSylvies\\b".toRegex(), context.getString(R.string.fz4_personnage_sylvie) + "s")
                res = res.replace("\\bLucie Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_lucie_belle))
                res = res.replace("\\bLucie\\b".toRegex(), context.getString(R.string.fz4_personnage_lucie))
                res = res.replace("\\bChloé Winsplit\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe_winsplit))
                res = res.replace("\\bChloe Winsplit\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe_winsplit))
                res = res.replace("\\bChloé\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe))
                res = res.replace("\\bChloe\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe))
                res = res.replace("\\bChloés\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe) + "s")
                res = res.replace("\\bChloes\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe) + "s")
                res = res.replace("\\bBryan\\b".toRegex(), context.getString(R.string.fz4_personnage_bryan))
                res = res.replace("\\bBryans\\b".toRegex(), context.getString(R.string.fz4_personnage_bryan) + "s")
                res = res.replace("\\bLenas\\b".toRegex(), context.getString(R.string.fz4_personnage_lana) + "s")
                res = res.replace("\\bLanas\\b".toRegex(), context.getString(R.string.fz4_personnage_lana) + "s")
                res = res.replace("\\bLena\\b".toRegex(), context.getString(R.string.fz4_personnage_lana))
                return res
            }
            else -> {
                var res = sentence.replace("\\bEva Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_eva_belle))
                res = res.replace("\\bEva\\b".toRegex(), context.getString(R.string.fz4_personnage_eva))
                res = res.replace("\\bEVA\\b".toRegex(), context.getString(R.string.fz4_personnage_eva).uppercase())
                res = res.replace("\\bZoé Topaze\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe_topaze))
                res = res.replace("\\bZoe Topaze\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe_topaze))
                res = res.replace("\\bZoe\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe))
                res = res.replace("\\bZoé\\b".toRegex(), context.getString(R.string.fz4_personnage_zoe))
                res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_christophe_belle))
                res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.fz4_personnage_christophe))
                res = res.replace("\\bSylvie Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_sylvie_belle))
                res = res.replace("\\bSylvie\\b".toRegex(), context.getString(R.string.fz4_personnage_sylvie))
                res = res.replace("\\bLucie Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_lucie_belle))
                res = res.replace("\\bLucy Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_lucie_belle))
                res = res.replace("\\bLucie\\b".toRegex(), context.getString(R.string.fz4_personnage_lucie))
                res = res.replace("\\bLucy\\b".toRegex(), context.getString(R.string.fz4_personnage_lucie))
                res = res.replace("\\bBryan\\b".toRegex(), context.getString(R.string.fz4_personnage_bryan))
                res = res.replace("\\bChloé Winsplit\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe_winsplit))
                res = res.replace("\\bChloe Winsplit\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe_winsplit))
                res = res.replace("\\bChloé\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe))
                res = res.replace("\\bChloe\\b".toRegex(), context.getString(R.string.fz4_personnage_chloe))
                res = res.replace("\\bLena\\b".toRegex(), context.getString(R.string.fz4_personnage_lana))
                res = res.replace("\\bChristophe Belle\\b".toRegex(), context.getString(R.string.fz4_personnage_christophe_belle))
                res = res.replace("\\bChristophe\\b".toRegex(), context.getString(R.string.fz4_personnage_christophe))
                res = res.replace("\\bChristopher\\b".toRegex(), context.getString(R.string.fz4_personnage_christophe))
                return res
            }
        }
    }
}