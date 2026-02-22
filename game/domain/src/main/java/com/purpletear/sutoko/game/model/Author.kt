package com.purpletear.sutoko.game.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class Author(
    val displayName: String,
    val avatarUrl: String?,
    val isCertified: Boolean = false,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        displayName = parcel.readString() ?: "",
        avatarUrl = parcel.readString(),
        isCertified = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayName)
        parcel.writeString(avatarUrl)
        parcel.writeByte(if (isCertified) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Author> {
        override fun createFromParcel(parcel: Parcel): Author {
            return Author(parcel)
        }

        override fun newArray(size: Int): Array<Author?> {
            return arrayOfNulls(size)
        }
    }
}
