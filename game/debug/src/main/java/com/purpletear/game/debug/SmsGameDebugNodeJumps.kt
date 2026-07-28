package com.purpletear.game.debug

object SmsGameDebugNodeJumps {

    private val chapterCodeToNodeId: Map<String, String> = mapOf(
        "9a" to "fxba4BVO3ul-9A-346",
    )

    fun getNodeId(chapterCode: String): String? =
        chapterCodeToNodeId[chapterCode.lowercase()]
}
