package com.purpletear.game.presentation.game_play.components.choices_box

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.RobotoFontFamily
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.engine.HandlerEffect

@Preview(name = "ChoiceBoxPreview")
@Composable
private fun Preview() {
    val choices = listOf(
        HandlerEffect.ShowChoices.Choice("1", "Prends ton temps"),
        HandlerEffect.ShowChoices.Choice("2", "J'espère que ce n'est pas trop grave"),
        HandlerEffect.ShowChoices.Choice("3", "(Ne rien dire)"),
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ChoicesBox(choices = choices)
    }
}

@Composable
fun ChoicesBox(
    modifier: Modifier = Modifier,
    choices: List<HandlerEffect.ShowChoices.Choice>,
    onClickChoice: (HandlerEffect.ShowChoices.Choice) -> Unit = {},
    onClickClose: () -> Unit = {},
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClickClose
            ),
        contentAlignment = Alignment.Center
    ) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 300.dp)
                .background(Color.White, shape = shape)
        ) {
            Text(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                text = stringResource(R.string.game_choices_title),
                color = Color(0xFF5E93C9),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Box(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.4f))
            )
            Column(
                Modifier.padding(vertical = 8.dp)
            ) {
                choices.forEach { choice ->
                    ChoiceRow(
                        choice = choice,
                        onClick = { onClickChoice(choice) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    choice: HandlerEffect.ShowChoices.Choice,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp)
    ) {
        Text(choice.text)
    }
}

@Composable
fun MakeAChoiceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(0.8f),
            text = stringResource(R.string.game_choices_make_a_choice).uppercase(),
            color = Color(0xFFC9C9C9),
            fontFamily = RobotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 0.09.em,
            textAlign = TextAlign.Center
        )
        Icon(
            painter = painterResource(R.drawable.ic_action_choice),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 12.dp)
                .size(40.dp)
                .alpha(0.7f),
            tint = Color.Unspecified
        )
    }
}