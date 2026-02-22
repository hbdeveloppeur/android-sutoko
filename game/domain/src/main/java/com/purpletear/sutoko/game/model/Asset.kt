package com.purpletear.sutoko.game.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class Asset(
    val id: Long,
    val originalFilename: String,
    val width: Int,
    val height: Int,
    val createdAt: Long,
    val fileSizeBytes: Int,
    val mimeType: String,
    val storagePath: String,
    val thumbnailStoragePath: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        originalFilename = parcel.readString() ?: "",
        width = parcel.readInt(),
        height = parcel.readInt(),
        createdAt = parcel.readLong(),
        fileSizeBytes = parcel.readInt(),
        mimeType = parcel.readString() ?: "",
        storagePath = parcel.readString() ?: "",
        thumbnailStoragePath = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(originalFilename)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeLong(createdAt)
        parcel.writeInt(fileSizeBytes)
        parcel.writeString(mimeType)
        parcel.writeString(storagePath)
        parcel.writeString(thumbnailStoragePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Asset> {
        override fun createFromParcel(parcel: Parcel): Asset {
            return Asset(parcel)
        }

        override fun newArray(size: Int): Array<Asset?> {
            return arrayOfNulls(size)
        }
    }
}
