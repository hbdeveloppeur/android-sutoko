package com.purpletear.ai_conversation.ui.screens.home.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.button.ButtonComposable
import com.purpletear.ai_conversation.ui.component.button.ButtonTheme
import com.purpletear.ai_conversation.ui.screens.home.state.PlayabilityState
import com.purpletear.ai_conversation.ui.screens.home.state.PlayabilityState.Loading
import com.purpletear.ai_conversation.ui.screens.home.viewModels.AiConversationHomeViewModel


@Composable
fun HomePlayButtonsComposable(
    navHostController: androidx.navigation.NavHostController,
    viewModel: AiConversationHomeViewModel,
) {
    Column(
        Modifier
            .padding(top = 20.dp)
            .fillMaxWidth(0.92f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val characterName: String? = viewModel.selectedCharacter.value?.firstName
        val state = viewModel.playabilityState.value

        ButtonComposable(
            title = when (state) {
                is PlayabilityState.Triable -> {
                    if (state.isAd) {
                        stringResource(R.string.ai_conversation_presentation_try_with_ad)
                    } else {
                        stringResource(R.string.ai_conversation_presentation_try)
                    }
                }

                else -> {
                    characterName?.let {
                        stringResource(R.string.ai_conversation_button_discuss_name, it)
                    } ?: stringResource(R.string.ai_conversation_start_conversation)
                }
            },
            subtitle = null,
            theme = ButtonTheme.Pink(iconId = R.drawable.gaming),
            isLoading = state == Loading,
            isEnabled = viewModel.selectedCharacter.value != null,
            onClick = {
                if (viewModel.isConnected()) {
                    when (state) {
                        is PlayabilityState.Triable -> {
                            viewModel.onTryPressed(isAd = state.isAd)
                        }

                        else -> {
                            navHostController.navigate("conversation/${viewModel.selectedCharacter.value?.id}")
                        }
                    }
                } else {
                    viewModel.openAccountConnection()
                }
            }
        )

        ButtonComposable(
            title = stringResource(R.string.ai_conversation_button_buy_coins_title),
            subtitle = if (viewModel.isConnected.value) {
                stringResource(
                    R.string.ai_conversation_button_buy_coins_subtitle,
                    viewModel.customerCoins.value ?: -1
                )
            } else {
                stringResource(R.string.ai_conversation_you_are_not_connected)
            },
            theme = ButtonTheme.Maroon(iconId = R.drawable.cart),
            onClick = {
                viewModel.onBuyCoinsPressed()
            }
        )
    }
}