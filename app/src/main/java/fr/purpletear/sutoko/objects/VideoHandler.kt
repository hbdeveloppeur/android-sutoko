package fr.purpletear.sutoko.objects

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
class VideoHandler(
    var isLoading: Boolean,
    var currentVideoName: String
) : Parcelable {


    constructor() : this(false, "")

    constructor(source: Parcel) : this(source.readByte() == 1.toByte(), source.readString() ?: "")

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        dest.writeByte(if (isLoading) 1 else 0)
        dest.writeString(currentVideoName)
    }

    fun isReadyFor(url: String): Boolean {
        return !isLoading && currentVideoName == url
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VideoHandler> = object : Parcelable.Creator<VideoHandler> {
            override fun createFromParcel(source: Parcel): VideoHandler = VideoHandler(source)
            override fun newArray(size: Int): Array<VideoHandler?> = arrayOfNulls(size)
        }
    }
}