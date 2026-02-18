/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */
package fr.purpletear.sutoko.tools

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import fr.purpletear.sutoko.R
import purpletear.fr.purpleteartools.DelayHandler
import java.lang.ref.WeakReference

object Animation {

    private val delayHandler: DelayHandler = DelayHandler()

    enum class Animations {
        ANIMATION_FADEIN,
        ANIMATION_FADEOUT,

        // Slides in from left
        ANIMATION_SLIDE_IN_FROM_LEFT,

        // Zooms in
        ANIMATION_ZOOM_IN,

        // Slides in from bottom
        ANIMATION_SLIDE_IN_FROM_BOTTOM,

        // Slides out to right
        ANIMATION_SLIDE_OUT_TO_RIGHT,

        // Slides in from right
        ANIMATION_SLIDE_IN_FROM_RIGHT,
        ANIMATION_SLIDE_IN_FROM_TOP,
        ANIMATION_SLIDE_OUT_TO_LEFT,
        ANIMATION_SLIDE_OUT_AND_STAY
    }

    @JvmOverloads
    fun setAnimation(
        view: View,
        mode: Animations,
        activity: Activity,
        duration: Int = 0,
        keep: Boolean = true
    ): Long {
        val viewToAnimate = WeakReference<View>(view).get() ?: return 0
        return setAnimation(viewToAnimate.toString(), viewToAnimate, mode, activity, duration, keep)
    }

    fun stop() {
        delayHandler.stop()
    }

    private fun stop(name: String) {
        delayHandler.stopEvery(name)
    }

    /**
     * Permits to create an animation with a specific duration
     *
     */
    @JvmOverloads
    fun setAnimation(
        name: String,
        view: View,
        mode: Animations,
        activity: Activity,
        duration: Int = 0,
        keep: Boolean = true
    ): Long {
        val animation: android.view.animation.Animation
        val viewToAnimate = WeakReference<View>(view).get() ?: return 0
        when (mode) {
            Animations.ANIMATION_FADEIN -> {
                stop(viewToAnimate.toString())
                animation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in)
                animation.interpolator = DecelerateInterpolator()
                if (keep) {
                    delayHandler.operation(
                        name,
                        if (duration == 0) animation.duration.toInt() else duration
                    ) {
                        viewToAnimate.visibility = View.VISIBLE
                    }
                }
            }
            Animations.ANIMATION_FADEOUT -> {
                stop(viewToAnimate.toString())
                animation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out)
                animation.interpolator = android.view.animation.AccelerateInterpolator()
                if (keep) {
                    delayHandler.operation(
                        name,
                        if (duration == 0) animation.duration.toInt() else duration
                    ) {
                        viewToAnimate.visibility = View.INVISIBLE
                    }
                }
            }

            Animations.ANIMATION_SLIDE_IN_FROM_RIGHT -> {
                stop(viewToAnimate.toString())
                animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_right)
                if (keep) {
                    delayHandler.operation(
                        name,
                        if (duration == 0) animation.duration.toInt() else duration
                    ) {
                        viewToAnimate.visibility = View.VISIBLE
                    }
                }
            }
            Animations.ANIMATION_SLIDE_IN_FROM_BOTTOM -> {
                stop(viewToAnimate.toString())
                animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_bottom)
                animation.setInterpolator(activity, R.anim.decelerate_interpolator)
                if (keep) {
                    delayHandler.operation(
                        name,
                        if (duration == 0) animation.duration.toInt() else duration
                    ) {
                        viewToAnimate.visibility = View.VISIBLE
                    }
                }
            }
            Animations.ANIMATION_SLIDE_IN_FROM_TOP -> {
                stop(viewToAnimate.toString())
                animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_top)
                animation.interpolator = DecelerateInterpolator()
                if (keep) {
                    delayHandler.operation(
                        name,
                        if (duration == 0) animation.duration.toInt() else duration
                    ) {
                        viewToAnimate.visibility = View.VISIBLE
                    }
                }
            }
            Animations.ANIMATION_SLIDE_IN_FROM_LEFT -> {
                stop(viewToAnimate.toString())
                animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_left)
                if (keep) {
                    delayHandler.operation(
                        name,
                        if (duration == 0) animation.duration.toInt() else duration
                    ) {
                        viewToAnimate.visibility = View.VISIBLE
                    }
                }
            }
            Animations.ANIMATION_SLIDE_OUT_TO_RIGHT -> {
                stop(viewToAnimate.toString())
                animation =
                    AnimationUtils.loadAnimation(activity, android.R.anim.slide_out_right)
                if (keep) {
                    delayHandler.operation(
                        name,
                        if (duration == 0) animation.duration.toInt() else duration
                    ) {
                        viewToAnimate.visibility = View.INVISIBLE
                    }
                }
            }
            Animations.ANIMATION_SLIDE_OUT_TO_LEFT -> animation =
                AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_left)
            Animations.ANIMATION_SLIDE_OUT_AND_STAY -> animation =
                AnimationUtils.loadAnimation(activity, R.anim.slide_out_right_and_stay)
            Animations.ANIMATION_ZOOM_IN -> {
                viewToAnimate.visibility = View.VISIBLE
                animation = AnimationUtils.loadAnimation(activity, R.anim.zoom_in_new)
            }
        }
        if (duration > 0) {
            animation.duration = duration.toLong()
        }
        Handler(Looper.getMainLooper()).post {
            viewToAnimate.startAnimation(animation)
        }


        return animation.duration
    }

    /**
     * Animates a IMAGE into an ImageView
     * @param activity Activity
     * @param imageViewId int
     * @param animationId int
     */
    fun animate(activity: Activity, imageViewId: Int, animationId: Int) {
        val i = activity.findViewById<ImageView>(imageViewId)
        i.setBackgroundResource(animationId)
        val a = i.background as AnimationDrawable
        a.callback = i
        a.setVisible(true, true)
        a.start()
    }

    /**
     * Animates a IMAGE into an ImageView
     * @param root View
     * @param imageViewId int
     * @param animationId int
     */
    fun animate(root: View, imageViewId: Int, animationId: Int) {
        val i = root.findViewById<ImageView>(imageViewId)
        i.setBackgroundResource(animationId)
        val a = i.background as AnimationDrawable
        a.callback = i
        a.setVisible(true, true)
        a.start()
    }
}