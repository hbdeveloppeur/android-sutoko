package fr.purpletear.friendzone2.tables

import java.util.*

class Language {

    companion object {

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
            val code = Locale.getDefault().language.lowercase()
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
                "fr" -> Code.FR
                "en" -> Code.EN
                "jp" -> Code.JP
                "es_es" -> Code.ES_ES
                "es_al" -> Code.ES_419
                else -> Code.EN
            }
        }
    }
}