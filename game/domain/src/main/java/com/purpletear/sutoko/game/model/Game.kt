package com.purpletear.sutoko.game.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.purpletear.sutoko.core.domain.model.MediaImage

@Keep
data class Game(
    val id: Int = 0,
    val isPremium: Boolean = false,
    val menuSoundUrl: String? = null,
    val minAppCode: Int = 0,
    val keywords: List<String> = emptyList(),
    val releaseDate: Long = 0L,
    val mediaLogoSquare: MediaImage? = null,
    val mediaMainBanner: MediaImage? = null,
    val mediaPreviewBackground: MediaImage? = null,
    val versionCode: String = "",
    val price: Int? = null,
    val metadata: GameMetadata,
    val skuIdentifiers: List<String> = emptyList(),
    val videoUrl: String? = null,
    val cachedChaptersCount: Int = 0,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        isPremium = parcel.readByte() != 0.toByte(),
        menuSoundUrl = parcel.readString(),
        minAppCode = parcel.readInt(),
        keywords = parcel.createStringArrayList() ?: emptyList(),
        releaseDate = parcel.readLong(),
        mediaLogoSquare = parcel.readParcelable(MediaImage::class.java.classLoader),
        mediaMainBanner = parcel.readParcelable(MediaImage::class.java.classLoader),
        mediaPreviewBackground = parcel.readParcelable(MediaImage::class.java.classLoader),
        versionCode = parcel.readString() ?: "",
        price = parcel.readValue(Int::class.java.classLoader) as? Int,
        metadata = parcel.readParcelable(GameMetadata::class.java.classLoader) ?: GameMetadata(""),
        skuIdentifiers = parcel.createStringArrayList() ?: emptyList(),
        videoUrl = parcel.readString(),
        cachedChaptersCount = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeByte(if (isPremium) 1 else 0)
        parcel.writeString(menuSoundUrl)
        parcel.writeInt(minAppCode)
        parcel.writeStringList(keywords)
        parcel.writeLong(releaseDate)
        parcel.writeParcelable(mediaLogoSquare, flags)
        parcel.writeParcelable(mediaMainBanner, flags)
        parcel.writeParcelable(mediaPreviewBackground, flags)
        parcel.writeString(versionCode)
        parcel.writeValue(price)
        parcel.writeParcelable(metadata, flags)
        parcel.writeStringList(skuIdentifiers)
        parcel.writeString(videoUrl)
        parcel.writeInt(cachedChaptersCount)
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
    return this.isPremium && this.skuIdentifiers.isNotEmpty()
}
