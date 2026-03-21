package com.purpletear.game.presentation.smsgame

internal object SmsGameRoutes {
    const val DEBUG = "debug/{gameId}"
    const val DESCRIPTION = "description/{chapterCode}"
    const val GAME = "game"
    
    fun debug(gameId: String): String = "debug/$gameId"
    fun description(chapterCode: String): String = "description/${chapterCode.lowercase()}"
}