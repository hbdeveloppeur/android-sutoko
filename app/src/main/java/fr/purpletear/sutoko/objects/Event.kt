package fr.purpletear.sutoko.objects

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
class Event(
    val title: String,
    val shortDescription: String,
    val buttonText: String,
    val url: String
) : Parcelable {


    constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readString() ?: "",
        source.readString() ?: "",
        source.readString() ?: ""
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(shortDescription)
        writeString(buttonText)
        writeString(url)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(source: Parcel): Event = Event(source)
            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }
    }
}