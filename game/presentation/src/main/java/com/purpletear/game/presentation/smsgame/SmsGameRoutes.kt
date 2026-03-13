package com.purpletear.game.presentation.smsgame

internal object SmsGameRoutes {
    const val DESCRIPTION = "description/{chapterCode}"
    const val GAME = "game"
    
    fun description(chapterCode: String): String = "description/$chapterCode"
}