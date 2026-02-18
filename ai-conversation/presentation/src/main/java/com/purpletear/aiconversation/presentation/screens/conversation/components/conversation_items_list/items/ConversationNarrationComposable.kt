package com.purpletear.aiconversation.presentation.screens.conversation.components.conversation_items_list.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.theme.AiConversationTheme


@Composable
@Preview(name = "ConversationNarrationComposable", showBackground = false, showSystemUi = false)
private fun Preview() {

    val verticalRules = listOf(14.dp)
    val rulesEnabled = false
    AiConversationTheme {
        Box {
            Column(
                Modifier.background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preview_conversation_narration),
                    contentDescription = null,
                )
                Box(Modifier.padding(vertical = 12.dp)) {
                    ConversationNarrationComposable(
                        modifier = Modifier
                            .fillMaxWidth(1f),
                        text = "Alors que le vent devient chaud, les couleurs du ciel se mettent à changer soudainement."
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
internal fun ConversationNarrationComposable(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier = Modifier.padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .height(30.dp)
                .width(.5.dp)
                .background(Color.White.copy(0.29f))
        )
        Text(
            modifier = Modifier.widthIn(max = 250.dp),
            text = text.replace("*", ""),
            style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
            color = Color.White
        )
        Spacer(Modifier.weight(1f))
    }
}