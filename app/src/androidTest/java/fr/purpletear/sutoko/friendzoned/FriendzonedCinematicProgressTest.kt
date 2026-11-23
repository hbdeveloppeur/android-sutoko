package fr.purpletear.sutoko.friendzoned

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sharedelements.SmsGameTreeStructure
import fr.purpletear.friendzone2.activities.textcinematic.TextCinematic
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import purpletear.fr.purpleteartools.CinematicProgressView
import purpletear.fr.purpleteartools.GameLanguage
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols
import java.io.File
import android.graphics.Bitmap
import kotlin.math.roundToInt
import fr.purpletear.friendzone4.game.config.Params

@RunWith(AndroidJUnit4::class)
class FriendzonedCinematicProgressTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun friendzoned2ProgressPausesResumesAndDisappearsBeforeStats() {
        val game = GlobalData.Game.FRIENDZONE2.id
        withScript(game, "9f", """[{"id":1,"id_author":46,"sentence":"A quiet cinematic","seen":100,"wait":7000,"type":1,"code":""}]""",
            """[{"src":"0","dest":"1"}]""") {
            val intent = Intent(context, TextCinematic::class.java)
                .putExtra("symbols", TableOfSymbols(game).apply { chapterCode = "9f" })
            ActivityScenario.launch<TextCinematic>(intent).use { scenario ->
                SystemClock.sleep(700)
                var beforePause = 0f
                scenario.onActivity { activity ->
                    val ring = activity.findViewById<CinematicProgressView>(fr.purpletear.friendzone2.R.id.cinematic_progress)
                    assertEquals(View.VISIBLE, ring.visibility)
                    assertEquals((18 * context.resources.displayMetrics.density).roundToInt(), ring.width)
                    assertSafeArea(ring)
                    assertTrue(ring.progress > 0f && ring.progress < 1f)
                }
                screenshot("cinematic-friendzoned2.png")
                scenario.moveToState(Lifecycle.State.CREATED)
                scenario.onActivity { beforePause = it.findViewById<CinematicProgressView>(fr.purpletear.friendzone2.R.id.cinematic_progress).progress }
                SystemClock.sleep(3200)
                scenario.onActivity {
                    val ring = it.findViewById<CinematicProgressView>(fr.purpletear.friendzone2.R.id.cinematic_progress)
                    assertEquals(beforePause, ring.progress, 0.001f)
                    assertEquals(View.VISIBLE, ring.visibility)
                }
                scenario.moveToState(Lifecycle.State.RESUMED)
                SystemClock.sleep(500)
                scenario.onActivity {
                    assertTrue(it.findViewById<CinematicProgressView>(fr.purpletear.friendzone2.R.id.cinematic_progress).progress > beforePause)
                }
                await(scenario) { it.findViewById<View>(fr.purpletear.friendzone2.R.id.cinematic_progress).visibility == View.INVISIBLE }
                scenario.onActivity {
                    val ring = it.findViewById<CinematicProgressView>(fr.purpletear.friendzone2.R.id.cinematic_progress)
                    assertEquals(1f, ring.progress, 0f)
                    assertEquals(View.INVISIBLE, ring.visibility)
                    assertEquals(View.VISIBLE, it.findViewById<View>(fr.purpletear.friendzone2.R.id.textcinematic_stats).visibility)
                }
            }
        }
    }

    @Test
    fun friendzoned1ShowsTheSameRingAndStopsUpdatesOnExit() {
        val game = GlobalData.Game.FRIENDZONE.id
        val intent = Intent(context, fr.purpletear.friendzone.activities.textcinematic.TextCinematic::class.java)
            .putExtra("symbols", TableOfSymbols(game).apply { chapterCode = "9b" })
        verifyPauseAndExit(intent, fr.purpletear.friendzone.R.id.cinematic_progress, "cinematic-friendzoned1.png")
    }

    @Test
    fun friendzoned3ShowsTheSameRingAndCompletes() {
        val game = GlobalData.Game.FRIENDZONE3.id
        withScript(game, "0a", singleLine, initialLink) {
            val intent = Intent(context, friendzone3.purpletear.fr.friendzon3.textcinematic.TextCinematic::class.java)
                .putExtra("symbols", TableOfSymbols(game).apply { chapterCode = "1a" })
            verifyPauseAndExit(intent, friendzone3.purpletear.fr.friendzon3.R.id.cinematic_progress, "cinematic-friendzoned3.png", completes = true)
        }
    }

    @Test
    fun friendzoned4ShowsTheSameRingAndCompletes() {
        val game = GlobalData.Game.FRIENDZONE4.id
        withScript(game, "0a", singleLine, initialLink) {
            val intent = Intent(context, fr.purpletear.friendzone4.game.activities.textcinematic.TextCinematic::class.java)
                .putExtra("symbols", TableOfSymbols(game).apply { chapterCode = "1a" })
                .putExtra("params", Params() as android.os.Parcelable)
            verifyPauseAndExit(intent, fr.purpletear.friendzone4.R.id.cinematic_progress, "cinematic-friendzoned4.png", completes = true)
        }
    }

    @Test
    fun friendzoned4TimedChoiceCompletesAndClearsTheRingWhenChosen() {
        val game = GlobalData.Game.FRIENDZONE4.id
        withScript(game, "0a", """[{"id":1,"id_author":46,"sentence":"[TIMED_CHOICE]","seen":100,"wait":0,"type":1,"code":""}]""", initialLink) {
            val intent = Intent(context, fr.purpletear.friendzone4.game.activities.textcinematic.TextCinematic::class.java)
                .putExtra("symbols", TableOfSymbols(game).apply { chapterCode = "1a" })
                .putExtra("params", Params() as android.os.Parcelable)
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            var activity: Activity? = null
            var ring: CinematicProgressView? = null
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                val deadline = SystemClock.uptimeMillis() + 5000
                var chosen = false
                while (!chosen && SystemClock.uptimeMillis() < deadline) {
                    instrumentation.runOnMainSync {
                        activity = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED)
                            .firstOrNull { it is fr.purpletear.friendzone4.game.activities.textcinematic.TextCinematic }
                        activity?.let {
                            ring = it.findViewById(fr.purpletear.friendzone4.R.id.cinematic_progress)
                            val box = it.findViewById<View>(fr.purpletear.friendzone4.R.id.fz4_textcinematic_choicebox)
                            if (box.visibility == View.VISIBLE) {
                                assertTrue(ring!!.progress > 0f && ring!!.progress < 1f)
                                val choices = it.findViewById<android.view.ViewGroup>(fr.purpletear.friendzone4.R.id.fz4_mainactivity_choicebox_parent)
                                assertTrue(choices.getChildAt(0).performClick())
                                chosen = true
                            }
                        }
                    }
                    if (!chosen) SystemClock.sleep(50)
                }
                assertTrue("Timed choice was not displayed", chosen)
                val finishDeadline = SystemClock.uptimeMillis() + 3000
                var destroyed = false
                while (!destroyed && SystemClock.uptimeMillis() < finishDeadline) {
                    instrumentation.runOnMainSync { destroyed = activity!!.isDestroyed }
                    if (!destroyed) SystemClock.sleep(50)
                }
                instrumentation.runOnMainSync {
                    assertTrue("Choosing should finish the cinematic", destroyed)
                    assertEquals(1f, ring!!.progress, 0f)
                    assertEquals(View.INVISIBLE, ring!!.visibility)
                }
            } finally {
                instrumentation.runOnMainSync { activity?.finish() }
            }
        }
    }

    @Test
    fun friendzoned4TrophyCueAdvancesRatherThanLoopingForever() {
        val game = GlobalData.Game.FRIENDZONE4.id
        val phrases = """[{"id":1,"id_author":-1,"sentence":"[TROPHY$$$94]","seen":0,"wait":0,"type":19},
            {"id":2,"id_author":46,"sentence":"After the trophy","seen":100,"wait":3000,"type":1}]"""
        withScript(game, "0a", phrases, """[{"src":"0","dest":"1"},{"src":"1","dest":"2"}]""") {
            val intent = Intent(context, fr.purpletear.friendzone4.game.activities.textcinematic.TextCinematic::class.java)
                .putExtra("symbols", TableOfSymbols(game).apply { chapterCode = "1a" })
                .putExtra("params", Params() as android.os.Parcelable)
            verifyPauseAndExit(intent, fr.purpletear.friendzone4.R.id.cinematic_progress, "cinematic-friendzoned4.png", completes = true)
        }
    }

    @Test
    fun friendzoned2ProgressUsesOnlyTheSelectedBranchAndIncludesImageFades() {
        val game = GlobalData.Game.FRIENDZONE2.id
        val phrases = """[
            {"id":1,"sentence":"[testFlag==yes]{2}{3}","seen":0,"wait":0,"type":2},
            {"id":2,"sentence":"Unselected long branch","seen":0,"wait":60000,"type":1},
            {"id":3,"sentence":"[BACKGROUND_cinematic_test.jpg]","seen":0,"wait":0,"type":1},
            {"id":4,"sentence":"The selected cinematic branch","seen":100,"wait":7000,"type":1}
        ]"""
        val links = """[{"src":"0","dest":"1"},{"src":"3","dest":"4"}]"""
        withScript(game, "9f", phrases, links) {
            val intent = Intent(context, TextCinematic::class.java)
                .putExtra("symbols", TableOfSymbols(game).apply { chapterCode = "9f" })
            ActivityScenario.launch<TextCinematic>(intent).use { scenario ->
                // Background fade (1 s), first text fade (1 s), seen (0.1 s), wait (7 s).
                scenario.onActivity {
                    val field = TextCinematic::class.java.getDeclaredField("model").apply { isAccessible = true }
                    val model = field.get(it) as fr.purpletear.friendzone2.activities.textcinematic.TextCinematicModel
                    assertEquals(9100L, model.cinematicDurationMillis())
                }
                var before = 0f
                scenario.onActivity { before = it.findViewById<CinematicProgressView>(fr.purpletear.friendzone2.R.id.cinematic_progress).progress }
                SystemClock.sleep(1200)
                scenario.onActivity {
                    val ring = it.findViewById<CinematicProgressView>(fr.purpletear.friendzone2.R.id.cinematic_progress)
                    assertTrue(ring.progress > before && ring.progress < 1f)
                }
                await(scenario) { it.findViewById<View>(fr.purpletear.friendzone2.R.id.cinematic_progress).visibility == View.INVISIBLE }
                scenario.onActivity {
                    assertEquals(View.INVISIBLE, it.findViewById<View>(fr.purpletear.friendzone2.R.id.cinematic_progress).visibility)
                }
            }
        }
    }

    @Test
    fun audioCinematicTracksPlaybackPausesCompletesAndRestarts() {
        val game = GlobalData.Game.FRIENDZONE4.id
        val language = GameLanguage.determineLangDirectory()
        val sound = File(SmsGameTreeStructure.getMediaFilePath(context, game.toString(), "sounds/fz4_intro_6/intro_$language.wav"))
        val original = if (sound.exists()) sound.readBytes() else null
        try {
            check(sound.parentFile!!.isDirectory || sound.parentFile!!.mkdirs())
            sound.writeBytes(silentWave(4000))
            ActivityScenario.launch(fr.purpletear.friendzone4.game.activities.audiocinematic.AudioCinematic::class.java).use { scenario ->
                SystemClock.sleep(600)
                fun play() { scenario.onActivity { tap(it.findViewById(fr.purpletear.friendzone4.R.id.fz4_audio_cinematic_button_play)) } }
                play()
                SystemClock.sleep(1000)
                var paused = 0f
                scenario.onActivity {
                    val ring = it.findViewById<CinematicProgressView>(fr.purpletear.friendzone4.R.id.cinematic_progress)
                    assertTrue(ring.progress > 0.1f && ring.progress < 0.5f)
                    assertSafeArea(ring)
                }
                screenshot("cinematic-audio-friendzoned4.png")
                scenario.moveToState(Lifecycle.State.CREATED)
                scenario.onActivity { paused = it.findViewById<CinematicProgressView>(fr.purpletear.friendzone4.R.id.cinematic_progress).progress }
                SystemClock.sleep(1300)
                scenario.onActivity { assertEquals(paused, it.findViewById<CinematicProgressView>(fr.purpletear.friendzone4.R.id.cinematic_progress).progress, 0.01f) }
                scenario.moveToState(Lifecycle.State.RESUMED)
                await(scenario) { it.findViewById<CinematicProgressView>(fr.purpletear.friendzone4.R.id.cinematic_progress).progress == 1f }
                scenario.onActivity { assertEquals(1f, it.findViewById<CinematicProgressView>(fr.purpletear.friendzone4.R.id.cinematic_progress).progress, 0f) }
                play()
                SystemClock.sleep(300)
                scenario.onActivity { assertTrue(it.findViewById<CinematicProgressView>(fr.purpletear.friendzone4.R.id.cinematic_progress).progress < 0.3f) }
                play() // Stop and reset rather than leave an active player behind.
                scenario.onActivity { assertEquals(0f, it.findViewById<CinematicProgressView>(fr.purpletear.friendzone4.R.id.cinematic_progress).progress, 0f) }
            }
        } finally {
            if (original == null) sound.delete() else sound.writeBytes(original)
        }
    }

    private val singleLine = """[{"id":1,"id_author":46,"sentence":"A quiet cinematic","seen":100,"wait":3000,"type":1,"code":""}]"""
    private val initialLink = """[{"src":"0","dest":"1"}]"""

    private fun verifyPauseAndExit(intent: Intent, ringId: Int, image: String, completes: Boolean = false) {
        var ring: CinematicProgressView? = null
        ActivityScenario.launch<Activity>(intent).use { scenario ->
            SystemClock.sleep(650)
            scenario.onActivity {
                ring = it.findViewById(ringId)
                assertEquals(View.VISIBLE, ring!!.visibility)
                assertTrue(ring!!.progress > 0f && ring!!.progress < 1f)
                assertSafeArea(ring!!)
            }
            screenshot(image)
            scenario.moveToState(Lifecycle.State.CREATED)
            var paused = 0f
            scenario.onActivity { paused = ring!!.progress }
            SystemClock.sleep(1200)
            scenario.onActivity { assertEquals(paused, ring!!.progress, 0.001f) }
            scenario.moveToState(Lifecycle.State.RESUMED)
            SystemClock.sleep(600)
            scenario.onActivity { assertTrue(ring!!.progress > paused) }
            if (completes) {
                awaitDestroyed(scenario)
                assertEquals(1f, ring!!.progress, 0f)
                assertEquals(View.INVISIBLE, ring!!.visibility)
            }
        }
        val afterExit = ring!!.progress
        SystemClock.sleep(300)
        assertEquals(afterExit, ring!!.progress, 0f)
    }

    private fun awaitDestroyed(scenario: ActivityScenario<*>) {
        val deadline = SystemClock.uptimeMillis() + 8000
        while (scenario.state != Lifecycle.State.DESTROYED && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(50)
        assertEquals(Lifecycle.State.DESTROYED, scenario.state)
    }

    private fun <A : Activity> await(scenario: ActivityScenario<A>, condition: (A) -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 12_000
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            scenario.onActivity { ready = condition(it) }
            if (ready) return
            SystemClock.sleep(50)
        }
        fail("Cinematic did not reach the expected state")
    }

    private fun assertSafeArea(ring: View) {
        val location = IntArray(2).also { ring.getLocationOnScreen(it) }
        val insets = ring.rootWindowInsets
        val top = maxOf(insets.systemWindowInsetTop, insets.displayCutout?.safeInsetTop ?: 0)
        assertTrue("Progress ring overlaps status bar or cutout", location[1] >= top)
    }

    private fun screenshot(name: String) {
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        try { File(context.getExternalFilesDir(null), name).outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        finally { screenshot.recycle() }
    }

    private fun tap(view: View) {
        val now = SystemClock.uptimeMillis()
        for (action in listOf(android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_UP)) {
            val event = android.view.MotionEvent.obtain(now, now, action, view.width / 2f, view.height / 2f, 0)
            try { view.dispatchTouchEvent(event) } finally { event.recycle() }
        }
    }

    private fun silentWave(durationMillis: Int): ByteArray {
        val samples = 8000 * durationMillis / 1000
        return java.nio.ByteBuffer.allocate(44 + samples * 2).order(java.nio.ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(36 + samples * 2); put("WAVEfmt ".toByteArray())
            putInt(16); putShort(1); putShort(1); putInt(8000); putInt(16000); putShort(2); putShort(16)
            put("data".toByteArray()); putInt(samples * 2)
        }.array()
    }

    private fun withScript(game: Int, chapter: String, phrases: String, links: String, action: () -> Unit) {
        val language = GameLanguage.determineLangDirectory()
        val files = mapOf(
            SmsGameTreeStructure.getStoryPhrasesFile(context, game.toString(), chapter, language) to phrases,
            SmsGameTreeStructure.getStoryLinksFile(context, game.toString(), chapter, language) to links,
        )
        val originals = files.keys.associateWith { if (it.exists()) it.readBytes() else null }
        try {
            files.forEach { (file, content) ->
                check(file.parentFile!!.isDirectory || file.parentFile!!.mkdirs())
                file.writeText(content)
            }
            action()
        } finally {
            originals.forEach { (file, bytes) -> if (bytes == null) file.delete() else file.writeBytes(bytes) }
        }
    }
}
