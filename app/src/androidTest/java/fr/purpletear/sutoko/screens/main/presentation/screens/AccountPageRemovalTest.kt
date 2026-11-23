package fr.purpletear.sutoko.screens.main.presentation.screens

import android.content.pm.PackageManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountPageRemovalTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun removedAccountPageIsNotRegisteredAndRemainingHeaderActionsWork() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        @Suppress("DEPRECATION")
        val activities = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_ACTIVITIES
        ).activities.orEmpty()
        assertFalse(activities.any { it.name == "${context.packageName}.screens.account.AccountActivity" })
        var coinsClicks = 0
        var diamondsClicks = 0
        var settingsClicks = 0
        compose.setContent {
            TopNavigation(
                balance = Resource.Success(Balance(coins = 1500, diamonds = 2000)),
                isConnected = true,
                onSignInButtonPressed = {},
                onCoinsButtonPressed = { coinsClicks++ },
                onDiamondsButtonPressed = { diamondsClicks++ },
                onOptionsButtonPressed = { settingsClicks++ },
            )
        }
        compose.onNodeWithTag("AccountTestTag").assertDoesNotExist()
        compose.onNodeWithTag("home_coins").assertIsDisplayed().performClick()
        compose.onNodeWithTag("home_diamonds").assertIsDisplayed().performClick()
        compose.onNodeWithTag(ParamsTestTag).assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, coinsClicks)
            assertEquals(1, diamondsClicks)
            assertEquals(1, settingsClicks)
        }
    }
}
