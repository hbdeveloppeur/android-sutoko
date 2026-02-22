package com.example.sharedelements

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.example.sharedelements.Data.Companion.INSTAGRAM_PROFIL_NAME
import com.example.sharedelements.Data.Companion.PRIVACY_POLICY_URL
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import java.io.Serializable

@Keep
class SutokoAppParams() : Parcelable, Serializable {
    var isReliable: Boolean = false

    @PropertyName("leavingKeys")
    @get:PropertyName("leavingKeys")
    var leavingKeys: Int = 10

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_SHOULD_START_MAIN_HEADER_ANIMATION)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_SHOULD_START_MAIN_HEADER_ANIMATION)
    var shouldStartMainHeaderAnimation: Boolean = false

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_SHOULD_START_MAIN_POINTS_ANIMATION)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_SHOULD_START_MAIN_POINTS_ANIMATION)
    var shouldStartMainPointsAnimation: Boolean = false

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_HOME_VIDEOS_ENABLED)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_HOME_VIDEOS_ENABLED)
    var homeVideosEnabled: Boolean = false

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_SHOULD_SLIDE_MAIN_HEADER)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_SHOULD_SLIDE_MAIN_HEADER)
    var shouldSlideMainHeader: Boolean = false

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_INSTAGRAM_URL)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_INSTAGRAM_URL)
    var instagramUrl: String = INSTAGRAM_PROFIL_NAME

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_PRIVACY_POLICY_URL)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_PRIVACY_POLICY_URL)
    var privacyPolicyUrl: String = PRIVACY_POLICY_URL

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_REPORT_A_BUG_URL)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_REPORT_A_BUG_URL)
    var reportABugUrl: String = "https://www.sutoko.app/report-a-bug"

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_SHOP_HEADER_BACKGROUND_URL)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_SHOP_HEADER_BACKGROUND_URL)
    var shopBackgroundUrl: String = ""

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_TERMS_OF_USE_URL)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_TERMS_OF_USE_URL)
    var termOfUseUrl: String = ""

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_USELESS_SENTENCE_URL)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_USELESS_SENTENCE_URL)
    var slectedSku: String? = "premium_month_9_49"
        get() {
            if (field == null) return "premium_month_9_49"
            return field
        }

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_MY_ORDERS_HEADER_BACKGROUND_URLL)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_MY_ORDERS_HEADER_BACKGROUND_URLL)
    var myOrdersHeaderbackgroundUrl: String? = ""
        get() {
            if (field == null) return ""
            return field
        }

    @PropertyName(Data.FIREBASE_APP_PARAMS_KEY_AI_CONVERSATION_AVAILABILITY)
    @get:PropertyName(Data.FIREBASE_APP_PARAMS_KEY_AI_CONVERSATION_AVAILABILITY)
    var aiConversationAvailability: Boolean = true

    override fun describeContents() = 0


    companion object {
        /**
         * Returns a Cards array from a QuerySnapshot
         * @param cards : QuerySnapshot
         * @return ArrayList<Card>
         */
        fun getAppParamsFromFirebaseDocumentSnapshot(item: DocumentSnapshot): SutokoAppParams? {
            return item.toObject(SutokoAppParams::class.java)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SutokoAppParams> =
            object : Parcelable.Creator<SutokoAppParams> {
                override fun createFromParcel(source: Parcel): SutokoAppParams =
                    SutokoAppParams(source)

                override fun newArray(size: Int): Array<SutokoAppParams?> = arrayOfNulls(size)
            }
    }

    protected constructor(`in`: Parcel) : this() {
        isReliable = `in`.readByte() == 1.toByte()
        leavingKeys = `in`.readInt()
        shouldStartMainHeaderAnimation = `in`.readByte() == 1.toByte()
        shouldStartMainPointsAnimation = `in`.readByte() == 1.toByte()
        homeVideosEnabled = `in`.readByte() == 1.toByte()
        shouldSlideMainHeader = `in`.readByte() == 1.toByte()
        instagramUrl = `in`.readString() ?: INSTAGRAM_PROFIL_NAME
        privacyPolicyUrl = `in`.readString() ?: PRIVACY_POLICY_URL
        reportABugUrl = `in`.readString() ?: "https://www.sutoko.app/report-a-bug"
        shopBackgroundUrl = `in`.readString() ?: ""
        termOfUseUrl = `in`.readString() ?: ""
        slectedSku = `in`.readString() ?: ""
        myOrdersHeaderbackgroundUrl = `in`.readString() ?: ""
        aiConversationAvailability = `in`.readByte() == 1.toByte()

    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (isReliable) 1 else 0)
        dest.writeInt(leavingKeys)
        dest.writeByte(if (shouldStartMainHeaderAnimation) 1 else 0)
        dest.writeByte(if (shouldStartMainPointsAnimation) 1 else 0)
        dest.writeByte(if (homeVideosEnabled) 1 else 0)
        dest.writeByte(if (shouldSlideMainHeader) 1 else 0)
        dest.writeString(instagramUrl)
        dest.writeString(privacyPolicyUrl)
        dest.writeString(reportABugUrl)
        dest.writeString(shopBackgroundUrl)
        dest.writeString(termOfUseUrl)
        dest.writeString(slectedSku)
        dest.writeString(myOrdersHeaderbackgroundUrl)
        dest.writeByte(if (aiConversationAvailability) 1 else 0)
    }
}