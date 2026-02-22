package fr.purpletear.sutoko.shop.coinsLogic

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.Keep
import com.android.billingclient.api.Purchase
import com.example.sharedelements.User
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.shop.coinsLogic.helpers.FirebaseCoinsLogicPaths
import fr.purpletear.sutoko.shop.coinsLogic.objects.GameHistory
import fr.purpletear.sutoko.shop.coinsLogic.objects.History
import fr.purpletear.sutoko.shop.coinsLogic.objects.Operation
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Gain
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Order
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.BookOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.ChoiceOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.PackOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.StoryOrder
import fr.purpletear.sutoko.shop.shop.ShopValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import purpletear.fr.purpleteartools.Std


interface CustomerCallbacks {
    fun onCoinsOrDiamondsUpdated(coins: Int, diamonds: Int)
}

@Keep
class Customer(private val context: Context? = null, private val callbacks: CustomerCallbacks?) {
    val user: User = User {
        callbacks?.onCoinsOrDiamondsUpdated(this.getCoins(), this.getDiamonds())
    }
    val history: GameHistory = GameHistory()

    private var _pendingMessagesCoinsToSync: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val pendingMessagesCoinsToSync: StateFlow<Int?> get() = _pendingMessagesCoinsToSync

    enum class ResultCode {
        SUCCESS,
        NOT_ENOUGH_DIAMONDS,
        ALREADY_BOUGHT,
        NOT_ENOUGH_COINS,
        NOT_CONNECTED,
        DEV_ERROR,
    }

    fun getUserId(): String {
        return user.uid!!
    }

    fun getUserToken(): String {
        return user.token!!
    }

    fun addPendingCoins(coins: Int) {
        _pendingMessagesCoinsToSync.value = (_pendingMessagesCoinsToSync.value ?: 0) + coins
    }

    fun isUserConnected(): Boolean {
        return this.user.isConnected()
    }

    fun getPackFromProduct(shopValues: ShopValues, purchase: Purchase): ShopValues.SutokoShopPack {
        val sku = purchase.products[0]
        return try {
            shopValues.getPackFromSku(sku)
        } catch (e: IllegalStateException) {
            throw e
        }
    }


    fun getCoins(): Int {
        return this.user.coins
    }

    fun getDiamonds(): Int {
        return this.user.diamonds
    }


