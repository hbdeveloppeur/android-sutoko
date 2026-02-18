package com.example.sharedelements.tables.points

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.QuerySnapshot

@Keep
class PlayerPoint(type: PointType, value: Int, time: Long, earnedItemId: Int) :
    Parcelable {

    val type: PointType = type
    val value: Int = value
    private var time: Long = time
    @get:Exclude
    var isSynchronizedWithFirebase: Boolean = false
    val earnedItemId: Int = earnedItemId
    @get:Exclude
    val codeIdentifier: String
        get() {
            return "${type.str}-$earnedItemId"
        }

    enum class PointType(val str: String) {
        TROPHY("TROPHY"),
        CONSUMPTION("CONSUMPTION"),
        NONE("NONE")
    }

    constructor() : this(PointType.NONE, 0, -1, -1)

    constructor(source: Parcel) : this(
        source.readSerializable() as PointType,
        source.readInt(),
        source.readLong(),
        source.readInt()

    ) {
        isSynchronizedWithFirebase = 1.toByte() == source.readByte()
    }


    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeSerializable(type)
        writeInt(value)
        writeLong(time)
        writeInt(earnedItemId)
        writeByte((if (isSynchronizedWithFirebase) 1 else 0))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerPoint

        if (codeIdentifier != other.codeIdentifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value
        result = 31 * result + time.hashCode()
        result = 31 * result + isSynchronizedWithFirebase.hashCode()
        result = 31 * result + codeIdentifier.hashCode()
        return result
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PlayerPoint> = object : Parcelable.Creator<PlayerPoint> {
            override fun createFromParcel(source: Parcel): PlayerPoint = PlayerPoint(source)
            override fun newArray(size: Int): Array<PlayerPoint?> = arrayOfNulls(size)
        }

        /**
         * Returns a Cards array from a QuerySnapshot
         * @param cards : QuerySnapshot
         * @return ArrayList<Card>
         */
        fun getOperationsFromFirebaseDocumentSnapshot(pointsOperation: QuerySnapshot): ArrayList<PlayerPoint> {
            val array = ArrayList<PlayerPoint>()
            for (snapshot in pointsOperation) {
                array.add(snapshot.toObject(PlayerPoint::class.java))
            }
            return array
        }
    }
}