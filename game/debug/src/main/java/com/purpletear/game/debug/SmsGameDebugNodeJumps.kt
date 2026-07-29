package com.purpletear.game.debug

object SmsGameDebugNodeJumps {

    private val chapterCodeToNodeId: Map<String, String> = mapOf(
        "1a" to "WMPmUkArG6d-1A-57",
    )

    fun getNodeId(chapterCode: String): String? =
        chapterCodeToNodeId[chapterCode.lowercase()]
}
