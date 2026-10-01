package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import fr.purpletear.sutoko.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun halloweenLogoPreservesSignedOutHeaderLayoutAndActions() {
        var signInClicks = 0
        var optionsClicks = 0
        compose.setContent {
            Box(Modifier.width(354.dp).background(Color.Black)) {
                TopNavigation(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                    balance = Resource.Loading(),
                    isConnected = false,
                    onAccountButtonPressed = {},
                    onSignInButtonPressed = { signInClicks++ },
                    onCoinsButtonPressed = {},
                    onDiamondsButtonPressed = {},
                    onOptionsButtonPressed = { optionsClicks++ },
                )
            }
        }
        compose.onNodeWithTag(HeaderLogoTestTag)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(104.dp)
            .assertHeightIsEqualTo(43.dp)
        compose.onNodeWithContentDescription(string(R.string.sutoko_logo_content_description)).assertIsDisplayed()
        compose.onNodeWithText("Sutoko").assertDoesNotExist()
        compose.onNodeWithTag("home_coins").assertDoesNotExist()
        compose.onNodeWithTag("home_diamonds").assertDoesNotExist()
        compose.onNodeWithText(string(R.string.sutoko_sign_in)).performClick()
        compose.onNodeWithTag(ParamsTestTag).assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, signInClicks)
            assertEquals(1, optionsClicks)
        }
    }

    @Test
    fun connectedHeaderKeepsLogoAndAllActionsVisible() {
        var accountClicks = 0
        var coinsClicks = 0
        var diamondsClicks = 0
        var optionsClicks = 0
        compose.setContent {
            Box(Modifier.width(354.dp).background(Color.Black)) {
                TopNavigation(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                    balance = Resource.Success(Balance(coins = 1500, diamonds = 2000)),
                    isConnected = true,
                    onAccountButtonPressed = { accountClicks++ },
                    onSignInButtonPressed = {},
                    onCoinsButtonPressed = { coinsClicks++ },
                    onDiamondsButtonPressed = { diamondsClicks++ },
                    onOptionsButtonPressed = { optionsClicks++ },
                )
            }
        }
        val logo = compose.onNodeWithTag(HeaderLogoTestTag).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val account = compose.onNodeWithTag(AccountTestTag).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("Logo overlaps the account button", logo.right <= account.left)
        compose.onNodeWithTag(AccountTestTag).performClick()
        compose.onNodeWithTag("home_coins").assertIsDisplayed().performClick()
        compose.onNodeWithTag("home_diamonds").assertIsDisplayed().performClick()
        compose.onNodeWithTag(ParamsTestTag).assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, accountClicks)
            assertEquals(1, coinsClicks)
            assertEquals(1, diamondsClicks)
            assertEquals(1, optionsClicks)
        }
    }

    @Test
    fun accountAndBalanceLoadingDoNotMoveTheLogoOrOptions() {
        val connected = mutableStateOf<Boolean?>(null)
        val balance = mutableStateOf<Resource<Balance>>(Resource.Loading())
        compose.setContent {
            Box(Modifier.width(354.dp).background(Color.Black)) {
                TopNavigation(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                    balance = balance.value,
                    isConnected = connected.value,
                    onAccountButtonPressed = {},
                    onSignInButtonPressed = {},
                    onCoinsButtonPressed = {},
                    onDiamondsButtonPressed = {},
                    onOptionsButtonPressed = {},
                )
            }
        }
        compose.onNodeWithTag("home_coins").assertIsNotEnabled()
        compose.onNodeWithTag("home_diamonds").assertIsNotEnabled()
        val logoBefore = compose.onNodeWithTag(HeaderLogoTestTag).fetchSemanticsNode().boundsInRoot
        val optionsBefore = compose.onNodeWithTag(ParamsTestTag).fetchSemanticsNode().boundsInRoot
        compose.runOnIdle {
            connected.value = true
            balance.value = Resource.Success(Balance(coins = 1500, diamonds = 2000))
        }
        compose.onNodeWithTag(HeaderLogoTestTag).assertIsDisplayed()
        compose.onNodeWithTag(AccountTestTag).assertIsDisplayed()
        compose.onNodeWithTag(ParamsTestTag).assertIsDisplayed()
        assertEquals(logoBefore, compose.onNodeWithTag(HeaderLogoTestTag).fetchSemanticsNode().boundsInRoot)
        assertEquals(optionsBefore, compose.onNodeWithTag(ParamsTestTag).fetchSemanticsNode().boundsInRoot)
    }

    @Test
    fun compactConnectedHeaderKeepsControlsVisibleWithoutLogoOverlap() {
        compose.setContent {
            Box(Modifier.width(320.dp).background(Color.Black)) {
                TopNavigation(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                    balance = Resource.Success(Balance(coins = 1500, diamonds = 2000)),
                    isConnected = true,
                    onAccountButtonPressed = {},
                    onSignInButtonPressed = {},
                    onCoinsButtonPressed = {},
                    onDiamondsButtonPressed = {},
                    onOptionsButtonPressed = {},
                )
            }
        }
        val logo = compose.onNodeWithTag(HeaderLogoTestTag).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val account = compose.onNodeWithTag(AccountTestTag).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("Compact logo overlaps the account button", logo.right <= account.left)
        compose.onNodeWithTag("home_coins").assertIsDisplayed()
        compose.onNodeWithTag("home_diamonds").assertIsDisplayed()
        compose.onNodeWithTag(ParamsTestTag).assertIsDisplayed()
    }

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
