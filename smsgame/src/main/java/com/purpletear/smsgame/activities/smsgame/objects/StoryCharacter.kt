/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package com.purpletear.smsgame.activities.smsgame.objects

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class StoryCharacter(
    /**
     * Contains the character's id
     */
    @SerializedName("id")
    var id: Int,

    /**
     * Contains the small image id
     */
    @SerializedName("fname")
    val firstName: String,

    /**
     * Contains the first name of the character
     */
    @SerializedName("lname")
    val lastName: String,

    /**
     * Contains the first name of the character
     */
    @SerializedName("isMainCharacter")
    val isMainCharacter: Boolean
) : Parcelable {

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<StoryCharacter> {
            override fun createFromParcel(parcel: Parcel) = StoryCharacter(parcel)
            override fun newArray(size: Int) = arrayOfNulls<StoryCharacter>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() == 1.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeByte(if (isMainCharacter) 1 else 0)
    }

    override fun describeContents() = 0

    override fun toString(): String {
        return "Character{" +
                "id=" + id +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", isMainCharacter=" + isMainCharacter +
                '}'.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoryCharacter

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}