    fun load(context: Context, onCompletion: () -> Unit = {}) {
        this.read(context)
        val f: () -> Unit = {
            getRemotely(context) { isSuccessful, exception ->
                if (!isSuccessful && exception != null) {
                    FirebaseCrashlytics.getInstance()
                        .recordException(Exception(exception.toString()))
                } else {
                    this.save(context)
                }
                Handler(Looper.getMainLooper()).post(onCompletion)
            }
        }

        if (this.user.isConnected()) {
            if (this.history.hasNonSyncedOperations()) {
                this.syncMissedOperations(context) { isSuccessful, exception ->
                    if (isSuccessful) {
                        f()
                    } else {
                        if (exception != null) {
                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception(exception.toString()))
                        }
                        Handler(Looper.getMainLooper()).post(onCompletion)
                    }
                }
            } else {
                f()
            }
        } else {
            Handler(Looper.getMainLooper()).post(onCompletion)
        }
    }

    // Reload locally
    fun read(context: Context) {
        this.history.read(context)
        this.user.readLocalData(context)
    }

    fun readNoContext() {
        if (context == null) {
            return
        }
        this.history.read(context)
        this.user.readLocalData(context)
    }

    private fun getRemotely(
        context: Context,
        onComplete: (isSuccessful: Boolean, exception: ResultCode?) -> Unit
    ) {
        assert(this.user.isConnected() && this.user.uid != null)
        this.getHistoryRemotely(context) { isSuccessful, exception ->
            if (isSuccessful) {
                Handler(Looper.getMainLooper()).post {
                    onComplete(true, exception)
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    onComplete(false, ResultCode.DEV_ERROR)
                }
            }
        }
        this.getCoinsAndDiamondsRemotely(context) { isSuccessful, coins, diamonds, exception ->
            if (isSuccessful) {
                this.user.coins = coins
                this.user.diamonds = diamonds
            } else {
                Log.e("Customer", "getCoinsAndDiamondsRemotely: $exception")
            }
        }
    }

    fun save(context: Context) {
        this.user.saveLocalData(context)
    }


    @Suppress("MemberVisibilityCanBePrivate")
    fun hasEnoughCoins(amount: Int): Boolean {
        return amount <= this.user.coins
    }

    private fun hasEnoughDiamonds(amount: Int): Boolean {
        return amount <= this.user.diamonds
    }

    private fun updateCoinsAndDiamondRemotely(
        operation: Operation,
        onComplete: (isSuccessful: Boolean, exception: String?) -> Unit
    ) {
        assert(this.user.isConnected() && this.user.uid != null)

        if ((operation is StoryOrder && operation.money == Order.Money.REAL) || operation.state.step > Operation.State.UPDATING_COINS_DIAMONDS.step) {
            onComplete(true, null)
            return
        }

        val instance = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "diamonds" to this.user.diamonds,
            "coins" to this.user.coins
        )
        operation.state = Operation.State.UPDATING_COINS_DIAMONDS
        instance
            .document(FirebaseCoinsLogicPaths.getUserDocument(this.user))
            .set(data, SetOptions.merge())
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, task.exception?.message)
            }
    }

    private fun updateCoinsAndDiamondLocally(context: Context, operation: Operation) {
        if (operation.state.step > Operation.State.UPDATING_COINS_DIAMONDS.step) {
            return
        }
        operation.state = Operation.State.UPDATING_COINS_DIAMONDS
        this.save(context)
    }


    fun buyPack(
        activity: Activity,
        purchase: Purchase,
        shopValues: ShopValues,
        coinsBuyer: CoinsBuyer,
        onDelivered: () -> Unit,
        onComplete: (isSuccessful: Boolean, exception: ResultCode?) -> Unit
    ) {
        val sku = purchase.products[0]
        val pack = try {
            shopValues.getPackFromSku(sku)
        } catch (e: IllegalStateException) {
            return
        }
        val operation = PackOrder(
            purchase.products[0],
            pack.coins,
            pack.diamonds,
            purchase.orderId!!,
            Order.Money.REAL
        )
        val alreadySaved = this.history.hasOrder(purchase.orderId!!)
        if (!alreadySaved) {
            // Deliver
            try {
                this.user.addCoins(activity, pack.coins)
                this.user.addDiamonds(activity, pack.diamonds)
                operation.state = Operation.State.DELIVERED
            } catch (e: Exception) {
                this.saveOrderAndSendException("Customer::buyPack", activity, operation, e.message)
            }
            this.history.orders.add(operation)
            this.history.orders.save(activity)
            Handler(Looper.getMainLooper()).post(onDelivered)
        }


        coinsBuyer.setDelivered(purchase) { isSuccessful ->
            if (isSuccessful) {
                this.operate(
                    activity,
                    operation,
                    this.history.orders
                ) { isSuccessfulOperate, resultCode ->
                    Handler(Looper.getMainLooper()).post {
                        onComplete(isSuccessfulOperate, resultCode)
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    onComplete(false, ResultCode.DEV_ERROR)
                }
            }
        }
    }

    fun onBuyChoice(
        activity: Activity,
        storyId: Int,
        choiceCode: String,
        diamonds: Int,
        onComplete: (isSuccessful: Boolean, exception: ResultCode?) -> Unit
    ) {
        assert(diamonds > 0)
        if (!hasEnoughDiamonds(diamonds)) {
            onComplete(false, ResultCode.NOT_ENOUGH_DIAMONDS)
            return
        }
        val operation = ChoiceOrder(storyId, choiceCode, diamonds, Order.Money.DIAMOND)
        this.history.orders.add(operation)
        this.history.orders.save(activity)
        try {
            this.user.deductDiamonds(activity, diamonds)
            operation.state = Operation.State.DELIVERED
        } catch (e: Exception) {
            this.saveOrderAndSendException("Customer::onBuyChoice", activity, operation, e.message)
        }
        this.operate(activity, operation, this.history.orders, onComplete)
    }

    fun onSeenAds(
        activity: Activity,
        diamonds: Int,
        onComplete: (isSuccessful: Boolean, exception: ResultCode?) -> Unit
    ) {
        assert(diamonds > 0)
        val operation = Gain("seen_ads_${System.currentTimeMillis()}", 0, diamonds)
        this.history.gains.add(operation)
        this.history.gains.save(activity)
        try {
            this.user.addDiamonds(activity, diamonds)
            operation.state = Operation.State.DELIVERED
        } catch (e: Exception) {
            this.saveOrderAndSendException(
                "onSeenAds::saveLocalData",
                activity,
                operation,
                e.message
            )
        }
        this.operate(activity, operation, this.history.gains, onComplete)
    }

    fun onUnlockedCoins(
        activity: Activity,
        id: String,
        coins: Int,
        onComplete: (isSuccessful: Boolean, exception: ResultCode?) -> Unit
    ) {
        assert(coins > 0)
        val operation = Gain(id, coins, 0)
        this.history.gains.add(operation)
        this.history.gains.save(activity)
        try {
            this.user.addCoins(activity, coins)
            operation.state = Operation.State.DELIVERED
        } catch (e: Exception) {
            this.saveOrderAndSendException(
                "onSeenAds::onUnlockedCoins",
                activity,
                operation,
                e.message
            )
        }
        this.operate(activity, operation, this.history.gains, onComplete)
    }

    fun onBuyStory(
        activity: Activity,
        card: Game,
        money: Order.Money,
    ) {
        assert(card.id.isNotBlank())
        assert(card.price >= 0)
        if (this.history.hasStory(card.id)) {
            return
        }
        val operation = StoryOrder(card.id, card.price, money)
        this.history.orders.add(operation)
        this.history.orders.save(activity)
        try {
            operation.state = Operation.State.DELIVERED
        } catch (e: Exception) {
            this.saveOrderAndSendException("onSeenAds::onBuyStory", activity, operation, e.message)
        }
        this.operate(activity, operation, this.history.orders, { isSuccessful, resultCode ->
        })
    }

    fun processPurchasesIfRequired(
        activity: Activity,
        shopValues: ShopValues,
        coinsBuyer: CoinsBuyer,
        purchases: MutableList<Purchase>,
        onComplete: (isSuccessful: Boolean, exception: ResultCode?) -> Unit
    ) {
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                purchase.products.forEach f@{
                    if (it.startsWith("coins_pack")) {
                        Handler(Looper.getMainLooper()).post {
                            buyPack(
                                activity,
                                purchase,
                                shopValues,
                                coinsBuyer, {},
                                onComplete
                            )
                        }
                        return@f
                    }
                }
            }
        }
    }

    fun operate(
        context: Context,
        operation: Operation,
        history: History,
        onComplete: (isSuccessful: Boolean, resultCode: ResultCode?) -> Unit
    ) {
        if (this.user.isConnected()) {
            history.addOrSetToRemoteHistory(
                context,
                this.user,
                operation
            ) { isSuccessful, exception ->
                if (isSuccessful) {

                    this.updateCoinsAndDiamondLocally(context, operation)
                    updateCoinsAndDiamondRemotely(operation) { isSuccessful1, exception1 ->
                        if (isSuccessful1) {
                            history.setSyncedRemotely(
                                context,
                                user,
                                operation
                            ) { isSuccessful2, exception2 ->
                                if (isSuccessful2) {
                                    history.setSyncedLocally(context, operation)
                                    onComplete(true, ResultCode.SUCCESS)

                                } else {
                                    saveOrderAndSendException(
                                        "Customer::operate::setSyncedRemotely:result",
                                        context,
                                        operation,
                                        exception2
                                    )
                                    onComplete(false, ResultCode.DEV_ERROR)
                                }
                            }
                        } else {
                            saveOrderAndSendException(
                                "Customer::operate::updateCoinsAndDiamondRemotely:result",
                                context,
                                operation,
                                exception1
                            )
                            onComplete(false, ResultCode.DEV_ERROR)
                        }
                    }
                } else {
                    saveOrderAndSendException(
                        "Customer::operate::addOrSetToRemoteHistory:result",
                        context,
                        operation,
                        exception
                    )
                    onComplete(false, ResultCode.DEV_ERROR)
                }
            }
        } else {
            onComplete(false, ResultCode.NOT_CONNECTED)
        }
    }


    private fun saveOrderAndSendException(
        code: String,
        context: Context,
        operation: Operation,
        exception: String?
    ) {
        this.history.orders.set(operation)
        this.history.orders.save(context)

        if (context is Activity) {
            FirebaseCrashlytics.getInstance().recordException(
                Exception(
                    "code : $code, uid : ${user.uid}, token is not null or blank : ${!user.token.isNullOrBlank()} - $operation - $exception"
                )
            )
        }
    }


    private fun syncMissedOperations(
        context: Context,
        onComplete: (isSuccessful: Boolean, exception: ResultCode?) -> Unit
    ) {
        if (!history.hasNonSyncedOperations()) {
            onComplete(true, ResultCode.SUCCESS)
            return
        }
        val operations = hashMapOf(
            0 to false,
            1 to false
        )
        // orders
        history.orders.operations.forEach { operation ->
            if (!operation.synced) {
                operate(context, operation, history.orders) { isSuccessful, resultCode ->
                    operations[0] = true
                    operations.forEach {
                        if (!it.value) {
                            return@operate
                        }
                    }
                    onComplete(isSuccessful, resultCode)
                }
            }
        }
        // gains
        history.gains.operations.forEach { operation ->
            if (!operation.synced) {
                operate(context, operation, history.gains) { isSuccessful, resultCode ->
                    operations[1] = true
                    operations.forEach {
                        if (!it.value) {
                            return@operate
                        }
                    }
                    onComplete(isSuccessful, resultCode)
                }
            }
        }
    }

    private fun getHistoryRemotely(
        context: Context,
        onComplete: (isSuccessful: Boolean, exception: ResultCode?) -> Unit
    ) {
        assert(!this.history.hasNonSyncedOperations())
        val types = Operation.Companion.Type.values()
        types.forEachIndexed { i, type ->
            val instant = FirebaseFirestore.getInstance()
            instant.collection(FirebaseCoinsLogicPaths.getHistoryPath(this.user, type))
                .get()
                .addOnSuccessListener { result ->
                    when (type) {
                        Operation.Companion.Type.PACKS_ORDER -> {
                            try {
                                val o = result.toObjects(PackOrder::class.java)
                                o.forEach { order ->
                                    val indexOf = this.history.orders.operations.indexOf(order)
                                    if (indexOf != -1) {
                                        this.history.orders.operations[indexOf] = order
                                    } else {
                                        this.history.orders.add(order)
                                    }
                                }
                            } catch (e: Exception) {
                                Std.debug(e)
                            }
                        }

                        Operation.Companion.Type.COINS_DIAMOND_UNLOCK -> {
                            try {
                                val o = result.toObjects(Gain::class.java)
                                o.forEach { order ->
                                    val indexOf = this.history.gains.operations.indexOf(order)
                                    if (indexOf != -1) {
                                        this.history.gains.operations[indexOf] = order
                                    } else {
                                        this.history.gains.add(order)
                                    }
                                }
                            } catch (e: Exception) {
                                Std.debug(e)
                            }
                        }

                        Operation.Companion.Type.CHOICE_ORDER -> {
                            try {
                                val o = result.toObjects(ChoiceOrder::class.java)
                                this.history.orders.addAll(o)
                                o.forEach { order ->
                                    val indexOf = this.history.orders.operations.indexOf(order)
                                    if (indexOf != -1) {
                                        this.history.orders.operations[indexOf] = order
                                    } else {
                                        this.history.orders.add(order)
                                    }
                                }
                            } catch (e: Exception) {
                                Std.debug(e)
                            }
                        }

                        Operation.Companion.Type.STORY_ORDER -> {
                            try {
                                val o = result.toObjects(StoryOrder::class.java)
                                this.history.orders.addAll(o)
                                o.forEach { order ->
                                    val indexOf = this.history.orders.operations.indexOf(order)
                                    if (indexOf != -1) {
                                        this.history.orders.operations[indexOf] = order
                                    } else {
                                        this.history.orders.add(order)
                                    }
                                }
                            } catch (e: Exception) {
                                Std.debug(e)
                            }
                        }

                        Operation.Companion.Type.BOOK_ORDER -> {
                            try {
                                val o = result.toObjects(BookOrder::class.java)
                                this.history.orders.addAll(o)
                                o.forEach { order ->
                                    val indexOf = this.history.orders.operations.indexOf(order)
                                    if (indexOf != -1) {
                                        this.history.orders.operations[indexOf] = order
                                    } else {
                                        this.history.orders.add(order)
                                    }
                                }
                            } catch (e: Exception) {
                                Std.debug(e)
                            }
                        }
                    }

                    try {
                        this.history.orders.save(context)
                        this.history.gains.save(context)
                    } catch (_: Exception) {

                    }

                    if (types.lastIndex == i) {
                        onComplete(true, ResultCode.SUCCESS)
                    }
                }
                .addOnFailureListener { exception ->
                    FirebaseCrashlytics.getInstance().recordException(exception)
                    onComplete(false, ResultCode.DEV_ERROR)
                }
        }
    }

    private fun getCoinsAndDiamondsRemotely(
        context: Context,
        onComplete: (isSuccessful: Boolean, coins: Int, diamonds: Int, exception: ResultCode?) -> Unit
    ) {
        assert(!this.history.hasNonSyncedOperations())
        val instant = FirebaseFirestore.getInstance()
        instant.document(FirebaseCoinsLogicPaths.getUserDocument(this.user))
            .get()
            .addOnSuccessListener { document ->
                val diamonds =
                    if (document["diamonds"] != null) document["diamonds"].toString().toInt() else 0
                val coins =
                    if (document["coins"] != null) document["coins"].toString().toInt() else 0

                try {
                    this.user.saveLocalData(context)
                } catch (_: Exception) {
                }
                onComplete(true, coins, diamonds, ResultCode.SUCCESS)
            }
            .addOnFailureListener { exception ->
                FirebaseCrashlytics.getInstance().recordException(exception)
                onComplete(false, 0, 0, ResultCode.DEV_ERROR)
            }
    }

}