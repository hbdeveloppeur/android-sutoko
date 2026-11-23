package fr.purpletear.sutoko.friendzoned

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.example.sharedelements.R
import com.example.sharedelements.SmsGameTreeStructure
import java.io.File
import purpletear.fr.purpleteartools.GameLanguage
import fr.purpletear.friendzone4.game.config.Params
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import purpletear.fr.purpleteartools.GlobalData
import purpletear.fr.purpleteartools.TableOfSymbols

@RunWith(AndroidJUnit4::class)
class FriendzonedChapterNavigationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun friendzoned1OpensAndAdvancesChapter() = openAndAdvance(GlobalData.Game.FRIENDZONE.id,
        fr.purpletear.friendzone.activities.main.Main::class.java)

    @Test
    fun friendzoned2OpensAndAdvancesChapter() = openAndAdvance(GlobalData.Game.FRIENDZONE2.id,
        fr.purpletear.friendzone2.activities.main.Main::class.java)

    @Test
    fun friendzoned3OpensAndAdvancesChapter() = openAndAdvance(GlobalData.Game.FRIENDZONE3.id,
        friendzone3.purpletear.fr.friendzon3.MainActivity::class.java)

    @Test
    fun friendzoned4OpensAndAdvancesChapter() = openAndAdvance(GlobalData.Game.FRIENDZONE4.id,
        fr.purpletear.friendzone4.game.activities.main.Main::class.java)

    private fun openAndAdvance(legacyId: Int, gameClass: Class<out Activity>) {
        val savedSymbols = TableOfSymbols(legacyId).apply { read() }
        val symbols = TableOfSymbols(legacyId).apply {
            read()
            chapterCode = "2a"
            firstName = "Alex"
        }
        val savedParams = if (legacyId == GlobalData.Game.FRIENDZONE4.id) Params().apply { read(context) } else null
        val chapterFiles = createChapterFiles(legacyId)
        try {
            assertTrue("Could not save the starting chapter", symbols.save())
            val loaderClass = requireNotNull(FriendzonedGameRouter.loaderClassFor(legacyId))
            context.startActivity(Intent(context, loaderClass)
                .putExtra("symbols", symbols)
                .putExtra("granted", true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            val firstChapter = awaitChapter(gameClass)
            assertChapter(firstChapter, "2a")
            SystemClock.sleep(700)

            // Return the same result the game's next-chapter narrative event produces.
            instrumentation.runOnMainSync {
                symbols.chapterCode = "3a"
                assertTrue(symbols.save())
                val result = Intent().putExtra("symbols", symbols)
                if (savedParams != null) {
                    val params = requireNotNull(firstChapter.intent.getParcelableExtra<Params>("params"))
                    params.chapterCode = "3a"
                    params.save(context)
                    result.putExtra("params", params as android.os.Parcelable)
                }
                firstChapter.setResult(Activity.RESULT_OK, result)
                firstChapter.finish()
            }
            val nextChapter = awaitChapter(gameClass, firstChapter)
            assertChapter(nextChapter, "3a")
            SystemClock.sleep(700)
        } finally {
            instrumentation.runOnMainSync {
                val monitor = ActivityLifecycleMonitorRegistry.getInstance()
                val activities = listOf(Stage.RESUMED, Stage.PAUSED, Stage.STARTED, Stage.STOPPED, Stage.CREATED)
                    .flatMap { monitor.getActivitiesInStage(it) }.distinct()
                for (activity in activities) {
                    activity.setResult(Activity.RESULT_CANCELED)
                    activity.finish()
                }
            }
            instrumentation.waitForIdleSync()
            savedSymbols.save()
            savedParams?.save(context)
            for ((file, contents) in chapterFiles) {
                if (contents == null) file.delete() else file.writeBytes(contents)
            }
        }
    }

    private fun createChapterFiles(legacyId: Int): Map<File, ByteArray?> {
        if (legacyId == GlobalData.Game.FRIENDZONE.id) return emptyMap()
        val originals = mutableMapOf<File, ByteArray?>()
        val language = GameLanguage.determineLangDirectory()
        // Keep these UI tests independent of downloaded stories and network availability.
        for (chapter in listOf("2a", "3a")) {
            val phrases = SmsGameTreeStructure.getStoryPhrasesFile(context, legacyId.toString(), chapter, language)
            val links = SmsGameTreeStructure.getStoryLinksFile(context, legacyId.toString(), chapter, language)
            for ((file, content) in listOf(
                phrases to """[{"id":1,"id_author":0,"sentence":"Chapter transition test","seen":0,"wait":60000,"type":0,"code":""}]""",
                links to """[{"src":"0","dest":"1"}]""",
            )) {
                originals[file] = if (file.exists()) file.readBytes() else null
                check(file.parentFile?.isDirectory == true || file.parentFile?.mkdirs() == true)
                file.writeText(content)
            }
        }
        if (legacyId == GlobalData.Game.FRIENDZONE4.id) {
            val image = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.DKGRAY) }
            try {
                for (name in listOf("fz4_zoe_lucie.png", "fz4_eva.png")) {
                    val file = File(SmsGameTreeStructure.getMediaFilePath(context, legacyId.toString(), name))
                    originals[file] = if (file.exists()) file.readBytes() else null
                    check(file.parentFile?.isDirectory == true || file.parentFile?.mkdirs() == true)
                    file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
                }
            } finally {
                image.recycle()
            }
        }
        return originals
    }

    private fun awaitChapter(gameClass: Class<out Activity>, previous: Activity? = null): Activity {
        val deadline = SystemClock.uptimeMillis() + 15_000
        while (SystemClock.uptimeMillis() < deadline) {
            var activity: Activity? = null
            instrumentation.runOnMainSync {
                activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                    .firstOrNull { gameClass.isInstance(it) && it !== previous }
            }
            activity?.let { return it }
            SystemClock.sleep(50)
        }
        throw AssertionError("${gameClass.name} did not reveal the chapter")
    }

    private fun assertChapter(activity: Activity, code: String) {
        instrumentation.runOnMainSync {
            assertEquals(R.style.FriendzonedChapterWindowAnimations, activity.window.attributes.windowAnimations)
            assertEquals(code, activity.intent.getParcelableExtra<TableOfSymbols>("symbols")?.chapterCode)
        }
    }
}
