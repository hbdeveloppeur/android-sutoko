package fr.purpletear.sutoko.shop.coinsLogic.objects.orders

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import fr.purpletear.sutoko.shop.coinsLogic.objects.Operation
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Order

@Keep
class StoryOrder(
    val storyId: String,
    val price: Int,
    override val money: Order.Money
) : Order {
    override val id: String
    override var state: Operation.State = Operation.State.INITIAL
    override val timestamp: Long = System.currentTimeMillis()
    override var synced: Boolean = false
    override val firebaseDirName: String = "stories"

    @Exclude
    override var token: String? = null

    init {
        id = "story_order_$timestamp"
    }

    constructor() : this("", 0, Order.Money.COINS)

    override fun equals(other: Any?): Boolean {
        return other is StoryOrder
                && id == other.id
    }

    override fun hashCode(): Int {
        var result = storyId.hashCode()
        result = 31 * result + money.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
