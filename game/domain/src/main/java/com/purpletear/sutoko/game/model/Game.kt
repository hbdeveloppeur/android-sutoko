package com.purpletear.sutoko.game.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
data class Game(
    val id: String = "",
    val version: Int = 0,
    val interactionCount: Int = 0,
    val downloadCount: Int = 0,
    val isCertified: Boolean = false,
    val status: String = "",
    val createdAt: Long = 0L,
    val price: Int = 0,
    val skuIdentifiers: List<String> = emptyList(),
    val videoUrl: String? = null,
    val cachedChaptersCount: Int = 0,
    val bannerAsset: Asset? = null,
    val logoAsset: Asset? = null,
    val metadata: GameMetadata,
    val author: Author? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        version = parcel.readInt(),
        interactionCount = parcel.readInt(),
        downloadCount = parcel.readInt(),
        isCertified = parcel.readByte() != 0.toByte(),
        status = parcel.readString() ?: "",
        createdAt = parcel.readLong(),
        price = parcel.readInt(),
        skuIdentifiers = parcel.createStringArrayList() ?: emptyList(),
        videoUrl = parcel.readString(),
        cachedChaptersCount = parcel.readInt(),
        bannerAsset = parcel.readParcelable(Asset::class.java.classLoader),
        logoAsset = parcel.readParcelable(Asset::class.java.classLoader),
        metadata = parcel.readParcelable(GameMetadata::class.java.classLoader) ?: GameMetadata(""),
        author = parcel.readParcelable(Author::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(version)
        parcel.writeInt(interactionCount)
        parcel.writeInt(downloadCount)
        parcel.writeByte(if (isCertified) 1 else 0)
        parcel.writeString(status)
        parcel.writeLong(createdAt)
        parcel.writeInt(price)
        parcel.writeStringList(skuIdentifiers)
        parcel.writeString(videoUrl)
        parcel.writeInt(cachedChaptersCount)
        parcel.writeParcelable(bannerAsset, flags)
        parcel.writeParcelable(logoAsset, flags)
        parcel.writeParcelable(metadata, flags)
        parcel.writeParcelable(author, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }
    }
}

fun Game.isPaying(): Boolean {
    return this.price > 0 && this.skuIdentifiers.isNotEmpty()
}

fun Game.isPremium(): Boolean {
    return this.price > 0
}
