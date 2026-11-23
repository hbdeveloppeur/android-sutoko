package fr.purpletear.sutoko.smsstress

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class SmsAutoAdvanceStressTest : AbstractSmsStressCase() {
    @Test
    fun automaticAdvanceFinishesTheNextMessageWithoutRestarting() = runBlocking {
        installChain(3)
        setMode(StoryAdvanceMode.AUTO_PLAY)
        withReader {
            assertMessages(awaitCheckpoint("end", GameCheckpoint.Gate.FINISHED), 3)
            assertTrue(device.wait(Until.hasObject(By.text("M3")), 3000))
        }
    }

    @Test
    fun zeroDelaySceneGateAdvancesEvenInManualModeWithoutCancellingTyping() = runBlocking {
        installGraph(listOf(
            """{"id":"start","type":"start","data":{}}""",
            """{"id":"mode","type":"memory-save-node","data":{"memory":{"id":"typingAnimation","name":"typingAnimation","value":"true"}}}""",
            """{"id":"scene","type":"scene-node","data":{"sceneId":1}}""",
            """{"id":"m1","type":"message","data":{"text":"M1","characterId":2,"wait":500}}""",
            """{"id":"end","type":"end","data":{}}"""
        ), listOf("start" to "mode", "mode" to "scene", "scene" to "m1", "m1" to "end").map { (from, to) ->
            """{"source":"$from","target":"$to"}"""
        })
        setMode(StoryAdvanceMode.CLICK_TO_ADVANCE)
        withReader {
            assertMessages(awaitCheckpoint("m1", GameCheckpoint.Gate.TAP), 1)
            assertTrue(device.wait(Until.hasObject(By.text("M1")), 3000))
        }
    }

    @Test
    fun longSmsHistoryFinishesWithoutLostDuplicatedOrStrandedMessages() = runBlocking {
        installChain(100, waitMs = 5)
        setMode(StoryAdvanceMode.AUTO_PLAY)
        withReader {
            assertMessages(awaitCheckpoint("end", GameCheckpoint.Gate.FINISHED, 180000), 100)
            assertTrue(device.wait(Until.hasObject(By.text("M100")), 5000))
        }
    }

    @Test
    fun seededRepeatedSessionsRetainEveryMessageAndFinish() = runBlocking {
        val args = InstrumentationRegistry.getArguments()
        val iterations = (args.getString("stressIterations")?.toInt() ?: 6).also { require(it in 1..100) }
        val seed = args.getString("stressSeed")?.toInt() ?: 20261002
        val random = Random(seed)
        setMode(StoryAdvanceMode.AUTO_PLAY)
        repeat(iterations) { iteration ->
            clearProgress()
            val count = random.nextInt(2, 5)
            val sms = iteration % 2 == 0
            installChain(count, sms, waitMs = random.nextLong(50, 250))
            println("SMS_STRESS_CASE: seed=$seed iteration=$iteration sms=$sms count=$count")
            withReader { assertMessages(awaitCheckpoint("end", GameCheckpoint.Gate.FINISHED, 15000), count) }
        }
    }

    @Test
    fun switchingToManualDuringTypingDoesNotCancelTheMessage() = runBlocking {
        installChain(3, waitMs = 1200)
        setMode(StoryAdvanceMode.AUTO_PLAY)
        withReader {
            awaitCheckpoint("m1", GameCheckpoint.Gate.TAP)
            delay(1300) // M1's 1s reading countdown expired; M2 is still typing.
            setMode(StoryAdvanceMode.CLICK_TO_ADVANCE)
            val saved = awaitCheckpoint("m2", GameCheckpoint.Gate.TAP)
            assertMessages(saved, 2)
            delay(1600)
            assertMessages(requireNotNull(checkpoint()), 2)
        }
    }
}
