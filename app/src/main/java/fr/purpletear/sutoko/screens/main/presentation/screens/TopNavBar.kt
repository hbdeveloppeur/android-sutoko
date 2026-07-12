package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.sharedelements.theme.SutokoTypography
import com.purpletear.core.presentation.extensions.Resource
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.components.coins_display.CoinsDisplay
import com.example.sharedelements.R as SharedElementsR


const val ParamsTestTag = "ParamsTestTag"
const val AccountTestTag = "AccountTestTag"

@Composable
fun TopNavigation(
    modifier: Modifier = Modifier,
    balance: Resource<Balance>,
    isConnected: Boolean,
    onAccountButtonPressed: () -> Unit,
    onSignInButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit
) {


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Sutoko",
                color = Color.White.copy(alpha = 0.7f),
                style = SutokoTypography.h1,
                fontSize = 18.sp,
            )
            val painter = painterResource(R.drawable.sutoko_your_turn_logo)
            val intrinsicSize = painter.intrinsicSize
            val ratio = intrinsicSize.width / intrinsicSize.height

            Image(
                painter = painter,
                contentDescription = stringResource(R.string.sutoko_logo_content_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(16.dp)
                    .aspectRatio(ratio)
            )
        }
        Spacer(Modifier.weight(1f))


        // Account: icon when connected, localized Sign in button otherwise
        if (isConnected) {
            Image(
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 2.dp)
                    .testTag(AccountTestTag)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onAccountButtonPressed()
                    },
                painter = painterResource(R.drawable.sutoko_ic_account),
                contentDescription = null
            )
        } else {
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
        if (loadedBalance != null) {
            // User coins amount
            CoinsDisplay(
                modifier = Modifier.padding(start = 8.dp),
                amount = loadedBalance.coins,
                onClick = onCoinsButtonPressed
            )

            // User diamonds amount
            CoinsDisplay(
                modifier = Modifier.padding(start = 8.dp),
                amount = loadedBalance.diamonds,
                onClick = onDiamondsButtonPressed,
                iconResId = SharedElementsR.drawable.sutoko_ic_diamond,
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
                .alpha(if (balance is Resource.Loading) 0.3f else 1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    onOptionsButtonPressed()
                }, painter = painterResource(R.drawable.ic_more_vert), contentDescription = null
        )
    }
}