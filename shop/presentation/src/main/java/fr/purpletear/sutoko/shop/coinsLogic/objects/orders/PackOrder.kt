package fr.purpletear.sutoko.shop.coinsLogic.objects.orders

import com.google.firebase.firestore.Exclude
import fr.purpletear.sutoko.shop.coinsLogic.objects.Operation
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Order

class PackOrder(
    val sku: String,
    val coins: Int,
    val diamonds: Int,
    val orderId: String,
    override val money: Order.Money
) : Order {
    override val id: String
    override val timestamp: Long = System.currentTimeMillis()
    override var state: Operation.State = Operation.State.INITIAL
    override var synced: Boolean = false
    override val firebaseDirName: String = "packs"

    @set:Exclude
    override var token: String? = null


    init {
        id = "pack_order_$timestamp"
    }

    constructor() : this("", 0, 0, "", Order.Money.REAL) {

    }

    override fun equals(other: Any?): Boolean {
        return other is PackOrder
                && orderId == other.orderId
    }

    override fun hashCode(): Int {
        var result = sku.hashCode()
        result = 31 * result + coins
        result = 31 * result + diamonds
        result = 31 * result + money.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }

    override fun toString(): String {
        return "PackOrder(sku='$sku', coins=$coins, diamonds=$diamonds, orderId='$orderId', money=$money, id='$id', timestamp=$timestamp, state=$state, synced=$synced)"
    }


}