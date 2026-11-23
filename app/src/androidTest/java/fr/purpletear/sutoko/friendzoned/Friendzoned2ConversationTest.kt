package fr.purpletear.sutoko.friendzoned

import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.sharedelements.SmsGameTreeStructure
import fr.purpletear.friendzone2.R
import fr.purpletear.friendzone2.activities.main.GameConversationAdapter
import fr.purpletear.friendzone2.activities.main.Main
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.tables.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols

@RunWith(AndroidJUnit4::class)
class Friendzoned2ConversationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)

    @Test
    fun chloeHeaderClearsSystemBarsAcrossChapters() {
        for (chapter in listOf("1a", "3a", "7a")) {
            withChapter(chapter) { activity ->
                instrumentation.runOnMainSync {
                    val decor = activity.window.decorView
                    val insets = requireNotNull(ViewCompat.getRootWindowInsets(decor))
                        .getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                    val header = activity.findViewById<View>(R.id.mainactivity_header)
                    val position = IntArray(2)
                    header.getLocationOnScreen(position)
                    assertTrue("Header overlaps the status bar in $chapter", position[1] >= insets.top)
                    assertTrue("Header overlaps the cutout in $chapter", position[0] >= insets.left)
                    val button = activity.findViewById<View>(R.id.main_button)
                    button.getLocationOnScreen(position)
                    assertTrue("Conversation controls overlap navigation", position[1] + button.height <= decor.height - insets.bottom)
                }
            }
        }
    }

    @Test
    fun evaMessagesAndTypingAppearOnTheRightInChapterOne() {
        val phrases = """[
            {"id":1,"id_author":47,"sentence":"Eva delayed","seen":0,"wait":2500,"type":0,"code":""},
            {"id":2,"id_author":48,"sentence":"Chloe incoming","seen":0,"wait":0,"type":0,"code":""},
            {"id":3,"id_author":47,"sentence":"Eva immediate","seen":0,"wait":0,"type":0,"code":""}
        ]"""
        val links = """[{"src":"0","dest":"1"},{"src":"1","dest":"2"},{"src":"2","dest":"3"}]"""
        withChapter("1a", phrases, links) { activity ->
            instrumentation.runOnMainSync { activity.findViewById<View>(R.id.main_button_continue).performClick() }
            await {
                (activity.findViewById<RecyclerView>(R.id.main_recyclerview).adapter as GameConversationAdapter).itemCount > 0
            }
            instrumentation.runOnMainSync {
                val list = activity.findViewById<RecyclerView>(R.id.main_recyclerview)
                val adapter = list.adapter as GameConversationAdapter
                assertEquals(Phrase.Type.meTyping, adapter.getAll().first().getType())
            }
            await {
                (activity.findViewById<RecyclerView>(R.id.main_recyclerview).adapter as GameConversationAdapter).itemCount == 3
            }
            await { activity.findViewById<RecyclerView>(R.id.main_recyclerview).findViewHolderForAdapterPosition(2) != null }
            instrumentation.runOnMainSync {
                val list = activity.findViewById<RecyclerView>(R.id.main_recyclerview)
                val adapter = list.adapter as GameConversationAdapter
                assertEquals(listOf(Phrase.Type.me, Phrase.Type.dest, Phrase.Type.me), adapter.getAll().map { it.getType() })
                for (position in listOf(0, 2)) {
                    val row = requireNotNull(list.findViewHolderForAdapterPosition(position)).itemView
                    val bubble = row.findViewById<View>(R.id.phrase_me_background)
                    assertNotNull(bubble)
                    assertTrue("Eva's bubble is not right aligned", bubble.right > row.width * 0.9f)
                }
            }
        }
    }

    @Test
    fun exitConfirmationDoesNotRestartTheChapterFade() {
        withChapter("3a") { activity ->
            instrumentation.runOnMainSync { activity.findViewById<View>(R.id.main_button_continue).performClick() }
            await { activity.findViewById<View>(R.id.main_description).visibility == View.INVISIBLE }
            val filter = activity.findViewById<View>(R.id.main_main_filter_black)
            instrumentation.runOnMainSync {
                assertEquals(View.INVISIBLE, filter.visibility)
                activity.onWindowFocusChanged(false)
                assertTrue("Losing focus restarts the black fade", filter.animation == null || filter.animation.hasEnded())
                activity.onWindowFocusChanged(true)
                assertTrue("Regaining focus restarts the black fade", filter.animation == null || filter.animation.hasEnded())
            }
            repeat(2) {
                device.pressBack()
                assertTrue(device.wait(Until.hasObject(By.text(context.getString(R.string.quit_conv))), 3000))
                instrumentation.runOnMainSync {
                    assertFalse(activity.isFinishing)
                    assertTrue("Exit dialog restarts the black fade", filter.animation == null || filter.animation.hasEnded())
                }
                device.findObject(By.text(context.getString(R.string.no))).click()
                await { activity.hasWindowFocus() }
                instrumentation.runOnMainSync {
                    assertFalse(activity.isFinishing)
                    assertTrue("Canceling exit restarts the black fade", filter.animation == null || filter.animation.hasEnded())
                }
            }
            device.pressBack()
            assertTrue(device.wait(Until.hasObject(By.text(context.getString(R.string.yes))), 3000))
            device.findObject(By.text(context.getString(R.string.yes))).click()
            await { activity.isFinishing }
        }
    }

    private fun withChapter(
        chapter: String,
        phrases: String = """[{"id":1,"id_author":48,"sentence":"Conversation test","seen":0,"wait":0,"type":0,"code":""}]""",
        links: String = """[{"src":"0","dest":"1"}]""",
        test: (Main) -> Unit,
    ) {
        val gameId = GlobalData.Game.FRIENDZONE2.id
        val language = Language.determineLangDirectory()
        val files = mapOf(
            SmsGameTreeStructure.getStoryPhrasesFile(context, gameId.toString(), chapter, language) to phrases,
            SmsGameTreeStructure.getStoryLinksFile(context, gameId.toString(), chapter, language) to links,
        )
        val originals = files.keys.associateWith { if (it.exists()) it.readBytes() else null }
        var activity: Main? = null
        try {
            files.forEach { (file, contents) ->
                check(file.parentFile?.isDirectory == true || file.parentFile?.mkdirs() == true)
                file.writeText(contents)
            }
            val symbols = TableOfSymbols(gameId).apply { chapterCode = chapter; firstName = "Alex" }
            activity = instrumentation.startActivitySync(Intent(context, Main::class.java)
                .putExtra("symbols", symbols).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as Main
            val launched = activity
            await { launched.hasWindowFocus() && launched.findViewById<View>(R.id.main_main_filter_black).visibility == View.INVISIBLE }
            test(launched)
        } finally {
            instrumentation.runOnMainSync { activity?.finish() }
            instrumentation.waitForIdleSync()
            originals.forEach { (file, contents) -> if (contents == null) file.delete() else file.writeBytes(contents) }
        }
    }

    private fun await(condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            instrumentation.runOnMainSync { ready = condition() }
            if (ready) return
            SystemClock.sleep(25)
        }
        throw AssertionError("Conversation did not reach the expected state")
    }
}
