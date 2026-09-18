package com.purpletear.game.presentation.game_preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.model.GameActionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameActionButtonsAnimationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun downloadingToLaterChapterKeepsIncomingButtonsAtFinalWidth() {
        val actionState = mutableStateOf<GameActionState>(GameActionState.Downloading(0.6f))
        val actions = mutableListOf<GamePreviewAction>()
        val restart = string(R.string.game_presentation_game_menu_restart)
        val play = string(R.string.game_presentation_game_menu_play)
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(280.dp)) {
                    GameActionButtons(actionState.value, { actions += it }, Modifier.testTag("actions"))
                }
            }
        }
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { actionState.value = GameActionState.Play(chapterNumber = 2) }
        compose.mainClock.advanceTimeByFrame()

        val incomingWidths = mutableListOf<Float>()
        repeat(4) {
            compose.mainClock.advanceTimeBy(32)
            val row = compose.onNodeWithTag("actions").fetchSemanticsNode().boundsInRoot
            val restartBounds = compose.onNodeWithText(restart).fetchSemanticsNode().boundsInRoot
            val maxHeight = with(compose.density) { (80.dp * fontScale.coerceAtLeast(1f)).toPx() }
            assertTrue("Action row grew beyond two readable text lines: $row", row.height <= maxHeight)
            incomingWidths += restartBounds.width
        }

        compose.mainClock.advanceTimeBy(400)
        val finalWidth = compose.onNodeWithText(restart).fetchSemanticsNode().boundsInRoot.width
        incomingWidths.forEach { width ->
            assertEquals("Restart must not grow from a narrow, wrapping column", finalWidth, width, 1f)
        }
        compose.onNodeWithText(play).assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(listOf(GamePreviewAction.OnPlay), actions) }
    }

    @Test
    fun outgoingDownloadCannotBeTappedWhilePreparingFadesIn() {
        val actionState = mutableStateOf<GameActionState>(GameActionState.Download)
        val actions = mutableListOf<GamePreviewAction>()
        val download = string(R.string.game_presentation_game_menu_download_game)
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(280.dp)) {
                    GameActionButtons(actionState.value, { actions += it })
                }
            }
        }
        compose.onNodeWithText(download).assertIsEnabled()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { actionState.value = GameActionState.PreparingDownload }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeBy(32)

        compose.onNodeWithText(download)
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.runOnIdle { assertTrue("Outgoing action fired during the fade", actions.isEmpty()) }
    }

    private fun string(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
