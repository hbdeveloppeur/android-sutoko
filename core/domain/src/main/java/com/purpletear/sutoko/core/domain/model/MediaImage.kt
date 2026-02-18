package com.purpletear.sutoko.core.domain.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class MediaImage(
    override val id: Long,
    override val type: String,
    val width: Int,
    val height: Int,
    val bytes: Int,
    val directory: String,
    val mimeType: String,
    val filename: String,
) : Media() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(type)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeInt(bytes)
        parcel.writeString(directory)
        parcel.writeString(mimeType)
        parcel.writeString(filename)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MediaImage> = object : Parcelable.Creator<MediaImage> {
            override fun createFromParcel(parcel: Parcel): MediaImage {
                val id = parcel.readLong()
                val type = parcel.readString() ?: ""
                val width = parcel.readInt()
                val height = parcel.readInt()
                val bytes = parcel.readInt()
                val directory = parcel.readString() ?: ""
                val mimeType = parcel.readString() ?: ""
                val filename = parcel.readString() ?: ""
                return MediaImage(id, type, width, height, bytes, directory, mimeType, filename)
            }

            override fun newArray(size: Int): Array<MediaImage?> {
                return arrayOfNulls(size)
            }
        }
    }
}
