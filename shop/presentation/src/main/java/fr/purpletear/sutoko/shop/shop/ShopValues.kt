package fr.purpletear.sutoko.shop.shop

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

@Keep
class ShopValues() : Parcelable {
    var canBuyStoriesWithMoney: Boolean = false
    var lowPack: SutokoShopPack? = null
        private set
    var mediumPack: SutokoShopPack? = null
        private set
    var highPack: SutokoShopPack? = null
        private set
    var premium: SutokoShopPack? = null
        private set

    constructor(parcel: Parcel) : this() {
        canBuyStoriesWithMoney = parcel.readByte() == 1.toByte()
        lowPack = parcel.readParcelable(SutokoShopPack::class.java.classLoader)
        mediumPack = parcel.readParcelable(SutokoShopPack::class.java.classLoader)
        highPack = parcel.readParcelable(SutokoShopPack::class.java.classLoader)
        premium = parcel.readParcelable(SutokoShopPack::class.java.classLoader)
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (canBuyStoriesWithMoney) 1 else 0)
        parcel.writeParcelable(lowPack, flags)
        parcel.writeParcelable(mediumPack, flags)
        parcel.writeParcelable(highPack, flags)
        parcel.writeParcelable(premium, flags)
    }

    fun get() {
        val json = "{\n" +
                "  \"canBuyStoriesWithMoney\": false,\n" +
                "  \"lowPack\": {\n" +
                "    \"d\": 550,\n" +
                "    \"c\": 550,\n" +
                "    \"s\": \"coins_pack_starter\"\n" +
                "  },\n" +
                "  \"mediumPack\": {\n" +
                "    \"d\": 1500,\n" +
                "    \"c\": 1500,\n" +
                "    \"s\": \"coins_pack_treasure\"\n" +
                "  },\n" +
                "  \"highPack\": {\n" +
                "    \"d\": 3000,\n" +
                "    \"c\": 3000,\n" +
                "    \"s\": \"coins_pack_mega\"\n" +
                "  },\n" +
                "  \"premium\": {\n" +
                "    \"d\": 1000,\n" +
                "    \"c\": 0,\n" +
                "    \"s\": \"sutoko_premium_yearly_69\"\n" +
                "  }\n" +
                "}"
        val item = Gson().fromJson(json, ShopValues::class.java)
        this.copy(item)
    }

    fun getSkus(): Array<String> {
        return arrayOf(
            this.lowPack?.sku ?: "coins_pack_starter",
            this.mediumPack?.sku ?: "coins_pack_treasure",
            this.highPack?.sku ?: "coins_pack_mega",
            this.premium?.sku ?: "sutoko_premium_yearly_69",
        )
    }

    private fun getPacks(): Array<SutokoShopPack?> {
        return arrayOf(
            this.lowPack,
            this.mediumPack,
            this.highPack,
            this.premium,
        )
    }

    /**
     * @throws IllegalStateException
     */
    fun getPackFromSku(sku: String): SutokoShopPack {
        assert(sku.isNotBlank())
        this.getPacks().forEach { s ->
            if (sku == s?.sku) {
                return@getPackFromSku s
            }
        }
        throw IllegalStateException("Pack not found for sku : $sku")
    }

    private fun copy(o: ShopValues?) {
        val values = o ?: getDefaultShopValues()
        this.canBuyStoriesWithMoney = values.canBuyStoriesWithMoney
        this.lowPack = values.lowPack
        this.mediumPack = values.mediumPack
        this.highPack = values.highPack
        this.premium = values.premium
    }

    private fun getDefaultShopValues(): ShopValues {
        val o = ShopValues()
        o.canBuyStoriesWithMoney = false
        o.lowPack = SutokoShopPack(550, 550, "coins_pack_starter")
        o.mediumPack = SutokoShopPack(1500, 1500, "coins_pack_treasure")
        o.highPack = SutokoShopPack(3200, 3200, "coins_pack_starter")
        o.premium = SutokoShopPack(1000, 0, "sutoko_premium_yearly_69")
        return o
    }

    @Keep
    class SutokoShopPack() : Parcelable {
        @SerializedName("d")
        var diamonds: Int = 0

        @SerializedName("c")
        var coins: Int = 0

        @SerializedName("s")
        var sku: String? = null

        constructor(parcel: Parcel) : this() {
            diamonds = parcel.readInt()
            coins = parcel.readInt()
            sku = parcel.readString()
        }

        override fun equals(other: Any?): Boolean {
            return other is SutokoShopPack && other.sku == sku
        }

        constructor(diamonds: Int, coins: Int, sku: String) : this() {
            this.diamonds = diamonds
            this.coins = coins
            this.sku = sku
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(diamonds)
            parcel.writeInt(coins)
            parcel.writeString(sku)
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun hashCode(): Int {
            var result = diamonds
            result = 31 * result + coins
            result = 31 * result + (sku?.hashCode() ?: 0)
            return result
        }

        companion object CREATOR : Parcelable.Creator<SutokoShopPack> {
            override fun createFromParcel(parcel: Parcel): SutokoShopPack {
                return SutokoShopPack(parcel)
            }

            override fun newArray(size: Int): Array<SutokoShopPack?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShopValues> {
        override fun createFromParcel(parcel: Parcel): ShopValues {
            return ShopValues(parcel)
        }

        override fun newArray(size: Int): Array<ShopValues?> {
            return arrayOfNulls(size)
        }
    }
}

