package fr.purpletear.sutoko.screens

import android.content.Intent
import android.net.Uri
import java.io.File
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.purpletear.game.data.local.entity.ChapterEntity
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.local.entity.GameInstallEntity
import com.purpletear.game.data.local.entity.UserGameProgressEntity
import com.purpletear.game.presentation.game_preview.GamePreviewDeepLink
import com.purpletear.sutoko.game.model.game.GameMetadata
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerOnlyGamePreviewTest {
    @Test
    fun testerAndStoredAdministratorCannotRevealToolsOrLaunchLockedChapter() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val dependencies = EntryPointAccessors.fromApplication(context, PlayerOnlyPreviewDependencies::class.java)
        val database = dependencies.database()
        val users = dependencies.users()
        val preferences = dependencies.gamePreferences()
        val originalUser = users.observeUser().first()
        val roleKey = stringPreferencesKey("user_role")
        val ownerKey = stringPreferencesKey("user_role_owner")
        val originalPreferences = preferences.data.first()
        val storyId = "test-player-only-preview"
        val testerId = "8be954c7a18f4e7cba9c"
        val progress = UserGameProgressEntity(storyId, "1A", "1a", "Alex")
        val device = UiDevice.getInstance(instrumentation)
        try {
            users.connect(testerId, "local-ui-test-token").getOrThrow()
            preferences.edit { it[roleKey] = "ADMINISTRATOR"; it[ownerKey] = testerId }
            database.gameDao().upsertAll(listOf(GameCatalogEntity(
                id = storyId, version = 1, metadata = GameMetadata("Player-only test story"),
            )))
            database.gameInstallationDao().upsert(GameInstallEntity(storyId, 1))
            database.chapterDao().insert(ChapterEntity(
                id = "$storyId-1", story = storyId, number = 1, code = "1A",
                title = "Locked test chapter", releaseDate = 4_102_444_800L, available = false,
            ))
            database.userGameProgressDao().save(progress)
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(GamePreviewDeepLink.url(storyId))
            }
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                val favoriteDescription = context.getString(com.purpletear.game.presentation.R.string.game_presentation_game_preview_add_favorite)
                val previewVisible = device.wait(Until.hasObject(By.desc(favoriteDescription)), 30_000)
                if (!previewVisible) {
                    device.dumpWindowHierarchy(File(context.cacheDir, "player-preview-failure.xml"))
                    device.takeScreenshot(File(context.cacheDir, "player-preview-failure.png"))
                }
                assertTrue("GamePreview did not render", previewVisible)
                assertEquals(testerId, users.observeUser().first()?.id)
                assertNoDeveloperControls(device)
                val playText = context.getString(com.purpletear.game.presentation.R.string.game_presentation_game_menu_play)
                val play = requireNotNull(device.wait(Until.findObject(By.text(playText)), 10_000))
                play.click()
                device.waitForIdle()
                assertTrue("Locked chapter launched instead of staying on GamePreview", device.hasObject(By.desc(favoriteDescription)))
                assertEquals(progress, database.userGameProgressDao().get(storyId))

                users.disconnect().getOrThrow()
                device.waitForIdle()
                assertNoDeveloperControls(device)
                users.connect("ordinary-test-player", "local-ui-test-token").getOrThrow()
                scenario.recreate()
                assertTrue(device.wait(Until.hasObject(By.desc(favoriteDescription)), 15_000))
                assertNoDeveloperControls(device)
                assertEquals(progress, database.userGameProgressDao().get(storyId))
            }
        } finally {
            preferences.edit {
                originalPreferences[roleKey]?.let { value -> it[roleKey] = value } ?: it.remove(roleKey)
                originalPreferences[ownerKey]?.let { value -> it[ownerKey] = value } ?: it.remove(ownerKey)
            }
            if (originalUser == null) users.disconnect() else users.connect(originalUser.id, originalUser.token)
            database.chapterDao().deleteAllForStory(storyId)
            database.userGameProgressDao().delete(storyId)
            database.gameInstallationDao().deleteByGameId(storyId)
            database.openHelper.writableDatabase.execSQL("DELETE FROM games WHERE id = ?", arrayOf(storyId))
        }
    }

    private fun assertNoDeveloperControls(device: UiDevice) {
        assertFalse(device.hasObject(By.desc("Story options")))
        assertFalse(device.hasObject(By.text("Download preview")))
        assertFalse(device.hasObject(By.text("Administrator")))
    }
}
