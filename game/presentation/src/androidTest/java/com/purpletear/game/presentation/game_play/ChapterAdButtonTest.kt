package com.purpletear.game.presentation.game_play

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.purpletear.game.presentation.game_play.components.message.MessageNextChapter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ChapterAdButtonTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun requiredAdShowsOnlyUnlockButton() {
        var actions = 0
        compose.setContent {
            MessageNextChapter(requiresAd = true, onClick = { actions++ })
        }
        compose.onNodeWithTag("game_next_chapter_ads_button").assertIsDisplayed().performClick()
        compose.onNodeWithTag("game_next_chapter_button").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, actions) }
    }

    @Test
    fun noRequiredAdShowsOnlyContinueButton() {
        var actions = 0
        compose.setContent {
            MessageNextChapter(requiresAd = false, onClick = { actions++ })
        }
        compose.onNodeWithTag("game_next_chapter_button").assertIsDisplayed().performClick()
        compose.onNodeWithTag("game_next_chapter_ads_button").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, actions) }
    }

    @Test
    fun busyStateRemovesBothActionsAndIgnoresFurtherTaps() {
        val busy = mutableStateOf(false)
        var actions = 0
        compose.setContent {
            MessageNextChapter(
                modifier = Modifier.testTag("chapter_end"),
                requiresAd = true,
                isBusy = busy.value,
                onClick = {
                    actions++
                    busy.value = true
                },
            )
        }
        compose.onNodeWithTag("game_next_chapter_ads_button").performClick()
        compose.onNodeWithTag("game_next_chapter_ads_button").assertDoesNotExist()
        compose.onNodeWithTag("game_next_chapter_button").assertDoesNotExist()
        compose.onNodeWithTag("chapter_end").performTouchInput {
            click()
            click()
        }
        compose.runOnIdle { assertEquals(1, actions) }
    }
}
