package friendzone3.purpletear.fr.friendzon3

import android.app.Activity
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.OnlineAssetsManager
import com.example.sutokosharedelements.OnlineAssetsManager.getImageFilePath
import friendzone3.purpletear.fr.friendzon3.MainActivity.Support
import friendzone3.purpletear.fr.friendzon3.custom.Character.Companion.updateNames
import friendzone3.purpletear.fr.friendzon3.custom.Video
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.GlobalData

class MainActivityGraphics : Parcelable {
    var isOpenedDescription: Boolean = false
    var isChoiceButtonVisible: Boolean = false
    var isDisplayedOnlinePoint: Boolean = false
    var conversationTitle: String = ""
    var conversationStatus: String = ""
    var videoNameToReload: String = ""
    var backgroundImageName: String = ""
    var profilPictureId: String = ""
    var backgroundImageId: String = ""


    constructor(parcel: Parcel) {
        isOpenedDescription = parcel.readByte() == 1.toByte()
        isChoiceButtonVisible = parcel.readByte() == 1.toByte()
        isDisplayedOnlinePoint = parcel.readByte() == 1.toByte()
        conversationTitle = parcel.readString() ?: ""
        conversationStatus = parcel.readString() ?: ""
        videoNameToReload = parcel.readString() ?: ""
        backgroundImageName = parcel.readString() ?: ""
        profilPictureId = parcel.readString() ?: ""
        backgroundImageId = parcel.readString() ?: ""
    }

