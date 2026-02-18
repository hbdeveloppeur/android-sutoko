package com.purpletear.smsgame.activities.manga

import android.os.Parcel
import android.os.Parcelable

class MangaMessage() : Parcelable {
    var text: String = ""

    var size: Float = 30f

    var x: Float = 0f

    var y: Float = 0f

    var w: Float = 0f

    constructor(source: Parcel) : this() {
        text = source.readString() ?: ""
        size = source.readFloat()
        x = source.readFloat()
        y = source.readFloat()
        w = source.readFloat()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(text)
        dest.writeFloat(size)
        dest.writeFloat(x)
        dest.writeFloat(y)
        dest.writeFloat(w)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MangaMessage> = object : Parcelable.Creator<MangaMessage> {
            override fun createFromParcel(source: Parcel): MangaMessage = MangaMessage(source)
            override fun newArray(size: Int): Array<MangaMessage?> = arrayOfNulls(size)
        }
    }
}