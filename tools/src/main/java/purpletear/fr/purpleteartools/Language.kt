package purpletear.fr.purpleteartools

import java.util.*

class Language {

    companion object {
        enum class Code {
            FR,
            EN,
            DE,
            ES_ES,
            ES_419
        }

        /**
         * Determines the language directory
         * Main language is en
         * @return String
         */
        fun determineLangDirectory(): String {
            val local = Locale.getDefault()
            val code = local.language.lowercase(local)
            return if (code.startsWith("fr")) {
                "fr-FR"
            } else if (code.startsWith("de")) {
                "de-DE"
            } else if (code == "es" && local.country.lowercase(local) == "es" || code == "es_es" || code == "es_res") {
                "es-ES"
            } else if (code.startsWith("es")) {
                "es-419"
            } else "en-GB"
        }

        /**
         * Determines the language code
         * @return Code
         */
        fun determineCode(): Code {
            return when (determineLangDirectory()) {
                "de-DE" -> Code.DE
                "fr-FR" -> Code.FR
                "en-GB" -> Code.EN
                "es-ES" -> Code.ES_ES
                "es-419" -> Code.ES_419
                else -> Code.EN
            }
        }
    }
}