    companion object {


        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<MainActivityGraphics> =
            object : Parcelable.Creator<MainActivityGraphics> {
                override fun createFromParcel(`in`: Parcel): MainActivityGraphics {
                    return MainActivityGraphics(`in`)
                }

                override fun newArray(size: Int): Array<MainActivityGraphics?> {
                    return arrayOfNulls(size)
                }
            }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (isOpenedDescription) 1 else 0)
        dest.writeByte(if (isChoiceButtonVisible) 1 else 0)
        dest.writeByte(if (isDisplayedOnlinePoint) 1 else 0)
        dest.writeString(conversationTitle)
        dest.writeString(conversationStatus)
        dest.writeString(videoNameToReload)
        dest.writeString(backgroundImageName)
        dest.writeString(profilPictureId)
        dest.writeString(backgroundImageId)
    }

    override fun describeContents(): Int {
        return 0
    }


    constructor(
        isOpenedDescription: Boolean,
        isChoiceButtonVisible: Boolean,
        conversationTitle: String,
        conversationStatus: String,
        videoNameToReload: String,
        profilPictureId: String,
        backgroundImageId: String
    ) {
        this.isOpenedDescription = isOpenedDescription
        this.isChoiceButtonVisible = isChoiceButtonVisible
        this.conversationTitle = conversationTitle
        this.conversationStatus = conversationStatus
        this.videoNameToReload = videoNameToReload
        this.profilPictureId = profilPictureId
        this.backgroundImageId = backgroundImageId
    }


    fun setImages(
        activity: Activity,
        requestManager: RequestManager
    ) {
        requestManager.load(
            getImageFilePath(
                activity,
                GlobalData.Game.FRIENDZONE3.id,
                "friendzone3_online_point"
            )
        ).into((activity.findViewById<View>(R.id.mainactivity_online_point) as ImageView))

        requestManager.load(
            getImageFilePath(
                activity,
                GlobalData.Game.FRIENDZONE3.id,
                "friendzone3_overlay"
            )
        ).into(
            (activity.findViewById<View>(R.id.mainactivity_overlay)
                .findViewById(R.id.phrase_notification_image) as ImageView)
        )

        requestManager.load(
            getImageFilePath(
                activity,
                GlobalData.Game.FRIENDZONE3.id,
                "friendzone3_ico_call"
            )
        ).into((activity.findViewById<View>(R.id.mainactivity_header_icon_1) as ImageView))

        requestManager.load(
            getImageFilePath(
                activity,
                GlobalData.Game.FRIENDZONE3.id,
                "friendzone3_ico_share"
            )
        ).into((activity.findViewById<View>(R.id.mainactivity_header_icon_2) as ImageView))

        requestManager.load(
            getImageFilePath(
                activity,
                GlobalData.Game.FRIENDZONE3.id,
                "friendzone3_ico_info"
            )
        ).into((activity.findViewById<View>(R.id.mainactivity_header_icon_3) as ImageView))
    }

    /**
     * Sets the description visibility
     *
     * @param activity
     * @param isVisible
     * @param support
     */
    fun setDescriptionVisibility(activity: Activity, isVisible: Boolean, support: Support) {
        val v: View
        when (support) {
            Support.NORMAL -> v = activity.findViewById<View>(R.id.mainactivity_filter)
            Support.SHELL -> v = activity.findViewById<View>(R.id.mainactivity_shell_filter)
        }
        v.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        isOpenedDescription = isVisible
    }

    /**
     *
     *
     * @param activity
     * @param imageId
     * @param support
     * @param requestManager
     */
    fun setProfilPicture(
        activity: Activity,
        imageId: String,
        support: Support,
        requestManager: RequestManager
    ) {
        if (support == Support.NORMAL) {
            if (imageId == profilPictureId) {
                return
            }
            profilPictureId = OnlineAssetsManager.getImageFilePath(
                activity,
                GlobalData.Game.FRIENDZONE3.id,
                imageId
            )
            requestManager.load(profilPictureId)
                .into(activity.findViewById(R.id.mainactivity_photo))
        }
    }

    /**
     * Sets conversation name
     *
     * @param activity
     * @param name
     */
    fun setNameConversation(activity: Activity, name: String, support: Support) {
        if (support == Support.SHELL) return
        conversationTitle = name
        (activity.findViewById<View>(R.id.mainactivity_name) as TextView).text =
            updateNames(activity, name)
    }

    /**
     * Changes the status with the libelle you want, the color and the execution code to concerning it
     *
     * @param libelle              the libelle you want
     * @param colorId              the id of the color
     * @param onlinePointDisplayed do the onlinePoint should be displayed ?
     */
    fun changeStatus(
        activity: Activity,
        libelle: String,
        colorId: Int,
        onlinePointDisplayed: Boolean,
        support: Support
    ) {
        val t: TextView
        when (support) {
            Support.NORMAL -> t = activity.findViewById<TextView>(R.id.mainactivity_status)
            else -> return
        }
        t.text = updateNames(
            activity,
            libelle
        )
        t.setTextColor(ContextCompat.getColor(activity, colorId))
        activity.findViewById<View>(R.id.mainactivity_online_point).visibility =
            if (onlinePointDisplayed) View.VISIBLE else View.INVISIBLE

        this.conversationStatus = libelle
        this.isDisplayedOnlinePoint = onlinePointDisplayed
    }


    /**
     *
     *
     * @param activity
     * @param isVisible
     * @param support
     */
    fun setButtonVisibility(activity: Activity, isVisible: Boolean, support: Support) {
        val button =
            when (support) {
                Support.NORMAL -> {
                    activity.findViewById<View>(R.id.mainactivity_button)
                }
                Support.SHELL -> {
                    activity.findViewById<View>(R.id.mainactivity_shell_button)
                }
                else -> throw java.lang.IllegalArgumentException("Unknow support type.")
            }
        button.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        isChoiceButtonVisible = isVisible
    }

    /**
     * Switch the background to the video mode
     *
     * @param name the name of the resource without the extension.
     */
    fun switchVideo(activity: Activity, name: String) {
        videoNameToReload = name
        if (name.replace(" ", "") == "") {
            return
        }

        val r: RelativeLayout =
            activity.findViewById(R.id.mainactivity_background)
        val v: VideoView =
            activity.findViewById(R.id.mainactivty_background_video)
        val i: ImageView =
            activity.findViewById(R.id.mainactivty_background_image)
        MainActivity.hasBackgroundMedia = true
        Video.put(
            v,
            Uri.parse(
                "android.resource://" + activity.packageName + "/" + Video.determine(
                    name,
                    activity
                )
            ),
            true
        ) { videoNameToReload = name }
        Animation.setAnimation(
            r,
            Animation.Animations.ANIMATION_FADEIN,
            activity
        )
        r.visibility = View.VISIBLE
        Animation.setAnimation(
            v,
            Animation.Animations.ANIMATION_FADEIN,
            activity
        )
        v.visibility = View.VISIBLE
        Animation.setAnimation(
            i,
            Animation.Animations.ANIMATION_FADEOUT,
            activity
        )
        i.visibility = View.INVISIBLE
    }

    /**
     * Switch the background to the Image mode
     *
     * @param name the name of the resource without the extension.
     */
    fun switchImage(
        activity: Activity,
        name: String,
        support: Support,
        requestManager: RequestManager
    ) {
        if (name.replace(" ", "") == "") {
            backgroundImageId = ""
            backgroundImageName = ""
            return
        }
        MainActivity.hasBackgroundMedia = false
        require(support == Support.NORMAL) { "MainActivity.switchImage cannot change image with Support type of ordinal +" + " " + support.ordinal.toString() }
        val r: RelativeLayout =
            activity.findViewById(R.id.mainactivity_background)
        val v: VideoView =
            activity.findViewById(R.id.mainactivty_background_video)
        val i: ImageView =
            activity.findViewById(R.id.mainactivty_background_image)
        val id: String =
            getImageFilePath(activity, GlobalData.Game.FRIENDZONE3.id, "friendzone3_$name")
        //require(id != 0) { "Image $name not found" }
        backgroundImageId = id
        backgroundImageName = name
        requestManager.load(id).into(i)
        MainActivity.hasBackgroundMedia = true
        if (r.visibility == View.INVISIBLE) {
            Animation.setAnimation(
                r,
                Animation.Animations.ANIMATION_FADEIN,
                activity
            )
            r.visibility = View.VISIBLE
        }
        if (v.visibility == View.VISIBLE) {
            Animation.setAnimation(
                v,
                Animation.Animations.ANIMATION_FADEOUT,
                activity
            )
            v.visibility = View.INVISIBLE
        }
        if (i.visibility == View.INVISIBLE) {
            Animation.setAnimation(
                i,
                Animation.Animations.ANIMATION_FADEIN,
                activity
            )
            i.visibility = View.VISIBLE
        }
    }

}