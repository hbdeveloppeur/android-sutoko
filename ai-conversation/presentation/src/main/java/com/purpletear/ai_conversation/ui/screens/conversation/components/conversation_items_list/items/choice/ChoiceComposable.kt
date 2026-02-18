package com.purpletear.ai_conversation.ui.screens.conversation.components.conversation_items_list.items.choice

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageStoryChoiceGroup
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.core.presentation.services.performVibration
import com.purpletear.ai_conversation.ui.screens.conversation.viewmodels.ConversationViewModel
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme


@Composable
@Preview(name = "ChoiceComposable", showBackground = false, showSystemUi = false)
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
                    painter = painterResource(id = R.drawable.preview_choice),
                    contentDescription = null,
                )
                Box(Modifier.padding(vertical = 12.dp)) {

//                    ChoiceComposable(
//                        modifier = Modifier,
//                        choices = listOf(),
//                        groupId = "",
//                        viewModel = hiltViewModel()
//                    )
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
internal fun ChoiceComposable(
    modifier: Modifier = Modifier,
    messageStoryChoiceGroup: MessageStoryChoiceGroup,
    viewModel: ConversationViewModel,
) {
    val context = LocalContext.current


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        messageStoryChoiceGroup.choices.forEach { choice ->
            var isClicked by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isClicked) 0.93f else 1f,
                tween(
                    durationMillis = 180,
                    easing = FastOutSlowInEasing
                ),

                finishedListener = {
                    if (isClicked) {
                        isClicked = false
                    }
                }
            )

            val alphaState by animateFloatAsState(
                targetValue = if (messageStoryChoiceGroup.isConsumed.not()) 1f else if (choice.isSelected) 0.5f else 0.15f,
                animationSpec = tween(
                    durationMillis = 280,
                    delayMillis = 0,
                    easing = LinearOutSlowInEasing
                ),
                label = "Full image card loading back alpha state"
            )

            ChoiceRow(modifier = Modifier
                .widthIn(min = 260.dp)
                .alpha(alphaState)
                .scale(scale), onClick = {
                performVibration(context)
                isClicked = true
                viewModel.onClickChoice(choice, messageStoryChoiceGroup)
            }) {
                ChoiceText(text = choice.text)
            }
        }
    }
}


@Composable
private fun ChoiceText(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        color = Color.White,
        modifier = modifier.widthIn(max = 180.dp),
        //fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        letterSpacing = 0.2.sp
    )
}


@Composable
private fun ChoiceRow(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val linearGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF262E39), Color(0xFF42385D))
    )
    val shape = RoundedCornerShape(6.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .background(linearGradient)
            .border(.25.dp, Color(0xFF485568), MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp, horizontal = 14.dp),

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        content = {
            Image(
                modifier = Modifier
                    .size(16.dp)
                    .alpha(0.3f),
                painter = painterResource(id = R.drawable.ic_arrow_choice),
                contentDescription = null,
            )
            content()
            Image(
                modifier = Modifier
                    .size(16.dp)
                    .alpha(0.3f)
                    .scale(scaleX = -1f, scaleY = 1f),
                painter = painterResource(id = R.drawable.ic_arrow_choice),
                contentDescription = null,
            )
        }
    )
}
