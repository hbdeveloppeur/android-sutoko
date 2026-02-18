package com.purpletear.aiconversation.presentation.screens.shopDialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.BuyTokensDialogComposable
import com.purpletear.aiconversation.presentation.component.buy_tokens_dialog.viewModels.BuyTokensDialogViewModel

@Composable
fun MessagesCoinsDialogComposable(viewModel: BuyTokensDialogViewModel = hiltViewModel()) {

    AnimatedVisibility(
        visible = viewModel.isVisible.value,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    viewModel.close()
                }) {
            BuyTokensDialogComposable(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClickLogin = {

                },
            )
        }
    }
}
