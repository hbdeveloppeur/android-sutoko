package fr.purpletear.friendzone2.activities.phone.homescreen

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sutokosharedelements.OnlineAssetsManager
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.activities.main.MainGraphics
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.MemoryHandler
import purpletear.fr.purpleteartools.Runnable2

class HomeScreenGraphics : Parcelable {
    val mh: MemoryHandler = MemoryHandler()

    // Is switch visible
    var switchVisibility: Boolean = true
    // Is signal visible
    var signalVisibility: Boolean = true
    // Is notification visible
    var notificationVisibility: Boolean = false

    constructor()

    /**
     * Sets the initial images
     * @param a : Activity
     * @param rm : RequestManager
     */
    fun setImages(a: Activity, rm: RequestManager) {
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "evaphone")).into(a.findViewById(R.id.phone_homescreen_background))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ic_arrow_left_white")).into(a.findViewById(R.id.phone_homescreen_button_back))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "btn_call")).into(a.findViewById(R.id.phone_homescreen_button_call))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "btn_sms")).into(a.findViewById(R.id.phone_homescreen_button_sms))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "btn_photos")).into(a.findViewById(R.id.phone_homescreen_button_pictures))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "btn_sound")).into(a.findViewById(R.id.phone_homescreen_button_music))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ico_call_miss")).into(a.findViewById(R.id.phone_statusbar_icon_call))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ico_sms_miss")).into(a.findViewById(R.id.phone_statusbar_icon_sms))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ico_battery")).into(a.findViewById(R.id.phone_statusbar_icon_battery))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "ico_signal")).into(a.findViewById(R.id.phone_statusbar_icon_signal))

        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id, "hour")).into(a.findViewById(R.id.phone_homescreen_clock))

        // rm.load(R.drawable.friendzone).apply(requestOptions).into(a.findViewById(R.id.main_lost))
    }

    /**
     * Updates the battery's percent visibility every 1000 ms
     * @param activity : Activity
     * @param visible : Boolean
     */
    fun percentGraphics(activity: Activity, visible: Boolean = false) {
        val runnable = object : Runnable2("percentGraphics", 1000) {
            override fun run() {
                setPercentVisibility(activity, visible)
                percentGraphics(activity, !visible)
            }
        }
        mh.push(runnable)
        mh.run(runnable)
    }

    /**
     * Updates the battery's percent visibility
     * @param activity : Activity
     * @param isVisible : Boolean
     */
    private fun setPercentVisibility(activity: Activity, isVisible: Boolean) {
        activity.findViewById<View>(R.id.phone_statusbar_icon_battery).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Updates the signal icon's visibility
     * @param activity : Activity
     * @param isVisible : Boolean
     */
    fun setSignalIconVisibility(activity: Activity, isVisible: Boolean = signalVisibility) {
        signalVisibility = isVisible
        activity.findViewById<View>(R.id.phone_statusbar_icon_signal).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Updates the notification icon's visibility
     * @param activity : Activity
     * @param isVisible : Boolean
     */
    fun setSmsNotificationVisibility(activity: Activity, isVisible: Boolean = notificationVisibility) {
        notificationVisibility = isVisible
        activity.findViewById<View>(R.id.phone_homescreen_button_sms_notification).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Updates the switch element's visibility
     * @param activity : Activity
     * @param isVisible : Boolean
     */
    fun setSwitchVisibility(activity: Activity, isVisible: Boolean = switchVisibility) {
        switchVisibility = isVisible
        activity.findViewById<View>(R.id.phone_homescreen_switch).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        activity.findViewById<View>(R.id.phone_homescreen_switch_label).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }


    constructor(parcel: Parcel) {
        signalVisibility = (parcel.readByte().toInt() == 1)
        notificationVisibility = (parcel.readByte().toInt() == 1)
        switchVisibility = (parcel.readByte().toInt() == 1)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (signalVisibility) {
            1.toByte()
        } else {
            0.toByte()
        })
        dest.writeByte(if (notificationVisibility) {
            1.toByte()
        } else {
            0.toByte()
        })
        dest.writeByte(if (switchVisibility) {
            1.toByte()
        } else {
            0.toByte()
        })
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<HomeScreenGraphics> = object : Parcelable.Creator<HomeScreenGraphics> {
            override fun createFromParcel(`in`: Parcel): HomeScreenGraphics {
                return HomeScreenGraphics(`in`)
            }

            override fun newArray(size: Int): Array<HomeScreenGraphics?> {
                return arrayOfNulls(size)
            }
        }
    }
}
