package com.purpletear.smsgame.activities.smsgame.items

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.sutokosharedelements.GraphicsPreference
import com.example.sharedelements.SmsGameTreeStructure
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.MessageColor
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import purpletear.fr.purpleteartools.TextViewHelper

class PhraseMe {
    companion object {
        val LAYOUT_ID = R.layout.sutoko_phrase_me

        enum class Views(val id: Int) {
            NAME(R.id.sutoko_phrase_me_name),
            MESSAGE(R.id.sutoko_phrase_me_message),
            TIME(R.id.sutoko_phrase_me_time),
            AVATAR(R.id.sutoko_phrase_me_avatar_image)
        }

        private enum class DesignTheme {
            SAME_TYPE_PREVIOUS_AND_NEXT,
            SAME_TYPE_PREVIOUS,
            FIRST,

        }

        fun design(
            itemView: View,
            context: Context,
            requestManager: RequestManager,
            storyId: String,
            messageColor: MessageColor,
            phrase: Phrase,
            character: StoryCharacter,
            seenCharacter: StoryCharacter?,
            previousItem: Phrase?,
            nextItem: Phrase?,
            firstName: String,
            storyType: StoryType,
            seenState: PhraseDest.Companion.SeenState?
        ) {
            itemView.findViewById<TextView>(Views.NAME.id).text = character.firstName.replace(
                "[prenom]",
                firstName
            )
            itemView.findViewById<TextView>(Views.TIME.id).text = phrase.time

            val textView = itemView.findViewById<TextView>(Views.MESSAGE.id)


            textView.text = phrase.sentence.replace(
                "[prenom]",
                firstName
            )
            TextViewHelper.replaceBreakWordByNewLines(textView, context, 220)


            // If it is the same as the previous item and the next item
            if (previousItem != null && previousItem.`is`(phrase.getType())
                && nextItem != null && nextItem.`is`(phrase.getType())
                && previousItem.id_author == phrase.id_author
                && nextItem.id_author == phrase.id_author
            ) {
                designBackground(itemView, DesignTheme.SAME_TYPE_PREVIOUS_AND_NEXT, messageColor)
                setInformationVisibility(itemView, context, false)
            } else

            // If it is the same as the previous one
                if (previousItem != null && previousItem.`is`(phrase.getType())
                    && previousItem.id_author == phrase.id_author
                ) {
                    designBackground(itemView, DesignTheme.SAME_TYPE_PREVIOUS, messageColor)
                    setInformationVisibility(itemView, context, false)
                } else {
                    designBackground(itemView, DesignTheme.FIRST, messageColor)
                    setInformationVisibility(itemView, context, true)
                    setAvatarIfNecessary(
                        itemView,
                        context,
                        requestManager,
                        phrase,
                        storyId,
                        storyType
                    )
                }

            setSeenLinearViewVisibility(
                context,
                requestManager,
                storyId,
                itemView,
                seenState,
                seenCharacter
            )
        }

        private fun setSeenLinearViewVisibility(
            context: Context,
            requestManager: RequestManager,
            storyId: String,
            itemView: View,
            seenState: PhraseDest.Companion.SeenState?,
            character: StoryCharacter?
        ) {
            val linearLayout = itemView.findViewById<View>(R.id.sutoko_item_phrase_me_seen)

            when (seenState) {
                null, PhraseDest.Companion.SeenState.NONE -> {
                    // Hide linearview
                    linearLayout.visibility = View.GONE
                }


                PhraseDest.Companion.SeenState.SENT -> {
                    // Set text, Set image
                    val text = context.getString(R.string.sutoko_sent)
                    val image = R.drawable.sutoko_ic_sent
                    val imageView =
                        linearLayout.findViewById<ImageView>(R.id.sutoko_item_phrase_me_seen_image)
                    requestManager.load(image)
                        .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE))
                        .transition(withCrossFade())
                        .into(imageView)
                    linearLayout.findViewById<TextView>(R.id.sutoko_item_phrase_me_seen_text).text =
                        text

                    // Show linearview
                    linearLayout.visibility = View.VISIBLE
                }

                PhraseDest.Companion.SeenState.SEEN -> {
                    val text = context.getString(R.string.sutoko_seen)
                    val imageView =
                        linearLayout.findViewById<ImageView>(R.id.sutoko_item_phrase_me_seen_image)
                    if (character != null) {
                        val image = SmsGameTreeStructure.getCharactersMinPictureFilePath(
                            context,
                            storyId,
                            character.id
                        )
                        requestManager.load(image)
                            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE))
                            .transition(withCrossFade())
                            .circleCrop()
                            .into(imageView)
                        imageView.visibility = View.VISIBLE
                    } else {
                        imageView.visibility = View.GONE
                    }
                    linearLayout.findViewById<TextView>(R.id.sutoko_item_phrase_me_seen_text).text =
                        text
                    // Set text, Set image
                    // Show linearview
                    linearLayout.visibility = View.VISIBLE
                }
            }
        }

        private fun setAvatarIfNecessary(
            itemView: View,
            context: Context,
            requestManager: RequestManager,
            phrase: Phrase,
            storyId: String,
            storyType: StoryType
        ) {

            val res = if (storyType == StoryType.OTHER_USER_STORY) {
                "https://create.sutoko.app/api/build/data/story/${storyId}/characters/${phrase.id_author}_min.jpeg"
            } else {
                SmsGameTreeStructure.getCharactersMinPictureFilePath(
                    context,
                    storyId,
                    phrase.id_author
                )
            }


            requestManager.load(res)
                .apply(
                    GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE)
                        .circleCrop()
                )

                .into(itemView.findViewById(Views.AVATAR.id))
        }

        fun animate(itemView: View) {
            val v = itemView.findViewById<TextView>(Companion.Views.MESSAGE.id)

            val scaleAnim = ScaleAnimation(
                0f, 1f,
                0f, 1f,
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 1f
            )

            scaleAnim.duration = 280
            scaleAnim.repeatCount = 0
            scaleAnim.interpolator = AccelerateInterpolator()
            scaleAnim.fillAfter = true
            scaleAnim.fillBefore = true
            scaleAnim.isFillEnabled = true
            v.startAnimation(scaleAnim)
        }

        // If it is the same as the previous one
        private fun designBackground(
            itemView: View,
            theme: DesignTheme,
            messageColor: MessageColor
        ) {
            val resource = when (theme) {
                DesignTheme.FIRST -> R.drawable.background_rounded_sms_game_phrase_me_first
                DesignTheme.SAME_TYPE_PREVIOUS -> R.drawable.background_rounded_sms_game_phrase_me_third
                DesignTheme.SAME_TYPE_PREVIOUS_AND_NEXT -> R.drawable.background_rounded_sms_game_phrase_me_second
            }

            val d = ContextCompat.getDrawable(itemView.context, resource) as GradientDrawable
            d.setColor(Color.parseColor(messageColor.background))

            itemView.findViewById<TextView>(Views.MESSAGE.id).background = d
        }

        private fun setInformationVisibility(itemView: View, context: Context, isVisible: Boolean) {
            val visibility = if (isVisible) View.VISIBLE else View.GONE
            itemView.findViewById<TextView>(Views.NAME.id).visibility = visibility
            itemView.findViewById<TextView>(Views.TIME.id).visibility = visibility
            itemView.findViewById<ImageView>(Views.AVATAR.id).visibility = visibility
            val textView = itemView.findViewById<TextView>(Views.MESSAGE.id)
            val lp = textView.layoutParams as ConstraintLayout.LayoutParams
            val horizontalPadding =
                context.resources.getDimension(R.dimen.sutoko_phrase_padding_horizontal).toInt()
            val verticalPadding =
                context.resources.getDimension(R.dimen.sutoko_phrase_padding_vertical).toInt()
            lp.setMargins(
                0,
                if (isVisible) context.resources.getDimension(R.dimen.sutoko_phrase_margin_top)
                    .toInt() else 0,
                0,
                0
            )
            textView.layoutParams = lp
            textView.setPadding(
                horizontalPadding,
                verticalPadding,
                horizontalPadding,
                verticalPadding
            )
        }
    }
}