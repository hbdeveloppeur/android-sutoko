package fr.purpletear.sutoko.screens.main.presentation.screens.calendar

import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.game.GameCatalog
import com.purpletear.sutoko.game.model.game.GameMetadata
import com.purpletear.sutoko.game.model.game.NarrativeTheme
import fr.purpletear.sutoko.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.purpletear.game.presentation.R as GamePresentationR

@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun cachedUpcomingGameOpensItsPreviewAndNotificationSwitchTracksPreference() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notificationsOn = mutableStateOf(false)
        val game = GameCatalog(
            id = PureHeartStoryId,
            metadata = GameMetadata(title = "Pure Heart"),
            canvasTechnologyRequiredVersion = 0,
            narrativeThemes = listOf(NarrativeTheme("mystery", "Mystery")),
        )
        val chapter = Chapter(id = "upcoming-chapter", number = 8, story = game.id, releaseDate = 1_800_000_000L)
        var openedGame: String? = null

        compose.setContent {
            CalendarContent(
                events = emptyList(),
                releases = listOf(CalendarRelease(game, chapter)),
                notificationsOn = notificationsOn.value,
                isLoading = false,
                onNotificationsChanged = { notificationsOn.value = it },
                onGamePressed = { openedGame = it },
            ) {
                Text("Sutoko header")
            }
        }

        compose.onNodeWithText("Sutoko header").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.sutoko_calendar_title)).assertIsDisplayed()
        compose.onNodeWithText(
            context.getString(GamePresentationR.string.game_presentation_game_chapter_number, chapter.number)
        ).assertIsDisplayed()
        compose.onNodeWithText("Mystery").assertIsDisplayed()
        compose.waitUntil(timeoutMillis = 25_000L) {
            compose.onAllNodesWithTag(CalendarReleaseVideoTestTag, useUnmergedTree = true)
                .fetchSemanticsNodes().size == 1
        }
        compose.onNodeWithTag(CalendarReleaseVideoTestTag, useUnmergedTree = true).assertIsDisplayed()
        compose.onNode(isToggleable()).assertIsOff()
        compose.runOnIdle { notificationsOn.value = true }
        compose.onNode(isToggleable()).assertIsOn().performClick().assertIsOff()
        compose.onNodeWithText(game.metadata.title).assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(game.id, openedGame) }
    }
}
