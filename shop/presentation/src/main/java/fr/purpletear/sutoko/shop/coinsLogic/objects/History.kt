package fr.purpletear.sutoko.shop.coinsLogic.objects

import android.content.Context
import android.content.SharedPreferences
import com.example.sutokosharedelements.User
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import fr.purpletear.sutoko.shop.coinsLogic.helpers.FirebaseCoinsLogicPaths
import fr.purpletear.sutoko.shop.coinsLogic.objects.histories.GainsHistory
import fr.purpletear.sutoko.shop.coinsLogic.objects.histories.OrdersHistory
import fr.purpletear.sutoko.shop.coinsLogic.objects.operations.Gain
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.BookOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.ChoiceOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.PackOrder
import fr.purpletear.sutoko.shop.coinsLogic.objects.orders.StoryOrder
import purpletear.fr.purpleteartools.RuntimeTypeAdapterFactory


interface History {
    var operations: ArrayList<Operation>


    fun addOrSetToRemoteHistory(
        context: Context,
        user: User,
        operation: Operation,
        onComplete: (isSuccessful: Boolean, exception: String?) -> Unit
    ) {
        assert(user.isConnected())
        assert(user.uid != null)
        if (operation.state.step > Operation.State.PUSHING_TO_REMOTE_HISTORY.step) {
            onComplete(true, null)
            return
        }
        val instance = FirebaseFirestore.getInstance()
        operation.state = Operation.State.PUSHING_TO_REMOTE_HISTORY
        operation.token = user.token
        instance
            .collection(FirebaseCoinsLogicPaths.getHistoryPath(user, operation))
            .document(operation.id)

            .set(operation)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, task.exception?.message)
            }
    }

    fun setSyncedRemotely(
        context: Context,
        user: User,
        operation: Operation,
        onComplete: (isSuccessful: Boolean, exception: String?) -> Unit
    ) {
        assert(user.isConnected() && user.uid != null)

        if (operation.state.step > Operation.State.UPDATING_SYNC_FLAG.step) {
            onComplete(true, null)
            return
        }

        val instance = FirebaseFirestore.getInstance()
        operation.state = Operation.State.UPDATING_SYNC_FLAG
        operation.token = user.token
        operation.synced = true
        instance
            .collection(FirebaseCoinsLogicPaths.getHistoryPath(user, operation))
            .document(operation.id)
            .set(operation)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, task.exception?.message)
            }
    }

    fun hasUnsyncedOperation(): Boolean {
        operations.forEach { operation ->
            if (!operation.synced) {
                return true
            }
        }
        return false
    }

    private fun getIdentifier(): String {
        return when (this::class.java) {
            GainsHistory::class.java -> "gains_history"
            OrdersHistory::class.java -> "orders_history"
            else -> throw IllegalStateException("fun called in ${this::class.java}")
        }
    }

    fun set(operation: Operation) {
        val indexOf = operations.indexOf(operation)
        if (indexOf == -1) {
            return
        }
        operations.set(indexOf, operation)
    }

    fun save(context: Context) {
        val json: String
        try {
            json = GsonBuilder().registerTypeAdapterFactory(getGsonAdapter()).create().toJson(this)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            return
        }

        val s: SharedPreferences = context.getSharedPreferences(
            getIdentifier(),
            Context.MODE_PRIVATE
        )
        s.edit()
            .putString("json", json)
            .apply()
    }

    fun read(context: Context) {
        val s: SharedPreferences = context.getSharedPreferences(
            getIdentifier(),
            Context.MODE_PRIVATE
        )
        val json = s.getString("json", null) ?: return
        val o: History
        try {
            o = GsonBuilder().registerTypeAdapterFactory(getGsonAdapter()).create()
                .fromJson(json, this::class.java)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            FirebaseCrashlytics.getInstance()
                .recordException(Exception("History.read Gson.fromJson History::class.java $json"))
            this.operations = ArrayList()
            return
        }
        this.copy(o)
    }


    fun getGsonAdapter(): RuntimeTypeAdapterFactory<Operation> {

        return RuntimeTypeAdapterFactory.of(
            Operation::class.java
        )
            .registerSubtype(Gain::class.java)
            .registerSubtype(PackOrder::class.java)
            .registerSubtype(StoryOrder::class.java)
            .registerSubtype(ChoiceOrder::class.java)
            .registerSubtype(BookOrder::class.java)
    }

    private fun copy(o: History) {
        this.operations = o.operations
    }

    fun add(operation: Operation) {
        this.operations.add(operation)
    }

    fun addAll(operation: List<Operation>) {
        this.operations.addAll(operation)
    }

    fun setSyncedLocally(context: Context, operation: Operation) {
        operation.state = Operation.State.SUCCESS
        operation.synced = true
        val position = operations.indexOf(operation)
        if (position != -1) {
            operations[position] = operation
        }
        save(context)
    }
}