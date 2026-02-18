package com.purpletear.aiconversation.presentation.component.coins_indicator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme


@Composable
@Preview(name = "CoinsIndicatorComposable", showBackground = false, showSystemUi = false)
private fun Preview() {

    val verticalRules = listOf(140.dp, 204.dp)
    val rulesEnabled = true
    AiConversationTheme {
        Box {
            Column(
                Modifier
                    .background(Color.Black)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_coins_indicator),
                    contentDescription = null,
                )
                Box(
                    Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CoinsIndicatorComposable(
                        modifier = Modifier
                            .align(Alignment.Center),
                        amount = 1000
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
                    )
                }
            }
        }
    }
}

@Composable
internal fun CoinsIndicatorComposable(
    modifier: Modifier = Modifier,
    amount: Int?,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .then(modifier)
            .background(Color(0xFF232836))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .padding(start = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(14.dp),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = amount.toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
        Image(
            modifier = Modifier.size(16.dp),
            painter = painterResource(id = R.drawable.message_coin),
            contentDescription = null
        )
    }
}