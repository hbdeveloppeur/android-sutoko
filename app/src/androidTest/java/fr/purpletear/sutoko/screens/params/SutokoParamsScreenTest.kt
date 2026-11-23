package fr.purpletear.sutoko.screens.params

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.purpletear.sutoko.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SutokoParamsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun optionsHaveNoChapterStatisticsToggleAndKeepPrivacyActions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val events = mutableListOf<SutokoParamsEvent>()
        var privacyOptionsClicks = 0
        compose.setContent {
            SutokoParamsContent(
                uiState = SutokoParamsUiState(),
                onEvent = events::add,
                showPrivacyOptions = true,
                onPrivacyOptions = { privacyOptionsClicks++ },
            )
        }
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            .assertCountEquals(0)
        compose.onNodeWithText(context.getString(R.string.sutoko_privacy_policy))
            .assertIsDisplayed().performClick()
        compose.onNodeWithText(context.getString(R.string.sutoko_ad_privacy_options))
            .assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(listOf(SutokoParamsEvent.OnPrivacyPressed), events)
            assertEquals(1, privacyOptionsClicks)
        }
    }
}
