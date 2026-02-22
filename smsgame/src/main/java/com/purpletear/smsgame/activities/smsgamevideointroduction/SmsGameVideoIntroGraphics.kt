package com.purpletear.smsgame.activities.smsgamevideointroduction

import android.view.View
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.purpletear.smsgame.databinding.ActivitySmsGameVideoIntroBinding
import purpletear.fr.purpleteartools.Animation


object SmsGameVideoIntroGraphics {

    fun isVideoVisible(binding: ActivitySmsGameVideoIntroBinding): Boolean {
        return binding.sutokoSmsgameintroVideoFilter.visibility == View.VISIBLE
    }

    fun isBackgroundImageVisible(binding: ActivitySmsGameVideoIntroBinding): Boolean {
        return binding.sutokoSmsgameintroIntroImage.visibility == View.VISIBLE
    }

    fun isTextVisible(binding: ActivitySmsGameVideoIntroBinding): Boolean {
        return binding.sutokoSmsgameintroText.visibility == View.VISIBLE
    }

    fun setVideo(
        activity: SmsGameVideoIntro,
        path: String,
        isLooping: Boolean,
        onLoaded: () -> Unit
    ) {
        activity.binding.sutokoSmsgameintroVideoIntro.setVideo(activity, path, isLooping)
        activity.binding.sutokoSmsgameintroVideoIntro.post {
            if (activity.binding.sutokoSmsgameintroVideoIntro.shouldResize("920:1592")) {
                activity.binding.sutokoSmsgameintroVideoIntro.updateSize(
                    "920:1592",
                    SutokoSharedElementsData.screenSize
                )
                activity.binding.sutokoSmsgameintroVideoIntro.playVideo()
            }
        }
        onLoaded()
    }

    fun setVisibilityVideo(activity: SmsGameVideoIntro, isVisible: Boolean, duration: Int) {
        Animation.setAnimation(
            activity.binding.sutokoSmsgameintroVideoIntro,
            if (isVisible) Animation.Animations.ANIMATION_FADEIN else Animation.Animations.ANIMATION_FADEOUT,
            activity,
            duration
        )
    }

    fun setVisibilityFilterVideo(activity: SmsGameVideoIntro, isVisible: Boolean, duration: Int) {
        Animation.setAnimation(
            activity.binding.sutokoSmsgameintroVideoFilter,
            if (isVisible) Animation.Animations.ANIMATION_FADEOUT else Animation.Animations.ANIMATION_FADEIN,
            activity,
            duration
        )
    }

    fun setImage(activity: SmsGameVideoIntro, requestManager: RequestManager, path: String) {
        requestManager.load(path)
            .transition(withCrossFade())
            .apply(RequestOptions().centerCrop())
            .into(activity.binding.sutokoSmsgameintroIntroImage)
    }

    fun setVisibilityBackgroundImage(
        activity: SmsGameVideoIntro,
        isVisible: Boolean,
        duration: Int
    ) {
        Animation.setAnimation(
            activity.binding.sutokoSmsgameintroIntroImage,
            if (isVisible) Animation.Animations.ANIMATION_FADEIN else Animation.Animations.ANIMATION_FADEOUT,
            activity,
            duration
        )
    }

    fun fadeOut(activity: SmsGameVideoIntro) {
        activity.runOnUiThread {
            Animation.setAnimation(
                activity.binding.sutokoSmsgameintroFilter,
                Animation.Animations.ANIMATION_FADEIN,
                activity,
                1280
            )
        }
    }

    fun setVisibilityText(activity: SmsGameVideoIntro, isVisible: Boolean, duration: Int) {
        Animation.setAnimation(
            activity.binding.sutokoSmsgameintroText,
            if (isVisible) Animation.Animations.ANIMATION_FADEIN else Animation.Animations.ANIMATION_FADEOUT,
            activity,
            duration
        )
    }

    fun setText(binding: ActivitySmsGameVideoIntroBinding, text: String) {
        binding.sutokoSmsgameintroText.text = text
    }
}