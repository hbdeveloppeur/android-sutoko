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
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Run seed, force-stop the app from the host, then run verify in a new process. */
@RunWith(AndroidJUnit4::class)
class SmsProcessDeathStressTest : AbstractSmsStressCase() {
    private val phase = InstrumentationRegistry.getArguments().getString("stressProcessPhase")
    override val restoreExistingFixture get() = phase == "verify"
    override val retainFixtureForNextProcess get() = phase == "seed"

    @Test
    fun processDeathRetainsHistoryAndTheManualReadingGate() = runBlocking {
        assumeTrue("Use run_sms_stress.sh for the two-process test", phase == "seed" || phase == "verify")
        setMode(StoryAdvanceMode.CLICK_TO_ADVANCE)
        if (phase == "seed") {
            installChain(3)
            withReader {
                awaitCheckpoint("m1", GameCheckpoint.Gate.TAP)
                delay(800) // Wait for the entry shield before trusted UI input.
                device.click(device.displayWidth / 2, device.displayHeight * 4 / 5)
                assertMessages(awaitCheckpoint("m2", GameCheckpoint.Gate.TAP), 2)
            }
            println("SMS_PROCESS_DEATH_SEEDED: m2/TAP with exactly two messages")
        } else {
            assertMessages(requireNotNull(checkpoint()), 2)
            withReader {
                assertMessages(awaitCheckpoint("m2", GameCheckpoint.Gate.TAP), 2)
                assertTrue(device.wait(Until.hasObject(By.text("M2")), 5000))
                delay(800)
                device.click(device.displayWidth / 2, device.displayHeight * 4 / 5)
                assertMessages(awaitCheckpoint("m3", GameCheckpoint.Gate.TAP), 3)
            }
            println("SMS_PROCESS_DEATH_VERIFIED: persisted position resumed and advanced normally")
        }
    }
}
