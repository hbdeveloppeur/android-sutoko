package com.purpletear.aiconversation.presentation.component.quote_text

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme

@Composable
@Preview(name = "QuotedTextComposable", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_quoted_text),
                contentDescription = null,
            )
            QuotedTextComposable(
                modifier = Modifier.fillMaxWidth(0.92f),
                quote = "Anything is possible as long as it's not vulgar, immoral or dangerous."
            )
        }
    }
}

@Composable
internal fun QuotedTextComposable(modifier: Modifier = Modifier, quote: String) {
    var textHeight by remember { mutableIntStateOf(0) }

    Row(
        Modifier
            .padding(start = 10.dp, end = 28.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(with(LocalDensity.current) { textHeight.toDp() * 0.7f })
                .background(Color.White.copy(0.3f))

        )
        Text(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                textHeight = coordinates.size.height
            },
            text = quote,
            color = Color(0xFFDCDCDC),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}