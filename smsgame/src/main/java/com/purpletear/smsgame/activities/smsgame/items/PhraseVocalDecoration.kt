package com.purpletear.smsgame.activities.smsgame.items

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.RequestManager
import com.example.sharedelements.SmsGameTreeStructure
import com.example.sutokosharedelements.GraphicsPreference
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import purpletear.fr.purpleteartools.DelayHandler

object PhraseVocalDecoration {
    val layoutId: Int = R.layout.sutoko_phrase_vocal_me
    val layoutDestId: Int = R.layout.sutoko_phrase_vocal
    val delayHandler = DelayHandler()
    var maxBarWidth: Int = -1

    fun design(
        activity: Activity,
        itemView: View,
        requestManager: RequestManager,
        storyId: Int,
        phrase: Phrase,
        isPlaying: Boolean,
        previousItem: Phrase?,
        character: StoryCharacter
    ) {
        delayHandler.stop()

        if (isPlaying) {
            setProgress(activity, itemView, phrase)
        } else {
            delayHandler.stop()
            setWidth(itemView.findViewById(R.id.sutoko_phrase_vocal_me_progress), 0)
        }

        requestManager
            .load(if (!isPlaying) R.drawable.sutoko_ic_vocal_play else R.drawable.ic_sutoko_stop)
            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE))
            .into(itemView.findViewById(R.id.sutoko_phrase_vocal_me_button_image))

        requestManager
            .load(R.drawable.ic_sound_wave)
            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE))
            .into(itemView.findViewById(R.id.sutoko_phrase_vocal_me_image_soundwaves))

        val mustDisplayInfo = previousItem == null || previousItem.id_author != phrase.id_author

        setInformationVisibility(itemView, activity, mustDisplayInfo)

        if (mustDisplayInfo) {
            requestManager.load(
                SmsGameTreeStructure.getCharactersMinPictureFilePath(
                    activity,
                    storyId,
                    phrase.id_author
                )
            )
                .apply(
                    GraphicsPreference.getRequestOptions(GraphicsPreference.Level.CACHE)
                        .circleCrop()
                )
                .into(itemView.findViewById(R.id.sutoko_phrase_vocal_me_avatar_image))

            itemView.findViewById<TextView>(R.id.sutoko_phrase_vocal_me_name).text =
                character.firstName
        }
    }


    private fun setInformationVisibility(itemView: View, context: Context, isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        itemView.findViewById<TextView>(R.id.sutoko_phrase_vocal_me_name).visibility = visibility
        itemView.findViewById<ImageView>(R.id.sutoko_phrase_vocal_me_avatar_image).visibility =
            visibility
        itemView.findViewById<TextView>(R.id.sutoko_phrase_vocal_me_time).visibility = visibility

        val bg = itemView.findViewById<CardView>(R.id.sutoko_phrase_vocal_me_bg)
        val lp = bg.layoutParams as ConstraintLayout.LayoutParams
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
        bg.layoutParams = lp
        bg.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }

    private fun setProgress(activity: Activity, itemView: View, phrase: Phrase) {
        if (maxBarWidth == -1) {
            val v = itemView.findViewById<View>(R.id.sutoko_phrase_vocal_me_bg)
            v.post {
                maxBarWidth = v.width
                setProgress(activity, itemView, phrase)
            }
            return
        }

        animate(activity, itemView, 0, phrase)
    }

    private fun animate(activity: Activity, itemView: View, ms: Int, phrase: Phrase) {
        if (ms > phrase.seen + 100) {
            delayHandler.stop()
            return
        }

        delayHandler.operation("progress", 16) foo@{
            val progress = itemView.findViewById<View>(R.id.sutoko_phrase_vocal_me_progress)
            if (progress == null) {
                delayHandler.stop()
                return@foo
            }
            var percent = if (ms == 0 || phrase.seen == 0) {
                0
            } else {
                ms * 100 / phrase.seen
            }

            percent = if (percent >= 100) {
                100
            } else {
                percent
            }

            val px = if (maxBarWidth == 0 || percent == 0) {
                0
            } else {
                percent * maxBarWidth / 100
            }


            setWidth(progress, px)
            animate(activity, itemView, ms + 16, phrase)

        }
    }

    private fun setWidth(progress: View, px: Int) {
        val lp = progress.layoutParams
        lp.width = px
        progress.layoutParams = lp
    }

}