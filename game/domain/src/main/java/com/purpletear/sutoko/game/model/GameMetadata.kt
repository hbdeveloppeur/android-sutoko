package com.purpletear.sutoko.game.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class GameMetadata(
    val title: String,
    val description: String? = null,
    val lang: String? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        title = parcel.readString() ?: "",
        description = parcel.readString(),
        lang = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(lang)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GameMetadata> {
        override fun createFromParcel(parcel: Parcel): GameMetadata {
            return GameMetadata(parcel)
        }

        override fun newArray(size: Int): Array<GameMetadata?> {
            return arrayOfNulls(size)
        }
    }
}
