package com.purpletear.game.presentation.game_play.components.choices_box

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

private const val SCRIM_ALPHA = 0.67f
private const val COLOR_ANIMATION_MS = 220

private data class ChoicesBoxColors(
    val background: Color,
    val title: Color,
    val choice: Color,
    val divider: Color,
)

private val DarkChoicesBoxColors = ChoicesBoxColors(
    background = Color(0xFF1E1E1E),
    title = Color(0xFFE2E2E2),
    choice = Color(0xFFE2E2E2),
    divider = Color(0xFF222222),
)

private val LightChoicesBoxColors = ChoicesBoxColors(
    background = Color(0xFFFEFEFE),
    title = Color(0xFF007EFF),
    choice = Color(0xFF2C2C2C),
    divider = Color(0xFFE2E2E2),
)

@Composable
private fun animatedChoicesBoxColors(isDarkMode: Boolean): ChoicesBoxColors {
    val target = if (isDarkMode) DarkChoicesBoxColors else LightChoicesBoxColors
    val spec = tween<Color>(durationMillis = COLOR_ANIMATION_MS)
    return ChoicesBoxColors(
        background = animateColorAsState(target.background, spec, label = "choicesBg").value,
        title = animateColorAsState(target.title, spec, label = "choicesTitle").value,
        choice = animateColorAsState(target.choice, spec, label = "choicesChoice").value,
        divider = animateColorAsState(target.divider, spec, label = "choicesDivider").value,
    )
}

@Preview(name = "ChoiceBoxPreviewDark")
@Composable
private fun PreviewDark() {
    ChoicesBoxPreview(isDarkMode = true)
}

@Preview(name = "ChoiceBoxPreviewLight")
@Composable
private fun PreviewLight() {
    ChoicesBoxPreview(isDarkMode = false)
}

@Composable
private fun ChoicesBoxPreview(isDarkMode: Boolean) {
    val choices = listOf(
        HandlerEffect.ShowChoices.Choice("1", "Prends ton temps"),
        HandlerEffect.ShowChoices.Choice("2", "J'espère que ce n'est pas trop grave"),
        HandlerEffect.ShowChoices.Choice("3", "(Ne rien dire)"),
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ChoicesBox(choices = choices, isDarkMode = isDarkMode)
    }
}

@Composable
fun ChoicesBox(
    modifier: Modifier = Modifier,
    choices: List<HandlerEffect.ShowChoices.Choice>,
    isDarkMode: Boolean = true,
    onClickChoice: (HandlerEffect.ShowChoices.Choice) -> Unit = {},
    onClickClose: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
) {
    val colors = animatedChoicesBoxColors(isDarkMode)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClickClose
            ),
        contentAlignment = Alignment.Center
    ) {
        val shape = RoundedCornerShape(16.dp)
        Column(
            modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 300.dp)
                .background(colors.background, shape = shape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    text = stringResource(R.string.game_choices_title),
                    color = colors.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onToggleDarkMode) {
                    Icon(
                        painter = painterResource(if (isDarkMode) R.drawable.ic_darkmode_white else R.drawable.ic_darkmode),
                        contentDescription = stringResource(R.string.game_choices_toggle_dark_mode),
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                }
            }
            Box(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(colors.divider.copy(alpha = 0.4f))
            )
            Column(
                Modifier.padding(vertical = 8.dp)
            ) {
                choices.forEach { choice ->
                    ChoiceRow(
                        choice = choice,
                        textColor = colors.choice,
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
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier
            .fillMaxWidth()
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            })
            .padding(vertical = 14.dp, horizontal = 16.dp)
    ) {
        Text(text = choice.text, color = textColor)
    }
}

@Composable
fun MakeAChoiceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
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
