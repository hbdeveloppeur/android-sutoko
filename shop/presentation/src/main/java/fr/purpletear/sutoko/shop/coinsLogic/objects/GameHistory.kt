package fr.purpletear.sutoko.shop.coinsLogic.objects

import android.content.Context
import androidx.annotation.Keep
import fr.purpletear.sutoko.shop.coinsLogic.objects.histories.GainsHistory
import fr.purpletear.sutoko.shop.coinsLogic.objects.histories.OrdersHistory
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Gain
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.BookOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.ChoiceOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.PackOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.StoryOrder

@Keep
class GameHistory {
    val gains: GainsHistory = GainsHistory()
    val orders: OrdersHistory = OrdersHistory()


    // Important for Gson
    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor() {
    }

    fun hasOrder(purchaseOrderId: String): Boolean {
        this.orders.operations.forEach {
            if (it is PackOrder && purchaseOrderId == it.orderId) {
                return@hasOrder true
            }

            if (it is ChoiceOrder && purchaseOrderId == it.messageCode) {
                return@hasOrder true
            }
        }
        return false
    }

    fun hasBoughtAtLeastOneStory(): Boolean {
        this.orders.operations.forEach {
            if (it is StoryOrder) {
                return@hasBoughtAtLeastOneStory true
            }
        }
        return false
    }

    fun hasStory(id: Int): Boolean {
        this.orders.operations.forEach {
            if (it is StoryOrder && it.storyId == id) {
                return@hasStory true
            }
        }
        return false
    }

    fun hasBook(id: Int): Boolean {
        this.orders.operations.forEach {
            if (it is BookOrder && it.bookId == id) {
                return@hasBook true
            }
        }
        return false
    }

    fun hasGain(code: String): Boolean {
        this.gains.operations.forEach {
            if (it is Gain && it.code == code) {
                return@hasGain true
            }
        }
        return false
    }

    fun read(context: Context) {
        gains.read(context)
        orders.read(context)
    }

    fun hasNonSyncedOperations(): Boolean {
        return this.gains.hasUnsyncedOperation() || this.orders.hasUnsyncedOperation()
    }
}