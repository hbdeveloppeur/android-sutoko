package com.purpletear.smsgame.activities.smsgame

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.RequestManager
import com.example.sharedelements.SutokoSharedElementsData
import com.example.sharedelements.GraphicsPreference
import com.purpletear.smsgame.R
import com.purpletear.smsgame.databinding.IncUserStoryInfoHeaderBinding
import com.purpletear.smsgame.activities.smsgame.objects.Story
import com.purpletear.smsgame.activities.smsgame.objects.StoryHelper
import purpletear.fr.purpleteartools.FingerV2
import purpletear.fr.purpleteartools.TableOfSymbols

object UserStoryInfoHeaderHelper {
    val layoutId: Int = R.layout.inc_user_story_info_header

    /**
     * Inserts the View
     */
    fun insert(
        activity: Activity?,
        symbols: TableOfSymbols,
        parent: ViewGroup,
        requestManager: RequestManager,
        story: Story,
        onLikePressed: () -> Unit,
        onAuthorPressed: () -> Unit,
        onFlagPresed: () -> Unit
    ) {
        if (activity == null) {
            return
        }
        val view = activity.layoutInflater.inflate(layoutId, parent, false)

        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            @Suppress("SENSELESS_COMPARISON")
            override fun onViewAttachedToWindow(v: View) {
                val ready = activity != null && !activity.isFinishing && v != null
                if (ready) {
                    val binding = IncUserStoryInfoHeaderBinding.bind(view)
                    set(requestManager, story, binding)
                    setFlagButtonVisibility(
                        binding,
                        !symbols.hasReportedUserStory(story.firebaseId)
                    )
                    setListener(binding, onLikePressed, onAuthorPressed, onFlagPresed)
                    setLiked(
                        binding.sutokoIncUserStoryInfoHeaderLikeImage,
                        StoryHelper.userLiked(story.id)
                    )

                    view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topToTop = parent.id
                        topMargin = SutokoSharedElementsData.getStatusBarHeight(activity)
                    }
                }
            }

            override fun onViewDetachedFromWindow(v: View) {}
        })
        parent.addView(view)

    }


    fun setFlagButtonVisibility(binding: IncUserStoryInfoHeaderBinding, isVisible: Boolean) {
        binding.sutokoIncUserStoryInfoHeaderReportButton.visibility =
            if (isVisible) View.VISIBLE else View.INVISIBLE
    }


    fun setFlagButtonVisibility(activity: Activity, isVisible: Boolean) {
        activity.findViewById<View>(R.id.sutoko_inc_user_story_info_header_report_button).visibility =
            if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Sets liked image
     * @param animation LottieAnimationView
     * @param isLiked Boolean
     */
    fun setLiked(animation: LottieAnimationView, isLiked: Boolean) {
        animation.pauseAnimation()
        if (isLiked) {
            animation.frame = 75
        } else {
            animation.frame = 0
        }
    }

    fun animateLike(animation: LottieAnimationView) {
        animation.pauseAnimation()
        animation.setMinAndMaxFrame(41, 75)
        animation.playAnimation()
    }

    /**
     * Designs the View
     */
    private fun set(
        requestManager: RequestManager,
        story: Story,
        binding: IncUserStoryInfoHeaderBinding
    ) {
        binding.sutokoIncUserStoryInfoHeaderAuthorNickname.text = story.authorCachedName
        binding.sutokoIncUserStoryInfoHeaderTitle.text = story.title

        val uid = story.userId.toIntOrNull()

        val imageUrl = if (uid != null && story.hasProfilPicture == true) {
            "https://create.sutoko.app/data/user/profile-picture/${uid}"
        } else {
            R.drawable.ic_user_avatar_2
        }

        requestManager.load(imageUrl)
            .apply(
                GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE)
                    .circleCrop()
            )
            .into(binding.sutokoIncUserStoryInfoHeaderAuthorAvatar)

        requestManager.load(R.drawable.sutoko_ic_flag)
            .apply(
                GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE)
                    .circleCrop()
            )
            .into(binding.sutokoIncUserStoryInfoHeaderReportImage)

    }

    private fun setListener(
        binding: IncUserStoryInfoHeaderBinding,
        onLike: () -> Unit,
        onAuthorPressed: () -> Unit,
        onFlagPressed: () -> Unit
    ) {
        FingerV2.register(binding.sutokoIncUserStoryInfoHeaderLikeButton, null, onLike)
        FingerV2.register(binding.sutokoIncUserStoryInfoHeaderAuthor, null, onAuthorPressed)
        FingerV2.register(binding.sutokoIncUserStoryInfoHeaderReportButton, null, onFlagPressed)
    }
}