package com.purpletear.smsgame.activities.smsgame.items

import android.content.Context
import android.view.View
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.sharedelements.SmsGameTreeStructure
import com.example.sharedelements.GraphicsPreference
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.StoryEvent
import java.io.File

object PhraseEventDecoration {
    val LAYOUT_ID: Int = R.layout.sutoko_phrase_event

    fun design(
        context: Context,
        storyId: String,
        requestManager: RequestManager,
        itemView: View,
        phrase: Phrase
    ) {
        val event = parse(phrase) ?: StoryEvent()

        itemView.findViewById<TextView>(R.id.sutoko_item_phrase_event_title).text =
            event.title.replace("\\n", System.lineSeparator())
        itemView.findViewById<TextView>(R.id.sutoko_item_phrase_event_subtitle).text =
            event.subtitle

        // Icon
        val iconPath = SmsGameTreeStructure.getMediaFilePath(context, storyId, event.icon)
        if (File(iconPath).exists()) {
            requestManager
                .load(iconPath)
                .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE))
                .transition(withCrossFade())
                .into(itemView.findViewById(R.id.sutoko_item_phrase_event_icon))
        }
    }

    /**
     *
     * @param phrase Phrase
     * @return StoryEvent?
     */
    private fun parse(phrase: Phrase): StoryEvent? {
        val event = StoryEvent()
        val values = phrase.sentence.split("\n")
        if (values.size < 2) {
            return null
        }
        val map = mutableMapOf<String, String>()
        values.forEach { text ->
            val v = text.split(":")
            if (v.size == 2) {
                map[v[0]] = v[1]
            }
        }

        event.title = (map["title"] ?: "")
        event.subtitle = (map["subtitle"] ?: "")
        event.icon = (map["icon"] ?: "")

        return event
    }
}