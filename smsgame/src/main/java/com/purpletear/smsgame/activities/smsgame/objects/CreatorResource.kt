package com.purpletear.smsgame.activities.smsgame.objects

import android.os.Parcel
import android.os.Parcelable

/**
 * A Creator resource is an Object used by the user to fill their stories
 * @property name String
 * @property url String
 * @property filename String
 * @property minAppCode Int
 * @constructor
 */
class CreatorResource(
    val id: Int,
    val name: String,
    val url: String,
    val previewUrl: String,
    val filename: String,
    val minAppCode: Int,
    val isPremium: Boolean,
    var type: String
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString() ?: "",
        source.readString() ?: "",
        source.readString() ?: "",
        source.readString() ?: "",
        source.readInt(),
        source.readByte() == 1.toByte(),
        source.readString() ?: ""
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(id)
        writeString(name)
        writeString(url)
        writeString(previewUrl)
        writeString(filename)
        writeInt(minAppCode)
        writeByte(if (isPremium) 1 else 0)
        writeString(type)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreatorResource

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CreatorResource> =
            object : Parcelable.Creator<CreatorResource> {
                override fun createFromParcel(source: Parcel): CreatorResource =
                    CreatorResource(source)

                override fun newArray(size: Int): Array<CreatorResource?> = arrayOfNulls(size)
            }
    }
}