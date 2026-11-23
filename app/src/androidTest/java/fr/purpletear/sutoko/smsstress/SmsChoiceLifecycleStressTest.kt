package fr.purpletear.sutoko.smsstress

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmsChoiceLifecycleStressTest : AbstractSmsStressCase() {
    @Test
    fun choiceRecreationRapidTapsAndReopeningKeepOnlyTheChosenBranch() = runBlocking {
        installGraph(listOf(
            """{"id":"start","type":"start","data":{}}""",
            """{"id":"a","type":"message","data":{"text":"Stress branch A","characterId":1}}""",
            """{"id":"b","type":"message","data":{"text":"Stress branch B","characterId":1}}""",
            """{"id":"rememberA","type":"memory-save-node","data":{"memory":{"id":"branch","name":"branch","value":"A"}}}""",
            """{"id":"rememberB","type":"memory-save-node","data":{"memory":{"id":"branch","name":"branch","value":"B"}}}""",
            """{"id":"after","type":"message","data":{"text":"Stress branch committed","characterId":2}}""",
            """{"id":"end","type":"end","data":{}}"""
        ), listOf(
            """{"source":"start","target":"a"}""", """{"source":"start","target":"b"}""",
            """{"source":"a","target":"rememberA"}""", """{"source":"b","target":"rememberB"}""",
            """{"source":"rememberA","target":"after"}""", """{"source":"rememberB","target":"after"}""",
            """{"source":"after","target":"end"}"""
        ))
        setMode(StoryAdvanceMode.CLICK_TO_ADVANCE)
        withReader { scenario ->
            awaitCheckpoint("start", GameCheckpoint.Gate.CHOICE)
            // Recreate the same ViewModel repeatedly at a parked choice gate.
            repeat(5) {
                scenario.recreate()
                assertTrue(device.wait(Until.hasObject(By.res("game_make_a_choice_button")), 5000))
                awaitReaderInteractive(scenario)
            }
            device.findObject(By.res("game_make_a_choice_button")).click()
            assertTrue(device.wait(Until.hasObject(By.text("Stress branch B")), 5000))
            delay(400)
            val target = device.findObject(By.text("Stress branch B")).visibleCenter
            repeat(12) { device.click(target.x, target.y) }
            val saved = awaitCheckpoint("end", GameCheckpoint.Gate.FINISHED)
            assertEquals("B", saved.memories["branch"]?.value)
            assertEquals(listOf("Stress branch B", "Stress branch committed"),
                saved.messages.filterIsInstance<GameMessageText>().map { it.text })
            assertFalse(device.hasObject(By.text("Stress branch A")))
        }
        // Remaining taps can legitimately advance the reader after the choice disappears.
        // A fresh Activity/ViewModel must still retain exactly the committed branch.
        repeat(4) {
            withReader {
                assertTrue(device.wait(Until.hasObject(By.text("Stress branch committed")), 5000))
                val saved = awaitCheckpoint("end", GameCheckpoint.Gate.FINISHED)
                assertEquals("B", saved.memories["branch"]?.value)
                assertEquals(2, saved.messages.filterIsInstance<GameMessageText>().size)
                assertFalse(device.hasObject(By.res("game_make_a_choice_button")))
            }
        }
    }
}
