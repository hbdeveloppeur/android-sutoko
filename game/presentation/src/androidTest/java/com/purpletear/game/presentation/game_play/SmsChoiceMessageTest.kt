package com.purpletear.game.presentation.game_play

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.purpletear.game.presentation.game_play.state.GameUiState
import com.purpletear.sutoko.game.engine.ArrivalContext
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.handlers.MessageNodeHandler
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.model.character.CharacterColor
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmsChoiceMessageTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun choiceShowsActionButClickInsertsOnlyTrimmedSpokenText() {
        val label = "(mentir)  je vais bien  "
        val node = Node.Message(id = "choice", text = label, characterId = 1)
        val choice = HandlerEffect.ShowChoices.Choice(id = node.id, text = label, nextNodeId = node.id)
        val memory = createMemory().apply { set(GameMemory.TYPING_ANIMATION_KEY, "true") }
        val handler = MessageNodeHandler(TextProcessorImpl())
        val state = mutableStateOf(
            GameUiState(
                choices = listOf(choice),
                isAwaitingInput = true,
                isChoicesRevealed = true,
                characters = mapOf(1 to Character(1, "Alex", null, true, CharacterColor("#FFFFFF", "#FFFFFF")))
            )
        )
        compose.setContent {
            SmsGameScreen(
                state = state.value,
                onChoiceSelected = { selected ->
                    assertEquals(node.id, selected.nextNodeId)
                    val script = handler.buildScript(
                        node, memory, previousNode = null,
                        arrivalContext = ArrivalContext(selectedChoiceNodeId = selected.nextNodeId)
                    )
                    val message = (script.commands.single() as HandlerCommand.Emit).effect as HandlerEffect.AddMessage
                    state.value = state.value.copy(
                        messages = listOf(message.message),
                        choices = emptyList(),
                        isAwaitingInput = false,
                        isChoicesRevealed = false
                    )
                }
            )
        }

        compose.onNodeWithText(label).assertIsDisplayed().performClick()
        compose.onNodeWithText("je vais bien").assertIsDisplayed()
        compose.onNodeWithText(label).assertDoesNotExist()
    }

    private fun createMemory(): GameMemory {
        val memories = object : MemoryRepository {
            override suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry> = emptyMap()
            override suspend fun save(gameId: String, memories: Map<String, MemoryEntry>) {}
            override suspend fun clear(gameId: String) {}
            override suspend fun delete(gameId: String) {}
            override fun observe(gameId: String) = flowOf(emptyMap<String, String>())
            override suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int) {}
        }
        val progress = object : UserGameProgressRepository {
            override fun observe(gameId: String) = flowOf(UserGameProgress(gameId, "", ""))
            override suspend fun get(gameId: String) = UserGameProgress(gameId, "", "")
            override suspend fun save(progress: UserGameProgress) {}
            override suspend fun delete(gameId: String) {}
        }
        return GameMemory(memories, progress).apply { setCurrentChapterNumber(1) }
    }
}
