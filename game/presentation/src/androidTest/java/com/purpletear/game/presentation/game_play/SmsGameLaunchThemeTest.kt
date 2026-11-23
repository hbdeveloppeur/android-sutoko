package com.purpletear.game.presentation.game_play

import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.animation.AnimationUtils
import androidx.test.filters.SdkSuppress
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.game.presentation.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmsGameLaunchThemeTest {
    @Test
    fun launchWindowAndFadeOverlayUseTheSameBlack() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val theme = ContextThemeWrapper(context, R.style.game_presentation_SmsGame)
        val attributes = theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        try {
            assertEquals(Color.BLACK, attributes.getColor(0, Color.TRANSPARENT))
        } finally {
            attributes.recycle()
        }
    }

    @Test
    fun windowFadesMatchTheChapterFadeDuration() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(GAME_FADE_DURATION_MS.toLong(), AnimationUtils.loadAnimation(context, R.anim.game_presentation_fade_in).duration)
        assertEquals(GAME_FADE_DURATION_MS.toLong(), AnimationUtils.loadAnimation(context, R.anim.game_presentation_fade_out).duration)
    }

    @Test
    @SdkSuppress(minSdkVersion = 33)
    fun windowFadesDoNotRevealTheTaskBackground() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val enter = AnimationUtils.loadAnimation(context, R.anim.game_presentation_fade_in)
        val exit = AnimationUtils.loadAnimation(context, R.anim.game_presentation_fade_out)
        assertTrue(enter.showBackdrop)
        assertTrue(exit.showBackdrop)
        assertEquals(Color.BLACK, enter.backdropColor)
        assertEquals(Color.BLACK, exit.backdropColor)
    }

}
