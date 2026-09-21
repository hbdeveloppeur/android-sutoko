package com.purpletear.game.presentation.game_play

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import com.purpletear.game.debug.PreviewCharacter
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.message.GameMessageText
import org.junit.Rule
import org.junit.Test

class SmsRapidMessageTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun newestMessageRemainsVisibleAfterBurstDuringScroll() {
        fun message(index: Int) = GameMessageText("message-$index", "Message $index", 1)
        val state = mutableStateOf(GameUiState(messages = List(40, ::message), characters = mapOf(1 to PreviewCharacter.copy(id = 1))))
        compose.setContent { SmsGameScreen(state = state.value) }
        compose.onNodeWithTag("game_messages").performScrollToIndex(20)
        compose.mainClock.autoAdvance = false
        repeat(30) { index ->
            compose.runOnUiThread {
                state.value = state.value.copy(messages = state.value.messages + message(40 + index))
            }
            compose.mainClock.advanceTimeBy(32)
        }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithText("Message 69").assertIsDisplayed()
    }
}
