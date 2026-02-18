package com.purpletear.game.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.purpletear.core.presentation.services.performVibration
import com.purpletear.game.presentation.sealed.Background
import com.purpletear.game.presentation.states.GameMenuState
import com.purpletear.game.presentation.states.buttonConfig
import com.purpletear.game.presentation.viewmodels.GamePreviewViewModel

@Composable
internal fun GamePreviewButtonsWrapper(
    modifier: Modifier = Modifier,
    viewModel: GamePreviewViewModel,
) {
    val gameState: GameMenuState = viewModel.gameMenuState
    val buttonsConfig = gameState.buttonConfig(
        currentChapterNumber = viewModel.currentChapter.value?.number ?: 1,
        gamePrice = viewModel.game.value?.price,
    )
    val context = LocalContext.current

    // Collect vibration events and trigger vibration
    LaunchedEffect(Unit) {
        viewModel.vibrationEvents.collect {
            performVibration(context)
        }
    }

    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    val firstButtonWeight by animateFloatAsState(
        targetValue = buttonsConfig.left.weight,
        animationSpec = animationSpec,
        label = "FirstButtonWeight"
    )
    val secondButtonWeight by animateFloatAsState(
        targetValue = buttonsConfig.right.weight,
        animationSpec = animationSpec,
        label = "SecondButtonWeight"
    )

    val animatedSecondButtonColor by animateColorAsState(
        targetValue = buttonsConfig.right.backgroundColor,
        label = "SecondButtonColor"
    )

    Row(
        Modifier
            .fillMaxWidth()
            .then(modifier),
        horizontalArrangement = spacedBy(10.dp),
    ) {

        GamePreviewButton(
            modifier = Modifier
                .weight(firstButtonWeight),
            onClick = {
                performVibration(context)
                viewModel.onAction(action = buttonsConfig.left.action)
            },
            title = buttonsConfig.left.title?.asString(),
            subtitle = buttonsConfig.left.subtitle?.asString(),
            background = Background.Solid(Color(0xFF191919)),
            icon = buttonsConfig.left.icon,
            isEnabled = buttonsConfig.left.isEnabled,
            isLoading = buttonsConfig.left.isLoading,
        )

        GamePreviewButton(
            modifier = Modifier
                .weight(secondButtonWeight),
            title = buttonsConfig.right.title?.asString(),
            subtitle = buttonsConfig.right.subtitle?.asString(),
            onClick = {
                performVibration(context)
                viewModel.onAction(action = buttonsConfig.right.action)
            },
            background = Background.Solid(animatedSecondButtonColor),
            icon = buttonsConfig.right.icon,
            isEnabled = buttonsConfig.right.isEnabled,
            isLoading = buttonsConfig.right.isLoading,
        )
    }
}
