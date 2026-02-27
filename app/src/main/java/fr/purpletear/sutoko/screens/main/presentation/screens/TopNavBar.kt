package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Pink
import com.example.sharedelements.theme.SutokoTypography
import fr.purpletear.sutoko.R
import fr.purpletear.sutoko.screens.create.components.coins_display.CoinsDisplay
import com.example.sharedelements.R as SharedElementsR


const val ParamsTestTag = "ParamsTestTag"
const val AccountTestTag = "AccountTestTag"

@Composable
fun TopNavigation(
    modifier: Modifier = Modifier,
    coins: Int,
    diamonds: Int,
    isLoading: Boolean,
    onAccountButtonPressed: () -> Unit,
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
            // center
        ) {
            Text(
                text = "Sutoko",
                color = Color.White,
                style = SutokoTypography.h1,
                fontSize = 18.sp,
            )
            Text(
                text = "2026",
                color = Pink,
                style = SutokoTypography.h1,
                fontSize = 22.sp,
            )
        }
        Spacer(Modifier.weight(1f))


        // Account Button
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


        // User coins amount
        CoinsDisplay(
            modifier = Modifier
                .padding(start = 8.dp)
                .alpha(if (isLoading) 0.3f else 1f),
            amount = coins,
            onClick = onCoinsButtonPressed
        )

        // User diamonds amount
        CoinsDisplay(
            modifier = Modifier
                .padding(start = 8.dp)
                .alpha(if (isLoading) 0.3f else 1f),
            amount = diamonds,
            onClick = onDiamondsButtonPressed,
            iconResId = SharedElementsR.drawable.sutoko_ic_diamond,
            borderColor = Color(0xFF4DB9EC),
            backgroundColor = Color(0xFF2E3A4F)
        )


        // Options More
        Image(
            modifier = Modifier
                .testTag(ParamsTestTag)
                .size(20.dp)
                .padding(start = 8.dp)
                .alpha(if (isLoading) 0.3f else 1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    onOptionsButtonPressed()
                }, painter = painterResource(R.drawable.ic_more_vert), contentDescription = null
        )
    }
}