package fr.purpletear.sutoko.screens.account.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.components.coins_display.CoinsDisplay


@Composable
fun Connection(
    isConnected: Boolean,
    coins: Int,
    diamonds: Int,
    onClickCoins: () -> Unit,
    onClickDiamonds: () -> Unit,
    onTapSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp
            )
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        Text(
            text = stringResource(id = fr.purpletear.sutoko.R.string.sutoko_my_account),
            fontSize = 16.sp,
            fontFamily = FontFamily(
                Font(
                    R.font.font_poppins_bold,
                    FontWeight.Bold
                )
            ),
            style = TextStyle(
                letterSpacing = 0.5.sp,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )

        if (isConnected) {
            ConnectedView(
                coins = coins,
                diamonds = diamonds,
                onClickCoins = onClickCoins,
                onClickDiamonds = onClickDiamonds
            )
        } else {
            NotConnectedView(onTapSignIn = onTapSignIn)
        }
    }
}

@Composable
private fun ConnectedView(
    coins: Int,
    diamonds: Int,
    onClickCoins: () -> Unit,
    onClickDiamonds: () -> Unit
) {
    Text(

        text = stringResource(R.string.account_connected_description),
        fontSize = 14.sp,
        fontFamily = FontFamily(
            Font(
                R.font.font_poppins_semibold,
                FontWeight.Normal
            )
        ),
        style = TextStyle(
            letterSpacing = 0.5.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )
    )

    Row {
        // User coins amount
        CoinsDisplay(
            modifier = Modifier.padding(start = 8.dp),
            amount = coins,
            onClick = onClickCoins
        )

        // User diamonds amount
        CoinsDisplay(
            modifier = Modifier.padding(start = 8.dp),
            amount = diamonds,
            onClick = onClickDiamonds,
            iconResId = com.purpletear.smsgame.R.drawable.sutoko_ic_diamond,
            borderColor = Color(0xFF4DB9EC),
            backgroundColor = Color(0xFF2E3A4F)
        )
    }
}

@Composable
private fun NotConnectedView(onTapSignIn: () -> Unit) {

    Text(
        text = stringResource(R.string.account_signin_description),
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily(
            Font(
                R.font.font_poppins_regular,
                FontWeight.Normal
            )
        ),
        style = TextStyle(
            letterSpacing = 0.5.sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )
    )

    TextButton(
        onClick = onTapSignIn,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.textButtonColors(backgroundColor = Color(0xFFFF447C)),
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = 12.dp
            ),
            text = stringResource(id = fr.purpletear.sutoko.R.string.sutoko_sign_in),
            fontSize = 12.sp,
            color = Color.White,
            fontFamily = FontFamily(
                Font(
                    fr.purpletear.sutoko.R.font.font_poppins_semibold,
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