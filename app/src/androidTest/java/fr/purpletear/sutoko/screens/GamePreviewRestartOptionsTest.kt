package fr.purpletear.sutoko.screens

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.purpletear.game.data.local.entity.ChapterEntity
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.local.entity.GameInstallEntity
import com.purpletear.game.data.local.entity.MemoryEntity
import com.purpletear.game.data.local.entity.UserGameProgressEntity
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.GamePreviewDeepLink
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.model.game.GameMetadata
import dagger.hilt.android.EntryPointAccessors
import java.util.Locale
import java.io.File
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GamePreviewRestartOptionsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private val database = EntryPointAccessors.fromApplication(context, PlayerOnlyPreviewDependencies::class.java).database()
    private fun text(id: Int) = context.getString(id)

    @Test
    fun menuCancellationChapterRestartAndFullRestartUseRealSavedProgress() = runBlocking {
        val id = "preview-restart-ui"
        val checkpoint = GameCheckpoint("3B", "graph", "saved-node", GameCheckpoint.Gate.TAP, emptyMap(), emptyList())
        val progress = UserGameProgressEntity(id, "3B", "3b", "Alex", checkpoint)
        val install = GameInstallEntity(id, 1)
        try {
            seed(id, legacyId = 160)
            database.gameInstallationDao().upsert(install)
            database.userGameProgressDao().save(progress)
            database.memoryDao().insertAll(listOf(
                MemoryEntity(id, "relationship", "earlier-choice", 1),
                MemoryEntity(id, "relationship", "current-choice", 3),
                MemoryEntity(id, "future", "discard", 4),
            ))
            launch(id).use {
                awaitPreview()
                openOptions()
                device.takeScreenshot(File(context.cacheDir, "restart-options.png"))
                device.dumpWindowHierarchy(File(context.cacheDir, "restart-options.xml"))
                clickDescription(R.string.game_presentation_options_close)
                awaitPreview()
                assertEquals(progress, database.userGameProgressDao().get(id))

                openOptions()
                clickText(R.string.game_presentation_options_restart_chapter)
                assertTrue(device.wait(Until.hasObject(By.text(context.getString(R.string.game_presentation_options_confirm_chapter, 3))), 5_000))
                clickText(android.R.string.cancel)
                assertEquals(progress, database.userGameProgressDao().get(id))
                assertEquals(2, database.memoryDao().getAllForGameUpToChapter(id, 4).size)
                assertTrue(device.wait(Until.hasObject(By.text(text(R.string.game_presentation_options_restart_game))), 5_000))

                clickText(R.string.game_presentation_options_restart_chapter)
                assertTrue(device.wait(Until.hasObject(By.text(context.getString(R.string.game_presentation_options_confirm_chapter, 3))), 5_000))
                device.takeScreenshot(File(context.cacheDir, "restart-ux-confirmation.png"))
                clickText(R.string.game_presentation_game_restart_confirm_button)
                await { database.userGameProgressDao().get(id)?.checkpoint == null }
                assertTrue(device.wait(Until.hasObject(By.text(text(R.string.game_presentation_options_chapter_restarted))), 5_000))
                assertEquals(progress.copy(checkpoint = null), database.userGameProgressDao().get(id))
                assertEquals(listOf(MemoryEntity(id, "relationship", "earlier-choice", 1)),
                    database.memoryDao().getAllForGameUpToChapter(id, 4))

                openOptions()
                clickText(R.string.game_presentation_options_restart_game)
                clickText(android.R.string.cancel)
                assertEquals(progress.copy(checkpoint = null), database.userGameProgressDao().get(id))
                assertTrue(device.wait(Until.hasObject(By.text(text(R.string.game_presentation_options_restart_chapter))), 5_000))

                clickText(R.string.game_presentation_options_restart_game)
                clickText(R.string.game_presentation_game_restart_confirm_button)
                await { database.userGameProgressDao().get(id) == null }
                assertTrue(device.wait(Until.hasObject(By.text(text(R.string.game_presentation_options_game_restarted))), 5_000))
                device.takeScreenshot(File(context.cacheDir, "restart-ux-feedback.png"))
                assertTrue(database.memoryDao().getAllForGameUpToChapter(id, 4).isEmpty())
                assertEquals(install, database.gameInstallationDao().observeByGameId(id).first())
                awaitPreview()
            }
        } finally {
            clean(id)
        }
    }

    @Test
    fun friendzonedPreviewsDoNotExposeTheNewMenu() = runBlocking {
        for (legacyId in listOf(159, 161, 162, 163)) {
            val id = "preview-no-options-$legacyId"
            try {
                seed(id, legacyId)
                launch(id).use {
                    val favorite = text(R.string.game_presentation_game_preview_add_favorite)
                    assertTrue(device.wait(Until.hasObject(By.desc(favorite)), 15_000))
                    assertFalse(device.hasObject(By.desc(text(R.string.game_presentation_game_preview_options))))
                }
            } finally {
                clean(id)
            }
        }
    }

    @Test
    fun unavailableChapterCannotBeRestartedButGameCan() = runBlocking {
        val id = "preview-unavailable-chapter"
        try {
            seed(id, available = false)
            database.userGameProgressDao().save(UserGameProgressEntity(id, "3B", "3b", "Alex"))
            launch(id).use {
                awaitPreview()
                openOptions()
                val chapterLabel = requireNotNull(device.findObject(By.text(text(R.string.game_presentation_options_restart_chapter))))
                assertFalse(chapterLabel.parent.isEnabled)
                assertTrue(device.hasObject(By.text(text(R.string.game_presentation_options_chapter_unavailable))))
                assertTrue(requireNotNull(device.findObject(By.text(text(R.string.game_presentation_options_restart_game)))).parent.isEnabled)
                chapterLabel.click()
                device.waitForIdle()
                assertFalse(device.hasObject(By.text(context.getString(R.string.game_presentation_options_confirm_chapter, 3))))
                device.pressBack()
                awaitPreview()
                assertEquals("3B", database.userGameProgressDao().get(id)?.currentChapterCode)
            }
        } finally {
            clean(id)
        }
    }

    @Test
    fun successShortcutPlaysTheRestartedChapterWithoutOpeningTheMenuAgain() = runBlocking {
        val id = "preview-restart-play-shortcut"
        val directory = File(context.filesDir, "games/$id")
        val preferences = EntryPointAccessors.fromApplication(context, PlayerOnlyPreviewDependencies::class.java).gamePreferences()
        val modeKey = stringPreferencesKey("story_advance_mode")
        val originalMode = preferences.data.first()[modeKey]
        try {
            seed(id)
            database.userGameProgressDao().save(UserGameProgressEntity(id, "3B", "3b", "Alex",
                GameCheckpoint("3B", "old-graph", "old-position", GameCheckpoint.Gate.TAP, emptyMap(), emptyList())))
            val chapter = File(directory, "chapters/${Locale.getDefault().toLanguageTag()}/3b").apply { mkdirs() }
            chapter.resolve("nodes.json").writeText("""[
                {"id":"start","type":"start","data":{}},
                {"id":"opening","type":"message","data":{"text":"The restarted chapter begins here.","characterId":2}},
                {"id":"end","type":"end","data":{}}
            ]""")
            chapter.resolve("edges.json").writeText("""[
                {"source":"start","target":"opening"},{"source":"opening","target":"end"}
            ]""")
            File(directory, "characters").apply { mkdirs() }.resolve("characters.json").writeText("""[
                {"id":1,"name":"Alex","isMainCharacter":true,"colorStartingCode":"#FFFFFF","colorEndingCode":"#000000"},
                {"id":2,"name":"Yumi","isMainCharacter":false,"colorStartingCode":"#FFFFFF","colorEndingCode":"#000000"}
            ]""")
            preferences.edit { it[modeKey] = StoryAdvanceMode.CLICK_TO_ADVANCE.name }
            launch(id).use {
                awaitPreview()
                openOptions()
                clickText(R.string.game_presentation_options_restart_chapter)
                clickText(R.string.game_presentation_game_restart_confirm_button)
                val message = requireNotNull(device.wait(Until.findObject(By.text(text(R.string.game_presentation_options_chapter_restarted))), 5_000))
                var feedback = message
                val play = By.text(text(R.string.game_presentation_game_menu_play))
                while (feedback.findObject(play) == null && feedback.parent != null) feedback = feedback.parent
                requireNotNull(feedback.findObject(play)) { "Success feedback has no Play shortcut" }.click()
                assertTrue("Play shortcut did not open the chapter at its beginning",
                    device.wait(Until.hasObject(By.text("The restarted chapter begins here.")), 15_000))
                assertEquals("3B", database.userGameProgressDao().get(id)?.currentChapterCode)
            }
        } finally {
            preferences.edit { if (originalMode == null) it.remove(modeKey) else it[modeKey] = originalMode }
            directory.deleteRecursively()
            clean(id)
        }
    }

    private fun launch(id: String) = ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse(GamePreviewDeepLink.url(id))
    })

    private fun awaitPreview() {
        assertTrue("GamePreview options button missing", device.wait(Until.hasObject(By.desc(text(R.string.game_presentation_game_preview_options))), 20_000))
    }

    private fun openOptions() {
        clickDescription(R.string.game_presentation_game_preview_options)
        assertTrue(device.wait(Until.hasObject(By.text(text(R.string.game_presentation_options_restart_game))), 5_000))
    }

    private fun clickText(id: Int) {
        requireNotNull(device.wait(Until.findObject(By.text(text(id))), 5_000)) { "Missing text: ${text(id)}" }.click()
        device.waitForIdle()
    }

    private fun clickDescription(id: Int) {
        requireNotNull(device.wait(Until.findObject(By.desc(text(id))), 5_000)) { "Missing control: ${text(id)}" }.click()
        device.waitForIdle()
    }

    private suspend fun await(condition: suspend () -> Boolean) {
        repeat(50) {
            if (condition()) return
            delay(100)
        }
        fail("Saved progress did not update")
    }

    private suspend fun seed(id: String, legacyId: Int? = null, available: Boolean = true) {
        database.gameInstallationDao().upsert(GameInstallEntity(id, 1))
        database.gameDao().upsertAll(listOf(GameCatalogEntity(id = id, version = 1, legacyId = legacyId,
            metadata = GameMetadata("The quiet promise"))))
        database.chapterDao().insert(ChapterEntity(id = "$id-1", story = id, number = 1, code = "1A", available = true))
        database.chapterDao().insert(ChapterEntity(id = "$id-3", story = id, number = 3, code = "3B", title = "A second chance", available = available,
            releaseDate = if (available) 0L else 4_102_444_800L))
    }

    private suspend fun clean(id: String) {
        database.userGameProgressDao().delete(id)
        database.memoryDao().deleteAllForGame(id)
        database.chapterDao().deleteAllForStory(id)
        database.gameInstallationDao().deleteByGameId(id)
        database.openHelper.writableDatabase.execSQL("DELETE FROM games WHERE id = ?", arrayOf(id))
    }
}
