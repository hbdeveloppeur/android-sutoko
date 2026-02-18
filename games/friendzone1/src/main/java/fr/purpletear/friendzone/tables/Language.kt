package fr.purpletear.friendzone.tables

import java.util.*

class Language {

    enum class Code {
        FR, FR_FR, FR_BE, EN, JA, DE, ES_ES, ES_AL
    }

    companion object {
        /**
         * Determines the language directory
         * Main language is en
         * @return String
         */
        fun determineLangDirectory(): String {

            val code = Locale.getDefault().language.lowercase(Locale.ENGLISH)
            val country = Locale.getDefault().country.lowercase(Locale.ENGLISH)

            return if (code.startsWith("fr")) {
                "fr-FR"
            } else if (code.startsWith("de")) {
                "de-DE"
            } else if (code.startsWith("es") && country.startsWith("es")) {
                "es-ES"
            } else if (code.startsWith("es")) {
                "es-419"
            } else "en-GB"
        }


        /**
         * Determines the language code String
         * Main language is en
         * @return String
         */
        fun determineLangCodeString(): String {
            val code = Locale.getDefault().language.lowercase(Locale.ENGLISH)
            return if (code == "fr_fr" || code == "fr_rfr") {
                "fr_fr"
            } else if (code == "fr_be" || code == "fr_rbe") {
                "fr_be"
            } else if (code.startsWith("fr")) {
                "fr"
            } else if (code.startsWith("de")) {
                "de"
            } else if (code.startsWith("ja")) {
                "ja"
            } else if (code == "es" || code == "es_es" || code == "es_res") {
                "es"
            } else if (code.startsWith("es")) {
                "es_al"
            } else "en"
        }


        /**
         * Determines the language Code
         * Main language is en
         * @return Code
         */
        fun determineLangCode(): Code {
            val code = Locale.getDefault().language.lowercase(Locale.getDefault())
            return if (code == "fr_fr" || code == "fr_rfr") {
                Code.FR_FR
            } else if (code == "fr_be" || code == "fr_rbe") {
                Code.FR_BE
            } else if (code.startsWith("fr")) {
                Code.FR
            } else if (code.startsWith("de")) {
                Code.DE
            } else if (code.startsWith("ja")) {
                Code.JA
            } else if (code == "es" || code == "es_es" || code == "es_res") {
                Code.ES_ES
            } else if (code.startsWith("es")) {
                Code.ES_AL
            } else Code.EN
        }
    }
}