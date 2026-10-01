package fr.purpletear.sutoko.screens.main.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.rememberHomeLoadingVisible
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeLoadingTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun quickCacheReadDoesNotFlashLoadingPlaceholders() {
        val loading = mutableStateOf(true)
        var placeholderWasVisible = false
        compose.setContent {
            val visible = rememberHomeLoadingVisible(loading.value)
            SideEffect { if (visible) placeholderWasVisible = true }
            if (visible) Box(Modifier.testTag("loading"))
        }
        compose.runOnIdle { loading.value = false }
        compose.onNodeWithTag("loading").assertDoesNotExist()
        // A cancelled loading job must not resurrect the placeholders later.
        Thread.sleep(200)
        compose.onNodeWithTag("loading").assertDoesNotExist()
        compose.runOnIdle { assertFalse("Fast cache read flashed a placeholder", placeholderWasVisible) }
    }

    @Test
    fun slowLoadShowsPlaceholdersAndHidesThemAsSoonAsContentArrives() {
        val loading = mutableStateOf(true)
        compose.setContent {
            if (rememberHomeLoadingVisible(loading.value)) Box(Modifier.testTag("loading"))
        }
        compose.waitUntil(timeoutMillis = 3_000) {
            compose.onAllNodesWithTag("loading").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("loading").assertExists()
        compose.runOnIdle { loading.value = false }
        compose.onNodeWithTag("loading").assertDoesNotExist()
        compose.runOnIdle { loading.value = true }
        compose.onNodeWithTag("loading").assertDoesNotExist()
        compose.waitUntil(timeoutMillis = 3_000) {
            compose.onAllNodesWithTag("loading").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
