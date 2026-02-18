package com.purpletear.ai_conversation.ui.component.coins_title

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.coins_indicator.CoinsIndicatorComposable
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme


@Composable
@Preview(name = "TitleWithCoins", showBackground = false, showSystemUi = false)
private fun Preview() {

    val verticalRules = listOf(21.dp, 35.dp, 46.dp, 273.dp)
    val rulesEnabled = true
    AiConversationTheme {
        Box {
            Column(
                Modifier.background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_image_title_with_coins),
                    contentDescription = null,
                )
                Box(Modifier.padding(vertical = 12.dp)) {
                    TitleWithCoins(
                        modifier = Modifier
                            .fillMaxWidth(0.88f),
                        imageVector = ImageVector.vectorResource(id = R.drawable.vec_square_dot),
                        title = "Imagine your image",
                        coins = 1234
                    )
                }
            }
            if (rulesEnabled) {
                verticalRules.forEach { startPadding ->
                    Box(
                        Modifier
                            .padding(start = startPadding)
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}

@Composable
internal fun TitleWithCoins(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    title: String,
    coins : Int,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "Icon of the title",
            modifier = Modifier
                .size(14.dp),
            tint = Color(0xFF8F9DC6)
        )
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.weight(1f))
        CoinsIndicatorComposable(amount = coins)
    }
}