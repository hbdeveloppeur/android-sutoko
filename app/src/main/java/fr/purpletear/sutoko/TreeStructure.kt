package fr.purpletear.sutoko


object TreeStructure {

    fun getDefaultCardsPath(langCode: String) : String = "cards/${langCode}_cards.json"

    fun getDefaultNewsPath(langCode: String) : String = "news/${langCode}_news.json"

}