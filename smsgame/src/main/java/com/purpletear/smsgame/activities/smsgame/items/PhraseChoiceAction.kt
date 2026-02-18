package com.purpletear.smsgame.activities.smsgame.items

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.SmsGameTreeStructure
import com.example.sutokosharedelements.GraphicsPreference
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.ChoiceAction
import purpletear.fr.purpleteartools.FingerV2

object PhraseChoiceAction {
    val LAYOUT_ID = R.layout.sutoko_item_phrase_choice_actions

    fun design(
        context: Context,
        storyId: Int,
        itemView: View,
        requestManager: RequestManager,
        actionChoices: ArrayList<ChoiceAction>,
        onPressed: (Int) -> Unit
    ) {
        if (actionChoices.size != 2) {
            return
        }
        val button1 = itemView.findViewById<View>(R.id.sutoko_item_phrase_choice_actions_button_1)
        val button2 = itemView.findViewById<View>(R.id.sutoko_item_phrase_choice_actions_button_2)

        setIcon(context, storyId, button1, actionChoices[0].iconName, requestManager)
        setText(button1, actionChoices[0].label)
        setListener(button1, actionChoices[0].phraseId, onPressed)

        setIcon(context, storyId, button2, actionChoices[1].iconName, requestManager)
        setText(button2, actionChoices[1].label)
        setListener(button2, actionChoices[1].phraseId, onPressed)
    }

    private fun setIcon(
        context: Context,
        storyId: Int,
        rootView: View,
        filename: String,
        requestManager: RequestManager
    ) {
        requestManager.load(SmsGameTreeStructure.getMediaFilePath(context, storyId, filename))
            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE))
            .into(rootView.findViewById(R.id.sutoko_inc_choice_icon_image))
    }

    private fun setText(rootView: View, text: String) {
        rootView.findViewById<TextView>(R.id.sutoko_inc_choice_icon_text).text = text
    }

    private fun setListener(itemView: View, phraseId: Int, onPressed: (Int) -> Unit) {
        FingerV2.register(itemView, null) {
            Handler(Looper.getMainLooper()).post {
                onPressed(phraseId)
            }
        }
    }
}