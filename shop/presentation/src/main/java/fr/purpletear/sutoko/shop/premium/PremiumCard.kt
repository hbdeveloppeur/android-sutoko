package fr.purpletear.sutoko.shop.premium

import android.os.Parcel
import android.os.Parcelable

class PremiumCard() : Parcelable {
    var title: String = ""
        private set

    var label: String = ""
        private set

    var mainColor: String = ""
        private set

    var imageResId: String = ""
        private set

    var description: String = ""
        private set

    fun isBackgroundImageUrl(): Boolean {
        return imageResId.startsWith("http")
                || imageResId.startsWith("wwww.")
                || imageResId.contains(".com")
                || imageResId.contains(".net")
                || imageResId.contains(".app")
                || imageResId.contains(".fr")
    }

    constructor(source: Parcel) : this() {
        title = source.readString() ?: ""
        label = source.readString() ?: ""
        mainColor = source.readString() ?: ""
        imageResId = source.readString() ?: ""
        description = source.readString() ?: ""
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        dest.writeString(title)
        dest.writeString(label)
        dest.writeString(mainColor)
        dest.writeString(imageResId)
        dest.writeString(description)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PremiumCard> = object : Parcelable.Creator<PremiumCard> {
            override fun createFromParcel(source: Parcel): PremiumCard = PremiumCard(source)
            override fun newArray(size: Int): Array<PremiumCard?> = arrayOfNulls(size)
        }
    }
}