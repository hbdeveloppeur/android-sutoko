package fr.purpletear.sutoko.smsstress

import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmsHoldLifecycleStressTest : AbstractSmsStressCase() {
    @Test
    fun recreatingWhileFingerIsHeldReleasesTheTimingGate() = runBlocking {
        installChain(3)
        setMode(StoryAdvanceMode.CLICK_TO_ADVANCE)
        withReader { scenario ->
            awaitCheckpoint("m1", GameCheckpoint.Gate.TAP)
            setMode(StoryAdvanceMode.AUTO_PLAY)
            delay(100)
            val downAt = SystemClock.uptimeMillis()
            sendTouch(MotionEvent.ACTION_DOWN, downAt)
            delay(200)
            scenario.recreate()
            awaitReaderInteractive(scenario)
            sendTouch(MotionEvent.ACTION_UP, downAt)
            assertMessages(awaitCheckpoint("end", GameCheckpoint.Gate.FINISHED, 12000), 3)
        }
    }

    private fun sendTouch(action: Int, downAt: Long) {
        val event = MotionEvent.obtain(downAt, SystemClock.uptimeMillis(), action,
            device.displayWidth * 0.5f, device.displayHeight * 0.8f, 0)
        try { instrumentation.sendPointerSync(event) } finally { event.recycle() }
    }
}
