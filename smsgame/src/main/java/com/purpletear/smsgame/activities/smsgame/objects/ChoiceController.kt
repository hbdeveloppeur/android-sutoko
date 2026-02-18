/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package com.purpletear.smsgame.activities.smsgame.objects

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.RequestManager
import com.purpletear.smsgame.R
import com.purpletear.smsgame.databinding.LayoutSmsgameChoiceSecretBinding
import com.purpletear.sutoko.game.model.Game
import fr.purpletear.sutoko.shop.coinsLogic.objects.GameHistory
import purpletear.fr.purpleteartools.Animation
import purpletear.fr.purpleteartools.Finger
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.Measure
import purpletear.fr.purpleteartools.Std
import purpletear.fr.purpleteartools.TableOfSymbols


object ChoicesController {

    fun fill(
        activity: Activity,
        requestManager: RequestManager,
        card: Game,
        history: GameHistory,
        callback: ChoiceControllerInterface,
        parent: ViewGroup,
        phrases: ArrayList<Phrase>,
        symbols: TableOfSymbols,
        isDarkMode: Boolean
    ) {
        for (phrase in phrases) {
            phrase.formatName(symbols.firstName)
            if (skip(phrase, symbols)) {
                continue
            } else {
                if (phrase.isChoiceEqualCondition) {
                    phrase.sentence = phrase.choiceEqualConditionFormated
                }
            }
            parent.addView(
                buildChoice(
                    activity,
                    requestManager,
                    card,
                    history,
                    phrase,
                    parent.context,
                    callback,
                    isDarkMode
                )
            )
        }
    }

    fun clear(parent: ViewGroup) {
        parent.removeAllViews()
    }

    private fun buildChoice(
        activity: Activity,
        requestManager: RequestManager,
        card: Game,
        history: GameHistory,
        phrase: Phrase,
        context: Context,
        c: ChoiceControllerInterface,
        isDarkMode: Boolean = true
    ): View {

        if (phrase.isSecretChoice && !history.hasOrder("${card.id}s${phrase.id}")) {
            val view =
                LayoutSmsgameChoiceSecretBinding.inflate(activity.layoutInflater, null, false)
            view.realText.text = phrase.sentence
            view.secretLayoutDiamondsCount.text = phrase.secretChoiceDiamonds.toString()
            view.realText.post {
                view.background.updateLayoutParams<FrameLayout.LayoutParams> {
                    height = view.realText.height
                }
            }
            requestManager.load(R.drawable.sutoko_sms_game_secret_choice_bg)
                .into(view.background)
            requestManager.load(R.drawable.sutoko_ic_diamond)
                .into(view.secretLayerDiamond)
            view.text.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isDarkMode) R.color.darkModeWhite2 else R.color.sutokoMainChoicesText
                )
            )
            Finger.defineOnTouch(view.root, context) {
                Std.vibrate(view.root)
                Finger.defineOnTouch(view.root, context) {}
                view.loader.visibility = View.VISIBLE
                c.onClickSecretChoice(phrase, phrase.secretChoiceDiamonds, {
                    Animation.setAnimation(
                        view.secretLayer,
                        Animation.Animations.ANIMATION_FADEOUT,
                        activity,
                        560
                    )
                    Animation.setAnimation(
                        view.realText,
                        Animation.Animations.ANIMATION_FADEIN,
                        activity,
                        560
                    )
                    view.loader.visibility = View.GONE
                    Finger.defineOnTouch(view.root, context) {
                        Std.vibrate(view.root)
                        c.onClickChoice(phrase)
                    }
                }, {
                    view.loader.visibility = View.GONE
                })
            }
            return view.root
        } else {
            val padding_top = Math.round(Measure.px(12, context).toDouble()).toInt()
            val padding_left = Math.round(Measure.px(18, context).toDouble()).toInt()
            val textView = TextView(context)
            textView.text = phrase.sentence
            textView.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isDarkMode) R.color.darkModeWhite2 else R.color.sutokoMainChoicesText
                )
            )
            textView.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.sutoko_choice_text_font)
            )
            textView.setPadding(padding_left, padding_top, padding_left, padding_top)
            Finger.defineOnTouch(textView, context) {
                Std.vibrate(textView)
                c.onClickChoice(phrase)
            }
            return textView
        }
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
            return !table.condition(GlobalData.Game.FRIENDZONE.id, values[0], values[1])
        }
        return false
    }
}
