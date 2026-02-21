package com.purpletear.smsgame.activities.smsgame

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.sutokosharedelements.GraphicsPreference
import com.example.sharedelements.SutokoSharedElementsData
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.adapter.GameConversationAdapter
import com.purpletear.smsgame.activities.smsgame.objects.GameGridItemDecoration
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.sutoko.game.model.Game
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.Measure


class SmsGameGraphics {
    companion object {

        enum class Views(val id: Int) {
            RECYCLERVIEW(R.id.sutoko_smsgame_recyclerview_conversation),
            FADE_IN(R.id.sutoko_smsgame_filter),
            HEADER_IMAGE_PARENT(R.id.sutoko_smsgame_header_image_container),
            HEADER_IMAGE_STROKE(R.id.sutoko_smsgame_header_image_stroke),
            HEADER_IMAGE(R.id.sutoko_smsgame_header_image),
        }


        fun setDarkModeTheme(
            activity: SmsGameActivity,
            requestManager: RequestManager,
            isDarkMode: Boolean
        ) {
            requestManager.load(if (isDarkMode) R.drawable.ic_darkmode_white else R.drawable.ic_darkmode)
                .into(activity.findViewById(R.id.sutoko_smsgame_choicebox_button_darkmode_icon))
            val background =
                if (isDarkMode) R.drawable.sutoko_smsgame_choicebox_background_darkmode else R.drawable.sutoko_smsgame_choicebox_background
            activity.findViewById<View>(R.id.sutoko_smsgame_choicebox).background =
                ContextCompat.getDrawable(activity, background)
            val color = ContextCompat.getColor(
                activity,
                if (isDarkMode) R.color.darkModeWhite2 else R.color.smsGameChoiceBoxTitle
            )
            activity.findViewById<TextView>(R.id.sutoko_smsgame_choicebox_title).setTextColor(color)
            val textColor = ContextCompat.getColor(
                activity,
                if (isDarkMode) R.color.darkModeWhite2 else R.color.dark3
            )
            getChoiceBoxContentParent(activity).children.forEach { c ->
                when (c) {
                    is TextView -> {
                        c.setTextColor(textColor)
                    }

                    is FrameLayout -> {
                        // c.findViewById<TextView>(R.id.realText).setTextColor(textColor)
                        c.findViewById<TextView>(R.id.text).setTextColor(textColor)
                    }
                }
            }
            activity.findViewById<View>(R.id.sutoko_smsgame_choicebox_line_separator)
                .setBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        if (isDarkMode) R.color.smsGameChoiceBoxLineSeparatorDarkMode else R.color.smsGameChoiceBoxLineSeparator
                    )
                )
        }

        fun unlockItemIsVisible(activity: SmsGameActivity): Boolean {
            return activity.findViewById<View>(R.id.coins_diamonds_validation).visibility == View.VISIBLE
        }

        fun setUnlockItemIsVisible(activity: SmsGameActivity, isVisible: Boolean) {
            activity.findViewById<View>(R.id.coins_diamonds_validation).visibility =
                if (isVisible) View.VISIBLE else View.INVISIBLE
        }
        /*
                fun setUnlockItemImageSize(activity: SmsGameActivity) {
                    activity.findViewById<View>(R.id.coins_diamonds_validation)
                        .findViewById<FrameLayout>(R.id.image)
                        .updateLayoutParams<LinearLayout.LayoutParams> {
                            height = Std.dpToPx(100f, activity.resources)
                        }
                }*/

        fun setLoadingScreenVisibility(activity: SmsGameActivity, isVisible: Boolean) {
            val visibility = if (isVisible) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            activity.findViewById<View>(R.id.sutoko_smsgame_filter).visibility = visibility
            activity.findViewById<View>(R.id.sutoko_smsgame_progressbar).visibility = visibility
        }

        /**
         * Fades the filter given the visibility and the the duration
         *
         * @param activity
         * @param isVisible
         * @param duration
         */
        fun fadeFilter(activity: SmsGameActivity, isVisible: Boolean, duration: Int) {

            Animation.setAnimation(
                activity.findViewById<View>(Views.FADE_IN.id),
                if (isVisible) Animation.Animations.ANIMATION_FADEIN else Animation.Animations.ANIMATION_FADEOUT,
                activity,
                duration
            )
        }

        /**
         * Scrolls to the given position
         *
         * @param activity
         * @param position
         */
        fun scrollToPosition(activity: SmsGameActivity, position: Int) {
            activity.findViewById<RecyclerView>(Views.RECYCLERVIEW.id).scrollToPosition(position)
        }

        fun setChoiceBoxVisibility(activity: SmsGameActivity, isVisible: Boolean) {
            val v = activity.findViewById<View>(R.id.sutoko_mainactivity_choicebox_area)
            if (!isVisible) {
                Animation.setAnimation(
                    v,
                    Animation.Animations.ANIMATION_FADEOUT,
                    activity,
                    180
                )
            } else {
                Animation.setAnimation(
                    v,
                    Animation.Animations.ANIMATION_FADEIN,
                    activity,
                    280
                )
            }
        }

        fun getChoiceBoxContentParent(activity: SmsGameActivity): ViewGroup {
            return activity.findViewById(R.id.sutoko_mainactivity_choicebox_parent)
        }


        fun setFakeStatusBarSize(activity: SmsGameActivity) {
            val statusBar = SutokoSharedElementsData.getStatusBarHeight(activity)
            activity.binding.sutokoSmsgameRecyclerviewConversation.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = ConstraintSet.PARENT_ID
                topMargin = statusBar
            }
        }

        /**
         * Sets the RecyclerView
         *
         * @param activity
         * @param adapter
         */
        fun setRecyclerView(activity: SmsGameActivity, adapter: GameConversationAdapter) {
            val recyclerView =
                activity.findViewById<RecyclerView>(R.id.sutoko_smsgame_recyclerview_conversation)
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter
            recyclerView.setItemViewCacheSize(20)

            val lLayout = LinearLayoutManager(activity)
            lLayout.stackFromEnd = true
            recyclerView.addItemDecoration(
                GameGridItemDecoration(
                    Math.round(
                        Measure.percent(
                            Measure.Type.HEIGHT,
                            1f,
                            activity.windowManager.defaultDisplay
                        )
                    ).toInt(), false
                )
            )
            recyclerView.layoutManager = lLayout
        }


        /**
         * Loads an Image
         * @param imageRef Any
         * @param imageView ImageView
         * @param withRequestManager RequestManager
         * @param onLoaded Function0<Unit>
         * @param onFailure Function0<Unit>
         */
        fun setBackgroundImage(
            activity: SmsGameActivity,
            imageRef: Any,
            withRequestManager: RequestManager,
            onLoaded: () -> Unit,
            onFailure: () -> Unit
        ) {

            withRequestManager.load(imageRef)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        onFailure()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        onLoaded()
                        return false
                    }
                })
                .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE))
                .into(activity.findViewById(R.id.smsgame_image))
        }

        fun setBackgroundVideoVisibility(
            activity: SmsGameActivity,
            isVisible: Boolean,
            viewType: SmsGameModel.ViewType
        ) {
            when (viewType) {
                SmsGameModel.ViewType.PLAYERVIEW -> {
                    activity.findViewById<View>(R.id.smsgame_videoview).alpha =
                        if (isVisible) 1f else 0f
                }

                SmsGameModel.ViewType.VIDEOVIEW -> {
                    activity.findViewById<View>(R.id.smsgame_videoview_old).alpha =
                        if (isVisible) 1f else 0f
                }

                else -> {}
            }
        }

        fun setBackgroundImageVisibility(activity: SmsGameActivity, isVisible: Boolean) {
            activity.findViewById<View>(R.id.smsgame_image).visibility =
                if (isVisible) View.VISIBLE else View.INVISIBLE
        }

        fun fadeCurtains(activity: SmsGameActivity, isVisible: Boolean): Long {
            val view = activity.findViewById<View>(R.id.smsgame_media_curtains)
            val mode =
                if (isVisible) Animation.Animations.ANIMATION_FADEIN else Animation.Animations.ANIMATION_FADEOUT
            return Animation.setAnimation(
                view, mode,
                activity, 280
            )
        }

        fun curtainsAreOpen(activity: SmsGameActivity): Boolean {
            return activity.findViewById<View>(R.id.smsgame_media_curtains).visibility != View.VISIBLE
        }

        /**
         * Sets the header's picture
         *
         * @param activity
         * @param requestManager
         * @param path
         */
        fun setHeaderPicture(
            activity: SmsGameActivity,
            requestManager: RequestManager,
            card: Game,
            storyType: StoryType
        ) {
            if (StoryType.CURRENT_USER_STORY == storyType) {
                activity.findViewById<View>(Views.HEADER_IMAGE_PARENT.id).visibility =
                    View.INVISIBLE
                activity.findViewById<View>(Views.HEADER_IMAGE.id).visibility = View.INVISIBLE
                return
            }
            // TODO
//            requestManager.load(Data.FIREBASE_STORAGE_URL_PREFIX + card.squareImagePrefix)
//                .transition(withCrossFade())
//                .into(activity.findViewById(Views.HEADER_IMAGE.id))
        }

        fun setHeaderPictureVisibility(activity: SmsGameActivity, isVisible: Boolean) {
            activity.findViewById<View>(Views.HEADER_IMAGE_PARENT.id).visibility =
                if (isVisible) View.GONE else View.GONE
            activity.findViewById<View>(Views.HEADER_IMAGE_STROKE.id).visibility =
                if (isVisible) View.GONE else View.GONE
        }
    }
}