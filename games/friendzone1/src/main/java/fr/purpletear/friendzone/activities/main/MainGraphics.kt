/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone.activities.main

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import fr.purpletear.friendzone.config.CustomLinearLayoutManager
import fr.purpletear.friendzone.config.GameGridItemDecoration
import fr.purpletear.friendzone.R
import android.graphics.drawable.AnimationDrawable
import android.os.Parcel
import android.os.Parcelable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import purpletear.fr.purpleteartools.*

class MainGraphics : Parcelable {
    var choiceBoxIsVisible: Boolean
        private set
    var profilPictureId: Int
        private set
    private var conversationName: String
    private var conversationStatus: String
    private var backgroundImageId: Int
    private var mainButtonIsVisible: Boolean
    private var lostImageIsVisible: Boolean
    private var iconsAreVisible: Boolean
    private var isDescriptionVisible: Boolean

    var videoToReload: String = ""

    constructor(conversationName: String,
                conversationStatus: String,
                profilPictureId: Int,
                backgroundImageId: Int,
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
        profilPictureId = parcel.readInt()
        backgroundImageId = parcel.readInt()
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
        dest.writeInt(profilPictureId)
        dest.writeInt(backgroundImageId)
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
                    560
            )
        }


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
     * @param type : MainModel.GameType
     */
    fun setInitialImages(a: Activity, rm: RequestManager, type: MainModel.GameType) {
        val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
        when (type) {
            MainModel.GameType.SMS -> {
                rm.load(R.drawable.option_circles).apply(requestOptions).into(a.findViewById(R.id.main_header_icons_info))
                rm.load(R.drawable.background_sms).apply(requestOptions).into(a.findViewById(R.id.mainactivty_background_image))
                rm.load(R.drawable.friendzone).apply(requestOptions).into(a.findViewById(R.id.main_lost))
            }

            MainModel.GameType.NORMAL -> {
                rm.load(R.drawable.ico_share).apply(requestOptions).into(a.findViewById(R.id.main_header_icons_share))
                rm.load(R.drawable.ico_call).apply(requestOptions).into(a.findViewById(R.id.main_header_icons_call))
                rm.load(R.drawable.ico_info).apply(requestOptions).into(a.findViewById(R.id.main_header_icons_info))
                rm.load(R.drawable.ico_add_friend).apply(requestOptions).into(a.findViewById(R.id.main_notification_icon))
                rm.load(R.drawable.lost).apply(requestOptions).into(a.findViewById(R.id.main_lost))
            }
        }
    }

    /**
     * Sets the recyclerView
     * @param a : Activity
     */
    fun setRecyclerView(a: Activity, adapter: GameConversationAdapter, display: Display) {
        val recyclerview = a.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.main_recyclerview)
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
    fun setProfilPicture(a: Activity, rm: RequestManager, drawableId: Int = profilPictureId) {
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

        if(str === a.getString(R.string.mainactivity_online)) {
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
     * Starts the glitch animation
     * @param a : Activity
     * @param delay : Int
     * @param mh : MemoryHandler
     */
    fun glitchAnimation(a: Activity, delay: Int, mh: MemoryHandler) {
        a.findViewById<View>(R.id.mainactivity_background).visibility = View.VISIBLE
        a.findViewById<View>(R.id.mainactivty_background_video).visibility = View.INVISIBLE
        val i = a.findViewById<ImageView>(R.id.mainactivty_background_image)

        //i.visibility = View.VISIBLE
        //i.setImageResource(R.color.transparent)
        //i.setBackgroundResource(R.drawable.animation_glitch)
        //val animation = i.background as AnimationDrawable

        val runnable = object : Runnable2("Demande de notification overlay + ", delay) {
            override fun run() {
                //  animation.start()
            }
        }
        mh.push(runnable)
        mh.run(runnable)
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
        a.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.main_recyclerview).smoothScrollToPosition(position)
    }

    /**
     * Fills the notification
     * @param a : Activity
     * @param rm : RequestManager
     * @param name : String
     * @param status : String
     * @param pictureId : Int
     */
    fun fillNotification(a: Activity, rm: RequestManager, name: String, status: String, pictureId: Int) {
        val title = a.findViewById<TextView>(R.id.main_header_conversation_name_not)
        val mstatus = a.findViewById<TextView>(R.id.main_header_conversation_status_not)
        title.text = name
        mstatus.text = status
        rm.load(pictureId).into(a.findViewById(R.id.main_header_pp_not))
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
    fun switchBackgroundImage(a: Activity, glide: RequestManager, id: Int = backgroundImageId): Int {
        if (id == 0) {
            return R.color.transparent
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

