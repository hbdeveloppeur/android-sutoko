package fr.purpletear.sutoko.friendzoned

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcelable
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.sharedelements.SmsGameTreeStructure
import fr.purpletear.friendzone4.game.config.Params
import friendzone3.purpletear.fr.friendzon3.MainActivity
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import purpletear.fr.purpleteartools.GameLanguage
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols

@RunWith(AndroidJUnit4::class)
class FriendzonedConversationInsetsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun friendzoned3HeadersClearSystemBarsAcrossChapters() {
        for (chapter in listOf("2a", "4a")) {
            withChapter(GlobalData.Game.FRIENDZONE3.id, chapter, MainActivity::class.java) { activity ->
                assertSafeArea(activity, friendzone3.purpletear.fr.friendzon3.R.id.mainactivity_header)
            }
        }
    }

    @Test
    fun friendzoned3ShellRespectsTheSameSafeArea() {
        withChapter(GlobalData.Game.FRIENDZONE3.id, "3a", MainActivity::class.java) { activity ->
            assertSafeArea(activity)
        }
    }

    @Test
    fun friendzoned4HeadersClearSystemBarsAcrossChapters() {
        for (chapter in listOf("2a", "3a")) {
            withChapter(GlobalData.Game.FRIENDZONE4.id, chapter, fr.purpletear.friendzone4.game.activities.main.Main::class.java) { activity ->
                assertSafeArea(activity, fr.purpletear.friendzone4.R.id.fz4_mainactivity_header)
            }
        }
    }

    private fun assertSafeArea(activity: Activity, headerId: Int? = null) {
        instrumentation.runOnMainSync {
            val decor = activity.window.decorView
            val windowInsets = requireNotNull(ViewCompat.getRootWindowInsets(decor))
            val safeArea = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val position = IntArray(2)
            headerId?.let {
                val header = activity.findViewById<View>(it)
                header.getLocationOnScreen(position)
                assertTrue("Header overlaps status bar/cutout: y=${position[1]}, inset=${safeArea.top}", position[1] >= safeArea.top)
            }
            val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
            root.getLocationOnScreen(position)
            assertEquals("Conversation top inset", safeArea.top, position[1])
            assertEquals("Conversation left inset", safeArea.left, position[0])
            assertEquals("Conversation right inset", decor.width - safeArea.right, position[0] + root.width)
            assertEquals("Conversation bottom inset", decor.height - safeArea.bottom, position[1] + root.height)
            val margins = root.layoutParams as ViewGroup.MarginLayoutParams
            val initial = listOf(margins.leftMargin, margins.topMargin, margins.rightMargin, margins.bottomMargin)
            repeat(3) { ViewCompat.dispatchApplyWindowInsets(root, windowInsets) }
            val reapplied = root.layoutParams as ViewGroup.MarginLayoutParams
            assertEquals("Insets must not accumulate", initial, listOf(reapplied.leftMargin, reapplied.topMargin, reapplied.rightMargin, reapplied.bottomMargin))
        }
    }

    private fun withChapter(gameId: Int, chapter: String, activityClass: Class<out Activity>, test: (Activity) -> Unit) {
        val savedSymbols = TableOfSymbols(gameId).apply { read() }
        val language = GameLanguage.determineLangDirectory()
        val author = if (gameId == GlobalData.Game.FRIENDZONE4.id) 28 else 0
        val files = mapOf(
            SmsGameTreeStructure.getStoryPhrasesFile(context, gameId.toString(), chapter, language) to
                """[{"id":1,"id_author":$author,"sentence":"Conversation inset test","seen":0,"wait":60000,"type":0,"code":""}]""",
            SmsGameTreeStructure.getStoryLinksFile(context, gameId.toString(), chapter, language) to
                """[{"src":"0","dest":"1"}]""",
        )
        val originals = files.keys.associateWith { if (it.exists()) it.readBytes() else null }.toMutableMap()
        var activity: Activity? = null
        try {
            files.forEach { (file, content) ->
                check(file.parentFile?.isDirectory == true || file.parentFile?.mkdirs() == true)
                file.writeText(content)
            }
            if (gameId == GlobalData.Game.FRIENDZONE4.id) {
                val image = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.DKGRAY) }
                try {
                    for (name in listOf("fz4_zoe_lucie.png", "fz4_eva.png")) {
                        val file = File(SmsGameTreeStructure.getMediaFilePath(context, gameId.toString(), name))
                        originals[file] = if (file.exists()) file.readBytes() else null
                        check(file.parentFile?.isDirectory == true || file.parentFile?.mkdirs() == true)
                        file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    }
                } finally {
                    image.recycle()
                }
            }
            val symbols = TableOfSymbols(gameId).apply { read(); chapterCode = chapter; firstName = "Alex" }
            assertTrue(symbols.save())
            val intent = Intent(context, activityClass).putExtra("symbols", symbols).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (gameId == GlobalData.Game.FRIENDZONE4.id) intent.putExtra("params", Params(chapter) as Parcelable)
            activity = instrumentation.startActivitySync(intent)
            val launched = activity
            await { launched.hasWindowFocus() && launched.window.decorView.isLaidOut }
            SystemClock.sleep(750)
            test(launched)
            if (InstrumentationRegistry.getArguments().getString("screenshots") == "true") {
                if (gameId == GlobalData.Game.FRIENDZONE4.id) {
                    instrumentation.runOnMainSync { launched.findViewById<View>(fr.purpletear.friendzone4.R.id.fz4_main_button_continue).performClick() }
                    SystemClock.sleep(1200)
                } else {
                    SystemClock.sleep(9500)
                }
                assertTrue(UiDevice.getInstance(instrumentation).takeScreenshot(File(context.getExternalFilesDir(null), "conversation-$gameId-$chapter.png")))
            }
        } finally {
            instrumentation.runOnMainSync { activity?.finish() }
            instrumentation.waitForIdleSync()
            savedSymbols.save()
            originals.forEach { (file, content) -> if (content == null) file.delete() else file.writeBytes(content) }
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
        throw AssertionError("Conversation did not finish laying out")
    }
}
