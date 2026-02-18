/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.activities.main

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.configs.Var
import fr.purpletear.friendzone2.tables.Character
import purpletear.fr.purpleteartools.Finger
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Measure
import purpletear.fr.purpleteartools.TableOfSymbols
import java.util.ArrayList


object ChoicesController {

    fun choose(context : Context, parent: ViewGroup, phrases: ArrayList<Phrase>, c: MainInterface, table: TableOfSymbols) {
        for (phrase in phrases) {
            phrase.formatName(table)
            if (skip(phrase, table)) {
                continue
            } else {
                if (phrase.isChoiceEqualCondition) {
                    phrase.sentence = phrase.choiceEqualConditionFormated
                }
                phrase.sentence = insertSymbols(phrase, table)
                phrase.sentence = Character.updateNames(context, phrase.sentence)
            }
            parent.addView(buildChoice(phrase, parent.context, c))
        }
    }

    private fun insertSymbols(p : Phrase, symbols : TableOfSymbols) : String {
        for(s in symbols.getArray(GlobalData.Game.FRIENDZONE2.id)) {
            p.sentence = p.sentence.replace("[${s.n}]", s.v)
        }
        return p.sentence
    }

    fun clear(parent: ViewGroup) {
        parent.removeAllViews()
    }

    private fun buildChoice(phrase: Phrase, context: Context, c : MainInterface): View {
        val padding_top = Math.round(Measure.px(12, context).toDouble()).toInt()
        val padding_left = Math.round(Measure.px(10, context).toDouble()).toInt()
        val textView = TextView(context)
        textView.text = phrase.sentence
        textView.setTextColor(ContextCompat.getColor(context, R.color.mainChoicesText))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.choice_text_font))
        textView.setPadding(padding_left, padding_top, padding_left, padding_top)
        Finger.defineOnTouch(textView, context) { c.onClickChoice(phrase) }
        return textView
    }

    private fun skip(p: Phrase, table: TableOfSymbols): Boolean {

        if (!p.isChoiceEqualCondition) {
            return false
        }
        if (p.isChoiceEqualCondition) {
            val values = p.choiceEqualCondition
                    .replace("[", "")
                    .replace("]", "")
                    .split("==")
            return !table.condition(GlobalData.Game.FRIENDZONE2.id, values[0], values[1])
        }
        return false
    }
}
