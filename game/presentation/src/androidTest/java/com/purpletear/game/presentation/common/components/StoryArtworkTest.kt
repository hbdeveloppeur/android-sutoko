package com.purpletear.game.presentation.common.components

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.imageLoader
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoryArtworkTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var previousLoader: ImageLoader
    private lateinit var testLoader: ImageLoader
    private val titleLoaded = CompletableDeferred<Unit>()

    @Before
    fun installControlledImageLoader() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        previousLoader = context.imageLoader
        testLoader = ImageLoader.Builder(context).components {
            add(object : Interceptor {
                override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
                    val color = when (chain.request.data) {
                        "background" -> android.graphics.Color.RED
                        "title" -> {
                            titleLoaded.await()
                            android.graphics.Color.BLUE
                        }
                        else -> return chain.proceed(chain.request)
                    }
                    return SuccessResult(
                        drawable = ColorDrawable(color),
                        request = chain.request,
                        dataSource = DataSource.NETWORK,
                    )
                }
            })
        }.build()
        Coil.setImageLoader(testLoader)
    }

    @After
    fun restoreImageLoader() {
        Coil.setImageLoader(previousLoader)
        testLoader.shutdown()
    }

    @Test
    fun backgroundDoesNotAppearSeparatelyFromTheTitle() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                StoryArtwork(
                    imageUrl = "background",
                    titleUrl = "title",
                    title = "Story",
                    modifier = Modifier.size(100.dp),
                    titleModifier = Modifier.size(50.dp),
                )
            }
        }
        assertTrue("Background appeared before title was ready", pixel(0.75f).red < 0.05f)
        compose.runOnIdle { titleLoaded.complete(Unit) }
        compose.mainClock.advanceTimeBy(500)
        assertTrue("Background did not reveal", pixel(0.75f).red > 0.99f)
        assertTrue("Title did not reveal with background", pixel(0.25f).blue > 0.99f)
    }

    @Test
    fun failedImagesRevealReadableFallbackAndBadges() {
        compose.setContent {
            Box(Modifier.size(120.dp).background(Color.Black).testTag("surface")) {
                StoryArtwork(
                    imageUrl = "file:///missing-background.png",
                    titleUrl = "file:///missing-title.png",
                    title = "Offline story",
                    modifier = Modifier.size(120.dp),
                ) {
                    Box(Modifier.size(20.dp).background(Color.White).testTag("badge"))
                }
            }
        }
        compose.waitUntil(timeoutMillis = 3_000) {
            val pixels = compose.onNodeWithTag("badge").captureToImage().toPixelMap()
            pixels[pixels.width / 2, pixels.height / 2].red > 0.99f
        }
        compose.onNodeWithText("Offline story").assertIsDisplayed()
    }

    private fun pixel(fraction: Float): Color {
        val pixels = compose.onNodeWithTag("surface").captureToImage().toPixelMap()
        return pixels[(pixels.width * fraction).toInt(), (pixels.height * fraction).toInt()]
    }
}
