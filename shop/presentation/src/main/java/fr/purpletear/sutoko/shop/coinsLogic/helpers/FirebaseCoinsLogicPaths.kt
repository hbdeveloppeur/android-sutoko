package fr.purpletear.sutoko.shop.coinsLogic.helpers

import com.example.sutokosharedelements.User
import fr.purpletear.sutoko.shop.coinsLogic.objects.Operation
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Gain
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.BookOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.ChoiceOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.PackOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.StoryOrder
import java.lang.IllegalStateException

object FirebaseCoinsLogicPaths {

    fun getUserDocument(user: User): String {
        return "users/${user.uid}/"
    }

    private fun getTransactionHistory(user: User): String {
        return "${getUserDocument(user)}transaction_history/"
    }

    fun getHistoryPath(user: User, operation: Operation): String {
        return "${getTransactionHistory(user)}${operation.firebaseDirName}/items/"
    }

    fun getHistoryPath(user: User, type: Operation.Companion.Type): String {
        return when (type) {
            Operation.Companion.Type.PACKS_ORDER -> getHistoryPath(user, PackOrder())
            Operation.Companion.Type.COINS_DIAMOND_UNLOCK -> getHistoryPath(user, Gain())
            Operation.Companion.Type.CHOICE_ORDER -> getHistoryPath(user, ChoiceOrder())
            Operation.Companion.Type.STORY_ORDER -> getHistoryPath(user, StoryOrder())
            Operation.Companion.Type.BOOK_ORDER -> getHistoryPath(user, BookOrder())
            else -> throw IllegalStateException("Type unhandled type")
        }
    }

}