package com.purpletear.game.presentation.game_play

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import com.purpletear.game.presentation.game_play.state.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SmsChoiceTapTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun invisibleChoiceButtonLetsTapAdvanceStory() {
        var advances = 0
        var reveals = 0
        compose.setContent {
            SmsGameScreen(
                state = GameUiState(isAwaitingTap = true),
                onAdvanceOnTap = { advances++ },
                onRevealChoicesClicked = { reveals++ }
            )
        }
        compose.onNodeWithTag("game_make_a_choice_button")
            .assertHasNoClickAction()
            .performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(1, advances)
            assertEquals(0, reveals)
        }
    }

    @Test
    fun visibleChoiceButtonRevealsWithoutAdvancing() {
        var advances = 0
        var reveals = 0
        compose.setContent {
            SmsGameScreen(
                state = GameUiState(isAwaitingInput = true),
                onAdvanceOnTap = { advances++ },
                onRevealChoicesClicked = { reveals++ }
            )
        }
        compose.onNodeWithTag("game_make_a_choice_button").performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(0, advances)
            assertEquals(1, reveals)
        }
    }
}
