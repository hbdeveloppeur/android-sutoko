package com.purpletear.smsgame.activities.smsgame.objects


import android.os.Parcel
import android.os.Parcelable

class StoryEvent() : Parcelable {
    var title: String = ""
    var subtitle: String = ""
    var icon: String = ""

    constructor(source: Parcel) : this() {
        title = source.readString() ?: ""
        subtitle = source.readString() ?: ""
        icon = source.readString() ?: ""
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(subtitle)
        dest.writeString(icon)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StoryEvent> = object : Parcelable.Creator<StoryEvent> {
            override fun createFromParcel(source: Parcel): StoryEvent = StoryEvent(source)
            override fun newArray(size: Int): Array<StoryEvent?> = arrayOfNulls(size)
        }
    }
}