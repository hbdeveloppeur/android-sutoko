package fr.purpletear.sutoko.screens.main.presentation.screens.home

import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.transition.Transition
import com.purpletear.game.presentation.game_catalog.bannerImageRequest
import com.purpletear.game.presentation.game_catalog.verticalBannerImageRequest
import com.purpletear.sutoko.game.model.Asset
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeImageMotionTest {
    @Test
    fun disabledSystemAnimationsAlsoDisableStoryImageCrossfades() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val key = Settings.Global.ANIMATOR_DURATION_SCALE
        val previous = Settings.Global.getString(context.contentResolver, key)
        fun shell(command: String) {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
                .use { it.readBytes() }
        }
        try {
            shell("settings put global $key 0")
            assertEquals(0f, Settings.Global.getFloat(context.contentResolver, key), 0f)
            val asset = Asset(
                id = 1, originalFilename = "motion-test.png", width = 100, height = 100,
                createdAt = 0, fileSizeBytes = 0, mimeType = "image/png",
                storagePath = "motion-test.png", thumbnailStoragePath = "motion-test.png",
            )
            val story = GameCatalog(
                id = "motion-test", metadata = GameMetadata(title = "Motion test"),
                banner = asset, verticalBanner = asset, canvasTechnologyRequiredVersion = 1,
            )
            assertSame(Transition.Factory.NONE, requireNotNull(story.bannerImageRequest(context)).transitionFactory)
            assertSame(Transition.Factory.NONE, requireNotNull(story.verticalBannerImageRequest(context)).transitionFactory)
        } finally {
            shell(if (previous == null) "settings delete global $key" else "settings put global $key $previous")
        }
    }
}
