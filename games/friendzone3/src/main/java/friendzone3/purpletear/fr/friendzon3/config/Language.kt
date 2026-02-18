package friendzone3.purpletear.fr.friendzon3.config

import java.util.*

object Language {

    enum class Code {
        FR,
        EN,
        DE,
        JP,
        ES_ES,
        ES_419
    }
    /**
     * Determines the language directory
     * Main language is en
     * @return String
     */
    fun determineLangDirectory(): String {
        val locale = Locale.getDefault()
        val code = locale.language.lowercase(locale)
        return if (code.startsWith("fr")) {
            "fr-FR"
        } else if(code.startsWith("de")) {
            "de-DE"
        } else if (code == "es" || code == "es_es" || code == "es_res") {
            "es-ES"
        } else if (code.startsWith("es")) {
            "es-419"
        } else "en-GB"
    }

    /**
     * Determines the language code
     * @return Code
     */
    fun determinCode() : Code {
        return when (determineLangDirectory()) {
            "de" -> Code.DE
            "fr-FR" -> Code.FR
            "en-GB" -> Code.EN
            "jp" -> Code.JP
            "es-419" -> Code.ES_419
            "es-ES" -> Code.ES_ES
            else -> Code.EN
        }
    }

    fun getSimpleCode() : String {
        return determineLangDirectory().substringBefore("-").lowercase(Locale.getDefault())
    }
}