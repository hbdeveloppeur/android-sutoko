package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.components.coins_display.CoinsDisplay
import com.example.sharedelements.R as SharedElementsR


const val ParamsTestTag = "ParamsTestTag"
const val HeaderLogoTestTag = "HeaderLogoTestTag"

internal val MainTopNavigationPadding = Modifier
    .padding(top = 12.dp)
    .padding(horizontal = 16.dp)
    .padding(bottom = 8.dp)

@Composable
fun TopNavigation(
    modifier: Modifier = Modifier,
    balance: Resource<Balance>,
    isConnected: Boolean?,
    onSignInButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit
) {


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {

        Box(modifier = Modifier.weight(1f).padding(bottom = 3.dp)) {
            SnowflakeHeaderLogo(modifier = Modifier.testTag(HeaderLogoTestTag))
        }


        if (isConnected == false) {
            TextButton(
                onClick = onSignInButtonPressed,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.textButtonColors(backgroundColor = Color(0xFFFF447C)),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = stringResource(id = R.string.sutoko_sign_in),
                    fontSize = 12.sp,
                    color = Color.White,
                    fontFamily = FontFamily(
                        Font(
                            R.font.font_poppins_semibold,
                            FontWeight.SemiBold
                        )
                    ),
                    style = TextStyle(
                        letterSpacing = 0.5.sp,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        }


        val loadedBalance = balance.data?.takeIf { it.isLoaded() }
        if (isConnected != false) {
            // User coins amount
            CoinsDisplay(
                modifier = Modifier.padding(start = 8.dp).widthIn(min = 76.dp).testTag("home_coins"),
                amount = loadedBalance?.coins,
                enabled = loadedBalance != null,
                onClick = onCoinsButtonPressed
            )

            // User diamonds amount
            CoinsDisplay(
                modifier = Modifier.padding(start = 8.dp).widthIn(min = 76.dp).testTag("home_diamonds"),
                amount = loadedBalance?.diamonds,
                enabled = loadedBalance != null,
                onClick = onDiamondsButtonPressed,
                iconResId = SharedElementsR.drawable.shared_elements_sutoko_ic_diamond,
                borderColor = Color(0xFF4DB9EC),
                backgroundColor = Color(0xFF2E3A4F)
            )
        }


        // Options More
        Image(
            modifier = Modifier
                .testTag(ParamsTestTag)
                .size(20.dp)
                .padding(start = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    onOptionsButtonPressed()
                }, painter = painterResource(R.drawable.ic_more_vert), contentDescription = null
        )
    }
}
