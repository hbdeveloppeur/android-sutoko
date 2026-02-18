package fr.purpletear.sutoko.shop.coinsLogic.objects.operations

import androidx.annotation.Keep
import fr.purpletear.sutoko.shop.coinsLogic.objects.Operation


@Keep
interface Order : Operation {
    enum class Money {
        REAL,
        COINS,
        DIAMOND
    }

    val money: Money


}