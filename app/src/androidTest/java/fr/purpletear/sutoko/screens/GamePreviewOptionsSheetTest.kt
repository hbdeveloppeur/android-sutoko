package fr.purpletear.sutoko.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_preview.GamePreviewRestartResult
import com.purpletear.game.presentation.game_preview.GamePreviewRestartTarget
import com.purpletear.game.presentation.game_preview.components.GamePreviewOptionsSheet
import com.purpletear.sutoko.game.model.Chapter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GamePreviewOptionsSheetTest {
    @get:Rule val compose = createComposeRule()
    private val visible = mutableStateOf(true)
    private val busy = mutableStateOf(false)
    private val result = mutableStateOf<GamePreviewRestartResult?>(null)
    private var confirmations = 0
    private var completions = 0
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)
    private val chapter = Chapter(story = "test", number = 3, code = "3B", title = "A second chance", available = true)

    private fun open(chapter: Chapter? = this.chapter, fontScale: Float = 1f, onConfirm: (GamePreviewRestartTarget) -> Unit = { confirmations++ }) {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    Box(Modifier.fillMaxSize().background(Color(0xFF080D18))) {
                        Text("Story preview", modifier = Modifier.padding(24.dp))
                        if (visible.value) {
                            GamePreviewOptionsSheet(
                                storyTitle = "The quiet promise", chapter = chapter,
                                isLoadingChapters = false, isRestarting = busy.value, restartResult = result.value,
                                onDismiss = { visible.value = false }, onClearResult = { result.value = null },
                                onConfirm = onConfirm,
                                onRestartFinished = { completions++; visible.value = false },
                            )
                        }
                    }
                }
            }
        }
    }

    @Test fun cancellingConfirmationKeepsBothChoicesInTheSameSheet() {
        open()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_chapter)).performClick()
        compose.onNodeWithText(context.getString(R.string.game_presentation_options_confirm_chapter, 3)).assertIsDisplayed()
        compose.onNodeWithText(text(android.R.string.cancel)).performClick()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_chapter)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_game)).assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, confirmations) }
    }

    @Test fun backFromConfirmationReturnsToChoicesThenClosesTheSheet() {
        open()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_chapter)).performClick()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressBack()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_game)).assertIsDisplayed()
        device.pressBack()
        compose.onNodeWithText("Story preview").assertIsDisplayed()
        compose.onNodeWithText(text(R.string.game_presentation_options_title)).assertDoesNotExist()
    }

    @Test fun busyStateShowsProgressAndCannotBeDismissedOrConfirmedTwice() {
        open(onConfirm = { confirmations++; busy.value = true })
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_game)).performClick()
        compose.onNodeWithText(text(R.string.game_presentation_game_restart_confirm_button)).performClick()
        compose.onNodeWithText(text(R.string.game_presentation_options_restarting)).assertIsDisplayed().assertIsNotEnabled()
        compose.onNodeWithContentDescription(text(R.string.game_presentation_options_close)).assertIsNotEnabled()
        compose.onNodeWithText(text(android.R.string.cancel)).assertIsNotEnabled()
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        compose.onNodeWithText(text(R.string.game_presentation_options_restarting)).assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, confirmations) }
    }

    @Test fun failedRestartKeepsTheConfirmationAndOffersRetry() {
        open(onConfirm = { target -> confirmations++; result.value = GamePreviewRestartResult.Failure(target) })
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_chapter)).performClick()
        compose.onNodeWithText(text(R.string.game_presentation_game_restart_confirm_button)).performClick()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_failed)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.game_presentation_options_retry_restart)).performClick()
        compose.runOnIdle { assertEquals(2, confirmations); assertEquals(0, completions) }
    }

    @Test fun successClosesTheSheetOnlyAfterTheResetCompletes() {
        open(onConfirm = { target -> confirmations++; result.value = GamePreviewRestartResult.Success(target) })
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_chapter)).performClick()
        compose.onNodeWithText(text(R.string.game_presentation_game_restart_confirm_button)).performClick()
        compose.waitUntil(5_000) { completions == 1 }
        compose.onNodeWithText("Story preview").assertIsDisplayed()
        compose.onNodeWithText(text(R.string.game_presentation_options_title)).assertDoesNotExist()
    }

    @Test fun unavailableChapterExplainsWhyItCannotBeRestarted() {
        open(chapter = chapter.copy(available = false))
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_chapter)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.game_presentation_options_chapter_unavailable)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_game)).performClick()
        compose.onNodeWithText(text(R.string.game_presentation_game_restart_confirm_title)).assertIsDisplayed()
    }

    @Test fun missingChapterHasAnExplanationInsteadOfAnUnlabelledDisabledAction() {
        open(chapter = null)
        compose.onNodeWithText(text(R.string.game_presentation_options_chapter_missing)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_chapter)).assertIsNotEnabled()
    }

    @Test fun largeTextKeepsConfirmAndCancelReachableByScrolling() {
        open(fontScale = 2f)
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_chapter)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.game_presentation_game_restart_confirm_button)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(text(android.R.string.cancel)).performScrollTo().assertIsDisplayed().performClick()
        compose.onNodeWithText(text(R.string.game_presentation_options_restart_game)).performScrollTo().assertIsDisplayed()
    }
}
