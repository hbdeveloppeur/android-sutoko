package fr.purpletear.sutoko.shop.coinsLogic.objects.operations

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import fr.purpletear.sutoko.shop.coinsLogic.objects.Operation

@Keep
class Gain(
    val code: String,
    val coins: Int,
    val diamonds: Int
) : Operation {
    override val id: String
    override val timestamp: Long = System.currentTimeMillis()
    override var state: Operation.State = Operation.State.INITIAL
    override var synced: Boolean = false
    override val firebaseDirName: String = "gains"

    @Exclude
    override var token: String? = null

    init {
        id = "gain_$timestamp"
    }

    constructor() : this("", 0, 0) {

    }

    override fun equals(other: Any?): Boolean {
        return other != null
                && other is Gain
                && code == other.code
    }

    override fun hashCode(): Int {
        var result = coins
        result = 31 * result + diamonds
        result = 31 * result + id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }

    override fun toString(): String {
        return "Gain(code='$code', coins=$coins, diamonds=$diamonds, id='$id', timestamp=$timestamp, state=$state, synced=$synced, firebaseDirName='$firebaseDirName', token=$token)"
    }


}