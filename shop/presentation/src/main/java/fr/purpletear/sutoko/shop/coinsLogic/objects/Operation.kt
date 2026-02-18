package fr.purpletear.sutoko.shop.coinsLogic.objects

import androidx.annotation.Keep

@Keep
interface Operation {
    val id: String
    val timestamp: Long
    var state: State
    var synced: Boolean
    val firebaseDirName: String
    var token: String?

    companion object {
        enum class Type {
            PACKS_ORDER,
            COINS_DIAMOND_UNLOCK,
            CHOICE_ORDER,
            STORY_ORDER,
            BOOK_ORDER,
        }

    }

    enum class State(val step: Int) {
        INITIAL(1),
        DELIVERED(2),
        PUSHING_TO_REMOTE_HISTORY(3),
        UPDATING_COINS_DIAMONDS(4),
        UPDATING_SYNC_FLAG(5),
        SUCCESS(99),
    }
}