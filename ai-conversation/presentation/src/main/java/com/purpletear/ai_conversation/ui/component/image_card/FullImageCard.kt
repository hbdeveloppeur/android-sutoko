package com.purpletear.ai_conversation.ui.component.image_card

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.domain.model.AvatarBannerPair
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.common.utils.getRemoteAssetsUrl
import com.purpletear.ai_conversation.ui.component.character.avatar.character_avatar.CharacterAvatar
import com.purpletear.ai_conversation.ui.theme.AiConversationTheme


@Composable
@Preview(name = "FullImageCard", showBackground = false, showSystemUi = false)
private fun Preview() {
    AiConversationTheme {
        Column(
            Modifier.background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.preview_full_image_card),
                contentDescription = null,
            )
            Box(Modifier.padding(vertical = 12.dp)) {
                FullImageCardComposable(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
//                    avatarUrl = "",
//                    bannerUrl = "https://data.sutoko.app/resources/sutoko-ai/image/avatar_ai_woman.jpg"

                    avatarBannerPair = null,
                    isLoading = true
                )
            }
        }
    }
}


@Composable
internal fun FullImageCardComposable(
    modifier: Modifier = Modifier,
    avatarBannerPair: AvatarBannerPair?,
    avatarBitmap: ImageBitmap? = null,
    isLoading: Boolean = false
) {
    Box(
        modifier
            .border(1.dp, Color(0xFF537399), MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .height(200.dp)
    ) {
        // Background
        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    if (avatarBannerPair?.banner?.url.isNullOrBlank().not()) {
                        getRemoteAssetsUrl(
                            avatarBannerPair?.banner?.url ?: ""
                        )
                    } else {
                        "https://data.sutoko.app/resources/sutoko-ai/image/avatar_ai_woman.jpg"
                    }
                )
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        val hasAvatar = avatarBannerPair?.avatar != null || avatarBitmap != null
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (hasAvatar) 0.5f else 0.5f))
        )

        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (hasAvatar) {
                CharacterAvatar(
                    modifier = Modifier.size(52.dp),
                    bitmap = avatarBitmap,
                    orUrl = avatarBannerPair?.avatar?.url ?: "",

                    isSelected = true,
                    size = 60.dp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.ai_conversation_add_character_cta_use_ai_title),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.montserrat_medium))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape),
                    painter = painterResource(id = R.drawable.message_coin),
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.ai_conversation_add_character_cta_use_ai_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.montserrat_medium))
                )
            }
        }

        // Loader
        val isLoadingAlphaState by animateFloatAsState(
            targetValue = if (isLoading) 1f else 0f,
            animationSpec = tween(
                durationMillis = 1000,
                delayMillis = 280,
                easing = LinearOutSlowInEasing
            ), label = "Full image card loading back alpha state"
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = isLoadingAlphaState))
                .alpha(isLoadingAlphaState),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )
        }
    }
}