package com.purpletear.sutoko.game.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class Author(
    val displayName: String,
    val avatarUrl: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        displayName = parcel.readString() ?: "",
        avatarUrl = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayName)
        parcel.writeString(avatarUrl)
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
