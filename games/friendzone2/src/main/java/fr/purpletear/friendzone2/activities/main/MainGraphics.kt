/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.activities.main

import android.app.Activity
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import fr.purpletear.friendzone2.configs.CustomLinearLayoutManager
import fr.purpletear.friendzone2.configs.GameGridItemDecoration
import fr.purpletear.friendzone2.R
import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.sharedelements.OnlineAssetsManager
import purpletear.fr.purpleteartools.*

class MainGraphics : Parcelable {
    var choiceBoxIsVisible: Boolean
        private set
    private var profilPictureId: String
        private set
    private var conversationName: String
    private var conversationStatus: String
    private var backgroundImageId: String
    private var torchDrawableId: String
    private var mainButtonIsVisible: Boolean
    private var lostImageIsVisible: Boolean
    private var iconsAreVisible: Boolean
    private var isDescriptionVisible: Boolean

    var videoToReload: String = ""

    constructor(conversationName: String,
                conversationStatus: String,
                profilPictureId: String,
                backgroundImageId: String,
                torchDrawableId: String,
                choiceBoxIsVisible: Boolean,
                mainButtonIsVisible: Boolean,
                lostImageIsVisible: Boolean,
                iconsAreVisible: Boolean,
                isDescriptionVisible: Boolean
    ) {
        this.conversationName = conversationName
        this.conversationStatus = conversationStatus
        this.profilPictureId = profilPictureId
        this.backgroundImageId = backgroundImageId
        this.torchDrawableId = torchDrawableId
        this.choiceBoxIsVisible = choiceBoxIsVisible
        this.mainButtonIsVisible = mainButtonIsVisible
        this.lostImageIsVisible = lostImageIsVisible
        this.lostImageIsVisible = lostImageIsVisible
        this.iconsAreVisible = iconsAreVisible
        this.isDescriptionVisible = isDescriptionVisible

    }

