package com.purpletear.aiconversation.presentation.screens.media.image_generator.components.request_realtime_preview

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.purpletear.aiconversation.domain.enums.ProcessStatus
import com.purpletear.aiconversation.domain.model.Document
import com.purpletear.aiconversation.domain.model.hasNext
import com.purpletear.aiconversation.domain.model.hasPrevious
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.common.utils.getDirectoryImageRequest
import com.purpletear.aiconversation.presentation.common.utils.getDocumentImageUrl
import com.purpletear.aiconversation.presentation.common.utils.getRequestImageUrl
import com.purpletear.aiconversation.presentation.component.SecondCounter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun RequestRealTimePreviewComposable(
    modifier: Modifier = Modifier,
    viewModel: RequestRealTimePreviewViewModel = hiltViewModel(),
    onClickImage: (url: String) -> Unit
) {
    Column(modifier = modifier) {
        Column(
            Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 22.dp)
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .weight(1f)
                .clip(shape = RoundedCornerShape(14.dp))
                .background(Color.Red)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
            ) {
                DocumentCurrentImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    document = viewModel.selectedDocument.value,
                    onClick = onClickImage
                )
                Row(
                    modifier = Modifier
                        .clickable {

                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        modifier = Modifier
                            .graphicsLayer {
                                rotationY = 180f
                            },
                        icon = R.drawable.vec_forward,
                        enabled = viewModel.selectedDocument.value?.hasPrevious() == true,
                        onClick = viewModel::onClickPrevious
                    )
                    IconButton(
                        modifier = Modifier,
                        icon = R.drawable.vec_forward,
                        enabled = viewModel.selectedDocument.value?.hasNext() == true,
                        onClick = viewModel::onClickNext
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    viewModel.isLoading.value,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.5f))
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(12.dp)
                                .size(16.dp)
                                .align(Alignment.TopEnd),
                            color = Color.LightGray,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            Row {
                Button(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.ai_conversation_save_gallery),
                    onClick = viewModel::onClickDownloadImage,
                )
                Button(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.ai_conversation_delete),
                    onClick = viewModel::onClickDeleteImage
                )
            }
        }
    }
}

@Composable
private fun IconButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    enabled: Boolean,
    onClick: (() -> Unit)? = null
) {

    val onClickModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = onClick
        )
    }
    Box(
        modifier = modifier
            .size(width = 42.dp, height = 28.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) Color.White else Color(0xFF686868).copy(0.5f))
            .then(onClickModifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Color.Black
        )
    }
}

@Composable
private fun DocumentCurrentImage(
    modifier: Modifier = Modifier,
    document: Document?,
    onClick: (url: String) -> Unit
) {
    val request = getDirectoryImageRequest(document)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = request?.status == ProcessStatus.COMPLETED.code,
                onClick = {
                    document?.let {
                        val url: String? = getDocumentImageUrl(document)
                        url?.let {
                            val encodedUrl = URLEncoder.encode(
                                it,
                                StandardCharsets.UTF_8.toString()
                            )
                            onClick(encodedUrl)
                        }
                    }
                }
            ),
    ) {


        when (request?.status) {

            ProcessStatus.PROCESSING.code -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoaderLottieAnimation()
                        SecondCounter(startTimestamp = request.timeStamp)
                    }
                }
            }

            ProcessStatus.COMPLETED.code, ProcessStatus.INITIAL.code -> {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            getRequestImageUrl(request)
                                ?: "https://data.sutoko.app/resources/sutoko-ai/image/background_waiting_screen.jpg"
                        )
                        .crossfade(true).build(),
                    contentDescription = null, contentScale = ContentScale.Crop,
                )
            }

            ProcessStatus.FAILED.code -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_alert),
                            contentDescription = stringResource(R.string.ai_conversation_error_occured_refund),
                            modifier = Modifier.size(26.dp),
                            tint = Color(0xFFEC216B)
                        )
                        Text(
                            modifier = Modifier.widthIn(max = 200.dp),
                            textAlign = TextAlign.Center,
                            text = stringResource(R.string.ai_conversation_error_occured_refund),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            else -> {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            "https://data.sutoko.app/resources/sutoko-ai/image/background_waiting_screen.jpg"
                        )
                        .crossfade(true).build(),
                    contentDescription = null, contentScale = ContentScale.Crop,
                )
            }
        }



        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.35f))
        )
    }
}

@Composable
private fun Button(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {}
) {
    Box(
        Modifier
            .height(36.dp)
            .then(modifier)
            .background(Color(0xFF050E18))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ), contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun LoaderLottieAnimation() {
    val rawRes = R.raw.loader_animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    Box(
        modifier = Modifier
            .size(62.dp)
            .clipToBounds()

    ) {
        LottieAnimation(
            modifier = Modifier
                .size(62.dp)
                .graphicsLayer {
                    scaleX = 3.0f
                    scaleY = 3.0f
                },
            composition = composition,
            iterations = LottieConstants.IterateForever,
        )
    }
}
