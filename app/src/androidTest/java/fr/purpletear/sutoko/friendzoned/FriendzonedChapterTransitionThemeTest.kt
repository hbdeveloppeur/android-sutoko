package fr.purpletear.sutoko.friendzoned

import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sharedelements.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FriendzonedChapterTransitionThemeTest {
    @Test
    fun everyChapterActivityUsesPairedFades() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activities = listOf(
            "fr.purpletear.friendzone.activities.load.Load",
            "fr.purpletear.friendzone.activities.main.Main",
            "fr.purpletear.friendzone.activities.game.Game",
            "fr.purpletear.friendzone.activities.textcinematic.TextCinematic",
            "fr.purpletear.friendzone2.activities.load.Load",
            "fr.purpletear.friendzone2.activities.main.Main",
            "fr.purpletear.friendzone2.activities.chapterSwitcher.ChapterSwitcher",
            "fr.purpletear.friendzone2.activities.textcinematic.TextCinematic",
            "fr.purpletear.friendzone2.activities.poetry.Poetry",
            "friendzone3.purpletear.fr.friendzon3.Load",
            "friendzone3.purpletear.fr.friendzon3.MainActivity",
            "friendzone3.purpletear.fr.friendzon3.textcinematic.TextCinematic",
            "friendzone3.purpletear.fr.friendzon3.PoeticActivity",
            "friendzone3.purpletear.fr.friendzon3.End",
            "fr.purpletear.friendzone4.game.activities.load.Load",
            "fr.purpletear.friendzone4.game.activities.main.Main",
            "fr.purpletear.friendzone4.game.activities.audiocinematic.AudioCinematic",
            "fr.purpletear.friendzone4.game.activities.textcinematic.TextCinematic",
            "fr.purpletear.friendzone4.game.activities.loading.Loading",
            "fr.purpletear.friendzone4.game.activities.poetry.Poetry",
            "fr.purpletear.friendzone4.game.activities.end.End",
        )
        val attributes = intArrayOf(
            android.R.attr.activityOpenEnterAnimation,
            android.R.attr.activityOpenExitAnimation,
            android.R.attr.activityCloseEnterAnimation,
            android.R.attr.activityCloseExitAnimation,
        )
        for (name in activities) {
            val info = context.packageManager.getActivityInfo(ComponentName(context, name), 0)
            val themedContext = ContextThemeWrapper(context, info.theme)
            val background = themedContext.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
            try {
                assertEquals("$name must not flash a light starting window", Color.BLACK,
                    (background.getDrawable(0) as ColorDrawable).color)
            } finally {
                background.recycle()
            }
            val windowAttributes = themedContext.obtainStyledAttributes(intArrayOf(android.R.attr.windowAnimationStyle))
            val animationStyle = windowAttributes.getResourceId(0, 0)
            windowAttributes.recycle()
            assertEquals(name, R.style.FriendzonedChapterWindowAnimations, animationStyle)
            val animations = themedContext.obtainStyledAttributes(animationStyle, attributes)
            try {
                for (index in attributes.indices) {
                    val expected = if (index % 2 == 0) R.anim.game_launch_fade_in else R.anim.game_launch_fade_out
                    assertEquals("$name animation $index", expected, animations.getResourceId(index, 0))
                    val animation = AnimationUtils.loadAnimation(context, expected)
                    assertTrue(animation is AlphaAnimation)
                    assertEquals(500L, animation.duration)
                    animation.startTime = 0L
                    val frame = Transformation()
                    animation.getTransformation(250L, frame)
                    assertTrue("$name must fade, not cut", frame.alpha > 0f && frame.alpha < 1f)
                }
            } finally {
                animations.recycle()
            }
        }
    }
}