    constructor(parcel: Parcel) {
        videoToReload = parcel.readString() ?: videoToReload
        conversationName = parcel.readString() ?: ""
        conversationStatus = parcel.readString() ?: ""
        profilPictureId = parcel.readString() ?: ""
        backgroundImageId = parcel.readString() ?: ""
        torchDrawableId = parcel.readString() ?: ""
        choiceBoxIsVisible = (parcel.readByte().toInt() == 1)
        mainButtonIsVisible = (parcel.readByte().toInt() == 1)
        lostImageIsVisible = (parcel.readByte().toInt() == 1)
        iconsAreVisible = (parcel.readByte().toInt() == 1)
        isDescriptionVisible = (parcel.readByte().toInt() == 1)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(videoToReload)
        dest.writeString(conversationName)
        dest.writeString(conversationStatus)
        dest.writeString(profilPictureId)
        dest.writeString(backgroundImageId)
        dest.writeString(torchDrawableId)
        dest.writeByte(if (choiceBoxIsVisible) {
            1.toByte()
        } else {
            0.toByte()
        })
        dest.writeByte(if (mainButtonIsVisible) {
            1.toByte()
        } else {
            0.toByte()
        })
        dest.writeByte(if (lostImageIsVisible) {
            1.toByte()
        } else {
            0.toByte()
        })
        dest.writeByte(if (iconsAreVisible) {
            1.toByte()
        } else {
            0.toByte()
        })
        dest.writeByte(if (isDescriptionVisible) {
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
        val CREATOR: Parcelable.Creator<MainGraphics> = object : Parcelable.Creator<MainGraphics> {
            override fun createFromParcel(`in`: Parcel): MainGraphics {
                return MainGraphics(`in`)
            }

            override fun newArray(size: Int): Array<MainGraphics?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * Sets the initial images
     * @param a : Activity
     * @param rm : RequestManager
     */
    fun setInitialImages(a: Activity, rm: RequestManager) {
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)

        rm.load(R.drawable.ico_share).into(a.findViewById(R.id.main_header_icons_share))
        rm.load(R.drawable.ico_call).into(a.findViewById(R.id.main_header_icons_call))
        rm.load(OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "ico_info")).into(a.findViewById(R.id.main_header_icons_info))
        rm.load(R.drawable.friendzone).apply(requestOptions).into(a.findViewById(R.id.main_lost))
    }

    /**
     * Sets the recyclerView
     * @param a : Activity
     */
    fun setRecyclerView(a: Activity, adapter: GameConversationAdapter, display: Display) {
        val recyclerview = a.findViewById<RecyclerView>(R.id.main_recyclerview)
        val lLayout = CustomLinearLayoutManager(a, 6f).apply {
            stackFromEnd = true
        }
        recyclerview.apply {
            setHasFixedSize(true)
            setAdapter(adapter)
            addItemDecoration(
                    GameGridItemDecoration(
                            Math.round(

                                    Measure.percent(
                                            Measure.Type.HEIGHT,
                                            1.5f,
                                            display
                                    )), false))
            layoutManager = lLayout
        }
    }

    /**
     * Fades the filter out
     * @param a : Activity
     */
    fun fadeBlackFilter(a: Activity) {
        Std.debug("Fading Black filter")
        val filter = a.findViewById<View>(R.id.main_main_filter_black)

        Animation.setAnimation(
                filter,
                Animation.Animations.ANIMATION_FADEOUT,
                a,
                1280
        )
    }

    /**
     * Sets the description's title
     * @param a : Activity
     * @param str : String
     */
    fun setDescriptionTitle(a: Activity, str: String) {
        val description = a.findViewById<TextView>(R.id.mainactivity_description_title)
        description.text = str
    }

    /**
     * Sets the description's content
     * @param a : Activity
     * @param str : String
     */
    fun setDescriptionContent(a: Activity, str: String) {
        val description = a.findViewById<TextView>(R.id.mainactivity_description_content)
        description.text = str
    }

    /**
     * Hides the description's filter
     * @param a : Activity
     */
    fun fadeDescriptionFilter(a: Activity, isVisible: Boolean = isDescriptionVisible) {
        isDescriptionVisible = isVisible
        if (isVisible) {
            return
        }
        val description = a.findViewById<View>(R.id.main_description)

        Animation.setAnimation(
                description,
                Animation.Animations.ANIMATION_FADEOUT,
                a
        )
    }

    /**
     * Sets the profil's picture
     * @param a : Activity
     * @param rm : RequestManager
     * @param drawableId : Int
     */
    fun setProfilPicture(a: Activity, rm: RequestManager, drawableId: String = profilPictureId) {
        profilPictureId = drawableId
        val image = a.findViewById<ImageView>(R.id.main_header_pp)
        rm.load(drawableId).into(image)
    }

    /**
     * Sets the conversation's name
     * @param a : Activity
     * @paramm str : String
     */
    fun setConversationName(a: Activity, str: String = conversationName) {
        conversationName = str
        val elt = a.findViewById<TextView>(R.id.main_header_conversation_name)
        elt.text = str
    }


    /**
     * Sets the choicebox's visibility
     *
     * @param a         Activity
     * @param isVisible boolean
     */
    fun setChoiceBoxVisibility(a: Activity, isVisible: Boolean = choiceBoxIsVisible) {
        Std.debug("#1", isVisible)
        choiceBoxIsVisible = isVisible
        a.findViewById<View>(R.id.mainactivity_choicebox_area).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    fun setDescriptionVisibility(a: Activity, isVisible: Boolean = isDescriptionVisible) {
        a.findViewById<View>(R.id.main_description).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }



    /**
     * Sets the conversation's name
     * @param a : Activity
     * @paramm str : String
     */
    fun setConversationStatus(a: Activity, str: String = conversationStatus) {
        conversationStatus = str
        val elt = a.findViewById<TextView>(R.id.main_header_conversation_status)
        val onlinePoint = a.findViewById<View>(R.id.mainactivity_online_point)
        elt.text = str

        if(str == a.getString(R.string.mainactivity_online)) {
            onlinePoint.visibility = View.VISIBLE
        } else {
            onlinePoint.visibility = View.INVISIBLE
        }
    }

    /**
     * Sets the button's visibility
     * @param a : Activity
     * @param isVisible : Boolean
     */
    fun setButtonVisibility(a: Activity, isVisible: Boolean = mainButtonIsVisible) {
        mainButtonIsVisible = isVisible
        a.findViewById<View>(R.id.main_button).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Sets the button's visibility
     * @param a : Activity
     * @param isVisible : Boolean
     */
    fun setPauseButtonVisibility(a: Activity, isVisible: Boolean = mainButtonIsVisible) {
        a.findViewById<View>(R.id.mainactivity_button_pause).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Sets the button's image
     * @param a: Activity
     * @param state : PauseButtonSate
     */
    fun setPauseButtonImage(a: Activity, requestManager: RequestManager, state: MainModel.PauseButtonState) {
        val imageId = when (state) {
            MainModel.PauseButtonState.PAUSED -> OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_play")
            MainModel.PauseButtonState.PLAYING -> OnlineAssetsManager.getImageFilePath(a, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_pause")
        }
        requestManager.load(imageId).into(a.findViewById(R.id.mainactivity_button_pause))
    }

    /**
     * Gets the choice's parent's view
     * @param a : Activity
     * @return ViewGroup
     */
    fun getChoiceParentView(a: Activity): ViewGroup {
        return a.findViewById(R.id.mainactivity_choicebox_parent)
    }

    /**
     * Sets the lost image visible
     * @param a : Activity
     */
    fun setLostImageVisible(a: Activity, isVisible: Boolean = lostImageIsVisible) {
        lostImageIsVisible = isVisible
        a.findViewById<View>(R.id.main_lost).visibility = if (isVisible) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    /**
     * Sets an user offline or online
     * @param a : Activity
     */
    fun setUserStatus(a: Activity, online: Boolean) {
        val v = a.findViewById<TextView>(R.id.main_header_conversation_status)
        v.text = if (online) a.getString(R.string.mainactivity_online) else a.getString(R.string.mainactivity_offline)
        a.findViewById<View>(R.id.mainactivity_online_point).visibility = if (online) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Scrolls the RecyclerView to the given position
     * @param a : Activity
     * @param position : Int
     */
    fun scroll(a: Activity, position: Int) {
        if (position == -1) {
            return
        }
        a.findViewById<RecyclerView>(R.id.main_recyclerview).smoothScrollToPosition(position)
    }

    /**
     * Fills the notification
     * @param a : Activity
     * @param rm : RequestManager
     * @param name : String
     * @param status : String
     * @param pictureId : Int
     */
    fun fillNotification(a: Activity, rm: RequestManager, name: String, status: String, pictureId: String, iconId: String) {
        val title = a.findViewById<TextView>(R.id.main_header_conversation_name_not)
        val mstatus = a.findViewById<TextView>(R.id.main_header_conversation_status_not)
        title.text = name
        mstatus.text = status
        rm.load(pictureId).into(a.findViewById(R.id.main_header_pp_not))
        rm.load(iconId).into(a.findViewById(R.id.main_notification_icon))
        a.findViewById<View>(R.id.mainactivity_icons_not).visibility = View.GONE
    }

    /**
     * Updates the icons visibility
     * @param a : Activity
     * @param isVisible : Boolean
     */
    fun iconsVisibility(a: Activity, isVisible: Boolean = iconsAreVisible) {
        iconsAreVisible = isVisible
        a.findViewById<View>(R.id.mainactivity_icons).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Updates the torch's visibility
     * @param a : Activity
     * @param isVisible
     */
    fun setTorchVisibility(a: Activity, isVisible: Boolean) {
        a.findViewById<View>(R.id.mainactivity_button_torch).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Updates the gun's visibility
     * @param a : Activity
     * @param isVisible
     */
    fun setGunVisibility(a: Activity, isVisible: Boolean) {
        a.findViewById<View>(R.id.mainactivity_button_gun).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Updates the gun's visibility
     * @param activity : Activity
     * @param state : GunState
     */
    fun setGunImage(activity: Activity, state: MainModel.GunState, requrestManager: RequestManager) {
        val id = when (state) {
            MainModel.GunState.DISABLED -> OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_gun_off")
            MainModel.GunState.AVAILABLE -> OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_gun")
        }
        requrestManager.load(id).into(activity.findViewById<ImageView>(R.id.mainactivity_button_gun))
    }

    fun stopVideo(activity: Activity) {
        Video.pause(activity.findViewById(R.id.mainactivty_background_video))
    }


    /**
     * Sets the torch's image
     * @param state : TorchState
     */
    fun setTorchImage(activity: Activity, state: MainModel.TorchState, requestManager: RequestManager) {
        val id = when (state) {
            MainModel.TorchState.DISABLED -> OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_torch_disabled")
            MainModel.TorchState.ON -> OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_torch_on")
            MainModel.TorchState.OFF -> OnlineAssetsManager.getImageFilePath(activity, GlobalData.Game.FRIENDZONE2.id.toString(), "btn_torch_off")
        }
        setTorchImage(activity, requestManager, id)
    }

    /**
     * Sets the torch's image
     * @param state : TorchState
     */
    fun setTorchImage(activity: Activity, requestManager: RequestManager, imageId: String = torchDrawableId) {
        torchDrawableId = imageId
        requestManager.load(torchDrawableId).into(activity.findViewById(R.id.mainactivity_button_torch))
    }

    /***
     * Sets the faster button's image
     * @param activity : Activity
     * @param requestManager : RequestManager
     * @param imageId : String
     */
    fun setFasterImage(activity: Activity, requestManager: RequestManager, imageId: String) {
        requestManager.load(imageId).into(activity.findViewById(R.id.mainactivity_button_faster_build))
    }

    /**
     * Sets the faster's button's visibility
     * @param activity : Activity
     * @param isVisible
     */
    fun setFasterVisibility(activity: Activity, isVisible: Boolean) {
        activity.findViewById<View>(R.id.mainactivity_button_faster_build).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Makes a notification appear or disappear
     * @param a : Activity
     * @param appear : Boolean
     */
    fun notification(a: Activity, appear: Boolean): Int {
        val notification = a.findViewById<View>(R.id.mainactivity_header_notification)
        when (appear) {
            true -> {
                Animation.setAnimation(
                        notification,
                        Animation.Animations.ANIMATION_FADEIN,
                        a
                )
                return Animation.setAnimation(
                        notification,
                        Animation.Animations.ANIMATION_SLIDE_IN_FROM_RIGHT,
                        a
                ).toInt()
            }
            false -> {
                Animation.setAnimation(
                        notification,
                        Animation.Animations.ANIMATION_FADEOUT,
                        a
                )
                return Animation.setAnimation(
                        notification,
                        Animation.Animations.ANIMATION_SLIDE_OUT_TO_LEFT,
                        a
                ).toInt()
            }
        }
    }

    /**
     * Switch the background media to the given image name
     *
     * @param a     Activity
     * @param glide RequestManager
     * @param id    Int
     * @return Int
     */
    fun switchBackgroundImage(a: Activity, glide: RequestManager, id: String = backgroundImageId): String {
        if (id == "") {
            return ""
        }

        backgroundImageId = id

        val i = a.findViewById<ImageView>(R.id.mainactivty_background_image)
        glide.load(id).into(i)
        Animation.setAnimation(
                i,
                Animation.Animations.ANIMATION_FADEIN,
                a
        )

        val r = a.findViewById<View>(R.id.mainactivity_background)
        Animation.setAnimation(r, Animation.Animations.ANIMATION_FADEIN, a)
        return id
    }
}

