package com.purpletear.smsgame.activities.userStoryLoader

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.sutokosharedelements.GraphicsPreference
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.Story
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.DelayHandler

@Suppress("SameParameterValue")
internal class UserStoryLoaderGraphics {
    val delayHandler = DelayHandler()

    /**
     * Starts the animation
     * @param activity UserStoryLoaderActivity
     * @param onComplete Function0<Unit>
     */
    fun startAnimation(activity: UserStoryLoaderActivity, onComplete: () -> Unit) {
        val duration = (setContextVisibility(activity, true) * 4).toInt()
        delayHandler.operation("startAnimation:a", duration) {
            setTitleVisibility(activity, true)
            delayHandler.operation("startAnimation:b", duration) {
                setCreditsVisibility(activity, true)
                delayHandler.operation("startAnimation:c", 2000) {
                    Handler(Looper.getMainLooper()).post(onComplete)
                }
            }
        }
    }

    private fun setContextVisibility(activity: UserStoryLoaderActivity, isVisible: Boolean): Long {
        return setTextAnimation(activity, UserStoryReferences.Views.TEXT_CONTEXT, isVisible)
    }

    private fun setTitleVisibility(activity: UserStoryLoaderActivity, isVisible: Boolean): Long {
        return setTextAnimation(activity, UserStoryReferences.Views.TEXT_TITLE, isVisible)
    }

    private fun setCreditsVisibility(activity: UserStoryLoaderActivity, isVisible: Boolean): Long {
        return setTextAnimation(activity, UserStoryReferences.Views.CREDITS, isVisible)
    }

    /**
     * Set text animation
     * @param activity UserStoryLoaderActivity
     * @param view Views
     * @param isVisible Boolean
     */
    private fun setTextAnimation(
        activity: UserStoryLoaderActivity,
        view: UserStoryReferences.Views,
        isVisible: Boolean
    ): Long {
        val visibility = if (isVisible) {
            Animation.Animations.ANIMATION_SLIDE_IN_FROM_BOTTOM
        } else {
            Animation.Animations.ANIMATION_FADEOUT
        }

        return Animation.setAnimation(
            activity.findViewById(view.id),
            visibility,
            activity
        )
    }


    fun updateFilterVisibility(
        activity: UserStoryLoaderActivity,
        isVisible: Boolean,
        onComplete: () -> Unit
    ) {
        val visibility =
            if (isVisible) Animation.Animations.ANIMATION_FADEIN else Animation.Animations.ANIMATION_FADEOUT
        val duration = Animation.setAnimation(
            activity.findViewById(UserStoryReferences.Views.FILTER.id),
            visibility,
            activity
        ).toInt()
        delayHandler.operation("filterVisibility", duration, onComplete)
    }

    companion object {

        fun setTitle(activity: UserStoryLoaderActivity, title: String) {
            activity.findViewById<TextView>(UserStoryReferences.Views.TEXT_TITLE.id).text = title
        }

        fun setAuthorName(activity: UserStoryLoaderActivity, name: String) {
            val sentence = activity.getString(R.string.sutoko_created_by, name)
            activity.findViewById<TextView>(UserStoryReferences.Views.CREDITS_TEXT.id).text =
                sentence
        }

        fun setAuthorProfilPicture(
            activity: UserStoryLoaderActivity,
            requestManager: RequestManager,
            story: Story
        ) {
            val uid = story.userId.toIntOrNull()
            val image = if (story.hasProfilPicture) {
                if (uid != null) {
                    "https://create.sutoko.app/data/user/profile-picture/${uid}"
                } else {
                    story.creatorImageUrl64
                }
            } else {
                R.drawable.no_avatar
            }

            requestManager.load(image)
                .apply(
                    GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE)
                        .circleCrop()
                )
                .transition(withCrossFade())
                .into(activity.findViewById(UserStoryReferences.Views.CREDITS_IMAGE.id))
        }
    }
}