package fr.purpletear.sutoko.screens.main.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sutokosharedelements.theme.Pink
import com.example.sutokosharedelements.theme.SutokoTypography
import fr.purpletear.sutoko.R


const val ParamsTestTag = "ParamsTestTag"
const val AccountTestTag = "AccountTestTag"

@Composable
fun TopNavigation(
    coins: Int,
    diamonds: Int,
    isLoading: Boolean,
    onAccountButtonPressed: () -> Unit,
    onCoinsButtonPressed: () -> Unit,
    onDiamondsButtonPressed: () -> Unit,
    onOptionsButtonPressed: () -> Unit
) {


    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 12.dp)
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
                text = "2025",
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
        AmountLabel(
            modifier = Modifier.padding(start = 8.dp),
            number = coins,
            image = fr.purpletear.sutoko.shop.presentation.R.drawable.sutoko_item_coin,
            onTap = {
                onCoinsButtonPressed()
            })


        // User diamonds amount
        AmountLabel(
            modifier = Modifier
                .padding(start = 8.dp)
                .alpha(if (isLoading) 0.3f else 1f),
            number = diamonds,
            image = com.purpletear.smsgame.R.drawable.sutoko_ic_diamond,
            onTap = {
                onDiamondsButtonPressed()
            })


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


@Composable
fun AmountLabel(modifier: Modifier, number: Int, image: Int, onTap: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(25))
            .background(
                Color.White.copy(alpha = 0.1f)

            )
            .clickable {
                onTap?.invoke()
            }

            .padding(vertical = 2.dp, horizontal = 8.dp)) {
        Text(
            text = number.toString(),
            color = Color.White,
            style = SutokoTypography.h1,
            fontSize = 12.sp,
            fontFamily = FontFamily(
                Font(R.font.font_poppins_extrabold, FontWeight.ExtraBold)
            )
        )
        Image(
            modifier = Modifier
                .size(22.dp)
                .padding(start = 4.dp),
            painter = painterResource(image),
            contentDescription = null
        )
    }
}