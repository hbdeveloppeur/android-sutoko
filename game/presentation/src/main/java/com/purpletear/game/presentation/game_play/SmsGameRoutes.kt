package com.purpletear.game.presentation.game_play

internal object SmsGameRoutes {
    const val DEBUG = "debug/{gameId}"
    const val DESCRIPTION = "game/description"
    const val CHAPTER_SELECTION = "game/chapter-selection?currentChapterCode={currentChapterCode}"
    const val GAME = "game/play/{chapterCode}?isTestMode={isTestMode}"

    fun game(chapterCode: String, isTestMode: Boolean = false): String =
        "game/play/$chapterCode?isTestMode=$isTestMode"

    fun debug(gameId: String): String = "debug/$gameId"

    fun chapterSelection(currentChapterCode: String): String =
        "game/chapter-selection?currentChapterCode=$currentChapterCode"
}
