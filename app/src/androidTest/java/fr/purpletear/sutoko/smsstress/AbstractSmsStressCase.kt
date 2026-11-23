package fr.purpletear.sutoko.smsstress

import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.local.entity.ChapterEntity
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.local.entity.GameInstallEntity
import com.purpletear.game.presentation.game_play.SmsGameActivity
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import com.purpletear.sutoko.game.model.game.GameMetadata
import dagger.hilt.android.EntryPointAccessors
import fr.purpletear.sutoko.screens.PlayerOnlyPreviewDependencies
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import java.io.File
import java.util.Locale

abstract class AbstractSmsStressCase {
    protected val instrumentation = InstrumentationRegistry.getInstrumentation()
    protected val context = instrumentation.targetContext
    protected val device = UiDevice.getInstance(instrumentation)
    protected val id = "sms_stress_${javaClass.simpleName.lowercase(Locale.ROOT)}"
    private val directory = File(context.filesDir, "games/$id")
    private val key = stringPreferencesKey("story_advance_mode")
    protected lateinit var database: GameDatabase
    private lateinit var preferences: DataStore<Preferences>
    private var originalMode: String? = null
    protected open val restoreExistingFixture = false
    protected open val retainFixtureForNextProcess = false

    @Before
    fun prepare() = runBlocking {
        check(Build.MODEL.contains("sdk_gphone") || Build.FINGERPRINT.contains("generic")) {
            "SMS stress tests are restricted to an isolated emulator"
        }
        val entry = EntryPointAccessors.fromApplication(context.applicationContext, PlayerOnlyPreviewDependencies::class.java)
        database = entry.database()
        preferences = entry.gamePreferences()
        originalMode = preferences.data.first()[key]
        listOf("png", "xml").forEach {
            File(context.filesDir, "${javaClass.simpleName}-failure.$it").delete()
        }
        if (restoreExistingFixture) {
            check(directory.isDirectory && checkpoint() != null) { "No process-death fixture to restore" }
            return@runBlocking
        }
        directory.deleteRecursively()
        clearProgress()
        File(directory, "characters").apply { mkdirs() }.resolve("characters.json").writeText("""[
            {"id":1,"name":"Player","isMainCharacter":true,"colorStartingCode":"#FFFFFF","colorEndingCode":"#000000"},
            {"id":2,"name":"Tester","isMainCharacter":false,"colorStartingCode":"#FFFFFF","colorEndingCode":"#000000"}
        ]""")
        database.gameDao().upsertAll(listOf(GameCatalogEntity(id = id, version = 1, isOfficial = false,
            metadata = GameMetadata("SMS stress fixture"))))
        database.gameInstallationDao().upsert(GameInstallEntity(id, 1))
        database.chapterDao().insert(ChapterEntity(id = "$id-1", number = 1, alternative = "A", story = id, code = "1A", available = true))
    }

    @After
    fun cleanUp() = runBlocking {
        if (!::database.isInitialized) return@runBlocking
        preferences.edit { if (originalMode == null) it.remove(key) else it[key] = originalMode!! }
        if (retainFixtureForNextProcess) return@runBlocking
        clearProgress()
        database.chapterDao().deleteAllForStory(id)
        database.gameInstallationDao().deleteByGameId(id)
        database.openHelper.writableDatabase.execSQL("DELETE FROM games WHERE id = ?", arrayOf(id))
        directory.deleteRecursively()
    }

    protected suspend fun clearProgress() {
        database.userGameProgressDao().delete(id)
        database.memoryDao().deleteAllForGame(id)
    }

    protected suspend fun setMode(mode: StoryAdvanceMode) {
        preferences.edit { it[key] = mode.name }
    }

    protected fun installChain(count: Int, sms: Boolean = true, waitMs: Long = 200) {
        val nodes = mutableListOf("""{"id":"start","type":"start","data":{}}""")
        nodes += """{"id":"mode","type":"memory-save-node","data":{"memory":{"id":"typingAnimation","name":"typingAnimation","value":"$sms"}}}"""
        (1..count).forEach { i -> nodes += """{"id":"m$i","type":"message","data":{"text":"M$i","characterId":${1 + i % 2},"wait":$waitMs}}""" }
        nodes += """{"id":"end","type":"end","data":{}}"""
        val ids = listOf("start", "mode") + (1..count).map { "m$it" } + "end"
        installGraph(nodes, ids.zipWithNext().map { (from, to) -> """{"source":"$from","target":"$to"}""" })
    }

    protected fun installGraph(nodes: List<String>, edges: List<String>) {
        val chapter = File(directory, "chapters/${Locale.getDefault().toLanguageTag()}/1a").apply { mkdirs() }
        chapter.resolve("nodes.json").writeText(nodes.joinToString(",", "[", "]"))
        chapter.resolve("edges.json").writeText(edges.joinToString(",", "[", "]"))
    }

    protected suspend fun withReader(block: suspend (ActivityScenario<SmsGameActivity>) -> Unit) {
        val intent = Intent(context, SmsGameActivity::class.java).putExtra("gameId", id).putExtra("chapterCode", "1A")
        ActivityScenario.launch<SmsGameActivity>(intent).use { scenario ->
            try {
                awaitReaderInteractive(scenario)
                block(scenario)
            } catch (failure: Throwable) {
                device.takeScreenshot(File(context.filesDir, "${javaClass.simpleName}-failure.png"))
                device.dumpWindowHierarchy(File(context.filesDir, "${javaClass.simpleName}-failure.xml"))
                println("SMS_STRESS_FAILURE: ${javaClass.simpleName} checkpoint=${database.userGameProgressDao().get(id)?.checkpoint}")
                throw failure
            }
        }
    }

    protected suspend fun awaitReaderInteractive(scenario: ActivityScenario<SmsGameActivity>) {
        val deadline = SystemClock.elapsedRealtime() + 8000
        var focused = false
        while (SystemClock.elapsedRealtime() < deadline) {
            scenario.onActivity { focused = it.hasWindowFocus() }
            if (focused) break
            delay(50)
        }
        check(focused) { "Reader never acquired window focus" }
        // A persisted gate/accessibility node can exist before the 500ms entry fade
        // and the OS activity input sink have finished. It is not yet a trusted tap target.
        delay(800)
    }

    protected suspend fun checkpoint(): GameCheckpoint? = database.userGameProgressDao().get(id)?.checkpoint

    protected suspend fun awaitCheckpoint(node: String, gate: GameCheckpoint.Gate, timeoutMs: Long = 10000): GameCheckpoint {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            val saved = checkpoint()
            if (saved?.nodeId == node && saved.gate == gate) return saved
            delay(50)
        }
        error("No progress to $node/$gate before deadline; saved=${checkpoint()}")
    }

    protected fun assertMessages(saved: GameCheckpoint, count: Int) {
        val messages = saved.messages.filterIsInstance<GameMessageText>()
        assertEquals("History was lost or duplicated", (1..count).map { "M$it" }, messages.map { it.text })
        assertEquals(messages.size, messages.map { it.id }.distinct().size)
        assertTrue("A stable checkpoint must not contain a typing bubble", saved.messages.none { it.type.name == "Typing" })
    }
}
