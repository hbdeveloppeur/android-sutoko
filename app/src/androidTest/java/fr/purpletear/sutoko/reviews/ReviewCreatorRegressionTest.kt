package fr.purpletear.sutoko.reviews

import android.content.Intent
import java.io.File
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import fr.purpletear.sutoko.screens.MainActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewCreatorRegressionTest {
    @Test fun creatorPageOpensWithoutAnUnavailableUpdatePrompt() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        val device=UiDevice.getInstance(instrumentation)
        // MainActivity replaces its launch Intent on navigation; ActivityScenario's Intent-based
        // lifecycle tracking cannot follow that. Keep ownership of this launch directly instead.
        val activity=instrumentation.startActivitySync(Intent(context,MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as MainActivity
        try {
            instrumentation.runOnMainSync {
                MainActivity::class.java.getDeclaredMethod("onNewIntent",Intent::class.java).apply {isAccessible=true}
                    .invoke(activity,Intent(activity.intent).putExtra("destination","create_story"))
            }
            if (!device.wait(Until.hasObject(By.text("sutoko.com")),15000)) {
                device.takeScreenshot(File(context.filesDir,"review-creator-failure.png"))
                device.dumpWindowHierarchy(File(context.filesDir,"review-creator-failure.xml"))
                fail("Creator page not visible")
            }
            assertFalse(device.hasObject(By.text("Mettre à jour")))
            println("CREATOR_PAGE_OK: normal creator page, no update prompt")
        } finally {
            instrumentation.runOnMainSync {activity.finish()}
            assertTrue("Creator window did not close",device.wait(Until.gone(By.text("sutoko.com")),5000))
        }
    }
}
