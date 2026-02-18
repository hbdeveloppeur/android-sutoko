package com.purpletear.ai_conversation.ui.screens.home.components.character_row

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.domain.model.AiCharacter
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.common.utils.capitalizeFirstLetter
import com.purpletear.ai_conversation.ui.common.utils.getRemoteAssetsUrl
import com.purpletear.core.date.DateUtils

@Composable
internal fun CharacterRowComposable(
    character: AiCharacter,
    onClickDelete: () -> Unit,
    isDeleting: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isDeleting) 0.5f else 1f)
            .padding(vertical = 6.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CharacterAvatar(character, isDeleting)

        Column(
            Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                character.firstName.capitalizeFirstLetter(),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            val days = DateUtils.daysBetween(
                timestamp1 = character.createdAt * 1000,
                timestamp2 = System.currentTimeMillis(),
            )
            Text(
                stringResource(R.string.ai_conversation_add_x_days_ago, days),
                color = Color.LightGray,
                style = MaterialTheme.typography.labelSmall
            )
        }


        Box(
            Modifier
                .size(32.dp)
                .clickable(onClick = onClickDelete),
            contentAlignment = Alignment.Center,

            ) {
            DeleteIcon()
        }
    }
}


@Composable
private fun CharacterAvatar(character: AiCharacter, isDeleting: Boolean) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF040617))
            .border(2.dp, Color.White.copy(0.3f), MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        character.avatarUrl?.let {
            AsyncImage(
                model =
                ImageRequest.Builder(LocalContext.current)
                    .data(getRemoteAssetsUrl(getRemoteAssetsUrl(it)))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }

        AnimatedVisibility(
            visible = isDeleting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.3f))
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
}

@Composable
private fun DeleteIcon(color: Color = Color(0xFFFFB3B3)) {
    Icon(
        modifier = Modifier
            .size(16.dp),
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete_square),
        contentDescription = "Delete the voice message",
        tint = color
    )
}

