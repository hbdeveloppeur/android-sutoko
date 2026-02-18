package com.purpletear.ai_conversation.ui.screens.media.image_generator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sutokosharedelements.utils.UiText
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.component.coins_indicator.CoinsIndicatorComposable
import com.purpletear.ai_conversation.ui.component.multiline_input.TextAreaComposable
import com.purpletear.ai_conversation.ui.component.title_navigation.TitleNavigation
import com.purpletear.ai_conversation.ui.component.tool_button.ToolButtonComposable
import com.purpletear.ai_conversation.ui.navigation.AiConversationRouteDestination
import com.purpletear.ai_conversation.ui.screens.media.image_generator.components.document_row.DocumentsRowComposable
import com.purpletear.ai_conversation.ui.screens.media.image_generator.components.request_realtime_preview.RequestRealTimePreviewComposable
import com.purpletear.ai_conversation.ui.screens.media.image_generator.viewmodels.ImageGeneratorViewModel
import com.purpletear.ai_conversation.ui.theme.BlueBackground

@Composable
fun ImageGeneratorScreen(viewModel: ImageGeneratorViewModel, navController: NavController) {
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {
                viewModel.onResume()
            }
        }
    }

    BackgroundComposable()
    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .systemBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleNavigation(
            modifier = Modifier.padding(end = 12.dp),
            text = UiText.StringResource(R.string.ai_conversation_image_generator_title),
            content = {
                CoinsIndicatorComposable(
                    modifier = Modifier.clickable {
                        viewModel.openBuyTokensDialog()
                    },
                    amount = viewModel.coins.value,
                    isLoading = viewModel.isCoinsLoading.value
                )
            }, onClickClosed = {
                navController.popBackStack()
            })
        DocumentsRowComposable(
            Modifier
                .padding(top = 4.dp),
        )
        RequestRealTimePreviewComposable(
            modifier = Modifier
                .heightIn(min = 100.dp)
                .weight(1f),
            onClickImage = { encodedUrl ->
                navController.navigate(AiConversationRouteDestination.ImageViewer(url = encodedUrl).destination)
            })

        MainButton(
            Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            isEnabled = viewModel.canUseImage,
            onClick = {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "imageRequestSerialId",
                    viewModel.currentImageRequest.value?.serial
                )
                navController.popBackStack()
            },
        )

        TextAreaComposable(
            modifier = Modifier
                .imePadding()
                .fillMaxWidth(0.92f)
                .padding(top = 16.dp),
            text = viewModel.prompt.value,
            backgroundColor = Color(0xFF232836),
            placeholder = stringResource(R.string.ai_conversation_image_generator_prompt_placeholder_title),
            subPlaceholder = stringResource(R.string.ai_conversation_image_generator_prompt_placeholder_subtitle),
            onChange = viewModel::onPromptTextChanged,
        )

        ToolButtonComposable(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .padding(vertical = 16.dp)
                .fillMaxWidth(0.92f),
            text = if (!viewModel.isUserConnected.value) {
                stringResource(R.string.ai_conversation_signin)
            } else {
                stringResource(
                    R.string.ai_conversation_image_generator_button_generate,
                    viewModel.price.value
                )
            },
            isLoading = viewModel.isLoading.value,
            isEnabled = viewModel.isEnabled.value,
            onClick = {
                focusManager.clearFocus()
                viewModel.onClickGenerateImage()
            }
        )
    }
}


@Composable
private fun MainButton(modifier: Modifier = Modifier, isEnabled: Boolean, onClick: () -> Unit) {
    val bgColor = if (!isEnabled) Color(0xFF1F2633) else Color(0xFF6532F3)
    val textColor = if (!isEnabled) Color.White.copy(0.6f) else Color.White
    Box(
        modifier
            .fillMaxWidth(0.92f)
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(enabled = isEnabled, onClick = onClick),
    ) {
        val vector = ImageVector.vectorResource(id = R.drawable.vec_use)
        val painter = rememberVectorPainter(image = vector)

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(16.dp)
                .rotate(-90f)
                .align(Alignment.CenterStart),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFD9E3F8))
        )

        Text(
            text = stringResource(R.string.ai_conversation_use_image),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


@Composable
private fun BackgroundComposable() {
    Box(
        Modifier
            .fillMaxSize()
            .background(BlueBackground)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f),
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://data.sutoko.app/resources/sutoko-ai/image/image-generator-background-repeat.jpg")
                .crossfade(true).build(),
            contentDescription = null, contentScale = ContentScale.Crop,
        )

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.08f),
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://data.sutoko.app/resources/sutoko-ai/image/image-generator-background-repeat.jpg")
                .crossfade(true).build(),
            contentDescription = null, contentScale = ContentScale.FillWidth,
        )
    }
}
