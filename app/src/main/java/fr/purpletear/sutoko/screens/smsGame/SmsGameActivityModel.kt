package fr.purpletear.sutoko.screens.smsGame

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.example.sharedelements.Data

/**
 * Model class for SmsGameActivity containing the parameters needed to launch the game.
 *
 * @property gameId The unique identifier of the game to be played
 * @property isGranted Whether the user has premium/granted access to the game
 */
@Keep
class SmsGameActivityModel(
    val gameId: String,
    val isGranted: Boolean
) : Parcelable {

    constructor(parcel: Parcel) : this(
        gameId = parcel.readString() ?: "",
        isGranted = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(gameId)
        parcel.writeByte(if (isGranted) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object {
        /**
         * Extracts the model from the intent extras.
         *
         * @param activity The activity to extract the model from
         * @return The SmsGameActivityModel or null if not found
         */
        fun fromIntent(activity: Activity): SmsGameActivityModel? {
            return activity.intent.getParcelableExtra(
                Data.Companion.Extra.SMS_GAME_MODEL.id
            ) as? SmsGameActivityModel
        }

        /**
         * Key used for storing the model in intent extras.
         */
        val extraKey: String
            get() = Data.Companion.Extra.SMS_GAME_MODEL.id

        @JvmField
        val CREATOR: Parcelable.Creator<SmsGameActivityModel> =
            object : Parcelable.Creator<SmsGameActivityModel> {
                override fun createFromParcel(source: Parcel): SmsGameActivityModel =
                    SmsGameActivityModel(source)

                override fun newArray(size: Int): Array<SmsGameActivityModel?> = arrayOfNulls(size)
            }
    }
}
