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
import com.example.sharedelements.GraphicsPreference
import com.example.sharedelements.SmsGameTreeStructure
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.MessageColor
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import purpletear.fr.purpleteartools.TextViewHelper

class PhraseDest {
    companion object {
        val LAYOUT_ID = R.layout.sutoko_phrase_dest

        enum class SeenState {
            NONE,
            SENT,
            SEEN,

        }

        enum class Views(val id: Int) {
            NAME(R.id.sutoko_phrase_dest_name),
            MESSAGE(R.id.sutoko_phrase_dest_message),
            TIME(R.id.sutoko_phrase_dest_time),
            AVATAR(R.id.sutoko_phrase_dest_avatar_image)
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
            phrase: Phrase,
            character: StoryCharacter,
            previousItem: Phrase?,
            nextItem: Phrase?,
            currentMessageColor: MessageColor,
            firstName: String?,
            storyType: StoryType
        ) {

            itemView.findViewById<TextView>(Views.NAME.id).text =
                character.firstName.replace("[prenom]", firstName ?: "Nick")
            itemView.findViewById<TextView>(Views.TIME.id).text = phrase.time
            val textView = itemView.findViewById<TextView>(Views.MESSAGE.id)
            textView.text = phrase.sentence.replace("[prenom]", firstName ?: "Nick")

            TextViewHelper.replaceBreakWordByNewLines(textView, context, 220)
            // If it is the same as the previous item and the next item
            if (previousItem != null && previousItem.`is`(phrase.getType())
                && nextItem != null && nextItem.`is`(phrase.getType())
                && previousItem.id_author == phrase.id_author
                && nextItem.id_author == phrase.id_author
            ) {
                designBackground(
                    itemView,
                    DesignTheme.SAME_TYPE_PREVIOUS_AND_NEXT,
                    currentMessageColor
                )
                setInformationVisibility(itemView, context, false)
            }
            // If it is the same as the previous one
            else if (previousItem != null && previousItem.`is`(phrase.getType()) && previousItem.id_author == phrase.id_author) {
                designBackground(itemView, DesignTheme.SAME_TYPE_PREVIOUS, currentMessageColor)
                setInformationVisibility(itemView, context, false)
            } else {
                designBackground(itemView, DesignTheme.FIRST, currentMessageColor)
                setInformationVisibility(itemView, context, true)
                setAvatarIfNecessary(itemView, context, requestManager, phrase, storyId, storyType)
            }
        }

        fun animate(itemView: View) {
            val v = itemView.findViewById<TextView>(Views.MESSAGE.id)

            val scaleAnim = ScaleAnimation(
                0f, 1f,
                0f, 1f,
                Animation.ABSOLUTE, 0f,
                Animation.RELATIVE_TO_SELF, 1f
            )

            scaleAnim.duration = 460
            scaleAnim.repeatCount = 0
            scaleAnim.interpolator = AccelerateInterpolator()
            scaleAnim.fillAfter = true
            scaleAnim.fillBefore = true
            scaleAnim.isFillEnabled = true
            v.startAnimation(scaleAnim)
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

        private fun designBackground(
            itemView: View,
            theme: DesignTheme,
            messageColor: MessageColor
        ) {
            val resource = when (theme) {
                DesignTheme.FIRST -> R.drawable.background_rounded_sms_game_phrase_dest_first
                DesignTheme.SAME_TYPE_PREVIOUS -> R.drawable.background_rounded_sms_game_phrase_dest_third
                DesignTheme.SAME_TYPE_PREVIOUS_AND_NEXT -> R.drawable.background_rounded_sms_game_phrase_dest_second
            }
            val d = ContextCompat.getDrawable(itemView.context, resource) as GradientDrawable
            d.alpha = 160
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
            lp.topMargin =
                if (isVisible) context.resources.getDimension(R.dimen.sutoko_phrase_margin_top)
                    .toInt() else 0
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