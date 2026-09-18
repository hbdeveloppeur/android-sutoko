package com.purpletear.game.presentation.game_preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.purpletear.game.presentation.game_preview.components.GamePreviewButton
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GamePreviewButtonProgressTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun knownProgressExposesDeterminateRangeAndBlocksActions() {
        val progress = mutableStateOf(0.25f)
        var clicks = 0
        compose.setContent {
            MaterialTheme {
                Box(Modifier.width(280.dp)) {
                    GamePreviewButton(
                        modifier = Modifier.testTag("progressButton"),
                        title = "Downloading",
                        progress = progress.value,
                        onClick = { clicks++ },
                    )
                }
            }
        }
        val progressSemantics = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        compose.onAllNodes(progressSemantics, useUnmergedTree = true).assertCountEquals(1)
        compose.onNode(progressSemantics, useUnmergedTree = true).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(0.25f, 0f..1f),
            )
        )
        compose.onNodeWithTag("progressButton")
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.runOnIdle { assertEquals(0, clicks) }

        compose.mainClock.autoAdvance = false
        compose.runOnIdle { progress.value = 0.8f }
        compose.mainClock.advanceTimeBy(250)
        compose.onNode(progressSemantics, useUnmergedTree = true).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(0.8f, 0f..1f),
            )
        )
    }
}
