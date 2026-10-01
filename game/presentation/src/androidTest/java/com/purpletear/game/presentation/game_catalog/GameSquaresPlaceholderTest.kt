package com.purpletear.game.presentation.game_catalog

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameMetadata
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameSquaresPlaceholderTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun placeholdersMatchStoryRowHeightAtEachFontScale() {
        val loading = mutableStateOf(true)
        val fontScale = mutableStateOf(1f)
        val stories = (1..4).map {
            GameCatalog(id = "$it", metadata = GameMetadata(title = "Story $it"), canvasTechnologyRequiredVersion = 1)
        }
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale.value)) {
                if (loading.value) GameSquaresPlaceholder(Modifier.testTag("row"))
                else GameSquares(stories = stories, icons = emptyMap(), onTap = {}, modifier = Modifier.testTag("row"))
            }
        }
        for (scale in listOf(1f, 1.5f, 2f)) {
            compose.runOnIdle {
                fontScale.value = scale
                loading.value = true
            }
            val placeholderHeight = compose.onNodeWithTag("row").fetchSemanticsNode().boundsInRoot.height
            compose.runOnIdle { loading.value = false }
            val contentHeight = compose.onNodeWithTag("row").fetchSemanticsNode().boundsInRoot.height
            assertEquals("Story row jumps at font scale $scale", placeholderHeight, contentHeight, 1f)
        }
    }
}
