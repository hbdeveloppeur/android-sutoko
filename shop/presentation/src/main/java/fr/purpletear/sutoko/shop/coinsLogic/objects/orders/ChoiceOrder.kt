package fr.purpletear.sutoko.shop.coinsLogic.objects.orders

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import fr.purpletear.sutoko.shop.coinsLogic.objects.Operation
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Order

@Keep
class ChoiceOrder(
    val storyId: Int,
    val messageCode: String,
    val price: Int,
    override val money: Order.Money
) : Order {
    override val id: String
    override val timestamp: Long = System.currentTimeMillis()
    override var state: Operation.State = Operation.State.INITIAL
    override var synced: Boolean = false
    override val firebaseDirName: String = "choices"

    @Exclude
    override var token: String? = null

    init {
        id = "choice_order_$timestamp"
    }

    constructor() : this(0, "", 0, Order.Money.DIAMOND) {

    }

    override fun equals(other: Any?): Boolean {
        return other is ChoiceOrder
                && id == other.id
    }

    override fun hashCode(): Int {
        var result = storyId
        result = 31 * result + messageCode.hashCode()
        result = 31 * result + price
        result = 31 * result + money.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + synced.hashCode()
        result = 31 * result + firebaseDirName.hashCode()
        return result
    }

}