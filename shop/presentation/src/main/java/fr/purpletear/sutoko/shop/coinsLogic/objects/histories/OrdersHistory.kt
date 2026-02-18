package fr.purpletear.sutoko.shop.coinsLogic.objects.histories

import androidx.annotation.Keep
import fr.purpletear.sutoko.shop.coinsLogic.objects.History
import fr.purpletear.sutoko.shop.coinsLogic.objects.Operation
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Order


@Keep
class OrdersHistory : History {
    override var operations: ArrayList<Operation> = ArrayList()

    // Important for Gson
    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor() {
    }

    override fun add(operation: Operation) {
        assert(operation is Order)
        super.add(operation)
    }


}