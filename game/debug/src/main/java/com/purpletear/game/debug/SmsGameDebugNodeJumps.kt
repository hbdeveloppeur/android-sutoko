package com.purpletear.game.debug

object SmsGameDebugNodeJumps {

    private val chapterCodeToNodeId: Map<String, String> = mapOf(
        "1a" to "fxba4BVO3ul-1A-280",
    )

    fun getNodeId(chapterCode: String): String? =
        chapterCodeToNodeId[chapterCode.lowercase()]
}
