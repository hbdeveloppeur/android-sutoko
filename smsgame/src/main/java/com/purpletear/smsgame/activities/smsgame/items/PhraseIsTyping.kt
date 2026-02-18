package com.purpletear.smsgame.activities.smsgame.items

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.objects.MessageColor

object PhraseIsTyping {
    val LAYOUT_ID: Int = R.layout.sutoko_phrase_is_typing

    fun design(itemView: View, isMainCharacter: Boolean, messageColor: MessageColor) {
        val position: Position = if (isMainCharacter) Position.END else Position.START
        setPosition(itemView, position, messageColor)
    }

    private enum class Position {
        START,
        END
    }

    private fun setPosition(itemView: View, position: Position, messageColor: MessageColor) {
        val bias = when (position) {
            Position.END -> 1f
            Position.START -> 0f
        }
        val t = (itemView.findViewById<TextView>(R.id.sutoko_phrase_istyping_background))
        val d = t.background as GradientDrawable
        d.alpha = 180
        d.setColor(Color.parseColor(messageColor.background))
        t.background = d

        val params: ConstraintLayout.LayoutParams = t.layoutParams as ConstraintLayout.LayoutParams
        params.horizontalBias =
            bias // here is one modification for example. modify anything else you want :)

        t.layoutParams = params
    }

    fun animate(itemView: View, isMainCharacter: Boolean) {
        val position: Position = if (isMainCharacter) Position.END else Position.START
        val v = (itemView.findViewById<TextView>(R.id.sutoko_phrase_istyping_background))
        animation(v, position)
        animateAnimationView(itemView, position)
    }

    private fun animateAnimationView(itemView: View, position: Position) {
        val v = (itemView.findViewById<View>(R.id.sutoko_phrase_istyping_animation))
        animation(v, position)
    }

    private fun animation(view: View, position: Position) {
        val scaleAnim = when (position) {
            Position.END -> {
                ScaleAnimation(
                    0f, 1f,
                    0f, 1f,
                    Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 1f
                )
            }

            Position.START -> {
                ScaleAnimation(
                    0f, 1f,
                    0f, 1f,
                    Animation.ABSOLUTE, 0f,
                    Animation.RELATIVE_TO_SELF, 1f
                )
            }
        }

        scaleAnim.duration = 480
        scaleAnim.repeatCount = 0
        scaleAnim.interpolator = AccelerateInterpolator()
        scaleAnim.fillAfter = true
        scaleAnim.fillBefore = true
        scaleAnim.isFillEnabled = true
        view.startAnimation(scaleAnim)
    }
}