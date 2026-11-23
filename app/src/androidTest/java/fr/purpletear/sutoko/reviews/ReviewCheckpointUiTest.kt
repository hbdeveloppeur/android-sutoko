package fr.purpletear.sutoko.reviews

import android.content.Intent
import android.os.SystemClock
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
import com.purpletear.game.presentation.game_play.SmsGameActivity
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import com.purpletear.sutoko.game.model.game.GameMetadata
import dagger.hilt.android.EntryPointAccessors
import fr.purpletear.sutoko.screens.PlayerOnlyPreviewDependencies
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ReviewCheckpointUiTest {
    @Test fun closingAndReopeningTheRealReaderKeepsItsChoiceMessagesAndReadingGate() = runBlocking {
        val instrumentation=InstrumentationRegistry.getInstrumentation();val context=instrumentation.targetContext
        val entry=EntryPointAccessors.fromApplication(context.applicationContext,PlayerOnlyPreviewDependencies::class.java)
        val db=entry.database();val preferences=entry.gamePreferences()
        val key=stringPreferencesKey("story_advance_mode");val original=preferences.data.first()[key]
        val id="review_checkpoint_ui_fixture"
        val dir=File(context.filesDir,"games/$id");dir.deleteRecursively()
        val chapter=File(dir,"chapters/${Locale.getDefault().toLanguageTag()}/8a").apply {mkdirs()}
        chapter.resolve("nodes.json").writeText("""[
            {"id":"start","type":"start","data":{}},
            {"id":"stay","type":"message","data":{"text":"Stay with Yumi","characterId":1}},
            {"id":"leave","type":"message","data":{"text":"Leave Yumi","characterId":1}},
            {"id":"remember","type":"memory-save-node","data":{"memory":{"id":"IStayed","name":"IStayed","value":"true"}}},
            {"id":"after","type":"message","data":{"text":"Checkpoint retained after reopening","characterId":2}},
            {"id":"end","type":"end","data":{}}
        ]""")
        chapter.resolve("edges.json").writeText("""[
            {"source":"start","target":"stay"},{"source":"start","target":"leave"},
            {"source":"stay","target":"remember"},{"source":"remember","target":"after"},
            {"source":"leave","target":"after"},{"source":"after","target":"end"}
        ]""")
        File(dir,"characters").apply {mkdirs()}.resolve("characters.json").writeText("""[
            {"id":1,"name":"Alex","isMainCharacter":true,"colorStartingCode":"#FFFFFF","colorEndingCode":"#000000"},
            {"id":2,"name":"Yumi","isMainCharacter":false,"colorStartingCode":"#FFFFFF","colorEndingCode":"#000000"}
        ]""")
        val device=UiDevice.getInstance(instrumentation)
        val intent=Intent(context,SmsGameActivity::class.java).putExtra("gameId",id).putExtra("chapterCode","8A")
        try {
            preferences.edit {it[key]=StoryAdvanceMode.CLICK_TO_ADVANCE.name}
            db.gameDao().upsertAll(listOf(GameCatalogEntity(id=id,version=1,isOfficial=true,
                metadata=GameMetadata("Checkpoint fixture"))))
            db.gameInstallationDao().upsert(GameInstallEntity(id,1))
            db.chapterDao().insert(ChapterEntity(id="$id-8",number=8,alternative="A",story=id,code="8A",available=true))
            var savedMessages=0
            ActivityScenario.launch<SmsGameActivity>(intent).use {
                if (!device.wait(Until.hasObject(By.res("game_make_a_choice_button")),15000)) {
                    device.takeScreenshot(File(context.filesDir,"review-checkpoint-entry-failure.png"))
                    device.dumpWindowHierarchy(File(context.filesDir,"review-checkpoint-entry-failure.xml"))
                    println("CHECKPOINT_ENTRY_DIAGNOSTIC: " + db.userGameProgressDao().get(id)?.checkpoint)
                    fail("Reader did not expose its first choice gate")
                }
                SystemClock.sleep(800) // Wait for the reader entry fade before trusted UI input.
                device.findObject(By.res("game_make_a_choice_button")).click()
                if (!device.wait(Until.hasObject(By.text("Stay with Yumi")),5000)) {
                    device.takeScreenshot(File(context.filesDir,"review-checkpoint-failure.png"))
                    device.dumpWindowHierarchy(File(context.filesDir,"review-checkpoint-failure.xml"))
                    println("CHECKPOINT_UI_DIAGNOSTIC: " + db.userGameProgressDao().get(id)?.checkpoint)
                    fail("Choices did not open after the reader entry transition")
                }
                SystemClock.sleep(400)
                device.findObject(By.text("Stay with Yumi")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Checkpoint retained after reopening")),5000))
                val saved=awaitCheckpoint {db.userGameProgressDao().get(id)?.checkpoint}
                assertEquals("true",saved.memories["IStayed"]?.value);assertEquals("after",saved.nodeId)
                savedMessages=saved.messages.size
            }
            ActivityScenario.launch<SmsGameActivity>(intent).use {
                assertTrue(device.wait(Until.hasObject(By.text("Checkpoint retained after reopening")),10000))
                assertTrue(device.hasObject(By.text("Stay with Yumi")))
                assertFalse(device.hasObject(By.res("game_make_a_choice_button")))
                val restored=requireNotNull(db.userGameProgressDao().get(id)?.checkpoint)
                assertEquals("true",restored.memories["IStayed"]?.value)
                assertEquals("after",restored.nodeId);assertEquals(savedMessages,restored.messages.size)
                println("READER_CHECKPOINT_UI_OK: choice, reading position and history survived a new Activity/ViewModel")
            }
        } finally {
            preferences.edit {if(original==null)it.remove(key) else it[key]=original}
            db.userGameProgressDao().delete(id);db.memoryDao().deleteAllForGame(id)
            db.chapterDao().deleteAllForStory(id);db.gameInstallationDao().deleteByGameId(id)
            db.openHelper.writableDatabase.execSQL("DELETE FROM games WHERE id = ?",arrayOf(id))
            dir.deleteRecursively()
        }
    }

    private suspend fun awaitCheckpoint(load:suspend ()->GameCheckpoint?):GameCheckpoint {
        repeat(50){val checkpoint=load();if(checkpoint?.nodeId=="after")return checkpoint; kotlinx.coroutines.delay(100)}
        error("Reader did not commit the chosen branch")
    }
}
